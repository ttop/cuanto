package cuanto

import cuanto.test.TestObjects

/**
 * User: Todd Wells
 * Date: Apr 22, 2009
 * Time: 5:44:16 PM
 * 
 */
class InitializationServiceTests extends GroovyTestCase {

	def dataService
	def testOutcomeService
	def initializationService
	def to = new TestObjects()

	@Override
	void setUp()
	{
		super.setUp()
		to.dataService = dataService
	}

	void testDefaultAnalysisState() {
		initializationService.initializeAll()

		assertEquals "Wrong number of analysis states", 8, AnalysisState.list().size()
		def analyzed = AnalysisState.findAllByIsAnalyzed(true)
		assertEquals "Wrong number are analyzed", 6, analyzed.size()
		def stateNotDefault = AnalysisState.findAllByIsDefault(false)
		assertEquals "Wrong number are not default", 7, stateNotDefault.size()

		assertNull "Found Unanalyzed state in analyzed list", analyzed.find { it -> it.name == "Unanalyzed" }
		assertNull "Found Investigate state in analyzed list", analyzed.find { it -> it.name == "Investigate" }

		def unanalyzed = AnalysisState.findAllByIsAnalyzed(false)
		assertEquals "Wrong number of unanalyzed states", 2, unanalyzed.size()
		assertNotNull "Couldn't find Unanalyzed state in unanalyzed list", unanalyzed.find { it -> it.name == "Unanalyzed" }
		assertNotNull "Couldn't find Investigate state in unanalyzed list", unanalyzed.find { it -> it.name == "Investigate" }


		def stateDefault = AnalysisState.findAllByIsDefault(true)
		assertEquals "Should only be one default analysis state", 1, stateDefault.size()
		assertEquals "Wrong default analysis state", "Unanalyzed", stateDefault[0].name
	}

	void testInitializeAnalysisStatusForFirstFailureAndPass()
	{
		// create a project
		Project proj = to.getProject()
		proj.save()

		// create a couple of test cases
		TestCase tc1 = to.getTestCase(proj)
		TestCase tc2 = to.getTestCase(proj)
		dataService.saveDomainObject tc1
		dataService.saveDomainObject tc2

		// create test runs
		TestRun firstTestRun = to.getTestRun(proj)
		firstTestRun.save()

		// verify initial newFailure/newPasses counts
		assertEquals "Wrong number of new failures.", 0, testOutcomeService.countOutcomes([filter: 'newFailures'])
		assertEquals "Wrong number of new passes.", 0, testOutcomeService.countOutcomes([filter: 'newPasses'])

		/* For the first test run */

		// fail
		TestOutcome outcome1 = to.getTestOutcome(tc1, firstTestRun)
		outcome1.testResult = dataService.result("fail")
		outcome1.save()

		// pass
		TestOutcome outcome2 = to.getTestOutcome(tc2, firstTestRun)
		outcome2.testResult = dataService.result("pass")
		outcome2.save()

		// null isFailureStatusChanged should result in no new failures or passes
		assertEquals "Wrong number of new failures.", 0, testOutcomeService.countOutcomes(
			[id: firstTestRun.id, filter: 'newFailures'])
		assertEquals "Wrong number of new passes.", 0, testOutcomeService.countOutcomes(
			[id: firstTestRun.id, filter: 'newPasses'])

		initializationService.initializeAll()

		// fetch the outcomes and verify the correct isFailureStatusChanged values
		TestOutcome initializedOutcome1 = TestOutcome.get(outcome1.id)
		TestOutcome initializedOutcome2 = TestOutcome.get(outcome2.id)
		assertTrue "First failure should have been initialized with isFailureStatusChanged = true",
			initializedOutcome1.isFailureStatusChanged
		assertFalse "First pass should have been initialized with isFailureStatusChanged = false",
			initializedOutcome2.isFailureStatusChanged
	}

	void testInitializeAnalysisStatusForSubsequentOutcomesWithResults()
	{
		// create a project
		Project proj = to.getProject()
		proj.save()

		// create a couple of test cases
		TestCase tc1 = to.getTestCase(proj)
		TestCase tc2 = to.getTestCase(proj)
		dataService.saveDomainObject tc1
		dataService.saveDomainObject tc2

		// create test runs
		TestRun firstTestRun = to.getTestRun(proj)
		TestRun secondTestRun = to.getTestRun(proj)
		firstTestRun.save()
		secondTestRun.save()

		// verify initial newFailure/newPasses counts
		assertEquals "Wrong number of new failures.", 0, testOutcomeService.countOutcomes([filter: 'newFailures'])
		assertEquals "Wrong number of new passes.", 0, testOutcomeService.countOutcomes([filter: 'newPasses'])

		/* For the first test run */

		// fail
		TestOutcome outcome1 = to.getTestOutcome(tc1, firstTestRun)
		outcome1.testResult = dataService.result("fail")
		outcome1.save()

		// pass
		TestOutcome outcome2 = to.getTestOutcome(tc2, firstTestRun)
		outcome2.testResult = dataService.result("pass")
		outcome2.save()

		/* For the second test run */

		// fail -> pass
		TestOutcome outcome3 = to.getTestOutcome(tc1, secondTestRun)
		outcome3.testResult = dataService.result("pass")
		outcome3.isFailureStatusChanged = true
		outcome3.save()

		// pass -> fail
		TestOutcome outcome4 = to.getTestOutcome(tc2, secondTestRun)
		outcome4.testResult = dataService.result("error")
		outcome4.isFailureStatusChanged = true
		outcome4.save()

		initializationService.initializeAll()

		// fetch the outcomes and verify the correct isFailureStatusChanged values
		TestOutcome initializedOutcome1 = TestOutcome.get(outcome1.id)
		TestOutcome initializedOutcome2 = TestOutcome.get(outcome2.id)
		TestOutcome initializedOutcome3 = TestOutcome.get(outcome3.id)
		TestOutcome initializedOutcome4 = TestOutcome.get(outcome4.id)
		assertTrue "First failure should have been initialized with isFailureStatusChanged = true",
			initializedOutcome1.isFailureStatusChanged
		assertFalse "First pass should have been initialized with isFailureStatusChanged = false",
			initializedOutcome2.isFailureStatusChanged
		assertTrue "Pass after failure should have been initialized with isFailureStatusChanged = true",
			initializedOutcome3.isFailureStatusChanged
		assertTrue "Failure after pass should have been initialized with isFailureStatusChanged = true",
			initializedOutcome4.isFailureStatusChanged
	}

	void testInitializeAnalysisStatusForSubsequentOutcomesWithSameResults()
	{
		// create a project
		Project proj = to.getProject()
		proj.save()

		// create a couple of test cases
		TestCase tc1 = to.getTestCase(proj)
		TestCase tc2 = to.getTestCase(proj)
		dataService.saveDomainObject tc1
		dataService.saveDomainObject tc2

		// create test runs
		TestRun firstTestRun = to.getTestRun(proj)
		TestRun secondTestRun = to.getTestRun(proj)
		TestRun thirdTestRun = to.getTestRun(proj)
		firstTestRun.save()
		secondTestRun.save()
		thirdTestRun.save()

		// verify initial newFailure/newPasses counts
		assertEquals "Wrong number of new failures.", 0, testOutcomeService.countOutcomes([filter: 'newFailures'])
		assertEquals "Wrong number of new passes.", 0, testOutcomeService.countOutcomes([filter: 'newPasses'])

		/* For the first test run */

		// fail
		TestOutcome outcome1 = to.getTestOutcome(tc1, firstTestRun)
		outcome1.testResult = dataService.result("fail")
		outcome1.save()

		// pass
		TestOutcome outcome2 = to.getTestOutcome(tc2, firstTestRun)
		outcome2.testResult = dataService.result("pass")
		outcome2.save()

		/* For the second test run */

		// fail -> pass
		TestOutcome outcome3 = to.getTestOutcome(tc1, secondTestRun)
		outcome3.testResult = dataService.result("pass")
		outcome3.save()

		// pass -> fail
		TestOutcome outcome4 = to.getTestOutcome(tc2, secondTestRun)
		outcome4.testResult = dataService.result("error")
		outcome4.save()

		/* For the third test run */

		// fail -> pass -> pass
		TestOutcome outcome5 = to.getTestOutcome(tc1, thirdTestRun)
		outcome5.testResult = dataService.result("pass")
		outcome5.save()

		// pass -> fail -> fail
		TestOutcome outcome6 = to.getTestOutcome(tc2, thirdTestRun)
		outcome6.testResult = dataService.result("error")
		outcome6.save()

		initializationService.initializeAll()

		// fetch the outcomes and verify the correct isFailureStatusChanged values
		TestOutcome initializedOutcome1 = TestOutcome.get(outcome1.id)
		TestOutcome initializedOutcome2 = TestOutcome.get(outcome2.id)
		TestOutcome initializedOutcome3 = TestOutcome.get(outcome3.id)
		TestOutcome initializedOutcome4 = TestOutcome.get(outcome4.id)
		TestOutcome initializedOutcome5 = TestOutcome.get(outcome5.id)
		TestOutcome initializedOutcome6 = TestOutcome.get(outcome6.id)
		assertTrue "First failure should have been initialized with isFailureStatusChanged = true",
			initializedOutcome1.isFailureStatusChanged
		assertFalse "First pass should have been initialized with isFailureStatusChanged = false",
			initializedOutcome2.isFailureStatusChanged
		assertTrue "Pass after failure should have been initialized with isFailureStatusChanged = true",
			initializedOutcome3.isFailureStatusChanged
		assertTrue "Failure after pass should have been initialized with isFailureStatusChanged = true",
			initializedOutcome4.isFailureStatusChanged
		assertFalse "Pass after pass after fail should have been initialized with isFailureStatusChanged = false",
			initializedOutcome5.isFailureStatusChanged
		assertFalse "Fail after fail after pass should have been initialized with isFailureStatusChanged = false",
			initializedOutcome6.isFailureStatusChanged
	}

	void testInitializeAnalysisStatusForTestOutcomesLessThanBatchSize()
	{
		verifyBoundaryConditionForAnalysisStatusInitialization(99)
	}

	void testInitializeAnalysisStatusForTestOutcomesEqualToBatchSize()
	{
		verifyBoundaryConditionForAnalysisStatusInitialization(100)
	}

	void testInitializeAnalysisStatusForTestOutcomesGreaterThanBatchSize()
	{
		verifyBoundaryConditionForAnalysisStatusInitialization(101)
	}

	void testInitializeAnalysisStatusForTestOutcomesGreaterThanMultiplesOfBatchSize()
	{
		verifyBoundaryConditionForAnalysisStatusInitialization(201)
	}

	private void verifyBoundaryConditionForAnalysisStatusInitialization(int numOutcomes)
	{
		// create a project
		Project proj = to.getProject()
		proj.save()

		// create many test cases
		def testCases = []
		numOutcomes.times {
			TestCase tc = to.getTestCase(proj)
			testCases << tc
			dataService.saveDomainObject tc
		}

		// create a test run
		TestRun testRun = to.getTestRun(proj)
		testRun.testRunStatistics = new TestRunStats()
		testRun.save()

		// create test outcomes
		def outcomes = []
		testCases.each { tc ->
			TestOutcome outcome = to.getTestOutcome(tc, testRun)
			outcome.testResult = dataService.result("fail")
			outcome.save()
			outcomes << outcome
		}

		// verify initial newFailure/newPasses counts
		assertEquals "Wrong number of new failures.", 0, testOutcomeService.countOutcomes([filter: 'newFailures'])
		assertEquals "Wrong number of new passes.", 0, testOutcomeService.countOutcomes([filter: 'newPasses'])

		initializationService.initializeAll()

		def initializedOutcomes = TestOutcome.getAll(outcomes*.id)
		assertTrue "All test outcomes should have been initialized to isFailureStatusChanged = true",
			initializedOutcomes.every { it.isFailureStatusChanged }
		assertEquals "The TestRun's testRunStatistics.newFailures should have been initialized.",
			testRun.testRunStatistics.newFailures, outcomes.size()
	}
}