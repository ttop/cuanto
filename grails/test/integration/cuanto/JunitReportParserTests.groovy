package cuanto

import cuanto.test.TestObjects
import cuanto.parsers.JunitReportParser

/**
 * User: Todd Wells
 * Date: Sep 24, 2008
 * Time: 6:18:36 PM
 * 
 */
class JunitReportParserTests extends GroovyTestCase{

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
		testType = TestType.findByName("JUnit")
	}


	void testJUnitReportOfJUnit38SingleSuiteFile() {
		Project proj = getProject()
		proj.bugUrlPattern = "http://tpjira/browse/{BUG}"
		TestRun run = testRunService.createTestRun([project:proj.toString()])
		def testResultFile = getFile("junitReport_single_suite.xml")
		TestRun returnedRun = parsingService.parseFileWithTestRun(testResultFile, run.id)
		assertEquals run, returnedRun

		// should be 34 tests
		def outcomes = dataService.getTestOutcomes(new TestOutcomeQueryFilter(testRun: run))
		assertEquals 34, outcomes.size()

		TestOutcome leCarre = outcomes.find {
			it.testCase.testName == "LeCarre"
		}
		assertNotNull leCarre
		assertEquals "Wrong result", dataService.result("fail"), leCarre.testResult
		assertEquals "Wrong analysis state", dataService.getAnalysisStateByName("Unanalyzed"), leCarre.analysisState
		assertNotNull leCarre.bug
		assertEquals "http://tpjira/browse/John", leCarre.bug.url
		assertEquals "John", leCarre.bug.title



		TestOutcome bellow = outcomes.find {
			it.testCase.testName == "Bellow"
		}
		assertNotNull bellow
		assertEquals "Wrong result", dataService.result("error"), bellow.testResult
		assertEquals "Wrong analysis state", dataService.getAnalysisStateByName("Unanalyzed"), bellow.analysisState
		assertEquals "http://tpjira/browse/SAUL", bellow.bug.url
		assertEquals "SAUL", bellow.bug.title

		TestOutcome hesse = outcomes.find {
			it.testCase.testName == "Hesse"
		}
		assertNotNull hesse
		assertEquals "Wrong result", dataService.result("fail"), hesse.testResult
		assertEquals "Wrong analysis state", dataService.getAnalysisStateByName("Unanalyzed"), hesse.analysisState

		outcomes.each {
			 if (!isSingleSuiteFailure(it)) {
				 assertEquals "Wrong result", dataService.result("pass"), it.testResult
				 assertNull "No analysis state should've been set", it.analysisState
			 }
		}
	}

	void testJUnitReportOfJUnit38SingleSuiteStream() {
		Project proj = getProject()
		TestRun run = testRunService.createTestRun([project:proj.toString()])
		def testResultFile = getFile("junitReport_single_suite.xml")
		TestRun returnedRun = parsingService.parseFileFromStream(testResultFile.newInputStream(), run.id)
		assertEquals run, returnedRun

		// should be 34 tests
		def outcomes = dataService.getTestOutcomes(new TestOutcomeQueryFilter(testRun: run))
		assertEquals 34, outcomes.size()

		TestOutcome leCarre = outcomes.find {
			it.testCase.testName == "LeCarre"
		}
		assertNotNull leCarre
		assertEquals "Wrong result", dataService.result("fail"), leCarre.testResult
		assertEquals "Wrong analysis state", dataService.getAnalysisStateByName("Unanalyzed"), leCarre.analysisState


		TestOutcome bellow = outcomes.find {
			it.testCase.testName == "Bellow"
		}
		assertNotNull bellow
		assertEquals "Wrong result", dataService.result("error"), bellow.testResult
		assertEquals "Wrong analysis state", dataService.getAnalysisStateByName("Unanalyzed"), bellow.analysisState

		TestOutcome hesse = outcomes.find {
			it.testCase.testName == "Hesse"
		}
		assertNotNull hesse
		assertEquals "Wrong result", dataService.result("fail"), hesse.testResult
		assertEquals "Wrong analysis state", dataService.getAnalysisStateByName("Unanalyzed"), hesse.analysisState

		outcomes.each {
			 if (!isSingleSuiteFailure(it)) {
				 assertEquals "Wrong result", dataService.result("pass"), it.testResult
				 assertNull "No analysis state should've been set", it.analysisState
			 }
			assertEquals "Wrong parameters", null, it.testCase.parameters
		}
	}


	def isSingleSuiteFailure(outcome) {
		for (name in ["LeCarre", "Bellow", "Hesse"]){
			if (outcome.testCase.testName == name) {
				return true
			}
		}
		return false
	}


	void testJUnitReportOfJUnit38MultipleSuiteFile() {
		/* This style is actually created by running an Ant JUnitReport task on a directory of tests */
		Project proj = getProject()
		TestRun run = testRunService.createTestRun([project:proj.toString()])
		def testResultFile = getFile("junitReport_multiple_suite.xml")
		TestRun returnedRun = parsingService.parseFileWithTestRun(testResultFile, run.id)
		assertEquals run, returnedRun

		// should be 56 tests
		def outcomes = dataService.getTestOutcomes(new TestOutcomeQueryFilter(testRun: run))
		assertEquals 56, outcomes.size()

		def expectedFailures = ["Melville", "Williams", "Ferry", "Morrisson", "LeCarre", "Azimov", "Brownies", "Tart",
			"Cappuccino", "Breva"]
		def expectedErrors = ["Wilson", "Dunne", "Cookies", "Cake", "Macchiatto"]

		expectedFailures.each { testName ->
			TestOutcome failedTest = outcomes.find {
				it.testCase.testName == testName
			}
			assertNotNull failedTest
			assertEquals "Wrong result for ${testName}", dataService.result("fail"), failedTest.testResult
			assertEquals "Wrong analysis state for ${testName}", dataService.getAnalysisStateByName("Unanalyzed"), failedTest.analysisState
			assertEquals "Wrong parameters", null, failedTest.testCase.parameters
		}

		expectedErrors.each { testName ->
			TestOutcome errorTest = outcomes.find {
				it.testCase.testName == testName
			}
			assertNotNull errorTest
			assertEquals "Wrong result", dataService.result("error"), errorTest.testResult
			assertEquals "Wrong analysis state", dataService.getAnalysisStateByName("Unanalyzed"), errorTest.analysisState
			assertEquals "Wrong parameters", null, errorTest.testCase.parameters
		}


		outcomes.each {
			 if (!expectedErrors.contains(it.testCase.testName) && !expectedFailures.contains(it.testCase.testName)) {
				 assertEquals "Wrong result", dataService.result("pass"), it.testResult
				 assertNull "No analysis state should've been set", it.analysisState
			 }
		}
	}

	void testJUnitReportOfJUnit38MultipleSuiteStream() {
		/* This style is actually created by running an Ant JUnitReport task on a directory of tests */
		Project proj = getProject()
		TestRun run = testRunService.createTestRun([project:proj.toString()])
		def testResultFile = getFile("junitReport_multiple_suite.xml")
		TestRun returnedRun = parsingService.parseFileFromStream(testResultFile.newInputStream(), run.id)
		assertEquals run, returnedRun

		// should be 56 tests
		def outcomes = dataService.getTestOutcomes(new TestOutcomeQueryFilter(testRun: run))
		assertEquals 56, outcomes.size()

		def expectedFailures = ["Melville", "Williams", "Ferry", "Morrisson", "LeCarre", "Azimov", "Brownies", "Tart",
			"Cappuccino", "Breva"]
		def expectedErrors = ["Wilson", "Dunne", "Cookies", "Cake", "Macchiatto"]

		expectedFailures.each { testName ->
			TestOutcome failedTest = outcomes.find {
				it.testCase.testName == testName
			}
			assertNotNull failedTest
			assertEquals "Wrong result for ${testName}", dataService.result("fail"), failedTest.testResult
			assertEquals "Wrong analysis state for ${testName}", dataService.getAnalysisStateByName("Unanalyzed"), failedTest.analysisState
		}

		expectedErrors.each { testName ->
			TestOutcome errorTest = outcomes.find {
				it.testCase.testName == testName
			}
			assertNotNull errorTest
			assertEquals "Wrong result", dataService.result("error"), errorTest.testResult
			assertEquals "Wrong analysis state", dataService.getAnalysisStateByName("Unanalyzed"), errorTest.analysisState
		}


		outcomes.each {
			 if (!expectedErrors.contains(it.testCase.testName) && !expectedFailures.contains(it.testCase.testName)) {
				 assertEquals "Wrong result", dataService.result("pass"), it.testResult
				 assertNull "No analysis state should've been set", it.analysisState
			 }
		}
	}


	void testJUnitReportOfJUnit44ParameterizedTest() {
		Project proj = getProject()
		proj.bugUrlPattern = "http://tpjira/browse/{BUG}"
		TestRun run = testRunService.createTestRun([project:proj.toString()])
		def testResultFile = getFile("junitReport_ParameterizedSingleTest.xml")
		TestRun returnedRun = parsingService.parseFileWithTestRun(testResultFile, run.id)
		assertEquals run, returnedRun

		// should be 4 tests
		def outcomes = dataService.getTestOutcomes(new TestOutcomeQueryFilter(testRun: run))
		assertEquals 4, outcomes.size()

		def passingTests = outcomes.findAll {
			it.testCase.testName == "passTest"
		}

		assertEquals "Wrong number of tests named passTest", 2, passingTests.size()

		passingTests.eachWithIndex { it, indx ->
			assertEquals "Wrong result for test", dataService.result("pass"), it.testResult
			assertNull "Wrong analysis state for test $indx - should be null", it.analysisState
			assertNotNull "Parameters should not be null", it.testCase.parameters
			assertTrue "Parameters should not be blank", it.testCase.parameters != ""
		}

		def failTests = outcomes.findAll {
			it.testCase.testName == "failTest"
		}
		assertEquals "Wrong number of tests named failTest", 2, failTests.size()

		def failTest = outcomes.findAll {
			it.testCase.testName == "failTest" && it.testCase.parameters == "[1]"
		}
		assertEquals "Wrong number of failed tests", 1, failTest.size()
		assertEquals "Wrong analysis state", dataService.result("error"), failTest[0].testResult
		assertEquals "Wrong analysis state", dataService.getAnalysisStateByName("Unanalyzed"), failTest[0].analysisState
		assertEquals "Wrong parameters", "[1]", failTest[0].testCase.parameters
	}


	void testJUnitReportOfJUnit44ParameterizedSuite() {
		Project proj = getProject()
		proj.bugUrlPattern = "http://tpjira/browse/{BUG}"
		TestRun run = testRunService.createTestRun([project:proj.toString()])
		def testResultFile = getFile("junitReport_ParameterizedTestSuite.xml")
		TestRun returnedRun = parsingService.parseFileWithTestRun(testResultFile, run.id)
		assertEquals run, returnedRun

		// should be 13 tests
		def outcomes = dataService.getTestOutcomes(new TestOutcomeQueryFilter(testRun: run))
		assertEquals 13, outcomes.size()

		def passingTests = outcomes.findAll {
			it.testCase.testName == "passTest"
		}

		assertEquals "Wrong number of tests named passTest", 2, passingTests.size()

		passingTests.eachWithIndex { it, indx ->
			assertEquals "Wrong result for test", dataService.result("pass"), it.testResult
			assertNull "Wrong analysis state for test $indx - should be null", it.analysisState
			assertNotNull "Parameters should not be null", it.testCase.parameters
			assertTrue "Parameters should not be blank", it.testCase.parameters != ""
		}

		def failTests = outcomes.findAll {
			it.testCase.testName == "failTest"
		}
		assertEquals "Wrong number of tests named failTest", 2, failTests.size()

		def failTest = outcomes.findAll {
			it.testCase.testName == "failTest" && it.testCase.parameters == "[1]"
		}
		assertEquals "Wrong number of failed tests", 1, failTest.size()
		assertEquals "Wrong analysis state", dataService.result("error"), failTest[0].testResult
		assertEquals "Wrong analysis state", dataService.getAnalysisStateByName("Unanalyzed"), failTest[0].analysisState
		assertEquals "Wrong parameters", "[1]", failTest[0].testCase.parameters
	}


	File getFile(fileName) {
		File testFile = new File("test/resources/${fileName}")
		assertTrue "Couldn't find ${fileName} at ${testFile.absolutePath}", testFile.exists()
		return testFile
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