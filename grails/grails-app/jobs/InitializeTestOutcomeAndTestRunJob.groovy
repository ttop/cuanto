import cuanto.TestOutcome

import cuanto.TestRunStats
import cuanto.TestRun
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Semaphore

/**
 * Self-terminating Job that initializes TestOutcome.isFailureStatusChanged,
 * after which it initializes TestRuns whose testRunStatistics.newFailures is null.
 * Upon terminus, the job unschedules itself so that the job is no longer executed.
 */
class InitializeTestOutcomeAndTestRunJob {
	def dataService
	def testOutcomeService
	def initializationService
	def quartzScheduler
	def grailsApplication
	def concurrent = false

	static final String JOB_NAME = 'InitializeIsFailureStatusChanged'
	static final String JOB_GROUP = 'GRAILS_JOBS'
	static final String TEST_RUN_UPDATE_QUERY = "update TestRunStats stats set stats.newFailures = ? where stats.id = ?"

	static triggers = {
		simple name: JOB_NAME, startDelay: 10000, repeatInterval: 1000
	}

	static int initializedTestOutcomeCount = 0
	static int initializedTestRunCount = 0
	static boolean allTestOutcomesInitialized = false
	static boolean allTestRunsInitialized = false

	def execute() {
		if (grailsApplication.config.testOutcomeAndTestRunInitSleep)
			sleep(grailsApplication.config.testOutcomeAndTestRunInitSleep)

		if (allTestOutcomesInitialized && allTestRunsInitialized) {
			// since all test outcomes and test runs have been initialized, unschedule the job
			quartzScheduler.unscheduleJob(JOB_NAME, JOB_GROUP)
		} else if (!allTestOutcomesInitialized) {
			// if not all test outcomes have been initialized, there are some more work to do, here.
			initializeTestOutcomes()
			if (initializedTestOutcomeCount % 1000 == 0)
				log.info "Initialized $initializedTestOutcomeCount TestOutcomes."
		} else {
			// if not all test runs have been initialized, there are some more work to do, here.
			initializeTestRuns()
			if (initializedTestRunCount % 100 == 0)
				log.info "Initialized $initializedTestRunCount TestOutcomes."
		}
	}

	def initializeTestOutcomes() {
		TestOutcome.withTransaction {
			def testOutcomes = TestOutcome.findAllByIsFailureStatusChangedIsNull([offset: 0, max: 100])
			if (testOutcomes) {
				// there are test outcomes to update--so calculate isFailureStatusChanged and set it
				for (TestOutcome testOutcome: testOutcomes)
					testOutcome.isFailureStatusChanged = testOutcomeService.isFailureStatusChanged(testOutcome)
				dataService.saveTestOutcomes(testOutcomes)
				initializedTestOutcomeCount += testOutcomes.size()
			} else {
				// there are no test runs to update--this means all test outcomes have been initialized
				allTestOutcomesInitialized = true
				log.info "Finished initializing ${initializedTestOutcomeCount} TestOutcomes."
			}
		}
	}

	def initializeTestRuns() {
		TestRun.withTransaction {
			def testRunsToUpdate = getTestRunsWithNullNewFailures()
			if (testRunsToUpdate) {
				// there are test runs to update--so calculate newFailures and set it
				for (TestRun testRun: testRunsToUpdate) {
					testRun.testRunStatistics.newFailures = getNewFailuresCount(testRun)
					initializedTestRunCount += TestRunStats.executeUpdate(TEST_RUN_UPDATE_QUERY,
						[testRun.testRunStatistics.newFailures, testRun.testRunStatistics.id])
				}
				log.info "Initialized ${testRunsToUpdate.size()} TestRuns: ${testRunsToUpdate*.id}."
			} else {
				// there are no test runs to update--this means all test runs have been initialized
				allTestRunsInitialized = true
				log.info "Finished initializing ${initializedTestRunCount} TestRuns."
			}
		}
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