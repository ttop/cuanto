import cuanto.TestOutcome

import cuanto.TestRunStats
import cuanto.TestRun

/*
	Copyright (c) 2010 Suk-Hyun Cho

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

	static final String JOB_NAME = this.class.simpleName.replace("Job", "")
	static final String JOB_GROUP = 'GRAILS_JOBS'
	static final String TEST_RUN_UPDATE_QUERY = "update TestRunStats stats set stats.newFailures = ? where stats.id = ?"
	static final long DEFAULT_SLEEP_TIME = 5000

	static triggers = {
		simple name: JOB_NAME, startDelay: 0, repeatInterval: 1000
	}

	static int initializedTestOutcomeCount = 0
	static int initializedTestRunCount = 0
	static boolean allTestOutcomesInitialized = false
	static boolean allTestRunsInitialized = false

	def execute() {
		sleep(getSleepTime())

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
			def testRunStatsToUpdate = TestRunStats.executeQuery("from TestRunStats stats where stats.newFailures is null")
			if (testRunStatsToUpdate) {
				// there are test runs to update--so calculate newFailures and set it
				for (TestRunStats testRunStatistics: testRunStatsToUpdate) {
					testRunStatistics.newFailures = getNewFailuresCount(testRunStatistics.testRun)
					initializedTestRunCount += TestRunStats.executeUpdate(TEST_RUN_UPDATE_QUERY,
						[testRunStatistics.newFailures, testRunStatistics.id])
				}
				log.info "Initialized ${testRunStatsToUpdate.size()} TestRuns: ${testRunStatsToUpdate*.testRun.id}."
			} else {
				// there are no test runs to update--this means all test runs have been initialized
				allTestRunsInitialized = true
				log.info "Finished initializing ${initializedTestRunCount} TestRuns."
			}
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

	long getSleepTime()
	{
		return grailsApplication.config.testOutcomeAndTestRunInitSleepTime ?: DEFAULT_SLEEP_TIME
	}
}