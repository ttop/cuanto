package cuanto

import java.util.concurrent.ConcurrentLinkedQueue
import java.math.MathContext
import org.springframework.dao.OptimisticLockingFailureException
import grails.util.Environment

class StatisticService {

	boolean transactional = false
	def dataService
	def grailsApplication

	ConcurrentLinkedQueue<TestRun> testRunStatQueue = new ConcurrentLinkedQueue<TestRun>();
	Boolean processingTestRunStats = false;

	def queueTestRunStats(TestRun testRun) {
		synchronized (testRunStatQueue) {
			if (!testRunStatQueue.contains(testRun)) {
				testRunStatQueue.add(testRun);
			}
			if (Environment.current == Environment.TEST) {
				calculateTestRunStats(testRun)
			}
		}
	}


	def processTestRunStats() {
		setProcessingTestRunStats(true)
		while (testRunStatQueue.size() > 0) {
			def testRun = testRunStatQueue.poll()
			if (testRun) {
				try {
					TestRun.withTransaction {
						calculateTestRunStats(testRun)
					}
				} catch (OptimisticLockingFailureException e) {
					log.info "OptimisticLockingFailureException for test run ${testRun.id}"
					// re-queue
					queueTestRunStats(testRun)
				}
			}
			sleep(grailsApplication.config.statSleep)
		}
		setProcessingTestRunStats(false) 
	}



	def calculateTestRunStats(TestRun testRun) {
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

		dataService.saveDomainObject(calculatedStats)
		testRun.testRunStatistics = calculateAnalysisStats(testRun)
		dataService.saveTestRun(testRun)
		return testRun.testRunStatistics
	}


	def calculateAnalysisStats(TestRun testRun) {
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

		dataService.saveDomainObject(calculatedStats, true)
		return calculatedStats
	}

	def updateTestRunsWithoutAnalysisStats() {
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
