package cuanto

class FailureStatusService {

	def dataService
	boolean transactional = false

	def queueFailureStatusUpdateForOutcome(affectedOutcome) {
		if (affectedOutcome) {
			log.info "adding test outcome ${affectedOutcome.id} to failure status update queue"
			def updateTaskForAffectedOutcome = new FailureStatusUpdateTask(affectedOutcome)
			dataService.saveDomainObject(updateTaskForAffectedOutcome)
		}
	}

	def queueFailureStatusUpdateForRun(affectedTestRun) {
		if (affectedTestRun) {
			log.info "adding test run ${affectedTestRun.id} to failure status update queue"
			dataService.saveDomainObject(new FailureStatusUpdateTask(affectedTestRun))
		}
	}
}
