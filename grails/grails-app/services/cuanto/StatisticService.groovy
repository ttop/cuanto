/*
	Copyright (c) 2010 Todd E. Wells

	This file is part of Cuanto, a test results repository and analysis program.

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU Lesser General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package cuanto

import grails.util.Environment
import java.math.MathContext
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException
import org.hibernate.StaleObjectStateException

class StatisticService {

	boolean transactional = false
	def dataService
	def grailsApplication

	Boolean processingTestRunStats = false;
	final private static String queueLock = "Test Run Stat Queue Lock"
	final private static String calcLock = "Test Run Calculation Lock"

	void queueTestRunStats(TestRun testRun) {
		queueTestRunStats testRun.id
	}


	void queueTestRunStats(Long testRunId) {
		if (testRunId == null) {
			log.debug "TestRun id for which to queue TestRunStats was null."
			return
		}
		
		def testRun = TestRun.get(testRunId)
		if (testRun == null) {
			log.debug "TestRun [$testRunId] was not found."
			return
		}

		synchronized (queueLock) {
			if (!queueHasTestRun(testRunId)) {
				log.info "adding test run ${testRunId} to stat queue"
				def queuedItem = new QueuedTestRunStat(testRunId: testRunId)
				dataService.saveDomainObject queuedItem, true
			}
		}
	}


	void dequeueTestRunStats(Long testRunId) {
		if (testRunId == null) {
			log.debug "TestRun id for which to dequeue TestRunStats was null."
			return
		}

		def testRun = TestRun.get(testRunId)
		if (testRun == null) {
			log.debug "TestRun [$testRunId] was not found for dequeuing."
			return
		}

		synchronized (queueLock) {
			def queuedItem = QueuedTestRunStat.findByTestRunId(testRunId, [lock: true])
			if (queuedItem) {
				log.info "removing test run ${testRunId} from stat queue"
				queuedItem.delete(flush: true)
			}
		}
	}


	Boolean queueHasTestRun(Long testRunId) {
		def queuedItem = QueuedTestRunStat.findByTestRunId(testRunId)
		return queuedItem != null
	}


	def processTestRunStats() {
		synchronized (calcLock) {
			def queueSize = QueuedTestRunStat.list().size()
			while (queueSize > 0) {
				log.debug "${queueSize} items in stat queue"
				QueuedTestRunStat queuedItem = getFirstTestRunIdInQueue()
				if (queuedItem) {
					calculateTestRunStats(queuedItem.testRunId)
					queuedItem.delete(flush: true)
					queueSize = QueuedTestRunStat.list().size()
				}
			}
			if (grailsApplication.config.statSleep) {
				sleep(grailsApplication.config.statSleep)
			}

		}
	}


	void deleteStatisticForTestRuns(TestRun testRun) {
		def allStats = TestRunStats.findAllByTestRun(testRun)
		allStats.each {stats ->
			clearAnalysisStatistics(stats)
			stats.delete()
		}
	}


	QueuedTestRunStat getFirstTestRunIdInQueue() {
		def latest = QueuedTestRunStat.listOrderByDateCreated(max: 1)
		if (latest.size() == 0) {
			return null
		} else {
			return QueuedTestRunStat.lock(latest[0].id)
		}
	}


	void calculateTestRunStats(Long testRunId) {
		def testRun = TestRun.lock(testRunId)
		if (!testRun) {
			log.error "Couldn't find test run ${testRunId}"
		} else {
			def foundStats = TestRunStats.findByTestRun(testRun, [lock: true])
			TestRunStats calculatedStats = foundStats ?: new TestRunStats()
			def rawTestRunStats = dataService.getRawTestRunStats(testRun)
			calculatedStats.testRun = testRun
			calculatedStats.tests = rawTestRunStats[0]
			calculatedStats.totalDuration = rawTestRunStats[1]
			calculatedStats.averageDuration = rawTestRunStats[2]
			def allFailuresQueryFilter = new TestOutcomeQueryFilter(
				testRun: testRun,
				testResultIncludedInCalculations: true,
				isFailure: true)
			def newFailuresQueryFilter = new TestOutcomeQueryFilter(testRun: testRun,
				testResultIncludedInCalculations: true,
				isFailure: true,
				isFailureStatusChanged: true)
			def allSkipsQueryFilter = new TestOutcomeQueryFilter(
				testRun: testRun,
				testResultIncludedInCalculations: true,
				isSkip: true
			)
			calculatedStats.newFailures = dataService.countTestOutcomes(newFailuresQueryFilter)
			calculatedStats.failed = dataService.countTestOutcomes(allFailuresQueryFilter)
			calculatedStats.skipped = dataService.countTestOutcomes(allSkipsQueryFilter)
			calculatedStats.passed = calculatedStats.tests - calculatedStats.failed - calculatedStats.skipped

			if (calculatedStats.tests > 0) {
				BigDecimal successRate = (calculatedStats.passed / calculatedStats.tests) * 100
				calculatedStats.successRate = successRate.round(new MathContext(4))
			}

			dataService.saveDomainObject(calculatedStats)

			calculateAnalysisStats(testRun)
			dataService.saveDomainObject(calculatedStats)

			def testRunStatistics = TestRunStats.findByTestRun(testRun)
			if (testRunStatistics.tagStatistics?.size() > 0) {
				def statsToRemove = TagStatistic.findAllByTestRunStats(testRunStatistics)
				statsToRemove.each {
					testRunStatistics.removeFromTagStatistics(it).save()
					it.delete(flush:true)
				}
			}
			dataService.saveDomainObject(testRunStatistics)

			def tagStats = getTagStatistics(testRun)
			tagStats.each {
				testRunStatistics.addToTagStatistics(it)
			}
			dataService.saveDomainObject(testRunStatistics, true)
		}
	}


	def calculateAnalysisStats(TestRun testRun) {
		TestRun.withTransaction {
			def calculatedStats = TestRunStats.findByTestRun(testRun)
			clearAnalysisStatistics(calculatedStats)
			def analysisStats = dataService.getAnalysisStatistics(testRun)
			analysisStats?.each { stat ->
				calculatedStats?.addToAnalysisStatistics(stat)
			}
			def analyzedStats = analysisStats.findAll { it.state.isAnalyzed }
			def sum = analyzedStats.collect { it.qty }.sum()
			if (sum) {
				calculatedStats.analyzed = sum
			} else {
				calculatedStats.analyzed = 0
			}

			return calculatedStats
		}
	}


	List getTagStatistics(TestRun testRun) {
		def tagStats = []
		if (Environment.current != Environment.TEST) {
			// The HSQL database doesn't like the following query, so we won't do it in testing.
			testRun.tags.each {tag ->
				def rawStats = TestOutcome.executeQuery("select t.testResult, count(*) from cuanto.TestOutcome t " +
					"inner join t.tags tag_0 where t.testRun = ? and tag_0 = ? group by t.testResult", [testRun, tag])
				def tagStat = new TagStatistic()
				tagStat.tag = tag

				def passed = rawStats.findAll {!it[0].isFailure && !it[0].isSkip && it[0].includeInCalculations}.collect {it[1]}.sum()
				tagStat.passed = passed ? passed : 0;
				log.debug "${passed} passed for ${tag.name}"

				def failed = rawStats.findAll {it[0].isFailure && it[0].includeInCalculations && !it[0].isSkip}.collect {it[1]}.sum()
				tagStat.failed = failed ? failed : 0;
				log.debug "${failed} failed for ${tag.name}"

				def skipped = rawStats.findAll {it[0].isSkip && it[0].includeInCalculations}.collect {it[1]}.sum()
				tagStat.skipped = skipped ? skipped : 0;
				log.debug "${skipped} skipped for ${tag.name}"

				def total = rawStats.collect {it[1]}.sum()
				tagStat.total = total ? total : 0;
				tagStats << tagStat
			}
		}
		return tagStats
	}


	def updateTestRunsWithoutAnalysisStats() {
		TestRun.withTransaction {
			def runs = getTestRunsWithoutAnalysisStatistics().collect {it.id}
			log.debug "${runs.size()} runs without stats"

			def num = 0
			def numThreads = 20
			while (runs.size()) {

				def threads = []
				for (i in 1..numThreads) {
					if (runs.size() > 0) {
						def runid = runs.pop()
						threads << Thread.start {
							TestRun.withTransaction {
								def testRun = TestRun.get(runid)
								calculateAnalysisStats(testRun)
							}
						}
					}
				}

				threads.each {
					it.join()
				}
				num += threads.size()
				log.debug "$num runs completed"
			}
		}
		log.debug "Completed calculating analysis statistics"
	}


	def clearAnalysisStatistics(TestRunStats testRunStatistics) {
		if (testRunStatistics) {
			def statsToDelete = testRunStatistics?.analysisStatistics?.collect {it}
			statsToDelete?.each { AnalysisStatistic stat ->
				testRunStatistics.removeFromAnalysisStatistics(stat)
				stat.delete()
			}
			dataService.saveDomainObject testRunStatistics
		}
	}


	def deleteStatsForTestRun(TestRun testRun) {
		TestRunStats stats = TestRunStats.findByTestRun(testRun)
		if (stats) {
			stats.delete()
		}
	}


	def deleteEmptyTestRuns() {
		def now = new Date().time
		def TEN_MINUTES = 1000 * 60 * 10
		def ONE_DAY = 1000 * 60 * 60 * 24
		def beforeDate = new Date(now - TEN_MINUTES)
		def afterDate = new Date(now - ONE_DAY)
		def runDeletionCandidates = TestRun.executeQuery("from cuanto.TestRun as tr where " +
			"tr.dateExecuted > ? and tr.dateExecuted < ? ", [afterDate, beforeDate])

		def runsToDelete = []
		runDeletionCandidates.each { TestRun testRun ->
			def foundStats = TestRunStats.findByTestRun(testRun)
			if (!foundStats) {
				def outcomes = TestOutcome.countByTestRun(testRun)
				if (outcomes == 0) {
					runsToDelete << testRun
				} else {
					queueTestRunStats(testRun)
				}
			}
		}

		if (runsToDelete.size() > 0) {
			log.info "found ${runDeletionCandidates.size()} empty test runs to delete"

			runsToDelete.each {run ->
				run.delete()
			}
		}
	}


	def getTestRunsWithoutAnalysisStatistics() {
		def criteria = TestRunStats.createCriteria()
		def trStats = criteria.list {
			and {
				isEmpty("analysisStatistics")
				gt("failed", 0)
			}
			order("id")
		}
		def runs = trStats.collect { it.testRun }
		return runs
	}
}
