package cuanto

import cuanto.test.TestObjects

/**
 * User: Todd Wells
 * Date: Mar 9, 2010
 * Time: 4:03:02 PM
 * 
 */

public class TestOutcomeQueryFilterTests extends GroovyTestCase {

	DataService dataService
	Project project

	TestObjects testObjects = new TestObjects();


	void setUp() {
		project = testObjects.project;
		dataService.saveDomainObject project
		testObjects.dataService = dataService
	}

	void testFilterByTestRun() {
		def testCases = []
		1.upto(3) {
			def tc = testObjects.getTestCase(project)
			dataService.saveDomainObject tc
			testCases << tc
		}

		// create outcomes without TestRun, one for a testcase used by the test run, one that isn't
		[0, 2].each {
			def outcomeWithoutTestRun = testObjects.getTestOutcome(testCases[it], null)
			dataService.saveDomainObject outcomeWithoutTestRun
		}

		// create two outcomes for TestRun A, one for a testcase used by the test run, one that isn't
		def testRunA = testObjects.getTestRun(project)
		dataService.saveDomainObject testRunA

		def outcomesA = []
		0.upto(1) {
			def outcome = testObjects.getTestOutcome(testCases[it], testRunA)
			dataService.saveDomainObject outcome
			outcomesA << outcome
		}

		// create two outcomes for TestRun B
		def testRunB = testObjects.getTestRun(project)
		dataService.saveDomainObject testRunB

		[0,2].each {
			def outcome = testObjects.getTestOutcome(testCases[it], testRunB)
			dataService.saveDomainObject outcome
		}

		// create QueryFilter for TestRun A
		TestOutcomeQueryFilter queryFilterA = new TestOutcomeQueryFilter()
		queryFilterA.testRun = testRunA

		def fetchedOutcomes = dataService.getTestOutcomes(queryFilterA)
		assertEquals "Wrong number of outcomes", outcomesA.size(), fetchedOutcomes.size()
		assertNotNull "Outcome 1 not found", fetchedOutcomes.find { it.id == outcomesA[0].id }
		assertNotNull "Outcome 2 not found", fetchedOutcomes.find { it.id == outcomesA[1].id }
		assertEquals "Outcome 1", outcomesA[0], fetchedOutcomes[0]
		assertEquals "Outcome 2", outcomesA[1], fetchedOutcomes[1]

		queryFilterA.sort = "testCase.fullName"
		fetchedOutcomes = dataService.getTestOutcomes(queryFilterA)
		assertTrue "Wrong sort order", fetchedOutcomes[0].testCase.fullName < fetchedOutcomes[1].testCase.fullName

		queryFilterA.order = "desc"
		fetchedOutcomes = dataService.getTestOutcomes(queryFilterA)
		assertTrue "Wrong sort order", fetchedOutcomes[0].testCase.fullName > fetchedOutcomes[1].testCase.fullName

		

		//assertEquals "Outcome 1", outcomesA[0], fetchedOutcomes[0]
		//assertEquals "Outcome 2", outcomesA[1], fetchedOutcomes[1]

		// verify QueryFilter returns only outcomes for A
		// create QueryFilter for TestRun B
		// verify QueryFilter returns only outcomes for B


	}

}