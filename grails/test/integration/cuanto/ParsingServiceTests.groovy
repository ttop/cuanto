package cuanto

import cuanto.test.TestObjects

class ParsingServiceTests extends GroovyTestCase {
	ParsingService parsingService
	DataService dataService
	def initializationService
	def testOutcomeService
	def testRunService

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

	void testParseFileFromStream() {
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

	void testParsingWithNewFailures() {
		Project proj = fakes.getProject()
		proj.testType = TestType.findByName("TestNG")
		dataService.saveDomainObject proj

		TestRun testRun1 = fakes.getTestRun(proj)
		dataService.saveDomainObject testRun1
		parsingService.parseFileWithTestRun(getFile("testng-results-run1.xml"), testRun1.id)

		// run 1: passed
		def results1 = TestOutcome.executeQuery("from TestOutcome to where to.testCase.fullName like 'cuanto.test.testNgOne.testMethod1'")
		assertEquals 1, results1.size()
		def testMethod1OutcomeForFirstTime = results1[0]
		assertFalse "First success should not result in failure status change.",
			testMethod1OutcomeForFirstTime.isFailureStatusChanged

		// run 1: failed
		def results2 = TestOutcome.executeQuery("from TestOutcome to where to.testCase.fullName like 'cuanto.test.testNgOne.testMethod2'")
		assertEquals 1, results2.size()
		def testMethod2OutcomeForFirstTime = results2[0]
		assertTrue "First failure should result in failure status change.",
			testMethod2OutcomeForFirstTime.isFailureStatusChanged

		TestRun testRun2 = fakes.getTestRun(proj)
		dataService.saveDomainObject testRun2
		parsingService.parseFileWithTestRun(getFile("testng-results-run2.xml"), testRun2.id)

		// run 2: passed -> passed
		def results3 = TestOutcome.executeQuery("from TestOutcome to where to.testCase.fullName like 'cuanto.test.testNgOne.testMethod1' order by dateCreated desc")
		assertEquals 2, results3.size()
		assertEquals results3[1].id, testMethod1OutcomeForFirstTime.id
		def testMethod1OutcomeForSecondTime = results3[0]
		assertFalse "A failure after previous failure should not result in failure status change.",
			testMethod1OutcomeForSecondTime.isFailureStatusChanged

		// run 2: failed -> failed
		def results4 = TestOutcome.executeQuery("from TestOutcome to where to.testCase.fullName like 'cuanto.test.testNgOne.testMethod2' order by dateCreated desc")
		assertEquals 2, results4.size()
		assertEquals results4[1].id, testMethod2OutcomeForFirstTime.id
		def testMethod2OutcomeForSecondTime = results4[0]
		assertFalse "A failure after previous failure should not result in failure status change.",
			testMethod2OutcomeForSecondTime.isFailureStatusChanged

		TestRun testRun3 = fakes.getTestRun(proj)
		dataService.saveDomainObject testRun3
		parsingService.parseFileWithTestRun(getFile("testng-results-run3.xml"), testRun3.id)

		// run 3: passed -> passed -> failed
		def results5 = TestOutcome.executeQuery("from TestOutcome to where to.testCase.fullName like 'cuanto.test.testNgOne.testMethod1' order by dateCreated desc")
		assertEquals 3, results5.size()
		assertEquals results5[2].id, testMethod1OutcomeForFirstTime.id
		assertEquals results5[1].id, testMethod1OutcomeForSecondTime.id
		def testMethod1OutcomeForThirdTime = results5[0]
		assertTrue "A failure after a success should result in failure status change.",
			testMethod1OutcomeForThirdTime.isFailureStatusChanged

		// run 3: failed -> failed -> passed
		def results6 = TestOutcome.executeQuery("from TestOutcome to where to.testCase.fullName like 'cuanto.test.testNgOne.testMethod2' order by dateCreated desc")
		assertEquals 3, results6.size()
		assertEquals results6[2].id, testMethod2OutcomeForFirstTime.id
		assertEquals results6[1].id, testMethod2OutcomeForSecondTime.id
		def testMethod2OutcomeForThirdTime = results6[0]
		assertTrue "A success after previous failure should result in failure status change.",
			testMethod2OutcomeForThirdTime.isFailureStatusChanged
	}

	void testOutputSummary() {
		Project proj = fakes.getProject()
		proj.testType = TestType.findByName("NUnit")
		dataService.saveDomainObject proj

		TestRun testRun = fakes.getTestRun(proj)
		dataService.saveDomainObject testRun
		parsingService.parseFileWithTestRun(getFile("NUnit-TestResultNet.xml"), testRun.id)

		def outcomes = TestOutcome.executeQuery("from TestOutcome to where to.testResult.isFailure = true")
		assertEquals "Wrong number of failures", 5, outcomes.size()
		outcomes.each {
			assertNotNull "testOutputSummary is null", it.testOutputSummary
			assertTrue "testOutputSummary is blank", it.testOutputSummary?.size() > 0
		}

		outcomes = TestOutcome.executeQuery("from TestOutcome to where to.testResult.isSkip = true")
		assertEquals "Wrong number of skips", 41, outcomes.size()
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


    void testParseFileWithPreviouslyQuarantinedTestCase() {
        Project proj = fakes.getProject()
        proj.testType = TestType.findByName("TestNG")
        dataService.saveDomainObject(proj, true)

        TestRun testRun1 = fakes.getTestRun(proj)
        dataService.saveDomainObject(testRun1, true)
        parsingService.parseFileWithTestRun(getFile("testng-results-run1.xml"), testRun1.id)

        // run 1: TestOutcome.analysisState = Bug
        def testOutcomes1 = TestOutcome.executeQuery("from TestOutcome to where to.testRun = ? AND to.testCase.fullName like 'cuanto.test.testNgOne.testMethod1'", [testRun1])
        assert testOutcomes1.size() == 1
        testOutcomes1[0].analysisState = dataService.getAnalysisStateByName("Bug")
        dataService.saveDomainObject(testOutcomes1[0], true)

        // run 2: TestOutcome.analysisState = Quarantined
        TestRun testRun2 = fakes.getTestRun(proj)
        dataService.saveDomainObject(testRun2, true)
        parsingService.parseFileWithTestRun(getFile("testng-results-run1.xml"), testRun2.id)
        def testOutcomes2 = TestOutcome.executeQuery("from TestOutcome to where to.testRun = ? AND to.testCase.fullName like 'cuanto.test.testNgOne.testMethod1'", [testRun2])
        assert testOutcomes2.size() == 1
        assert testOutcomes2[0].analysisState == null
        def bug = new Bug(title: "bug", url: "url")
        dataService.saveDomainObject(bug)
        testOutcomes2[0].bug = bug
        testOutcomes2[0].note = "Note"
        testOutcomes2[0].analysisState = dataService.getAnalysisStateByName("Quarantined")
        dataService.saveDomainObject(testOutcomes2[0], true)

        // run 3: don't execute the quarantined test case.
        TestRun testRun3 = fakes.getTestRun(proj)
        dataService.saveDomainObject(testRun3, true)

        // run 4: TestOutcome.analysisState remains Quarantined , because the last time it ran, it was quarantined
        TestRun testRun4 = fakes.getTestRun(proj)
        dataService.saveDomainObject(testRun4, true)
        parsingService.parseFileWithTestRun(getFile("testng-results-run1.xml"), testRun4.id)
        def testOutcomes4 = TestOutcome.executeQuery("from TestOutcome to where to.testRun = ? AND to.testCase.fullName like 'cuanto.test.testNgOne.testMethod1'", [testRun4])
        assert testOutcomes4.size() == 1
        assert testOutcomes4[0].analysisState == testOutcomes2[0].analysisState
        assert testOutcomes4[0].bug == testOutcomes2[0].bug
        assert testOutcomes4[0].note == testOutcomes2[0].note

        // un-quarantine
        testOutcomes4[0].analysisState = dataService.getAnalysisStateByName("Environment")
        dataService.saveDomainObject(testOutcomes4[0])

        // run 4: TestOutcome.analysisState no longer retains quarantined states
        TestRun testRun5 = fakes.getTestRun(proj)
        dataService.saveDomainObject testRun5
        parsingService.parseFileWithTestRun(getFile("testng-results-run1.xml"), testRun5.id)
        def testOutcomes5 = TestOutcome.executeQuery("from TestOutcome to where to.testRun = ? AND to.testCase.fullName like 'cuanto.test.testNgOne.testMethod1'", [testRun5])
        assert testOutcomes5.size() == 1
        assert testOutcomes5[0].analysisState == null
        assert testOutcomes5[0].bug == null
        assert testOutcomes5[0].note == null
    }

	/*
	* This test is presently skipped because it occasionally hangs due to http://jira.grails.org/browse/GRAILS-7861,
	* which is resolved in grails 2.0.
	*/
    void skip_testParseTestResultsForSameTestRun()
    {
        Project proj = fakes.getProject()
        proj.testType = TestType.findByName("TestNG")
        dataService.saveDomainObject(proj, true)

        TestRun testRun = fakes.getTestRun(proj)
        dataService.saveDomainObject(testRun, true)

        def threads = []
        def exception = null
        def startTime = System.currentTimeMillis()
        def getElapsedTime = { System.currentTimeMillis() - startTime }
        10.times {
            threads << Thread.start {
                TestRun.withNewSession {
                    while (exception == null && getElapsedTime() < 30000)
                    {
                        try {
                            parsingService.parseFileWithTestRun(getFile("testng-results-run1.xml"), testRun.id)
                        } catch (Exception e) {
                            exception = e
                        }
                    }
                }
            }
        }
        threads*.join()

        if (exception)
        {
            throw new RuntimeException("Concurrent parseFileWithTestRun failed!", exception);
        }
    }
}