import cuanto.TestOutcome

import cuanto.TestRunStats
import cuanto.TestRun

/**
 * Self-terminating Job that initializes TestOutcome.isFailureStatusChanged.
 * Upon terminus, the job unschedules itself, after which it initializes TestRuns
 * whose testRunStatistics.newFailures is null.
 */
class InitializeTestOutcomeAndTestRunJob {
	def dataService
	def testOutcomeService
	def initializationService
	def quartzScheduler
	def grailsApplication

	static final String JOB_NAME = 'InitializeIsFailureStatusChanged'
	static final String JOB_GROUP = 'GRAILS_JOBS'

	static triggers = {
		simple name: JOB_NAME, startDelay: 10000, repeatInterval: 30000
	}

	static initializedTestOutcomeCount = 0
	static initializedTestRunCount = 0

	def execute() {
		def testOutcomes = TestOutcome.findAllByIsFailureStatusChangedIsNull([offset: 0, max: 100])
		if (testOutcomes) {
			TestOutcome.withTransaction {
				for (TestOutcome testOutcome: testOutcomes)
					testOutcome.isFailureStatusChanged = testOutcomeService.isFailureStatusChanged(testOutcome)
				dataService.saveTestOutcomes(testOutcomes)
			}
			initializedTestOutcomeCount += testOutcomes.size()
			if (initializedTestOutcomeCount % 1000 == 0)
				log.info "Initialized $initializedTestOutcomeCount TestOutcomes."
		} else {
			log.info "Finished initializing ${initializedTestOutcomeCount} TestOutcomes."
			log.info "Unscheduling job [$JOB_NAME] ..."
			quartzScheduler.unscheduleJob(JOB_NAME, JOB_GROUP)
			initTestRunStats()
		}
	}

	void initTestRunStats() {
		def testRunsToUpdate = getTestRunsWithNullNewFailures()
		def updateQuery = "update TestRunStats stats set stats.newFailures = ? where stats.id = ?"
		while (testRunsToUpdate.size() > 0) {
			TestRun.withTransaction {
				for (TestRun testRun: testRunsToUpdate) {
					testRun.testRunStatistics.newFailures = getNewFailuresCount(testRun)
					initializedTestRunCount += TestRunStats.executeUpdate(updateQuery,
						[testRun.testRunStatistics.newFailures, testRun.testRunStatistics.id])
				}
			}
			log.info "Initialized ${testRunsToUpdate.size()} TestRuns: ${testRunsToUpdate*.id}."
			if (grailsApplication.config.newFailuresSleep) {
				sleep(grailsApplication.config.newFailuresSleep)
			}
			testRunsToUpdate = getTestRunsWithNullNewFailures()
		}
		log.info "Finished initializing ${initializedTestRunCount} TestRuns."
	}

	List<TestRun> getTestRunsWithNullNewFailures() {
		return TestRun.createCriteria().list {
			testRunStatistics {
				isNull('newFailures')
			}
			maxResults(100)
		}
	}

	Integer getNewFailuresCount(TestRun testRun) {
		return TestOutcome.createCriteria().count {
			and {
				eq('testRun', testRun)
				eq('isFailureStatusChanged', true)
				testResult {
					eq('isFailure', true)
				}
			}
		}
	}
}