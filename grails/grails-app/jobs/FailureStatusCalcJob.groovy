import cuanto.TestOutcome
import cuanto.FailureStatusUpdateTask
import cuanto.TestRun

/**
 *
 */
class FailureStatusCalcJob {
	def dataService
	def testOutcomeService
	def statisticService
	def transactional = true
	def concurrent = false
	static final long INTERVAL = 15000

	static triggers = {
		simple name: "FailureStatusCalc", startDelay: 10000, repeatInterval: INTERVAL
	}

	def execute() {
		def updateTasks = getFailureStatusUpdateTasks(1000)
		def updatedTestOutcomes = []

		updateTasks.each { FailureStatusUpdateTask updateTask ->

			switch (updateTask.type) {
				case TestOutcome.class:
					def updatedTestOutcome = updateTestOutcome(updateTask.targetId)
					updatedTestOutcomes << updatedTestOutcome
					statisticService.queueTestRunStats(updatedTestOutcome.testRun?.id)
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
		if (testOutcome)
			testOutcome.isFailureStatusChanged = testOutcomeService.isFailureStatusChanged(testOutcome)
		dataService.saveDomainObject testOutcome

		return testOutcome
	}

	List updateTestOutcomesForTestRun(Long testRunId) {
		def updatedOutcomes = []
		def currentBatch = dataService.getTestOutcomesForTestRun(testRunId, 1000, 0)
		while (currentBatch) {
			for (TestOutcome outcome: currentBatch) {
				outcome.isFailureStatusChanged = testOutcomeService.isFailureStatusChanged(outcome)
				updatedOutcomes << outcome
			}

			dataService.saveTestOutcomes currentBatch

			log.info "re-initialized isFailureStatusChanged for ${currentBatch.size()} test outcomes"
			log.info "sleeping ${INTERVAL}ms before updating more test outcomes for test run $testRunId"
			sleep(INTERVAL)

			currentBatch = dataService.getTestOutcomesForTestRun(testRunId, 1000, updatedOutcomes.size() - 1)
		}

		// if there are some outcomes left, save them.
		// this happens when currentBatch.size() > 0 && currentBatch.size() > 1000
		if (currentBatch) {
			log.info "re-initialized isFailureStatusChanged for ${currentBatch.size()} test outcomes"
			dataService.saveTestOutcomes currentBatch
		}

		return updatedOutcomes
	}

	List<FailureStatusUpdateTask> getFailureStatusUpdateTasks(int numToGet) {
		return FailureStatusUpdateTask.list(max: numToGet)
	}
}