package cuanto

class FailureStatusUpdateTask {

	static constraints = {
		targetId(nullable: false)
	}

	Long targetId
	Class type

	FailureStatusUpdateTask() {}

	FailureStatusUpdateTask(testRunOrOutcome) {
		this.targetId = testRunOrOutcome.id
		this.type = testRunOrOutcome.class
	}

	FailureStatusUpdateTask(Long targetId, Class type) {
		this.targetId = targetId
		this.type = type
	}
}
