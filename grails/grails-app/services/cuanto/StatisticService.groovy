package cuanto

import java.math.MathContext
import org.springframework.dao.OptimisticLockingFailureException
import grails.util.Environment

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
				log.info "*** adding test run ${testRunId} to stat queue"
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
			while ( queueSize > 0) {
				log.info "*** ${queueSize} items in stat queue"
				QueuedTestRunStat queuedItem = getFirstTestRunIdInQueue()
				if (queuedItem) {
					try {
						calculateTestRunStats(queuedItem.testRunId)
						queuedItem.delete(flush: true)
					} catch (OptimisticLockingFailureException e) {
						log.info "OptimisticLockingFailureException for test run ${testRunId}"
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
}
