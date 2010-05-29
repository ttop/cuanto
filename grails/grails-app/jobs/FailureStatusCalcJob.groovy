import cuanto.TestOutcome
import cuanto.FailureStatusUpdateTask

/**
 *
 */
class FailureStatusCalcJob {
	def dataService
	def testOutcomeService
	def concurrent = false

	static triggers = {
		simple name: "FailureStatusCalc", startDelay: 10000, repeatInterval: 15000
	}

	def execute() {
		def updateTasks = getFailureStatusUpdateTasks(1000)
		TestOutcome.withTransaction {
			def updatedTestOutcomes = []

			updateTasks.each { FailureStatusUpdateTask updateTask ->
				def testOutcome = TestOutcome.get(updateTask.targetId)
				if (testOutcome) {
					testOutcome.isFailureStatusChanged = testOutcomeService.isFailureStatusChanged(testOutcome)
					updatedTestOutcomes << testOutcome
				}
				updateTask.delete()
			}

			if (updatedTestOutcomes) {
				dataService.saveTestOutcomes(updatedTestOutcomes)
				log.info "Re-initialized isFailureStatusChanged for ${updatedTestOutcomes.size()} TestOutcomes."
			}
		}
	}

	List<FailureStatusUpdateTask> getFailureStatusUpdateTasks(int numToGet) {
		return FailureStatusUpdateTask.list(max: numToGet)
	}
}