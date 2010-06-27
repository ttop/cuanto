package cuanto

class FailureStatusService {

	def dataService
	boolean transactional = false

	def queueFailureStatusUpdateForOutcomes(affectedOutcomes) {
		if (affectedOutcomes) {
			log.info "adding test outcomes ${affectedOutcomes*.id} to failure status update queue"
             def notNullOutcomes = affectedOutcomes.findAll {
                it != null
            }
			def updateTasksForAffectedOutcomes = notNullOutcomes.collect { affectedOutcome ->
				new FailureStatusUpdateTask(affectedOutcome)
			}
			dataService.saveTestOutcomes(updateTasksForAffectedOutcomes)
		}
	}

	def queueFailureStatusUpdateForOutcome(affectedOutcome) {
		queueFailureStatusUpdateForOutcomes([affectedOutcome])
	}

	def queueFailureStatusUpdateForRun(affectedTestRun) {
		if (affectedTestRun) {
			log.info "adding test run ${affectedTestRun.id} to failure status update queue"
			dataService.saveDomainObject(new FailureStatusUpdateTask(affectedTestRun))
		}
	}
}
