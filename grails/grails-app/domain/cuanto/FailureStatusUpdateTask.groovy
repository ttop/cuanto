package cuanto

class FailureStatusUpdateTask {

	static constraints = {
		targetId(nullable: false)
	}

	Long targetId

	FailureStatusUpdateTask() {}

	FailureStatusUpdateTask(Long targetId) {
		this.targetId = targetId
	}
}
