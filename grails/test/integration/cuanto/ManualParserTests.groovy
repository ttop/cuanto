package cuanto

import cuanto.test.TestObjects

/**
 * User: Todd Wells
 * Date: Oct 16, 2008
 * Time: 5:19:12 PM
 * 
 */
class ManualParserTests extends GroovyTestCase{

	ParsingService parsingService

	InitializationService initializationService
	DataService dataService
	def testRunService

	TestObjects to
	TestType testType


	@Override
	void setUp() {
		initializationService.initializeAll()
		to = new TestObjects()
		to.dataService = dataService
		testType = TestType.findByName("Manual")
		assertNotNull testType
	}


	void testManualTestParsing() {
		Project proj = getProject()
		TestRun run = testRunService.createTestRun([project:proj.toString()])
		def testResultFile = getFile("manual_tests.xml")
		TestRun returnedRun = parsingService.parseFileWithTestRun(testResultFile, run.id)
		assertEquals run, returnedRun

		def outcomes = dataService.getTestOutcomes(new TestOutcomeQueryFilter(testRun: run))
		assertEquals 8, outcomes.size()

		TestResult expectedResult = dataService.result("unexecuted")
		outcomes.each { 
			assertEquals "Wrong result", expectedResult, it.testResult
			assertNotNull "Invalid test case", it.testCase
			assertEquals "Wrong test run", returnedRun, it.testRun
			assertNotNull "Bad description", it.testCase.description
		}

		["Goose Test", "Duck Test", "Pigeon Test"].each { name->
			TestOutcome outcome = outcomes.find { out->
				out.testCase.testName == name
			}
			assertNotNull "${name} not found", outcome
			assertEquals "birds", outcome.testCase.packageName
			assertEquals "Wrong fullname", "birds.${name}", outcome.testCase.fullName
		}

		def cherryStOutcome = outcomes.find { it.testCase.testName == "Cherry Street Test" }
		assertNotNull "Wrong name", cherryStOutcome
		assertEquals "Wrong package", "beverage.coffee", cherryStOutcome.testCase.packageName
		assertEquals "Wrong description", "Why is it on First and Clay?", cherryStOutcome.testCase.description
	}


	void testNewManualTestRun() {
	 /* Submit two manual "runs" for the same project.  The second one updates description if not blank and submits more
	 * test cases. Check the total number of test cases.  Check the changed description. */
		Project proj = getProject()

		TestRun run = testRunService.createTestRun([project:proj.toString()])
		def testResultFile = getFile("manual_tests.xml")
		TestRun returnedRun = parsingService.parseFileWithTestRun(testResultFile, run.id)

		TestRun updatedRun = testRunService.createTestRun([project:proj.toString()])
		def updatedResultFile = getFile("manual_tests_update.xml")
		parsingService.parseFileWithTestRun(updatedResultFile, updatedRun.id,)

		def outcomes = dataService.getTestOutcomes(new TestOutcomeQueryFilter(testRun: updatedRun))
		assertEquals 9, outcomes.size()

		def cherryStOutcome = outcomes.find { it.testCase.testName == "Cherry Street Test" }
		assertNotNull "Wrong name", cherryStOutcome
		assertEquals "Wrong package", "beverage.coffee", cherryStOutcome.testCase.packageName
		assertEquals "Wrong description", "Why is it on First and Clay?", cherryStOutcome.testCase.description

		def gooseOutcome = outcomes.find { it.testCase.testName == "Goose Test" }
		assertNotNull "Wrong name", gooseOutcome
		assertEquals "Wrong package", "birds", gooseOutcome.testCase.packageName
		assertEquals "Wrong description", "Grey Goose", gooseOutcome.testCase.description

		def uptownOutcome = outcomes.find { it.testCase.testName == "Uptown Test" }
		assertNotNull "Wrong name", uptownOutcome
		assertEquals "Wrong package", "", uptownOutcome.testCase.packageName
		assertEquals "Wrong description", "I bet her mommy never told her a lie", uptownOutcome.testCase.description

		def tullysOutcome = outcomes.find { it.testCase.testName == "Tully's Test" }
		assertNotNull "Wrong name", tullysOutcome
		assertEquals "Wrong package", "beverage.coffee", tullysOutcome.testCase.packageName
		assertEquals "Wrong description", "Alliteration is my friend", tullysOutcome.testCase.description

		def lighthouseOutcome = outcomes.find { it.testCase.testName == "Lighthouse Test" }
		assertNotNull "Wrong name", lighthouseOutcome
		assertEquals "Wrong package", "beverage.coffee", lighthouseOutcome.testCase.packageName
		assertEquals "Wrong description", "The tastiest roast around", lighthouseOutcome.testCase.description
		

		def newPackageOutcome = outcomes.find { it.testCase.testName == "New Package Test" }
		assertNotNull "Wrong name", newPackageOutcome
		assertEquals "Wrong package", "new.package", newPackageOutcome.testCase.packageName
		assertEquals "Wrong description", "Hope this worked", newPackageOutcome.testCase.description

		assertEquals "Wrong number of test cases", 10, TestCase.list().size()
	}


	File getFile(fileName) {
		File testFile = new File("test/resources/${fileName}")
		assertTrue "Couldn't find ${fileName} at ${testFile.absolutePath}", testFile.exists()
		return testFile
	}

	Project getProject() {
		Project proj = to.getProject()
		proj.testType = testType
		dataService.saveDomainObject(proj)
		return proj
	}
}