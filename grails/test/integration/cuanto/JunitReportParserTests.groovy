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
		TestRun returnedRun = parsingService.parseFile(testResultFile, run.id)
		assertEquals run, returnedRun

		// should be 34 tests
		def outcomes = testRunService.getOutcomesForTestRun(run, null)
		assertEquals 34, outcomes.size()

		TestOutcome leCarre = outcomes.find {
			it.testCase.testName == "LeCarre"
		}
		assertNotNull leCarre
		assertEquals "Wrong result", dataService.result("fail"), leCarre.testResult
		assertEquals "Wrong analysis state", dataService.getAnalysisStateByName("Bug"), leCarre.analysisState
		assertNotNull leCarre.bug
		assertEquals "http://tpjira/browse/John", leCarre.bug.url
		assertEquals "John", leCarre.bug.title



		TestOutcome bellow = outcomes.find {
			it.testCase.testName == "Bellow"
		}
		assertNotNull bellow
		assertEquals "Wrong result", dataService.result("error"), bellow.testResult
		assertEquals "Wrong analysis state", dataService.getAnalysisStateByName("Bug"), bellow.analysisState
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
		def outcomes = testRunService.getOutcomesForTestRun(run, null)
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
		TestRun returnedRun = parsingService.parseFile(testResultFile, run.id)
		assertEquals run, returnedRun

		// should be 56 tests
		def outcomes = testRunService.getOutcomesForTestRun(run, null)
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

	void testJUnitReportOfJUnit38MultipleSuiteStream() {
		/* This style is actually created by running an Ant JUnitReport task on a directory of tests */
		Project proj = getProject()
		TestRun run = testRunService.createTestRun([project:proj.toString()])
		def testResultFile = getFile("junitReport_multiple_suite.xml")
		TestRun returnedRun = parsingService.parseFileFromStream(testResultFile.newInputStream(), run.id)
		assertEquals run, returnedRun

		// should be 56 tests
		def outcomes = testRunService.getOutcomesForTestRun(run, null)
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