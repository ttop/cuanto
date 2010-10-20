import cuanto.TestOutcome
import cuanto.FailureStatusUpdateTask
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

class FailureStatusCalcJob {
	def dataService
	def testOutcomeService
	def statisticService
	def grailsApplication
	def concurrent = false

	static final long DEFAULT_SLEEP_TIME = 1000
	static final int DEFAULT_BATCH_SIZE = 100

	static triggers = {
		simple name: "FailureStatusCalc", startDelay: 10000, repeatInterval: 15000
	}

	def execute() {
		def updateTasks = getFailureStatusUpdateTasks(getBatchSize())
		def updatedTestOutcomes = []

		updateTasks.each { FailureStatusUpdateTask updateTask ->

			switch (updateTask.type) {
				case TestOutcome.class:
					def updatedTestOutcome = updateTestOutcome(updateTask.targetId)
					if (updatedTestOutcome) {
						updatedTestOutcomes << updatedTestOutcome
						statisticService.queueTestRunStats(updatedTestOutcome.testRun?.id)
					}
					break
				case TestRun.class:
					updatedTestOutcomes + updateTestOutcomesForTestRun(updateTask.targetId)
					statisticService.queueTestRunStats(updateTask.targetId)
					break
			}

			updateTask.delete()
		}

		if (updatedTestOutcomes) {
			dataService.saveTestOutcomes(updatedTestOutcomes)
			log.info "Re-initialized isFailureStatusChanged for ${updatedTestOutcomes.size()} TestOutcomes."
		}
	}

	TestOutcome updateTestOutcome(Long testOutcomeId) {
		def testOutcome = TestOutcome.get(testOutcomeId)
		if (testOutcome) {
			def previousValue = testOutcome.isFailureStatusChanged
			def newValue = testOutcomeService.isFailureStatusChanged(testOutcome)
			if (previousValue != newValue) {
				testOutcome.isFailureStatusChanged = testOutcomeService.isFailureStatusChanged(testOutcome)
				dataService.saveDomainObject testOutcome
				return testOutcome
			}
		}

		log.debug "Ignoring TestOutcome $testOutcomeId, " +
			"because it either does not exist or the failure status did not change."

		return null
	}

	List updateTestOutcomesForTestRun(Long testRunId) {
		def updatedOutcomes = []
		def currentBatch = dataService.getTestOutcomesForTestRun(testRunId, getBatchSize(), 0)
		while (currentBatch) {
			for (TestOutcome outcome: currentBatch) {
				outcome.isFailureStatusChanged = testOutcomeService.isFailureStatusChanged(outcome)
				updatedOutcomes << outcome
			}

			dataService.saveTestOutcomes currentBatch

			log.info "re-initialized isFailureStatusChanged for ${currentBatch.size()} test outcomes"
			log.info "sleeping ${getSleepTime()}ms before updating more test outcomes for test run $testRunId"
			sleep(getSleepTime())

			currentBatch = dataService.getTestOutcomesForTestRun(testRunId, getBatchSize(), updatedOutcomes.size() - 1)
		}

		// if there are some outcomes left, save them.
		// this happens when currentBatch.size() > 0 && currentBatch.size() > BATCH_SIZE
		if (currentBatch) {
			log.info "re-initialized isFailureStatusChanged for ${currentBatch.size()} test outcomes"
			dataService.saveTestOutcomes currentBatch
		}

		return updatedOutcomes
	}

	List<FailureStatusUpdateTask> getFailureStatusUpdateTasks(int numToGet) {
		return FailureStatusUpdateTask.list(max: numToGet)
	}

	long getSleepTime()
	{
		return grailsApplication.config.failureStatusCalcJobSleepTime ?: DEFAULT_SLEEP_TIME
	}

	int getBatchSize()
	{
		return grailsApplication.config.failureStatusCalcJobBatchSize ?: DEFAULT_BATCH_SIZE
	}
}