package cuanto

class FailureStatusUpdateTask {

	static constraints = {
		targetId(nullable: false)
	}

	// the id of the domain object for which to recalculate the failure status
	Long targetId

	// the Class.getName() value of the domain object for which to recalculate the failure status
	String type

	private FailureStatusUpdateTask() {}

	FailureStatusUpdateTask(testRunOrOutcome) {
		this.targetId = testRunOrOutcome.id
		this.type = testRunOrOutcome.class.name
	}
}
