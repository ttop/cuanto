import cuanto.parsers.TestNgParser
import cuanto.parsers.ParsableTestOutcome
import cuanto.parsers.ParsableTestCase

class TestNgParserTests extends GroovyTestCase {

	void testBasicTestNgParsing() {
		def parser = new TestNgParser()
		List<ParsableTestOutcome> outcomes = parser.parseFile(getFile("testng_output.xml"))

		assertEquals "Wrong number of test outcomes", 2, outcomes.size()

		assertEquals "Wrong result", "Fail", outcomes[0].testResult
		assertEquals "Wrong fullname", "com.test.TestOne.test1", outcomes[0].testCase.fullName
		assertEquals "Wrong name", "test1", outcomes[0].testCase.testName
		assertEquals "Wrong duration", 23, outcomes[0].duration
		assertEquals "Wrong description", "someDescription2", outcomes[0].testCase.description
		assertEquals "Wrong startedAt", "2007-05-28T12:14:37Z", parser.dateFormatter.format(outcomes[0].startedAt)
		assertEquals "Wrong finishedAt", "2007-05-28T12:14:37Z", parser.dateFormatter.format(outcomes[0].finishedAt)

		assertEquals "Wrong result", "Pass", outcomes[1].testResult
		assertEquals "Wrong fullname", "com.test.TestOne.test2", outcomes[1].testCase.fullName
		assertEquals "Wrong name", "test2", outcomes[1].testCase.testName
		assertEquals "Wrong duration", 0, outcomes[1].duration
		assertEquals "Wrong description", "someDescription1", outcomes[1].testCase.description
		assertEquals "Wrong filetype", "TestNG", parser.testType
		assertEquals "Wrong startedAt", "2007-05-28T12:14:37Z", parser.dateFormatter.format(outcomes[0].startedAt)
		assertEquals "Wrong finishedAt", "2007-05-28T12:14:37Z", parser.dateFormatter.format(outcomes[0].finishedAt)
	}


	void testSimpleTestNgParsing() {
		def parser = new TestNgParser()
		List<ParsableTestOutcome> outcomes = parser.parseFile(getFile("testng-results-simple.xml"))
		
		ParsableTestOutcome pto1 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"testMethod3",
			fullName: "cuanto.test.testNgOne.testMethod3", packageName:"cuanto.test.testNgOne",
			description: "testMethod3 Description"), testResult: "Fail", duration:2)
		assertTestOutcomeEquals(pto1, outcomes[0])
		assertTrue("Wrong output", outcomes[0].testOutput.contains("Is this the right room for an argument?"))

		ParsableTestOutcome pto2 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"testMethod2",
			fullName: "cuanto.test.testNgOne.testMethod2", packageName:"cuanto.test.testNgOne"),
			testResult: "Fail", duration:6)
		assertTestOutcomeEquals(pto2, outcomes[1])
		assertTrue "Wrong output", outcomes[1].testOutput.contains("method 2 failed")

		ParsableTestOutcome pto3 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"skippableTest",
			fullName: "cuanto.test.testNgOne.skippableTest", packageName:"cuanto.test.testNgOne"),
			testResult: "Skip", duration:1)
		assertTestOutcomeEquals(pto3, outcomes[2])

		ParsableTestOutcome pto4 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"dependencySample",
			fullName: "cuanto.test.testNgOne.dependencySample", packageName:"cuanto.test.testNgOne"),
			testResult: "Fail", duration:5)
		assertTestOutcomeEquals(pto4, outcomes[3])

		ParsableTestOutcome pto5 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"testMethod1",
			fullName: "cuanto.test.testNgOne.testMethod1", packageName:"cuanto.test.testNgOne"),
			testResult: "Pass", duration:0)
		assertTestOutcomeEquals(pto5, outcomes[4])

		ParsableTestOutcome pto6 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"testMethod4",
			fullName: "cuanto.test.testNgOne.testMethod4", packageName:"cuanto.test.testNgOne"),
			testResult: "Pass", duration:0)
		assertTestOutcomeEquals(pto6, outcomes[5])
	}


	void testTestNgParsingWithParams() {
		def parser = new TestNgParser()
		List<ParsableTestOutcome> outcomes = parser.parseFile(getFile("testng-results-params.xml"))

		ParsableTestOutcome pto1 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"testMethod1",
			fullName: "cuanto.test.TestNgOne.testMethod1", packageName:"cuanto.test.TestNgOne"),
			testResult: "Pass", duration:3)
		assertTestOutcomeEquals(pto1, outcomes[0] as ParsableTestOutcome)

		ParsableTestOutcome pto2 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"passingParameterTestOne",
			fullName: "cuanto.test.TestNgOne.passingParameterTestOne", packageName:"cuanto.test.TestNgOne", parameters: "one"),
			testResult: "Pass", duration:0)
		assertTestOutcomeEquals(pto2, outcomes[1] as ParsableTestOutcome)

		ParsableTestOutcome pto3 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"dependencySample",
			fullName: "cuanto.test.TestNgOne.dependencySample", packageName:"cuanto.test.TestNgOne"),
			testResult: "Fail", duration:102)
		assertTestOutcomeEquals(pto3, outcomes[2] as ParsableTestOutcome)

		ParsableTestOutcome pto4 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"skippableTest",
			fullName: "cuanto.test.TestNgOne.skippableTest", packageName:"cuanto.test.TestNgOne"),
			testResult: "Skip", duration:2)
		assertTestOutcomeEquals(pto4, outcomes[3] as ParsableTestOutcome)

		ParsableTestOutcome pto5 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"failingParameterTestOne",
			fullName: "cuanto.test.TestNgOne.failingParameterTestOne", packageName:"cuanto.test.TestNgOne", parameters: "one"),
			testResult: "Fail", duration:0)
		assertTestOutcomeEquals(pto5, outcomes[4] as ParsableTestOutcome)

		ParsableTestOutcome pto6 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"testMethod2",
			fullName: "cuanto.test.TestNgOne.testMethod2", packageName:"cuanto.test.TestNgOne"),
			testResult: "Fail", duration:34)
		assertTestOutcomeEquals(pto6, outcomes[5] as ParsableTestOutcome)

		ParsableTestOutcome pto7 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"passingParameterTestTwo",
			fullName: "cuanto.test.TestNgOne.passingParameterTestTwo", packageName:"cuanto.test.TestNgOne",
			parameters: "one, two, three"),	testResult: "Pass", duration:0)
		assertTestOutcomeEquals(pto7, outcomes[6] as ParsableTestOutcome)

		ParsableTestOutcome pto8 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"testMethod3",
			fullName: "cuanto.test.TestNgOne.testMethod3", packageName:"cuanto.test.TestNgOne",
			description: "testMethod3 Description"), testResult: "Fail", duration:4)
		assertTestOutcomeEquals(pto8, outcomes[7] as ParsableTestOutcome)

		ParsableTestOutcome pto9 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"parametersNonExistent",
			fullName: "cuanto.test.TestNgOne.parametersNonExistent", packageName:"cuanto.test.TestNgOne"),
			testResult: "Fail", duration:0)
		assertTestOutcomeEquals(pto9, outcomes[8] as ParsableTestOutcome)

		ParsableTestOutcome pto10 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"failingParameterTestTwo",
			fullName: "cuanto.test.TestNgOne.failingParameterTestTwo", packageName:"cuanto.test.TestNgOne",
			parameters: "one, two, three"), testResult: "Fail", duration:0)
		assertTestOutcomeEquals(pto10, outcomes[9] as ParsableTestOutcome)

		ParsableTestOutcome pto11 = new ParsableTestOutcome(testCase: new ParsableTestCase(testName:"testMethod4",
			fullName: "cuanto.test.TestNgOne.testMethod4", packageName:"cuanto.test.TestNgOne"),
			testResult: "Pass", duration:0)
		assertTestOutcomeEquals(pto11, outcomes[10] as ParsableTestOutcome)
	}


    void testParsingGroupsFromTopOfXmlFile() {
        def parser = new TestNgParser()
        List<ParsableTestOutcome> outcomes = parser.parseFile(getFile("testng-results-groups-top.xml"))
        assertEquals "Wrong number of outcomes", 4, outcomes.size()

        assertEquals "Wrong number of tags", 2, outcomes[0].tags?.size()
        assertEquals "places", outcomes[0].tags[0]
        assertEquals "quirks", outcomes[0].tags[1]

        assertEquals "Wrong number of tags", 1, outcomes[1].tags?.size()
        assertEquals "quirks", outcomes[1].tags[0]

        assertEquals "Wrong number of tags", 1, outcomes[2].tags?.size()
        assertEquals "quirks", outcomes[2].tags[0]

        assertEquals "Wrong number of tags", 1, outcomes[3].tags?.size()
        assertEquals "places", outcomes[3].tags[0]
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
		assertEquals "Wrong note", a.note, b.note
		assertEquals "Wrong owner", a.owner, b.owner
		assertEquals "Wrong result", a.testResult, b.testResult
		assertEquals "Wrong duration", a.duration, b.duration
		assertTrue "Test cases are unequal", a.testCase.equals(b.testCase)
	}
}