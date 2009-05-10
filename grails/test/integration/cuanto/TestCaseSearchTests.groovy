package cuanto

import cuanto.test.TestObjects
import cuanto.test.WordGenerator
import cuanto.*

/**
 * User: Todd Wells
 * Date: Sep 20, 2008
 * Time: 9:10:19 AM
 * 
 */
class TestCaseSearchTests extends GroovyTestCase {
	TestOutcomeService testOutcomeService
	TestRunService testRunService
	InitializationService initializationService
	DataService dataService

	TestObjects to
	WordGenerator wordGen
	TestType testType


	@Override
	void setUp() {
		initializationService.initializeAll()
		to = new TestObjects()
		to.dataService = dataService
		wordGen = new WordGenerator()
		testType = TestType.findByName("JUnit")
	}


	void testSearchByTestCaseName() {

		Project project = getProject()
		project.testType = testType
		TestRun run = to.getTestRun(project, wordGen.getWord())
		saveDomainObject(run)

		def names = ["alpha", "bravo", "charlie", "delta", "echo", "foxtrot", "golf", "hotel", "Motel"]

		def outcomes = []
		names.each { name ->
			TestCase tc = to.getTestCase(project)
			tc.testName = name
			tc.fullName = tc.packageName + "." + tc.testName
			saveDomainObject(project.addToTestCases(tc))
			TestOutcome outcome = to.getTestOutcome(tc, run)
			saveDomainObject(run.addToOutcomes(outcome))
			outcomes += outcome
		}

		List results = testOutcomeService.findTestCaseByName("otel", project.id)
		assertEquals "Wrong number of results returned", 2, results.size()
		results = testOutcomeService.findTestCaseByName("bravo", project.id)
		assertEquals "Wrong number of results returned", 1, results.size()
		results = testOutcomeService.findTestCaseByName("ALPH", project.id)
		assertEquals "Wrong number of results returned", 1, results.size()
	}


	Project getProject() {
		Project proj = to.getProject()
		saveDomainObject(proj)
		return proj
	}


	def reportError(domainObj) {
		def errMsg = ""
		domainObj.errors.allErrors.each {
			errMsg += it.toString()
		}
		log.warning errMsg
		fail(errMsg)
	}

	def saveDomainObject(domainObj) {
		if (!domainObj.save(flush:true)) {
			reportError domainObj
		}
	}
}