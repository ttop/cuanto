package cuanto

import cuanto.test.TestObjects
import cuanto.test.WordGenerator

/**
 * User: Todd Wells
 * Date: Apr 28, 2009
 * Time: 6:09:09 PM
 *
 */
class TestCaseTests extends GroovyTestCase {
	TestOutcomeService testOutcomeService
	TestRunService testRunService
	InitializationService initializationService
	DataService dataService
	ProjectService projectService

	TestObjects to
	WordGenerator wordGen


	@Override
	void setUp() {
		initializationService.initializeAll()
		to = new TestObjects()
		to.dataService = dataService
		wordGen = new WordGenerator()
	}


	void testGetTestCases() {
		Project projectOne = to.getProject()
		projectOne.save()

		def numCases = 10

		for (x in 1..numCases) {
			TestCase tc = to.getTestCase(projectOne)
			dataService.saveDomainObject tc
		}

		assertEquals "Wrong number of test cases", numCases, dataService.getTestCases(projectOne).size()

		Project projectTwo = to.getProject()
		projectTwo.save()

		for (x in 1..numCases) {
			TestCase tc = to.getTestCase(projectTwo)
			dataService.saveDomainObject tc
		}

		// re-check total of project one
		assertEquals "Wrong number of test cases", numCases, dataService.getTestCases(projectOne).size()
	}


	void testCountTestCaseFailures() {
		def project = to.project
		dataService.saveDomainObject project

		def testCases = 2
		1.upto(testCases) {
			def testCase = to.getTestCase(project)
			dataService.saveDomainObject testCase
		}

		def numTestRuns = 10

		def runs = []
		1.upto(numTestRuns) {
			def testRun = to.getTestRun(project)
			dataService.saveDomainObject testRun
			runs << testRun
		}

		dataService.getTestCases(project).each {testCase ->
			runs.each {testRun ->
				def testOutcome = to.getTestOutcome(testCase, testRun)
				if (runs.indexOf(testRun) % 2 == 0) {
					testOutcome.testResult = dataService.result("Fail")
				} else if (testRun == runs[9]) {
					testOutcome.testResult = dataService.result("Ignore")
				}

				dataService.saveDomainObject testOutcome
			}
		}

		TestCase testCase = dataService.getTestCases(project)[0] 
		assertEquals "Wrong failure count", 5, dataService.countTestOutcomes(new TestOutcomeQueryFilter(testCase: testCase, isFailure:true,
			testResultIncludedInCalculations: true))
	}


	void testGetTestCasesWithParams() {
		def project = to.project
		dataService.saveDomainObject project

		def testCases = 12
		1.upto(testCases) {
			def testCase = to.getTestCase(project)
			dataService.saveDomainObject testCase
		}

		assertEquals "Wrong number of test cases", testCases, dataService.getTestCases(project, 0, 20).size()
		assertEquals "Wrong number of test cases", testCases, dataService.getTestCases(project, 0, 12).size()
		assertEquals "Wrong number of test cases", 5, dataService.getTestCases(project, 0, 5).size()
		assertEquals "Wrong number of test cases", 7, dataService.getTestCases(project, 5, 20).size()
	}


	void testDeleteTestCase() {
		def project = to.project
		dataService.saveDomainObject project

		def testCases = 2
		1.upto(testCases) {
			def testCase = to.getTestCase(project)
			dataService.saveDomainObject testCase
		}

		def numTestRuns = 10

		def runs = []
		1.upto(numTestRuns) {
			def testRun = to.getTestRun(project)
			dataService.saveDomainObject testRun
			runs << testRun
		}

		dataService.getTestCases(project).each {testCase ->
			runs.each {testRun ->
				def testOutcome = to.getTestOutcome(testCase, testRun)
				if (runs.indexOf(testRun) % 2 == 0) {
					testOutcome.testResult = dataService.result("Fail")
				} else if (testRun == runs[9]) {
					testOutcome.testResult = dataService.result("Ignore")
				}

				dataService.saveDomainObject testOutcome
			}
		}
		def testCase = dataService.getTestCases(project)[0]
		assertEquals "wrong outcome total for test case", numTestRuns, TestOutcome.findAllByTestCase(testCase).size()

		def testCaseId = dataService.getTestCases(project)[0].id
		dataService.deleteTestCase(dataService.getTestCases(project)[0])
		assertNull "Test case found", TestCase.get(testCaseId)

		assertEquals "No outcomes should exist for test case", 0, TestOutcome.findAllByTestCase(testCase).size()

		def fetchedProj = Project.get(project.id)
		assertEquals "Wrong number of test cases", 1, dataService.getTestCases(fetchedProj).size()
	}


	void testAnalysisCount() {
		def project = to.project
		dataService.saveDomainObject project

		// create test case
		TestCase testCase = to.getTestCase(project)
		dataService.saveDomainObject testCase

		// verify analysisCount is 0
		assertEquals "Wrong analysisCount", 0, 	dataService.countAnalysesForTestCase(testCase)
		assertEquals "Wrong analysisCount", 0, testCase.analysisCount

		// create an unanalyzed TestOutcome
		TestRun runA = to.getTestRun(project)
		dataService.saveDomainObject(runA)

		TestOutcome outcomeA = to.getTestOutcome(testCase, runA)
		dataService.saveDomainObject(outcomeA)


		// verify analysisCount is 0
		assertEquals "Wrong analysisCount", 0, 	dataService.countAnalysesForTestCase(testCase)
		dataService.updateAnalysisCountForTestCase(testCase)
		assertEquals "Wrong analysisCount", 0, testCase.analysisCount

		// create an unanalyzed TestOutcome
		TestRun runB = to.getTestRun(project)
		dataService.saveDomainObject(runB)

		TestOutcome outcomeB = to.getTestOutcome(testCase, runB)
		dataService.saveDomainObject(outcomeB)

		// verify analysisCount is 0
		assertEquals "Wrong analysisCount", 0, dataService.countAnalysesForTestCase(testCase)
		dataService.updateAnalysisCountForTestCase(testCase)
		assertEquals "Wrong analysisCount", 0, testCase.analysisCount


		// Change analysisState of one TestOutcome
		outcomeA.analysisState = dataService.getAnalysisStateByName("Bug")
		dataService.saveDomainObject(outcomeA)

		// verify analysisCount is 1
		assertEquals "Wrong analysisCount", 1, dataService.countAnalysesForTestCase(testCase)
		dataService.updateAnalysisCountForTestCase(testCase)
		assertEquals "Wrong analysisCount", 1, testCase.analysisCount

		// Change analysisState of another TestOutcome
		outcomeB.analysisState = dataService.getAnalysisStateByName("Environment")
		dataService.saveDomainObject(outcomeB)

		// verify analysisCount is 2
		assertEquals "Wrong analysisCount", 2, dataService.countAnalysesForTestCase(testCase)
		dataService.updateAnalysisCountForTestCase(testCase)
		assertEquals "Wrong analysisCount", 2, testCase.analysisCount

		// Change one analysis State to unanalyzed
		outcomeB.analysisState = dataService.getAnalysisStateByName("Investigate")
		dataService.saveDomainObject(outcomeB)

		// verify analysisCount is 1
		assertEquals "Wrong analysisCount", 1, dataService.countAnalysesForTestCase(testCase)
		dataService.updateAnalysisCountForTestCase(testCase)
		assertEquals "Wrong analysisCount", 1, testCase.analysisCount

		// Change second analysis State to unanalyzed
		outcomeA.analysisState = dataService.getAnalysisStateByName("Investigate")
		dataService.updateAnalysisCountForTestCase(testCase)
		dataService.saveDomainObject(outcomeA)

		// verify analysisCount is 0
		assertEquals "Wrong analysisCount", 0, dataService.countAnalysesForTestCase(testCase)
		dataService.updateAnalysisCountForTestCase(testCase)
		assertEquals "Wrong analysisCount", 0, testCase.analysisCount

	}
}