package cuanto

import cuanto.Project
import cuanto.TestCase
import cuanto.TestRun
import cuanto.TestType
import cuanto.test.TestObjects
import cuanto.ParsingException

class ParsingServiceTests extends GroovyTestCase {
	ParsingService parsingService
	DataService dataService
	def initializationService
	def testOutcomeService
	def testRunService
	def testResultService

	TestObjects fakes = new TestObjects()


	@Override
	void setUp() {
		initializationService.initializeAll()
		fakes.dataService = dataService
	}


	void testFindCases() {
		Project project = new Project(name: "ParsingServiceTestProject", projectKey: fakes.getProjectKey())
		project.testType = TestType.findByName("JUnit")
		dataService.saveDomainObject(project)

		TestCase tCase = new TestCase()
		tCase.packageName = "foo"
		tCase.testName = "Bar"
		tCase.fullName = tCase.packageName + "." + tCase.testName

		tCase.project = project
		dataService.saveDomainObject tCase
		
		TestCase t2 = new TestCase()
		t2.packageName = "foo"
		t2.testName = "Bar"
		t2.fullName = t2.packageName + "." + t2.testName
		t2.project = project

		TestCase t3 = new TestCase()
		t3.packageName = "foo"
		t3.testName = "Bar"
		t3.fullName = t3.packageName + "." + t3.testName
		t3.parameters = fakes.wordGen.getSentence(3)
		t3.project = project
		dataService.saveDomainObject t3 

		TestCase myCase = dataService.findMatchingTestCaseForProject(project, tCase)
		assertEquals("wrong fullName", tCase.fullName, myCase.fullName)
		myCase = dataService.findMatchingTestCaseForProject(project, t2)
		assertEquals("wrong fullName", t2.fullName, myCase.fullName)

		myCase = dataService.findMatchingTestCaseForProject(project, t3)
		assertEquals("wrong fullName", t3.fullName, myCase.fullName)
		assertEquals("wrong parameters", t3.parameters, myCase.parameters)
	}

	
	void testParsingRegistryInjection() {
		assertTrue "No parsers found", parsingService.testParserRegistry?.parsers?.size() > 0
	}	

	void testParseFileFromStream()
	{
		Project proj = fakes.getProject()
		proj.testType = TestType.findByName("JUnit")
		proj.save()

		TestRun testRun = fakes.getTestRun(proj)

		dataService.saveDomainObject(testRun)

		getFile("junitReport_single_suite.xml").withInputStream {
			TestRun tr = parsingService.parseFileFromStream(it, testRun.id)
			println tr
		}
	}


	void testGetParser() {
		shouldFail(ParsingException) {
			parsingService.getParser(new TestType(name: "foobar"))
		}
	}

	private File getFile(filename) {
		File file = new File("test/resources/${filename}")
		assertTrue("Couldn't find file: ${file.toString()}", file.exists())
		return file
	}


    void testParsingTags() {
        Project proj = fakes.getProject()
        proj.testType = TestType.findByName("TestNG")
        dataService.saveDomainObject proj

        TestRun testRun = fakes.getTestRun(proj)
        dataService.saveDomainObject testRun

        parsingService.parseFileWithTestRun(getFile("testng-results-groups-top.xml"), testRun.id)
        assertEquals "Wrong number of total tags", 2, Tag.count()

        def results = TestOutcome.executeQuery("from TestOutcome to where to.testCase.fullName like 'cuanto.sample.TwinPeaks.tibetanMethod'")
        assertEquals 1, results.size()
        TestOutcome outcome = results[0]
        assertEquals "Wrong number of tags", 1, outcome.tags.size()
        assertNotNull "Unable to find tag", outcome.tags.find {it.name == "quirks"}

        results = TestOutcome.executeQuery("from TestOutcome to where to.testCase.fullName like 'cuanto.sample.TwinPeaks.giantVisions'")
        assertEquals 1, results.size()
        outcome = results[0]
        assertEquals "Wrong number of tags", 1, outcome.tags.size()
        assertNotNull "Unable to find tag", outcome.tags.find {it.name == "quirks"}

        results = TestOutcome.executeQuery("from TestOutcome to where to.testCase.fullName like 'cuanto.sample.TwinPeaks.greatNorthern'")
        assertEquals 1, results.size()
        outcome = results[0]
        assertEquals "Wrong number of tags", 1, outcome.tags.size()
        assertNotNull "Unable to find tag", outcome.tags.find {it.name == "places"}

        results = TestOutcome.executeQuery("from TestOutcome to where to.testCase.fullName like 'cuanto.sample.TwinPeaks.blackLodge'")
        assertEquals 1, results.size()
        outcome = results[0]
        assertEquals "Wrong number of tags", 2, outcome.tags.size()
        assertNotNull "Unable to find tag", outcome.tags.find {it.name == "places"}
        assertNotNull "Unable to find tag", outcome.tags.find {it.name == "quirks"}

        assertEquals "Wrong number of tags on TestRun", 2, testRun.tags?.size()
        assertNotNull "Unable to find tag", testRun.tags.find {it.name == "places"}
        assertNotNull "Unable to find tag", testRun.tags.find {it.name == "quirks"}

        parsingService.parseFileWithTestRun(getFile("testng-results-groups-top.xml"), testRun.id)
        assertEquals "Wrong number of tags on TestRun", 2, testRun.tags?.size()
        assertNotNull "Unable to find tag", testRun.tags.find {it.name == "places"}
        assertNotNull "Unable to find tag", testRun.tags.find {it.name == "quirks"}
        assertEquals "Wrong number of total tags", 2, Tag.count()

        TestRun testRunTwo = fakes.getTestRun(proj)
        dataService.saveDomainObject testRunTwo

        parsingService.parseFileWithTestRun(getFile("testng-results-groups-top.xml"), testRunTwo.id)
        assertEquals "Wrong number of total tags", 2, Tag.count()
    }


    void testOutputSummary() {
        Project proj = fakes.getProject()
        proj.testType = TestType.findByName("NUnit")
        dataService.saveDomainObject proj

        TestRun testRun = fakes.getTestRun(proj)
        dataService.saveDomainObject testRun
        parsingService.parseFileWithTestRun(getFile("NUnit-TestResultNet.xml"), testRun.id)

        def outcomes = TestOutcome.executeQuery("from TestOutcome to where to.testResult.isFailure = true")
        assertEquals "Wrong number of failures", 46, outcomes.size()
        outcomes.each {
            assertNotNull "testOutputSummary is null", it.testOutputSummary
            assertTrue "testOutputSummary is blank", it.testOutputSummary?.size() > 0
        }

        TestOutcome targetOutcome = outcomes.find {
            it.testCase.fullName == "NETTests.Tests.Attachmate.Reflection.Emulation.IbmHosts.HostFieldTests.ForegroundColor_DeepBlue"
        }
        assertNotNull "didn't find target outcome", targetOutcome
        assertEquals "Wrong testOutputSummary", "EV313322 is broken in this build. (Build 344 of R2008Trunk.)", targetOutcome.testOutputSummary
    }
}