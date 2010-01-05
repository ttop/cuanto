package cuanto

import java.util.concurrent.ConcurrentLinkedQueue
import java.math.MathContext
import org.springframework.dao.OptimisticLockingFailureException
import grails.util.Environment
import org.hibernate.StaleObjectStateException

class StatisticService {

	boolean transactional = false
	def dataService
	def grailsApplication

	ConcurrentLinkedQueue<Long> testRunStatQueue = new ConcurrentLinkedQueue<Long>();
	Boolean processingTestRunStats = false;

	void queueTestRunStats(TestRun testRun) {
		queueTestRunStats testRun.id
	}


	void queueTestRunStats(Long testRunId) {
		synchronized (testRunStatQueue) {
			if (!testRunStatQueue.contains(testRunId)) {
				log.info "*** adding test run ${testRunId} to stat queue"
				testRunStatQueue.add(testRunId);
			}
			if (Environment.current == Environment.TEST) {
				calculateTestRunStats(testRunId)
			}
		}
	}


	def processTestRunStats() {
		setProcessingTestRunStats(true)
		while (testRunStatQueue.size() > 0) {
			log.info "*** ${testRunStatQueue.size()} items in stat queue"
			def testRunId = testRunStatQueue.poll()
			if (testRunId) {
				try {
					calculateTestRunStats(testRunId)
				} catch (OptimisticLockingFailureException e) {
					log.info "OptimisticLockingFailureException for test run ${testRunId}"
					// re-queue
					queueTestRunStats(testRunId)
				}
			}
			if (grailsApplication.config.statSleep) {
				sleep(grailsApplication.config.statSleep)
			}
		}
		setProcessingTestRunStats(false) 
	}


	void calculateTestRunStats(Long testRunId) {
		TestRun.withTransaction {
			def testRun = TestRun.get(testRunId)
			if (!testRun) {
				log.error "Couldn't find test run ${testRunId}"
			} else {
				try {
					dataService.deleteStatisticsForTestRun(testRun)
					TestRunStats calculatedStats = new TestRunStats(testRun: testRun)

					def rawTestRunStats = dataService.getRawTestRunStats(testRun)
					calculatedStats.tests = rawTestRunStats[0]
					calculatedStats.totalDuration = rawTestRunStats[1]
					calculatedStats.averageDuration = rawTestRunStats[2]
					calculatedStats.averageDuration = calculatedStats.averageDuration?.round(new MathContext(4))
					calculatedStats.failed = dataService.getTestRunFailureCount(testRun)
					calculatedStats.passed = calculatedStats.tests - calculatedStats.failed

					if (calculatedStats.tests > 0) {
						BigDecimal successRate = (calculatedStats.passed / calculatedStats.tests) * 100
						calculatedStats.successRate = successRate.round(new MathContext(4))
					}

					testRun.testRunStatistics = calculatedStats
					testRun.testRunStatistics = calculateAnalysisStats(testRun)
					dataService.saveDomainObject(testRun, true)

				} catch (StaleObjectStateException e) {
					log.error "StaleObjectStateException while calculating stats for test run ${testRunId}"
					queueTestRunStats(testRunId)
				}
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


	public Boolean getProcessingTestRunStats() {
		synchronized (processingTestRunStats) {
			return processingTestRunStats
		}

	}
	

	public void setProcessingTestRunStats(Boolean processing) {
		synchronized (processingTestRunStats) {
			processingTestRunStats = processing
		}
	}

}
