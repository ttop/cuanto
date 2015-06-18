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
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.collections.Predicate
import org.hibernate.StaleObjectStateException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

class StatisticService {

	boolean transactional = false
	def dataService
	def testRunService
	def grailsApplication

    int numRecentTestOutcomes = 40
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
            // don't calculate test run stats on-demand, because it is not thread-safe
//			if (Environment.current == Environment.TEST) {
//				calculateTestRunStats(testRunId)
//			}
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
			QueuedTestRunStat.withTransaction {
				def queuedItem = QueuedTestRunStat.findByTestRunId(testRunId, [lock: true])
				if (queuedItem) {
					log.info "removing test run ${testRunId} from stat queue"
					queuedItem.delete(flush: true)
				}
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
						QueuedTestRunStat.withTransaction {
							calculateTestRunStats(queuedItem.testRunId)
							calculateTestOutcomeStats(queuedItem.testRunId)
							queuedItem.delete(flush: true)
							queueSize = QueuedTestRunStat.list().size()
						}
					} catch (OptimisticLockingFailureException e) {
						log.info "OptimisticLockingFailureException for test run ${queuedItem.testRunId}"
						// leave it in queue so it gets tried again
					} catch (HibernateOptimisticLockingFailureException e) {
						log.info "HibernateOptimisticLockingFailureException for test run ${queuedItem.testRunId}"
					} catch (StaleObjectStateException e) {
						log.info "StaleObjectStateException for test run ${queuedItem.testRunId}"
						// leave it in queue so it gets tried again
					}
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
			return latest[0]
		}
	}


	void calculateTestRunStats(Long testRunId) {
		def testRun = TestRun.get(testRunId)
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
            def quarantinedPassQueryFilter = new TestOutcomeQueryFilter(
                testRun: testRun,
                testResultIncludedInCalculations: true,
                analysisState: dataService.getAnalysisStateByName('Quarantined'),
                isFailure: false,
                isSkip: false
            )
            def quarantinedQueryFilter = new TestOutcomeQueryFilter(
                testRun: testRun,
                testResultIncludedInCalculations: true,
                analysisState: dataService.getAnalysisStateByName('Quarantined'),
            )

            def quarantinedPasses = dataService.countTestOutcomes(quarantinedPassQueryFilter)
            def quarantined = dataService.countTestOutcomes(quarantinedQueryFilter)
			calculatedStats.newFailures = dataService.countTestOutcomes(newFailuresQueryFilter)
			calculatedStats.failed = dataService.countTestOutcomes(allFailuresQueryFilter)
			calculatedStats.skipped = dataService.countTestOutcomes(allSkipsQueryFilter)
			calculatedStats.passed = calculatedStats.tests - calculatedStats.failed - calculatedStats.skipped

			if (calculatedStats.tests > 0) {
                MathContext fourDigitRounding = new MathContext(4)
				//BigDecimal successRate = calculatedStats.passed / calculatedStats.tests * 100
                //calculatedStats.successRate = successRate.round(fourDigitRounding)
                int numNonQuarantinedPasses = calculatedStats.passed - quarantinedPasses
                int numNonQuarantinedTests = calculatedStats.tests - quarantined 
                calculatedStats.successRate = (numNonQuarantinedPasses / numNonQuarantinedTests * 100).round(fourDigitRounding)
                //calculatedStats.effectiveSuccessRate = calculatedStats.effectiveSuccessRate.round(fourDigitRounding)

				def previousSuccessRate = testRunService.getPreviousTestRunSuccessRate(testRun)
				if (previousSuccessRate != null) {
					calculatedStats.successRateChange = (calculatedStats.successRate - previousSuccessRate).round(fourDigitRounding)
				} else {
					calculatedStats.successRateChange = null
				}
			}

			dataService.saveDomainObject(calculatedStats)

			calculateAnalysisStats(testRun)
			dataService.saveDomainObject(calculatedStats)

			// delete the existing tagStatistics
			def testRunStatistics = TestRunStats.findByTestRun(testRun)
			if (testRunStatistics.tagStatistics?.size() > 0) {
				TagStatistic.executeUpdate("delete from TagStatistic t where t.testRunStats = ?", [testRunStatistics])
				testRunStatistics.tagStatistics.each {  stat ->
					stat.discard()
				}
				testRunStatistics.tagStatistics.clear()
				testRunStatistics.discard()
			}

			testRun.discard()

			// get new tagStatistics
			def tagStats = getTagStatistics(testRun)
			testRunStatistics = TestRunStats.findByTestRun(testRun)
			tagStats.each {
				testRunStatistics.addToTagStatistics(it)
			}
			dataService.saveDomainObject(testRunStatistics, true)
		}
	}

    void calculateTestOutcomeStats(Long testRunId) {
        def testRun = TestRun.lock(testRunId)
        if (!testRun) {
            log.error "Couldn't find test run ${testRunId}"
        } else {
            List<TestOutcome> testOutcomes = TestOutcome.findAllByTestRun(testRun)
            for (TestOutcome testOutcome : testOutcomes)
            {
                testOutcome.lock()
                def stats = new TestOutcomeStats()
                dataService.saveDomainObject(stats, true)
                testOutcome.testOutcomeStats = stats
                List<String> recentTestResults = TestOutcome.executeQuery(
                        "SELECT to.testResult FROM cuanto.TestOutcome to WHERE to.testCase = ?",
                        [testOutcome.testCase], [max: numRecentTestOutcomes, sort: 'id', order: 'desc'])
                calculateStreak(testOutcome, recentTestResults)
                calculateSuccessRate(testOutcome, recentTestResults)
                testOutcome.save()
            }
        }
    }

    private void calculateStreak(TestOutcome testOutcome, List<String> recentTestResults)
    {
        int streak = countRecentStreak(recentTestResults)
        testOutcome.testOutcomeStats.streak = streak
    }    


    private void calculateSuccessRate(TestOutcome testOutcome, List<TestResult> recentTestResults) {
        int passes = CollectionUtils.countMatches(recentTestResults, { TestResult testResult ->
            testResult.getName() == "Pass"
        } as Predicate)
        testOutcome.testOutcomeStats.successRate = 100 * passes / recentTestResults.size()
    }

    private int countRecentStreak(List list) {
        if (!list)
            return 0
        else if (list.size() == 1)
            return 1

        int streak = 0;
        def lastResult = list[0];
        for (int i = 0; i < list.size() && lastResult != null; ++i)
        {
            def result = list[i]
            if (lastResult == result)
            {
                streak++
            }
            else
            {
                lastResult = null
            }
        }
        return streak
    }

    def calculateAnalysisStats(TestRun testRun) {
		TestRun.withTransaction {
			def calculatedStats = TestRunStats.findByTestRun(testRun)
			clearAnalysisStatistics(calculatedStats)
			def analysisStats = dataService.getAnalysisStatistics(testRun)
			analysisStats?.each { stat ->
				calculatedStats?.addToAnalysisStatistics(stat)
			}
			def analyzedStats = analysisStats.findAll {
                it.state?.isAnalyzed && it.state != dataService.getAnalysisStateByName("Quarantined")}
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
		testRun = testRun.refresh()
		def tagStats = []
		if (Environment.current != Environment.TEST) {
			// The HSQL database doesn't like the following query, so we won't do it in testing.
			testRun.tags.each {tag ->
                def (iTestResult, iDuration, iCount) = [0, 1, 2]
				def rawStats = TestOutcome.executeQuery("select t.testResult, sum(t.duration), count(*) from cuanto.TestOutcome t " +
					"inner join t.tags tag_0 where t.testRun = ? and tag_0 = ? group by t.testResult", [testRun, tag])
				def tagStat = new TagStatistic()
				tagStat.tag = tag

				def passed = rawStats.findAll {!it[iTestResult].isFailure && !it[iTestResult].isSkip && it[iTestResult].includeInCalculations}.collect {it[iCount]}.sum()
				tagStat.passed = passed ?: 0;
				log.debug "${passed} passed for ${tag.name}"

				def failed = rawStats.findAll {it[iTestResult].isFailure && it[iTestResult].includeInCalculations && !it[iTestResult].isSkip}.collect {it[iCount]}.sum()
				tagStat.failed = failed ?: 0;
				log.debug "${failed} failed for ${tag.name}"

				def skipped = rawStats.findAll {it[iTestResult].isSkip && it[iTestResult].includeInCalculations}.collect {it[iCount]}.sum()
				tagStat.skipped = skipped ?: 0;
				log.debug "${skipped} skipped for ${tag.name}"

                def duration = rawStats.collect { it[iDuration] }.sum()
                tagStat.duration = Math.max(0, duration ?: 0);

				def total = rawStats.collect {it[iCount]}.sum()
				tagStat.total = total ?: 0;
                
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
