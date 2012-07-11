package cuanto

import cuanto.test.TestObjects
import cuanto.test.WordGenerator

class TestOutcomeTests extends GroovyTestCase {

	DataService dataService
	def initializationService
	def testOutcomeService
	def testRunService
	def bugService

	TestObjects to
	WordGenerator wordGen


	@Override
	void setUp() {
		initializationService.initializeAll()
		to = new TestObjects()
		to.dataService = dataService
		wordGen = new WordGenerator()
	}


	/**
	 Submit two test RunRuns, then get outcomes for each testRun and make sure the count is right and that they
	 are from the correct test Run.
	 */

	void testGetOutcomesByTestRun() {
		Project proj = to.getProject()
		if (!proj.save()) {
			dataService.reportSaveError proj
		}

		final int numTests = 10

		def testCases = []
		for (x in 1..numTests) {
			TestCase testCase = to.getTestCase(proj)
			dataService.saveDomainObject testCase
			testCases.add(testCase)
		}

		def testRunOne = to.getTestRun(proj)
		testRunOne.save()

		for (testCase in testCases) {
			TestOutcome out = to.getTestOutcome(testCase, testRunOne)
			dataService.saveDomainObject out
		}

		def testRunTwo = to.getTestRun(proj)
		testRunTwo.save()

		for (testCase in testCases) {
			TestOutcome out = to.getTestOutcome(testCase, testRunTwo)
			dataService.saveDomainObject out
		}

		def outcomes = dataService.getTestOutcomes(new TestOutcomeQueryFilter(testRun:testRunOne))

		assertEquals("Wrong number of outcomes", numTests, outcomes.size())
		for (outcome in outcomes) {
			assertEquals("Wrong testRun", testRunOne, outcome.testRun)
		}

		outcomes = dataService.getTestOutcomes(new TestOutcomeQueryFilter(testRun: testRunTwo))

		assertEquals("Wrong number of outcomes", numTests, outcomes.size())
		for (outcome in outcomes) {
			assertEquals("Wrong testRun", testRunTwo, outcome.testRun)
		}
	}


	void testGetOutcomesForTestRunsByPackage() {
		Project proj = to.getProject()
		proj.save()

		def numCases = 10

		TestRun testRun = to.getTestRun(proj)

		if (!testRun.save()) {
			dataService.reportSaveError testRun
		}

		for (x in 1..numCases) {
			TestCase tc = to.getTestCase(proj)
			tc.packageName = "a.b.c"
			dataService.saveDomainObject tc

			TestOutcome outcome = to.getTestOutcome(tc, testRun)
			outcome.duration = 1
			dataService.saveDomainObject outcome
		}

		for (x in 1..numCases) {
			TestCase tc = to.getTestCase(proj)
			tc.packageName = "x.y.z"
			dataService.saveDomainObject tc

			TestOutcome outcome = to.getTestOutcome(tc, testRun)
			outcome.duration = 2

			if (x == 2) {
				outcome.testResult = dataService.result("error")
			} else if (x == 3) {
				outcome.testResult = dataService.result("ignore")
			}

			dataService.saveDomainObject outcome
		}

		for (x in 1..numCases) {
			TestCase tc = to.getTestCase(proj)
			tc.packageName = "a.b."
			dataService.saveDomainObject tc

			TestOutcome outcome = to.getTestOutcome(tc, testRun)
			outcome.duration = 3
			if (x == 2) {
				outcome.testResult = dataService.result("fail")
			} else if (x == 3) {
				outcome.testResult = dataService.result("error")
			}
			dataService.saveDomainObject outcome
		}

		def abOutcomes = dataService.getTestOutcomes(
			new TestOutcomeQueryFilter(testRun: testRun, testCasePackage: "a.b", testResultIncludedInCalculations: true))
		assertEquals("Wrong number of a.b outcomes", 20, abOutcomes.size())

		def abcOutcomes = dataService.getTestOutcomes(
			new TestOutcomeQueryFilter(testRun:testRun, testCasePackage: "a.b.c", testResultIncludedInCalculations: true))
		assertEquals("Wrong number of a.b.c outcomes", 10, abcOutcomes.size())

		def xyzOutcomes = dataService.getTestOutcomes(
			new TestOutcomeQueryFilter(testRun:testRun, testCasePackage: "x.y.z", testResultIncludedInCalculations: true))
		assertEquals("Wrong number of x.y.z outcomes", 9, xyzOutcomes.size())
	}


	void testGetBug() {
		Bug bug = bugService.getBug(null, null)
		assertNull bug

		bug = bugService.getBug("none", null)
		assertNotNull bug
		assertEquals "none", bug.title
		assertNull bug.url

		bug = bugService.getBug(null, "http://none")
		assertNotNull bug
		assertNull bug.title

		bug = bugService.getBug("", "")
		assertNull bug

		String urlWithoutTitle = "http://${wordGen.getSentence(3)}"
		bug = bugService.getBug("", urlWithoutTitle)
		assertNotNull bug
		assertNull bug.title
		assertEquals urlWithoutTitle, bug.url

		String titleWithoutUrl = wordGen.getSentence(3)
		bug = bugService.getBug(titleWithoutUrl, "")
		assertNotNull bug
		assertNull bug.url
		assertEquals titleWithoutUrl, bug.title

		String createdBugTitle = wordGen.getSentence(3)
		String createdBugUrl = "http://${wordGen.getCamelWords(3)}"
		Bug createdBug = bugService.getBug(createdBugTitle, createdBugUrl)
		assertNotNull createdBug
		assertEquals createdBugTitle, createdBug.title
		assertEquals createdBugUrl, createdBug.url

		def createdBugId = createdBug.id

		String newTitle = wordGen.getSentence(3)
		Bug newTitleBug = bugService.getBug(newTitle, createdBugUrl)
		assertNotNull newTitleBug
		assertFalse createdBugId.equals(newTitleBug.id)
		assertEquals newTitle, newTitleBug.title
		assertEquals createdBugUrl, newTitleBug.url

		String newUrl = wordGen.getSentence(3)
		Bug newUrlBug = bugService.getBug(newTitle, newUrl)
		assertNotNull newUrlBug
		assertTrue createdBugId != newUrlBug.id
		assertEquals newTitle, newUrlBug.title
		assertEquals newUrl, newUrlBug.url

	}


	void testSearch() {
		Project proj = to.getProject()
		proj.save()

		for (name in ["Bob", "bobby", "Bulb", "Bobert", "Jim", "jimmy", "jimmer", "ace", "aced", "Maced",
			"zorba", "com.myfoo.blah", "com.foobar.blah", "com.foobar.blech", "com.foobar.yes"]) {
			TestCase tc = new TestCase()
			tc.setTestName name
			tc.project = proj
			tc.packageName = "cuanto.foo"
			tc.fullName = tc.packageName + "." + tc.testName
			tc.setDescription wordGen.getSentence(10)
			dataService.saveDomainObject tc
		}

		List<TestCase> bobs = testOutcomeService.findTestCaseByName("bob", proj.id.toString())
		assertEquals 3, bobs.size()

		bobs = testOutcomeService.findTestCaseByName("bobby", proj.id.toString())
		assertEquals 1, bobs.size()

		List<TestCase> jims = testOutcomeService.findTestCaseByName("Jim", proj.id.toString())
		assertEquals 3, jims.size()

		List<TestCase> ace = testOutcomeService.findTestCaseByName("ace", proj.id.toString())
		assertEquals 3, ace.size()


		List<TestCase> multi = testOutcomeService.findTestCaseByName("cuanto.foo", proj.id.toString())
		assertEquals multi.toListString(), 15, multi.size()

		multi = testOutcomeService.findTestCaseByName("com.foobar", proj.id.toString())
		assertEquals multi.toListString(), 3, multi.size()

	}


	void testApplyAnalysisAllFields() {
		Project proj = to.getProject()
		proj.save()

		def numCases = 10
		def testRuns = []

		1.upto(3){
			def testRun = to.getTestRun(proj)
			if (!testRun.save()) {
				dataService.reportSaveError testRun
			}
			testRuns << testRun
		}


		for (x in 1..numCases) {
			TestCase tc = to.getTestCase(proj)
			dataService.saveDomainObject tc

			TestOutcome outcome = to.getTestOutcome(tc, testRuns[0])
			outcome.duration = 1
			dataService.saveDomainObject outcome
		}

		for (x in 1..numCases) {
			TestCase tc = to.getTestCase(proj)
			dataService.saveDomainObject tc

			TestOutcome outcome = to.getTestOutcome(tc, testRuns[1])
			outcome.duration = 2

			if (x == 2) {
				outcome.testResult = dataService.result("error")
			} else if (x == 3) {
				outcome.testResult = dataService.result("ignore")
			}

			dataService.saveDomainObject outcome
		}

		def sourceOutcome
		def targetOutcomes = []

		for (x in 1..numCases) {
			TestCase tc = to.getTestCase(proj)
			dataService.saveDomainObject tc

			TestOutcome outcome = to.getTestOutcome(tc, testRuns[2])
			outcome.duration = 3
			if (x == 2) {
				sourceOutcome = outcome
				outcome.testResult = dataService.result("fail")
				outcome.analysisState = dataService.getAnalysisStateByName("Bug")
				outcome.bug = dataService.getBug(to.wordGen.getWord(), "http://${to.wordGen.getWord()}")
				outcome.owner = to.wordGen.getWord()
				outcome.note = to.wordGen.getSentence(10)
			} else if (x == 3) {
				outcome.testResult = dataService.result("error")
			} else if (x > 6) {
				outcome.testResult = dataService.result("fail")
				targetOutcomes << outcome
			}
			dataService.saveDomainObject outcome
		}

		// apply analysis to one outcome
		testOutcomeService.applyAnalysis(sourceOutcome, targetOutcomes[0..0], null)
		def fetchedOutcome = TestOutcome.get(targetOutcomes[0].id)
		assertAnalysisEquals sourceOutcome, fetchedOutcome

		// apply analysis to multiple outcomes
		testOutcomeService.applyAnalysis(sourceOutcome, targetOutcomes, null)
		targetOutcomes.each {
			def fetchedTargetOutcome = TestOutcome.get(it.id)
			assertAnalysisEquals sourceOutcome, fetchedTargetOutcome 
		}
	}


	void testApplyAnalysisSomeFields() {
		Project proj = to.getProject()
		proj.save()

		def numCases = 10
		def testRuns = []

		1.upto(3){
			def testRun = to.getTestRun(proj)
			if (!testRun.save()) {
				dataService.reportSaveError testRun
			}
			testRuns << testRun
		}


		for (x in 1..numCases) {
			TestCase tc = to.getTestCase(proj)
			dataService.saveDomainObject tc

			TestOutcome outcome = to.getTestOutcome(tc, testRuns[0])
			outcome.duration = 1
			dataService.saveDomainObject outcome
		}

		for (x in 1..numCases) {
			TestCase tc = to.getTestCase(proj)
			dataService.saveDomainObject tc

			TestOutcome outcome = to.getTestOutcome(tc, testRuns[1])
			outcome.duration = 2

			if (x == 2) {
				outcome.testResult = dataService.result("error")
			} else if (x == 3) {
				outcome.testResult = dataService.result("ignore")
			}

			dataService.saveDomainObject outcome
		}

		def sourceOutcome
		def targetOutcomes = []

		for (x in 1..numCases) {
			TestCase tc = to.getTestCase(proj)
			dataService.saveDomainObject tc

			TestOutcome outcome = to.getTestOutcome(tc, testRuns[2])
			outcome.duration = 3
			if (x == 2) {
				sourceOutcome = outcome
				outcome.testResult = dataService.result("fail")
				outcome.analysisState = dataService.getAnalysisStateByName("Bug")
				outcome.bug = dataService.getBug(to.wordGen.getWord(), "http://${to.wordGen.getWord()}")
				outcome.owner = to.wordGen.getWord()
				outcome.note = to.wordGen.getSentence(10)
			} else if (x == 3) {
				outcome.testResult = dataService.result("error")
			} else if (x > 6) {
				outcome.testResult = dataService.result("fail")
				targetOutcomes << outcome
			}
			dataService.saveDomainObject outcome
		}

		// apply analysis to one outcome
		def fields = ["analysisState", "note"]
		testOutcomeService.applyAnalysis(sourceOutcome, targetOutcomes[0..0], fields)

		def fetchedOutcome = TestOutcome.get(targetOutcomes[0].id)
		assertAnalysisEquals sourceOutcome, fetchedOutcome, fields
		assertFalse "Bug shouldn't have been applied", sourceOutcome.bug == fetchedOutcome.bug

		// apply analysis to multiple outcomes
		testOutcomeService.applyAnalysis(sourceOutcome, targetOutcomes, fields)
		targetOutcomes.each { assertAnalysisEquals(sourceOutcome, it , fields)}
		targetOutcomes.each { targetOutcome ->
			def fetchedTargetOutcome = TestOutcome.get(targetOutcome.id)
			assertFalse "Bug shouldn't have been applied", sourceOutcome.bug == fetchedTargetOutcome.bug
		}
	}


	void testTestCaseFormattersInjection() {
		def registry = testOutcomeService.testCaseFormatterRegistry
		assertNotNull "Test case formatters not injected", registry
		def formatterList = registry.formatterList
		assertEquals 5, formatterList.size()

		def formatMap = registry.getFormatMap()
		assertEquals 5, formatMap.size()
		assertTrue("Couldn't find formater with key ClassName.testMethod()",
			formatMap.containsKey("classname"))
	}


	void testGetTestOutcomesByTestRun() {
		def targetProject = to.project
		dataService.saveDomainObject targetProject

		def otherProject = to.project
		dataService.saveDomainObject otherProject

		def testCases = []
		def otherTestCases = []
		def numTestCases = 5
		1.upto(numTestCases) {
			def testCase = to.getTestCase(targetProject)
			testCases << testCase
			def otherTestCase = to.getTestCase(otherProject)
		    otherTestCases << otherTestCase
		}

		dataService.addTestCases(targetProject, testCases)
		dataService.addTestCases(otherProject, otherTestCases)

		
		def testRuns = []
		1.upto(3) {
			def testRun = to.getTestRun(targetProject)
			dataService.saveTestRun(testRun)
			testRuns << testRun
		}

		testCases.each {testCase ->
			testRuns.each {testRun ->
				def outcome = to.getTestOutcome(testCase, testRun)
				if (testCase == testCases[0]) {
					outcome.testResult = dataService.result("Ignore")
				}
				dataService.saveDomainObject outcome
			}
		}

		def targetRun = testRuns[0] as TestRun
		def outs = dataService.getTestOutcomes(new TestOutcomeQueryFilter(testRun:targetRun))
		assertEquals "Wrong number of outcomes", numTestCases, outs.size()
		outs.each {outcome ->
			assertEquals "Wrong test run for outcome", targetRun, outcome.testRun
		}

		outs = dataService.getTestOutcomes(
			new TestOutcomeQueryFilter(testRun: targetRun, queryMax: 10, queryOffset: 0, testResultIncludedInCalculations: true))
		assertEquals "Wrong number of outcomes", numTestCases - 1, outs.size()
		outs.each {outcome ->
			assertEquals "Wrong test run for outcome", targetRun, outcome.testRun
		}

		outs = dataService.getTestOutcomes(new TestOutcomeQueryFilter(testRun: targetRun, queryMax: 10, queryOffset: 0))
		assertEquals "Wrong number of outcomes", numTestCases, outs.size()
		outs.each {outcome ->
			assertEquals "Wrong test run for outcome", targetRun, outcome.testRun
		}

		shouldFail(IllegalArgumentException) {
			dataService.getTestOutcomes(new TestOutcomeQueryFilter(testRun: targetRun, sorts: [new SortParameters(sortOrder: "foo")]))
		}

		outs = dataService.getTestOutcomes(
			new TestOutcomeQueryFilter(testRun: targetRun, queryMax: 3, queryOffset: 1, testResultIncludedInCalculations: true)
		)
		assertEquals "Wrong number of outcomes", 3, outs.size()
		outs.each {outcome ->
			assertEquals "Wrong test run for outcome", targetRun, outcome.testRun
		}

		outs = dataService.getTestOutcomes(
			new TestOutcomeQueryFilter(testRun: targetRun, 
				queryMax: 3, queryOffset: 1, testResultIncludedInCalculations:true)
		)
		assertEquals "Wrong number of outcomes", 3, outs.size()
		outs.each {outcome ->
			assertEquals "Wrong test run for outcome", targetRun, outcome.testRun
		}


		outs = dataService.getTestOutcomes(new TestOutcomeQueryFilter(testRun: targetRun, queryMax: 3, queryOffset: 1))
		assertEquals "Wrong number of outcomes", 3, outs.size()
		outs.each {outcome ->
			assertEquals "Wrong test run for outcome", targetRun, outcome.testRun
		}

		outs = dataService.getTestOutcomes(new TestOutcomeQueryFilter(testRun: targetRun, queryMax: 3, queryOffset: 1))
		assertEquals "Wrong number of outcomes", 3, outs.size()
		outs.each {outcome ->
			assertEquals "Wrong test run for outcome", targetRun, outcome.testRun
		}

		outs = dataService.getTestOutcomes(new TestOutcomeQueryFilter(testRun: targetRun))
		assertEquals "Wrong number of outcomes", numTestCases, outs.size()
		outs.each {outcome ->
			assertEquals "Wrong test run for outcome", targetRun, outcome.testRun
		}

	}


	void testApplyTestResultToTestOutcome() {
		testOutcomeService.applyTestResultToTestOutcome(null, null) // should succeed without error

		Project proj = to.project
		dataService.saveDomainObject proj
		TestCase tc = to.getTestCase(proj)
		dataService.saveDomainObject tc

		TestRun run = to.getTestRun(proj)
		dataService.saveDomainObject run
		TestResult result = dataService.result("fail")

		testOutcomeService.applyTestResultToTestOutcome(null, result)

		TestOutcome outcome = to.getTestOutcome(tc, run)
		dataService.saveDomainObject outcome

		assertEquals "Wrong initial result", dataService.result("pass"), outcome.testResult
		def outcomeId = outcome.id
		testOutcomeService.applyTestResultToTestOutcome(outcome, result)
		def fetchedOutcome = TestOutcome.get(outcomeId)

		assertEquals "Wrong result", result, fetchedOutcome.testResult

		// apply again, no change
		testOutcomeService.applyTestResultToTestOutcome(outcome, result)
		fetchedOutcome = TestOutcome.get(outcomeId)
		assertEquals "Wrong result", result, fetchedOutcome.testResult

		// no error should occur:
		testOutcomeService.applyTestResultToTestOutcome(outcome, null)
	}


	void testApplyBugParametersToTestOutcome() {
		Project proj = to.project
		dataService.saveDomainObject proj
		TestCase tc = to.getTestCase(proj)
		dataService.saveDomainObject tc

		TestRun run = to.getTestRun(proj)
		dataService.saveDomainObject run

		TestOutcome outcome = to.getTestOutcome(tc, run)
		dataService.saveDomainObject outcome

		testOutcomeService.applyBugParametersToTestOutcome(null, null)

		// no bug params
		testOutcomeService.applyBugParametersToTestOutcome(outcome, [:])

		testOutcomeService.applyBugParametersToTestOutcome(outcome, [foo: "bar"])
	}


	void testOutcomeProperty() {
		Project proj = to.project
		dataService.saveDomainObject proj
		TestCase tc = to.getTestCase(proj)
		dataService.saveDomainObject tc

		TestRun run = to.getTestRun(proj)
		dataService.saveDomainObject run

		TestOutcome outcome = to.getTestOutcome(tc, run)
		outcome.addToTestProperties(to.testOutcomeProperty)
		outcome.addToTestProperties(to.testOutcomeProperty)
		dataService.saveDomainObject outcome

		TestOutcome fetchedOutcome = outcome.get(outcome.id)
		assertEquals "Wrong number of test properties", 2, fetchedOutcome.testProperties?.size()

		assertEquals "Wrong first property name", outcome.testProperties[0].name, fetchedOutcome.testProperties[0].name
		assertEquals "Wrong first property value", outcome.testProperties[0].value, fetchedOutcome.testProperties[0].value
		assertEquals "Wrong second property name", outcome.testProperties[1].name, fetchedOutcome.testProperties[1].name
		assertEquals "Wrong second property value", outcome.testProperties[1].value, fetchedOutcome.testProperties[1].value
	}


	void testOutcomeLinks() {
		Project proj = to.project
		dataService.saveDomainObject proj
		TestCase tc = to.getTestCase(proj)
		dataService.saveDomainObject tc

		TestRun run = to.getTestRun(proj)
		dataService.saveDomainObject run

		TestOutcome outcome = to.getTestOutcome(tc, run)
		outcome.addToLinks(to.testOutcomeLink)
		outcome.addToLinks(to.testOutcomeLink)
		dataService.saveDomainObject outcome

		TestOutcome fetchedOutcome = outcome.get(outcome.id)
		assertEquals "Wrong number of test links", 2, fetchedOutcome.links?.size()

		assertEquals "Wrong first link url", outcome.links[0].url, fetchedOutcome.links[0].url
		assertEquals "Wrong first link description", outcome.links[0].description, fetchedOutcome.links[0].description

		assertEquals "Wrong second link url", outcome.links[1].url, fetchedOutcome.links[1].url
		assertEquals "Wrong second link description", outcome.links[1].description, fetchedOutcome.links[1].description
	}

	
	def assertAnalysisEquals(TestOutcome source, TestOutcome target) {
		assertAnalysisEquals(source, target, ["analysisState", "bug", "owner", "note"])
	}

	def assertAnalysisEquals(TestOutcome source, TestOutcome target, fields) {
		fields.each { field ->
			assertEquals "Wrong $field", source.getProperty(field), target.getProperty(field)
		}
	}



}
