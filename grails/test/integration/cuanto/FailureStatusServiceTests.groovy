package cuanto

import cuanto.test.TestObjects

/**
 * @author SukHyun.Cho
 */
class FailureStatusServiceTests extends GroovyTestCase  {
	FailureStatusService failureStatusService
	DataService dataService
	TestObjects testObjects

	@Override
	void setUp() {
		super.setUp()
		testObjects = new TestObjects(dataService: dataService)
	}

	void testQueueMultipleTestOutcomes() {
		def project = testObjects.getProject()
		dataService.saveDomainObject(project)
		def testRun = testObjects.getTestRun(project)
		dataService.saveDomainObject(testRun)
		def testCase = testObjects.getTestCase(project)
		dataService.saveDomainObject(testCase)
		def testOutcome = testObjects.getTestOutcome(testCase, testRun)
		dataService.saveDomainObject(testOutcome)

		failureStatusService.queueFailureStatusUpdateForOutcome(testOutcome)
		assertEquals "unexpected number of FailureStatusUpdateTasks",
			1, FailureStatusUpdateTask.findAllByTargetId(testOutcome.id).size()

		failureStatusService.queueFailureStatusUpdateForOutcome(testOutcome)
		assertEquals "queueing multiple FailureStatusUpdateTasks for the same test outcome should be ignored",
			1, FailureStatusUpdateTask.findAllByTargetId(testOutcome.id).size()
	}

	void testQueueMultipleTestRuns() {
		def project = testObjects.getProject()
		dataService.saveDomainObject(project)
		def testRun = testObjects.getTestRun(project)
		dataService.saveDomainObject(testRun)
		def testCase = testObjects.getTestCase(project)
		dataService.saveDomainObject(testCase)
		def testOutcome = testObjects.getTestOutcome(testCase, testRun)
		dataService.saveDomainObject(testOutcome)

		failureStatusService.queueFailureStatusUpdateForRun(testRun)
		assertEquals "unexpected number of FailureStatusUpdateTasks",
			1, FailureStatusUpdateTask.findAllByTargetId(testRun.id).size()

		failureStatusService.queueFailureStatusUpdateForRun(testRun)
		assertEquals "queueing multiple FailureStatusUpdateTasks for the same test runs should be ignored",
			1, FailureStatusUpdateTask.findAllByTargetId(testRun.id).size()
	}
}
