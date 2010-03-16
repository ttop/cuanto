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

		def outcomes = testRunService.getOutcomesForTestRun(testRunOne, [includeIgnored: false])

		assertEquals("Wrong number of outcomes", numTests, outcomes.size())
		for (outcome in outcomes) {
			assertEquals("Wrong testRun", testRunOne, outcome.testRun)
		}

		outcomes = testRunService.getOutcomesForTestRun(testRunTwo, [includeIgnored: false])

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

		def abOutcomes = testRunService.getOutcomesForTestRun(testRun, "a.b", null, false)
		assertEquals("Wrong number of a.b outcomes", 20, abOutcomes.size())

		def abcOutcomes = testRunService.getOutcomesForTestRun(testRun, 'a.b.c', null, false)
		assertEquals("Wrong number of a.b.c outcomes", 10, abcOutcomes.size())

		def xyzOutcomes = testRunService.getOutcomesForTestRun(testRun, "x.y.z", null, false)
		assertEquals("Wrong number of x.y.z outcomes", 9, xyzOutcomes.size())
	}


	void testGetNewFailures() {
		Project proj = to.getProject()
		proj.save()

		def numCases = 10
		def testCases = []
		for (x in 1..numCases) {
			TestCase tc = to.getTestCase(proj)
			dataService.saveDomainObject tc
			testCases += tc
		}

		// create 2 test Runs
		TestRun testRunOne = to.getTestRun(proj)
		testRunOne.save()
		sleep 1000
		TestRun testRunTwo = to.getTestRun(proj)
		testRunTwo.save()


		[testRunOne, testRunTwo].each {testRun ->
			testCases.each {tc ->
				TestOutcome outcome = to.getTestOutcome(tc, testRun)
				outcome.duration = 1
				dataService.saveDomainObject outcome
			}
		}

		TestOutcomeQueryFilter outcomeFilter = new TestOutcomeQueryFilter()
		outcomeFilter.testRun = testRunTwo
		outcomeFilter.isFailure = true

		// results are identical
		def testRunOneOutcomes = testRunService.getOutcomesForTestRun(testRunOne, [includeIgnored: true])
		def testRunTwoOutcomes = testRunService.getOutcomesForTestRun(testRunTwo, [includeIgnored: true])
		assertEquals("No failures should've been found", 0, testOutcomeService.getNewFailures(outcomeFilter).size())

		// one failure in old testRun, none in new testRun
		testRunOneOutcomes[0].testResult = dataService.result("fail")
		testRunOneOutcomes[0].save(flush: true)
		assertEquals("No failures should've been found", 0, testOutcomeService.getNewFailures(outcomeFilter).size())


		// same test case failed in both testRuns
		testRunTwoOutcomes[0].testResult = dataService.result("fail")
		testRunTwoOutcomes[0].save(flush: true)
		assertEquals("No failures should've been found", 0, testOutcomeService.getNewFailures(outcomeFilter).size())


		// one failures in new testRun
		testRunTwoOutcomes[2].testResult = dataService.result("fail")
		testRunTwoOutcomes[2].save(flush: true)
		failures = testOutcomeService.getNewFailures(outcomeFilter)
		assertEquals("One failure should've been found", 1, failures.size())
		assertEquals "Wrong failure outcome returned", testRunTwoOutcomes[2], failures[0]

		// two failures in new testRun
		testRunTwoOutcomes[8].testResult = dataService.result("fail")
		testRunTwoOutcomes[8].save(flush: true)
		failures = testOutcomeService.getNewFailures(outcomeFilter)
		assertEquals("Two failure should've been found", 2, failures.size())
		assertTrue("Expected outcome not found", failures.contains(testRunTwoOutcomes[2]))
		assertTrue("Expected outcome not found", failures.contains(testRunTwoOutcomes[8]))

		assertEquals "wrong number of total failures", 3,
			dataService.countTestOutcomes(new TestOutcomeQueryFilter(testRun: testRunTwo, isFailure: true))
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
		assertEquals createdBugId, newTitleBug.id
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

		def targetRun = testRuns[0]
		def outs = dataService.getTestOutcomesByTestRun(targetRun, null, null, null)
		assertEquals "Wrong number of outcomes", numTestCases, outs.size()
		outs.each {outcome ->
			assertEquals "Wrong test run for outcome", targetRun, outcome.testRun
		}

		outs = dataService.getTestOutcomesByTestRun(targetRun, null, null, [max: 10, offset: 0], false)
		assertEquals "Wrong number of outcomes", numTestCases - 1, outs.size()
		outs.each {outcome ->
			assertEquals "Wrong test run for outcome", targetRun, outcome.testRun
		}

		outs = dataService.getTestOutcomesByTestRun(targetRun, null, null, [max: 10, offset: 0], true)
		assertEquals "Wrong number of outcomes", numTestCases, outs.size()
		outs.each {outcome ->
			assertEquals "Wrong test run for outcome", targetRun, outcome.testRun
		}

		shouldFail(IllegalArgumentException) {
			dataService.getTestOutcomesByTestRun(targetRun, null, "foo", null)
		}

		outs = dataService.getTestOutcomesByTestRun(targetRun, null, null, [max: 3, offset: 1], false)
		assertEquals "Wrong number of outcomes", 3, outs.size()
		outs.each {outcome ->
			assertEquals "Wrong test run for outcome", targetRun, outcome.testRun
		}

		outs = dataService.getTestOutcomesByTestRun(targetRun, null, "desc", [max: 3, offset: 1], false)
		assertEquals "Wrong number of outcomes", 3, outs.size()
		outs.each {outcome ->
			assertEquals "Wrong test run for outcome", targetRun, outcome.testRun
		}


		outs = dataService.getTestOutcomesByTestRun(targetRun, null, null, [max: 3, offset: 1], true)
		assertEquals "Wrong number of outcomes", 3, outs.size()
		outs.each {outcome ->
			assertEquals "Wrong test run for outcome", targetRun, outcome.testRun
		}

		outs = dataService.getTestOutcomesByTestRun(targetRun, null, null, [max: 3, offset: 1])
		assertEquals "Wrong number of outcomes", 3, outs.size()
		outs.each {outcome ->
			assertEquals "Wrong test run for outcome", targetRun, outcome.testRun
		}

		outs = dataService.getTestOutcomesByTestRun(targetRun, null, null, null)
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
	

	def assertAnalysisEquals(TestOutcome source, TestOutcome target) {
		assertAnalysisEquals(source, target, ["analysisState", "bug", "owner", "note"])
	}

	def assertAnalysisEquals(TestOutcome source, TestOutcome target, fields) {
		fields.each { field ->
			assertEquals "Wrong $field", source.getProperty(field), target.getProperty(field)
		}
	}



}
