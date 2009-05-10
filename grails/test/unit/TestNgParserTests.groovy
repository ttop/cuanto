import cuanto.ParsableTestCase
import cuanto.ParsableTestOutcome
import cuanto.parsers.TestNgParser

/**
 * User: Todd Wells
 * Date: Oct 24, 2008
 * Time: 2:03:45 PM
 * 
 */
class TestNgParserTests extends GroovyTestCase {

	void testBasicTestNgParsing() {
		def parser = new TestNgParser()
		List<ParsableTestOutcome> outcomes = parser.parseFile(getFile("testng_output.xml"))

		assertEquals "Wrong number of test outcomes", 3, outcomes.size()
		assertEquals "Wrong result", "Fail", outcomes[0].testResult
		assertEquals "Wrong result", "Pass", outcomes[1].testResult
		assertEquals "Wrong result", "Pass", outcomes[2].testResult

		assertEquals "Wrong fullname", "com.test.TestOne.test1", outcomes[0].testCase.fullName
		assertEquals "Wrong fullname", "com.test.TestOne.test2", outcomes[1].testCase.fullName
		assertEquals "Wrong fullname", "com.test.TestOne.setUp", outcomes[2].testCase.fullName

		assertEquals "Wrong name", "test1", outcomes[0].testCase.testName
		assertEquals "Wrong name", "test2", outcomes[1].testCase.testName
		assertEquals "Wrong name", "setUp", outcomes[2].testCase.testName

		assertEquals "Wrong duration", 23, outcomes[0].duration
		assertEquals "Wrong duration", 0, outcomes[1].duration
		assertEquals "Wrong duration", 15, outcomes[2].duration

		assertEquals "Wrong description", "someDescription2", outcomes[0].testCase.description
		assertEquals "Wrong description", "someDescription1", outcomes[1].testCase.description
		assertNull "Wrong description", outcomes[2].testCase.description

		assertEquals "Wrong filetype", "TestNG", parser.testType
	}


	void testSimpleTestNgParsing() {
		def parser = new TestNgParser()
		List<ParsableTestOutcome> outcomes = parser.parseFile(getFile("testng-results-simple.xml"))
		
		ParsableTestOutcome pto1 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"testMethod3",
			fullName: "cuanto.test.testNgOne.testMethod3", testPackage:"cuanto.test.testNgOne",
			description: "testMethod3 Description"), testResult: "Fail", duration:2)
		assertTestOutcomeEquals(pto1, outcomes[0])
		assertTrue("Wrong output", outcomes[0].testOutput.contains("Is this the right room for an argument?"))

		ParsableTestOutcome pto2 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"testMethod2",
			fullName: "cuanto.test.testNgOne.testMethod2", testPackage:"cuanto.test.testNgOne"),
			testResult: "Fail", duration:6)
		assertTestOutcomeEquals(pto2, outcomes[1])
		assertTrue "Wrong output", outcomes[1].testOutput.contains("method 2 failed")

		ParsableTestOutcome pto3 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"skippableTest",
			fullName: "cuanto.test.testNgOne.skippableTest", testPackage:"cuanto.test.testNgOne"),
			testResult: "Skip", duration:1)
		assertTestOutcomeEquals(pto3, outcomes[2])

		ParsableTestOutcome pto4 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"dependencySample",
			fullName: "cuanto.test.testNgOne.dependencySample", testPackage:"cuanto.test.testNgOne"),
			testResult: "Fail", duration:5)
		assertTestOutcomeEquals(pto4, outcomes[3])

		ParsableTestOutcome pto5 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"testMethod1",
			fullName: "cuanto.test.testNgOne.testMethod1", testPackage:"cuanto.test.testNgOne"),
			testResult: "Pass", duration:0)
		assertTestOutcomeEquals(pto5, outcomes[4])

		ParsableTestOutcome pto6 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"testMethod4",
			fullName: "cuanto.test.testNgOne.testMethod4", testPackage:"cuanto.test.testNgOne"),
			testResult: "Pass", duration:0)
		assertTestOutcomeEquals(pto6, outcomes[5])
	}


	File getFile(fileName) {
		File testFile = new File("test/resources/${fileName}")
		if (!testFile.exists()) {
			testFile = new File("grails/test/resources/${fileName}")
		}
		assertTrue "Couldn't find ${fileName} at ${testFile.absolutePath}", testFile.exists()
		return testFile
	}

	void assertTestOutcomeEquals(ParsableTestOutcome a, ParsableTestOutcome b) {
		assertEquals "Wrong bug", a.bug, b.bug
		assertEquals "Wrong note", a.note, b.note
		assertEquals "Wrong owner", a.owner, b.owner
		assertEquals "Wrong result", a.testResult, b.testResult
		assertEquals "Wrong duration", a.duration, b.duration
		assertEquals "Wrong test case", a.testCase, b.testCase
	}
}