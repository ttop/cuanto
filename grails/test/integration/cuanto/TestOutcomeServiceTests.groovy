package cuanto

import cuanto.test.TestObjects
import cuanto.test.WordGenerator

public class TestOutcomeServiceTests extends GroovyTestCase {

	DataService dataService
	TestOutcomeService testOutcomeService

	def initializationService
	def testRunService
	def bugService
	StatisticService statisticService
	ParsingService parsingService

	TestObjects to
	WordGenerator wordGen

	@Override
	void setUp() {
		initializationService.initializeAll()
		to = new TestObjects()
		to.dataService = this.dataService
		testOutcomeService.dataService = this.dataService
		wordGen = new WordGenerator()
	}

	void testUpdateTestOutcomeWithoutScriptTag() {
		// create a project
		Project proj = to.getProject()
		proj.save()

		// create a test case
		TestCase tc = to.getTestCase(proj)
		dataService.saveDomainObject tc

		// create a test run with an outcome
		TestRun testRun = to.getTestRun(proj)
		testRun.save()

		TestOutcome outcome = to.getTestOutcome(tc, testRun)
		outcome.save()

		def params = [
			id: outcome.id,
			testResult: 'fail',
			note: textWithoutScriptTags,
			owner: textWithoutScriptTags,
			bug: textWithoutScriptTags
		]
		testOutcomeService.updateTestOutcome(params)

		// the notes are the same
		def persistedOutcome = dataService.getTestOutcomes(new TestOutcomeQueryFilter(testRun: testRun, testResultIncludedInCalculations: true))[0]
		assertEquals textWithoutScriptTags, persistedOutcome.note
		assertEquals textWithoutScriptTags, persistedOutcome.owner
		assertEquals textWithoutScriptTags, persistedOutcome.bug.title
	}


	void testUpdateTestOutcomeNoOutcome() {
		testOutcomeService.updateTestOutcome([:])
	}



	void testCreateTestOutcomeWithScriptTags() {
		// create a project
		Project proj = to.getProject()
		proj.save()

		// create a test case
		TestCase tc = to.getTestCase(proj)
		dataService.saveDomainObject tc

		// create a test run with an outcome
		TestRun testRun = to.getTestRun(proj)
		testRun.save()

		TestOutcome outcome = to.getTestOutcome(tc, testRun)
		outcome.save()

		def params = [
			id: outcome.id,
			testResult: 'fail',
			note: textWithScriptTags,
			owner: textWithScriptTags,
			bug: textWithScriptTags
		]
		testOutcomeService.updateTestOutcome(params)

		// the notes are the same
		def persistedOutcome = dataService.getTestOutcomes(new TestOutcomeQueryFilter(testRun: testRun, testResultIncludedInCalculations: true))[0]
		assertEquals textWithSanitizedScriptTags, persistedOutcome.note
		assertEquals textWithSanitizedScriptTags, persistedOutcome.owner
		assertEquals textWithSanitizedScriptTags, persistedOutcome.bug.title
	}

	void testApplyAnalysisStateToTestOutcome() {
		// create a project
		Project proj = to.getProject()
		proj.save()

		// create a test case
		TestCase tc = to.getTestCase(proj)
		dataService.saveDomainObject tc

		// create a test run with an outcome
		TestRun testRun = to.getTestRun(proj)
		dataService.saveDomainObject testRun

		TestOutcome outcome = to.getTestOutcome(tc, testRun)
		dataService.saveDomainObject outcome
		statisticService.calculateTestRunStats(testRun.id)

		def stateToApply = dataService.getAnalysisStateByName("Bug")

		testOutcomeService.applyAnalysisStateToTestOutcome(outcome, [analysisState: stateToApply.id.toString()])
		assertEquals "Wrong analysis state when applying by ID", stateToApply, outcome.analysisState

		outcome.refresh()
		testOutcomeService.applyAnalysisStateToTestOutcome(outcome, [analysisStateName: stateToApply.name])
		assertEquals "Wrong analysis state when applying by name", stateToApply, outcome.analysisState

		outcome.refresh()
		testOutcomeService.applyAnalysisStateToTestOutcome(outcome, [:])
		assertNull "Wrong analysis state when applying to passing TestOutcome without parameters", outcome.analysisState

		outcome.refresh()
		testOutcomeService.applyAnalysisStateToTestOutcome(outcome, [testResult: "Pass"])
		assertNull "Wrong analysis state when applying to passing TestOutcome without parameters", outcome.analysisState

		outcome.testResult = TestResult.findByName("Fail")
		dataService.saveDomainObject outcome
		outcome.refresh()
		testOutcomeService.applyAnalysisStateToTestOutcome(outcome, [:])
		assertNull "Wrong analysis state when applying to passing TestOutcome without parameters", outcome.analysisState


	}


	void testGetCsvForTestOutcomes() {
		//todo: submit results
		// retrieve CSV
		// verify
		// create a project
		Project proj = to.getProject()
		proj.save()

		def testRun = to.getTestRun(proj)
		dataService.saveDomainObject testRun

		def outcomes = []
		1.upto(3) {
			def tc = to.getTestCase(proj)
			dataService.saveDomainObject tc
			def outcome = to.getTestOutcome(tc, testRun)
			dataService.saveDomainObject outcome
			outcomes << outcome
		}

		def csv = testOutcomeService.getDelimitedTextForTestOutcomes(outcomes, ",")
		def csvLines = csv.readLines()
		assertEquals "Wrong number of lines for CSV output", outcomes.size() + 1, csvLines.size()

		// do 0 results, 1 result, 3 results, 10 results
	}

    void testGetGroupedOutputSummaries() {
        Project proj = to.project
        proj.testType = TestType.findByName("TestNG")
        dataService.saveDomainObject proj

        TestRun testRun = to.getTestRun(proj)
        dataService.saveDomainObject testRun
        parsingService.parseFileWithTestRun(getFile("grouped-output-testng-results.xml"), testRun.id)

        def offset = 0
        def max = 5
        def outputGroups = testOutcomeService.getGroupedOutputSummaries(testRun, "AllFailures", offset, max)
        assertNotNull outputGroups
        assertEquals "Wrong number of groups returned", 4, outputGroups.size()

        assertEquals "Wrong first group size", 4, outputGroups[0][0]
        assertEquals "Wrong first group output", "java.lang.AssertionError: This has failed four times", outputGroups[0][1]

        assertEquals "Wrong second group size", 3, outputGroups[1][0]
        assertEquals "Wrong second group output", "java.lang.AssertionError: This has failed three times", outputGroups[1][1]

        assertEquals "Wrong third group size", 2, outputGroups[2][0]
        assertEquals "Wrong third group output", "java.lang.AssertionError: This has failed twice", outputGroups[2][1]

        assertEquals "Wrong fourth group size", 1, outputGroups[3][0]
        assertEquals "Wrong fourth group output", "java.lang.AssertionError: This has failed once", outputGroups[3][1]
    }


	void testGetCustomPropertyNames() {
		Project proj = to.project
		proj.testType = TestType.findByName("TestNG")
		TestRun testRun = to.getTestRun(proj)
		TestCase tc = to.getTestCase(proj)
		
		def outcomes = []
		1.upto(5){
			outcomes << to.getTestOutcome(tc, testRun)
		}

		outcomes[0].addToTestProperties(new TestOutcomeProperty("john", "lennon"))
		outcomes[1].addToTestProperties(new TestOutcomeProperty("paul", "mccartney"))
		outcomes[1].addToTestProperties(new TestOutcomeProperty("John", "lennon"))
		outcomes[2].addToTestProperties(new TestOutcomeProperty("Paul", "Jones"))
		outcomes[3].addToTestProperties(new TestOutcomeProperty("George", "Harrison"))

		def propNames = testOutcomeService.getCustomPropertyNames(outcomes)
		assertEquals "Wrong number of property names", 3, propNames.size()
		assertTrue "Couldn't find property name", propNames.contains("john")
		assertTrue "Couldn't find property name", propNames.contains("paul")
		assertTrue "Couldn't find property name", propNames.contains("George")

		assertEquals "Wrong property name", "George", propNames[0]
		assertEquals "Wrong property name", "john", propNames[1]
		assertEquals "Wrong property name", "paul", propNames[2]
	}


	void testFindPackagesMatching() {
		Project proj = to.project
		proj.testType = TestType.findByName("TestNG")
		dataService.saveDomainObject proj

		def testCases = []
		1.upto(5) {
			TestCase tc = to.getTestCase(proj)
			dataService.saveDomainObject tc
			testCases << tc
		}

		testCases[0].testName = "testZero"
		testCases[0].packageName = "org.codehaus.cuanto.foo"
		testCases[0].fullName = testCases[0].packageName + "." + testCases[0].testName  

		testCases[1].testName = "testOne"
		testCases[1].packageName = "org.codehaus.cuanto.foo"
		testCases[1].fullName = testCases[1].packageName + "." + testCases[1].testName  

		testCases[2].testName = "testTwo"
		testCases[2].packageName = "org.codehaus.cuanto.foo"
		testCases[2].fullName = testCases[2].packageName + "." + testCases[2].testName  

		testCases[3].testName = "testThree"
		testCases[3].packageName = "org.codehaus.cuanto.bar"
		testCases[3].fullName = testCases[3].packageName + "." + testCases[3].testName  

		testCases[4].testName = "testFour"
		testCases[4].packageName = "org.codehaus.cuanto.bar"
		testCases[4].fullName = testCases[4].packageName + "." + testCases[4].testName  

		testCases.each {
			dataService.saveDomainObject it
		}
		
		def results = testOutcomeService.findTestCaseFullNamesMatching(proj, "org.codehaus.cuanto.foo")
		assertEquals "Wrong number of testCases", 3, results.size()
		
		results = testOutcomeService.findTestCaseFullNamesMatching(proj, "org.codehaus.cuanto.bar")
		assertEquals "Wrong number of testCases", 2, results.size()

		results = testOutcomeService.findTestCaseFullNamesMatching(proj, "blah.de.blah")
		assertEquals "Wrong number of testCases", 0, results.size()
	}


	void testPreviewPackageRenameAndPackageRename() {
		Project proj = to.project
		proj.testType = TestType.findByName("TestNG")
		dataService.saveDomainObject proj

		def testCases = []
		1.upto(5) {
			TestCase tc = to.getTestCase(proj)
			dataService.saveDomainObject tc
			testCases << tc
		}

		testCases[0].testName = "testZero"
		testCases[0].packageName = "org.codehaus.cuanto.foo"
		testCases[0].fullName = testCases[0].packageName + "." + testCases[0].testName

		testCases[1].testName = "testOne"
		testCases[1].packageName = "org.codehaus.cuanto.foo"
		testCases[1].fullName = testCases[1].packageName + "." + testCases[1].testName

		testCases[2].testName = "testTwo"
		testCases[2].packageName = "org.codehaus.cuanto.foo"
		testCases[2].fullName = testCases[2].packageName + "." + testCases[2].testName

		testCases[3].testName = "testThree"
		testCases[3].packageName = "org.codehaus.cuanto.bar"
		testCases[3].fullName = testCases[3].packageName + "." + testCases[3].testName

		testCases[4].testName = "testFour"
		testCases[4].packageName = "org.codehaus.cuanto.bar"
		testCases[4].fullName = testCases[4].packageName + "." + testCases[4].testName

		testCases.each {
			dataService.saveDomainObject it
		}

		def results = testOutcomeService.previewTestRename(proj, "org.codehaus.cuanto.foo", "blah.de.blah")
		assertEquals "Wrong number of testCases", 3, results.size()

		results.eachWithIndex { it, idx ->
			assertTrue "Doesn't contain name", it.testCase.packageName.contains("org.codehaus.cuanto.foo")
			assertEquals "Wrong newName", it.testCase.fullName.replaceAll("org.codehaus.cuanto.foo", "blah.de.blah"), it.newName
		}

		def toRename = []
		0.upto(2) {
			toRename << [id: testCases[it].id, newName: testCases[it].fullName.replaceAll("org.codehaus.cuanto.foo", "blah.de.blah")]
		}

		testOutcomeService.bulkTestCaseRename(toRename)

		def found = TestCase.findAllByPackageName("blah.de.blah")
		assertEquals "Wrong number of renamed packages", 3, found.size()
	}


	void testFindTestCaseNamesMatching() {
		Project proj = to.project
		proj.testType = TestType.findByName("TestNG")
		dataService.saveDomainObject proj

		def testCases = []
		1.upto(5) {
			TestCase tc = to.getTestCase(proj)
			dataService.saveDomainObject tc
			testCases << tc
		}

		testCases[0].testName = "testMatch"
		testCases[0].packageName = "org.codehaus.cuanto.foo"
		testCases[0].fullName = testCases[0].packageName + "." + testCases[0].testName

		testCases[1].testName = "testOne"
		testCases[1].packageName = "org.codehaus.cuanto.foo"
		testCases[1].fullName = testCases[1].packageName + "." + testCases[1].testName

		testCases[2].testName = "testTwo"
		testCases[2].packageName = "org.codehaus.cuanto.foo"
		testCases[2].fullName = testCases[2].packageName + "." + testCases[2].testName

		testCases[3].testName = "testThree"
		testCases[3].packageName = "org.codehaus.cuanto.bar"
		testCases[3].fullName = testCases[3].packageName + "." + testCases[3].testName

		testCases[4].testName = "testMatch"
		testCases[4].packageName = "org.codehaus.cuanto.bar"
		testCases[4].fullName = testCases[4].packageName + "." + testCases[4].testName

		testCases.each {
			dataService.saveDomainObject it
		}

		def results = testOutcomeService.findTestCaseFullNamesMatching(proj, "testMatch")
		assertEquals "Wrong number of testCases", 2, results.size()

		results = testOutcomeService.findTestCaseFullNamesMatching(proj, "testThree")
		assertEquals "Wrong number of testCases", 1, results.size()

		results = testOutcomeService.findTestCaseFullNamesMatching(proj, "testFour")
		assertEquals "Wrong number of testCases", 0, results.size()
	}


	void testPreviewTestRenameAndTestRename() {
		Project proj = to.project
		proj.testType = TestType.findByName("TestNG")
		dataService.saveDomainObject proj

		def testCases = []
		1.upto(5) {
			TestCase tc = to.getTestCase(proj)
			dataService.saveDomainObject tc
			testCases << tc
		}

		testCases[0].testName = "testMatch"
		testCases[0].packageName = "org.codehaus.cuanto.foo"
		testCases[0].fullName = testCases[0].packageName + "." + testCases[0].testName

		testCases[1].testName = "testOne"
		testCases[1].packageName = "org.codehaus.cuanto.foo"
		testCases[1].fullName = testCases[1].packageName + "." + testCases[1].testName

		testCases[2].testName = "testTwo"
		testCases[2].packageName = "org.codehaus.cuanto.foo"
		testCases[2].fullName = testCases[2].packageName + "." + testCases[2].testName

		testCases[3].testName = "testThree"
		testCases[3].packageName = "org.codehaus.cuanto.bar"
		testCases[3].fullName = testCases[3].packageName + "." + testCases[3].testName

		testCases[4].testName = "testMatch"
		testCases[4].packageName = "org.codehaus.cuanto.bar"
		testCases[4].fullName = testCases[4].packageName + "." + testCases[4].testName

		testCases.each {
			dataService.saveDomainObject it
		}

		def results = testOutcomeService.previewTestRename(proj, "testMatch", "testRename")
		assertEquals "Wrong number of testCases", 2, results.size()

		results.eachWithIndex { it, idx ->
			assertTrue "Doesn't contain name", it.testCase.testName.contains("testMatch")
			assertEquals "Wrong newName", it.testCase.fullName.replaceAll("testMatch", "testRename"), it.newName
		}

		def toRename = []
		[0,4].each {
			toRename << [id: testCases[it].id, newName: testCases[it].fullName.replaceAll("testMatch", "testRename")]
		}

		testOutcomeService.bulkTestCaseRename(toRename)

		def found = TestCase.findAllByTestName("testRename")
		assertEquals "Wrong number of renamed tests", 2, found.size()
	}

	void testPreviewTestRenameAndBulkTestCaseRename() {
		Project proj = to.project
		proj.testType = TestType.findByName("TestNG")
		dataService.saveDomainObject proj

		def testCases = []
		1.upto(5) {
			TestCase tc = to.getTestCase(proj)
			dataService.saveDomainObject tc
			testCases << tc
		}

		testCases[0].testName = "testMatch"
		testCases[0].packageName = "org.codehaus.cuanto.foo"
		testCases[0].fullName = testCases[0].packageName + "." + testCases[0].testName

		testCases[1].testName = "testOne"
		testCases[1].packageName = "org.codehaus.cuanto.foo"
		testCases[1].fullName = testCases[1].packageName + "." + testCases[1].testName

		testCases[2].testName = "testTwo"
		testCases[2].packageName = "org.codehaus.cuanto.foo"
		testCases[2].fullName = testCases[2].packageName + "." + testCases[2].testName

		testCases[3].testName = "testThree"
		testCases[3].packageName = "org.codehaus.cuanto.bar"
		testCases[3].fullName = testCases[3].packageName + "." + testCases[3].testName

		testCases[4].testName = "testMatch"
		testCases[4].packageName = "org.codehaus.cuanto.bar"
		testCases[4].fullName = testCases[4].packageName + "." + testCases[4].testName

		testCases.each {
			dataService.saveDomainObject it
		}

		def results = testOutcomeService.previewTestRename(proj, "testMatch", "testRename")
		assertEquals "Wrong number of testCases", 2, results.size()

		results.eachWithIndex { it, idx ->
			assertTrue "Doesn't contain name", it.testCase.testName.contains("testMatch")
			assertEquals "Wrong newName", it.testCase.fullName.replaceAll("testMatch", "testRename"), it.newName
		}

		def toRename = []
		[0,4].each {
			toRename << [id: testCases[it].id, newName: testCases[it].testName.replaceAll("testMatch", "testRename")]
		}

		testOutcomeService.bulkTestCaseRename(toRename)

		def found = TestCase.findAllByTestName("testRename")
		assertEquals "Wrong number of renamed tests", 2, found.size()
	}


	void testExtractTestCaseNames() {
		def pkgName
		def tName
		
		assertTestNamesExtracted "a.b.c", "foo"
		assertTestNamesExtracted "dude", "chick"
		assertTestNamesExtracted "", "solo"
	}


	void testChangeBugOnTestOutcome() {
        int originalTestOutcomeCount = TestOutcome.count()
        int originalBugCount = Bug.count()
		// create a project
		Project proj = to.getProject()
		proj.save()

		// create a test case
		TestCase tc = to.getTestCase(proj)
		dataService.saveDomainObject tc

		// create a test run
		TestRun testRun = to.getTestRun(proj)
		dataService.saveDomainObject testRun

		Bug bugA = new Bug(title: "A", url: "http://a")
		Bug bugB = new Bug(title: "B", url: "http://b")
		Bug bugC = new Bug(title: "C", url: "http://c")
		Bug bugD = new Bug(title: "D", url: "http://a")

		// create a TestOutcome1 referencing bug A
		TestOutcome outcome1 = to.getTestOutcome(tc, testRun)
		dataService.saveDomainObject bugA
		outcome1.bug = bugA
		dataService.saveDomainObject outcome1

		// create a  TestOutcome2 referencing bug A
		TestOutcome outcome2 = to.getTestOutcome(tc, testRun)
		outcome2.bug = bugA
		dataService.saveDomainObject outcome2
		assertEquals("Wrong bug count", 1, Bug.count())

		// create a TestOutcome3 referencing bug B
		TestOutcome outcome3 = to.getTestOutcome(tc, testRun)
		dataService.saveDomainObject bugB
		outcome3.bug = bugB
		dataService.saveDomainObject outcome3
		assertEquals("Wrong bug count", 2, Bug.count())

		// update TestOutcome1 to bugC
		TestOutcome updatedOutcome1 = new TestOutcome()
		updatedOutcome1.id = outcome1.id
		updatedOutcome1.testCase = outcome1.testCase
		updatedOutcome1.note = outcome1.note
		updatedOutcome1.testRun = outcome1.testRun
		updatedOutcome1.testOutput = outcome1.testOutput
		updatedOutcome1.testResult = outcome1.testResult
		dataService.saveDomainObject bugC
		updatedOutcome1.bug = bugC
		testOutcomeService.updateTestOutcome(updatedOutcome1)
		assertEquals("Wrong number of TestOutcomes", originalTestOutcomeCount + 3, TestOutcome.count())
		assertEquals("Wrong number of Bugs", originalBugCount + 3, Bug.count())

		// update TestOutcome2 to reference bugB
		outcome2.bug = bugB
		testOutcomeService.updateTestOutcome(outcome2)
		def outcomeList = TestOutcome.findAllByBug(bugA)
		dataService.deleteBugIfUnused(bugA)
		def bugList = Bug.findAll()
		assertEquals("Wrong number of Bugs", originalBugCount + 2, Bug.count())

		// update TestOutcome3 to reference bugC
		outcome3.bug = bugC
		testOutcomeService.updateTestOutcome(outcome3)
		dataService.deleteBugIfUnused(bugB)
		assertEquals("Wrong number of Bugs", originalBugCount + 2, Bug.count())

		//update TestOutcome2 to reference bugC
		outcome2.bug = bugC
		testOutcomeService.updateTestOutcome(outcome2)
		dataService.deleteBugIfUnused(bugB)
		assertEquals("Wrong number of Bugs", originalBugCount + 1, Bug.count())

	}


	void assertTestNamesExtracted(pkgName, tName) {
		def fullName = pkgName? pkgName + "." + tName : tName
		def extracted = testOutcomeService.extractTestCaseNames(pkgName + "." + tName)
		assertEquals "Wrong package name for ${fullName}", pkgName, extracted.packageName
		assertEquals "Wrong test name for ${fullName}", tName, extracted.testName
	}

    private File getFile(filename) {
        File file = new File("test/resources/${filename}")
        assertTrue("Couldn't find file: ${file.toString()}", file.exists())
        return file
    }
    
	def textWithoutScriptTags = '''
			Lorem ipsum dolor sit amet, consectetur adipiscing elit.
			Duis luctus erat ultrices ipsum mollis nec scelerisque nibh eleifend.
			Vivamus hendrerit cursus viverra. Pellentesque dui sem, convallis non eleifend ut, commodo id elit.
			Aenean imperdiet lectus eu diam molestie sollicitudin. Curabitur id libero neque, non vulputate leo.'''
	def textWithScriptTags = '''
			Lorem ipsum dolor sit amet, consectetur adipiscing elit.
			<script type="text/javascript"><![CDATA[
				Duis luctus erat ultrices ipsum mollis nec scelerisque nibh eleifend.
			// ]]></script>
			Vivamus hendrerit cursus viverra. Pellentesque dui sem, convallis non eleifend ut, commodo id elit.
			Aenean imperdiet lectus eu diam molestie sollicitudin. Curabitur <script type="text/javascript">id</script>
			libero neque, non vulputate leo.'''
	def textWithSanitizedScriptTags = '''
			Lorem ipsum dolor sit amet, consectetur adipiscing elit.
			&lt;script type="text/javascript"><![CDATA[
				Duis luctus erat ultrices ipsum mollis nec scelerisque nibh eleifend.
			// ]]>&lt;/script&gt;
			Vivamus hendrerit cursus viverra. Pellentesque dui sem, convallis non eleifend ut, commodo id elit.
			Aenean imperdiet lectus eu diam molestie sollicitudin. Curabitur &lt;script type="text/javascript">id&lt;/script&gt;
			libero neque, non vulputate leo.'''
}