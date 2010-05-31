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
		synchronized (queueLock) {
			if (!queueHasTestRun(testRunId)) {
				log.info "adding test run ${testRunId} to stat queue"
				def queuedItem = new QueuedTestRunStat('testRunId': testRunId)
				dataService.saveDomainObject queuedItem, true
			}
			if (Environment.current == Environment.TEST) {
				calculateTestRunStats(testRunId)
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
					try {
						calculateTestRunStats(queuedItem.testRunId)
						queuedItem.delete(flush: true)
					} catch (OptimisticLockingFailureException e) {
						log.info "OptimisticLockingFailureException for test run ${queuedItem.testRunId}"
						// leave it in queue so it gets tried again
					}
					queueSize = QueuedTestRunStat.list().size()
				}
				if (grailsApplication.config.statSleep) {
					sleep(grailsApplication.config.statSleep)
				}
			}
		}
	}


	QueuedTestRunStat getFirstTestRunIdInQueue() {
		def latest = QueuedTestRunStat.listOrderByDateCreated(max: 1)
		if (latest.size() == 0) {
			return null
		} else {
			return latest[0]
		}
	}


	void calculateTestRunStats(Long testRunId) {
		TestRun.withTransaction {
			def testRun = TestRun.get(testRunId)
			if (!testRun) {
				log.error "Couldn't find test run ${testRunId}"
			} else {
				dataService.deleteStatisticsForTestRun(testRun)
				TestRunStats calculatedStats = new TestRunStats(testRun: testRun)

				def rawTestRunStats = dataService.getRawTestRunStats(testRun)
				calculatedStats.tests = rawTestRunStats[0]
				calculatedStats.totalDuration = rawTestRunStats[1]
				calculatedStats.averageDuration = rawTestRunStats[2]
				def newFailuresQueryFilter = new TestOutcomeQueryFilter(isFailure: true, isFailureStatusChanged: true)
				calculatedStats.failed = dataService.countTestOutcomes(newFailuresQueryFilter)
				calculatedStats.passed = calculatedStats.tests - calculatedStats.failed

				if (calculatedStats.tests > 0) {
					BigDecimal successRate = (calculatedStats.passed / calculatedStats.tests) * 100
					calculatedStats.successRate = successRate.round(new MathContext(4))
				}

				testRun.testRunStatistics = calculatedStats
				testRun.testRunStatistics = calculateAnalysisStats(testRun)

				def tagStats = getTagStatistics(testRun)
				tagStats.each {
					testRun.testRunStatistics.addToTagStatistics(it)
				}
				dataService.saveDomainObject(testRun, true)
			}
		}
	}


	def calculateAnalysisStats(TestRun testRun) {
		TestRun.withTransaction {
			def calculatedStats = testRun?.testRunStatistics
			dataService.clearAnalysisStatistics(testRun)
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

				def passed = rawStats.findAll {!it[0].isFailure && it[0].includeInCalculations}.collect {it[1]}.sum()
				tagStat.passed = passed ? passed : 0;
				log.debug "${passed} passed for ${tag.name}"

				def failed = rawStats.findAll {it[0].isFailure && it[0].includeInCalculations && it[0].name != "Skip"}.collect {it[1]}.sum()
				tagStat.failed = failed ? failed : 0;
				log.debug "${failed} failed for ${tag.name}"

				def skipped = rawStats.findAll {it[0].isFailure && it[0].includeInCalculations && it[0].name == "Skip"}.collect {it[1]}.sum()
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
			def runs = dataService.getTestRunsWithoutAnalysisStatistics().collect {it.id}
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
}
