package cuanto

import cuanto.test.TestObjects
import org.springframework.orm.hibernate3.HibernateSystemException

class DataServiceTests extends GroovyTestCase {

	def dataService
	def testRunService
	def testOutcomeService
	def initializationService
	def statisticService

	TestObjects to

	def static Long SYSTEM_SLEEP;
	static {
		if (System.properties['os.name'].toLowerCase().contains("windows")) {
			SYSTEM_SLEEP = 1000;
		} else {
			SYSTEM_SLEEP = 50;
		}
	}


	@Override
	void setUp() {
		initializationService.initializeAll()
		to = new TestObjects()
		to.dataService = dataService
	}


	void testGetTestCaseHistory() {
		Project proj = to.project
		proj.testType = TestType.findByName("JUnit")
		proj.save()

		TestCase tc = to.getTestCase(proj)
		tc.packageName = "a.b.c"
		dataService.saveDomainObject tc

		def numOutcomes = 10

		for (x in 1..numOutcomes) {
			TestRun testRun = to.getTestRun(proj)

			if (!testRun.save()) {
				reportError testRun
			}

			TestOutcome outcome = to.getTestOutcome(tc, testRun)
			outcome.duration = 1
			if (x == 2) {
				outcome.testResult = dataService.result("fail")
			} else if (x == 5) {
				outcome.testResult = dataService.result("error")
			} else if (x == 8) {
				outcome.testResult = dataService.result("ignore")
			}

			dataService.saveDomainObject outcome

			sleep(SYSTEM_SLEEP)
		}

		def outcomes = dataService.getTestOutcomeHistory(tc, 0, numOutcomes, "testRun.dateExecuted", "desc")
		assertEquals "wrong number of outcomes", numOutcomes, outcomes.size()

		for (indx in 1..outcomes.size - 1) {
			def valOne = outcomes[indx].testRun.dateExecuted
			def valTwo = outcomes[indx - 1].testRun.dateExecuted
			assertTrue "outcomes out-of-order", valOne < valTwo
		}

		outcomes = dataService.getTestOutcomeHistory(tc, 0, numOutcomes, null, "desc")
		assertEquals "Wrong number of outcomes", numOutcomes, outcomes.size()
		outcomes = dataService.getTestOutcomeHistory(tc, 0, numOutcomes, "testRun.dateExecuted", null)
		assertEquals "Wrong number of outcomes", numOutcomes, outcomes.size()

	}


	void testGetTestCaseFailureHistory() {
		Project proj = to.project
		proj.testType = TestType.findByName("JUnit")
		proj.save()

		TestCase tc = to.getTestCase(proj)
		tc.packageName = "a.b.c"
		dataService.saveDomainObject tc

		def numOutcomes = 20

		for (x in 1..numOutcomes) {
			TestRun testRun = to.getTestRun(proj)

			if (!testRun.save()) {
				reportError testRun
			}

			TestOutcome outcome = to.getTestOutcome(tc, testRun)
			outcome.duration = 1
			if (x == 2 || x == 9) {
				outcome.testResult = dataService.result("fail")
			} else if (x == 5 || x == 19) {
				outcome.testResult = dataService.result("error")
			} else if (x == 8) {
				outcome.testResult = dataService.result("ignore")
			}

			dataService.saveDomainObject outcome
			sleep(SYSTEM_SLEEP)
		}

		def outcomes = dataService.getTestOutcomeFailureHistory(tc, 0, numOutcomes, "testRun.dateExecuted", "desc")
		assertEquals "wrong number of outcomes", 4, outcomes.size()

		for (indx in 1..outcomes.size() - 1) {
			assertTrue "outcomes not in date order",
				outcomes[indx].testRun.dateExecuted < outcomes[indx - 1].testRun.dateExecuted
		}

		outcomes = dataService.getTestOutcomeFailureHistory(tc, 0, numOutcomes, null, "desc")
		assertEquals "wrong number of outcomes", 4, outcomes.size()
		outcomes = dataService.getTestOutcomeFailureHistory(tc, 0, numOutcomes, "testRun.dateExecuted", null)
		assertEquals "wrong number of outcomes", 4, outcomes.size()
	}


	public void testSummarizeAnalysis() {
		Project proj = to.project
		proj.testType = TestType.findByName("JUnit")
		proj.save()

		TestRun testRun = to.getTestRun(proj)
		if (!testRun.save()) {
			reportError testRun
		}

		TestCase tc = to.getTestCase(proj)
		tc.packageName = "a.b.c"
		dataService.saveDomainObject tc

		TestOutcome outcome

		for (x in 1..8) {
			outcome = to.getTestOutcome(tc, testRun)
			outcome.analysisState = null
			outcome.testResult = dataService.result("pass")
			dataService.saveDomainObject outcome
		}

		outcome = to.getTestOutcome(tc, testRun)
		outcome.testResult = dataService.result("fail")
		outcome.analysisState = dataService.getAnalysisStateByName("Unanalyzed")
		dataService.saveDomainObject outcome

		outcome = to.getTestOutcome(tc, testRun)
		outcome.testResult = dataService.result("error")
		outcome.analysisState = dataService.getAnalysisStateByName("Unanalyzed")
		dataService.saveDomainObject outcome


		outcome = to.getTestOutcome(tc, testRun)
		outcome.testResult = dataService.result("fail")
		outcome.analysisState = dataService.getAnalysisStateByName("Bug")
		dataService.saveDomainObject outcome

		outcome = to.getTestOutcome(tc, testRun)
		outcome.testResult = dataService.result("error")
		outcome.analysisState = dataService.getAnalysisStateByName("Bug")
		dataService.saveDomainObject outcome

		outcome = to.getTestOutcome(tc, testRun)
		outcome.testResult = dataService.result("fail")
		outcome.analysisState = dataService.getAnalysisStateByName("Environment")
		dataService.saveDomainObject outcome

		outcome = to.getTestOutcome(tc, testRun)
		outcome.testResult = dataService.result("error")
		outcome.analysisState = dataService.getAnalysisStateByName("Environment")
		dataService.saveDomainObject outcome

		outcome = to.getTestOutcome(tc, testRun)
		outcome.testResult = dataService.result("fail")
		outcome.analysisState = dataService.getAnalysisStateByName("Harness")
		dataService.saveDomainObject outcome

		outcome = to.getTestOutcome(tc, testRun)
		outcome.testResult = dataService.result("error")
		outcome.analysisState = dataService.getAnalysisStateByName("Harness")
		dataService.saveDomainObject outcome

		outcome = to.getTestOutcome(tc, testRun)
		outcome.testResult = dataService.result("fail")
		outcome.analysisState = dataService.getAnalysisStateByName("No Repro")
		dataService.saveDomainObject outcome

		outcome = to.getTestOutcome(tc, testRun)
		outcome.testResult = dataService.result("error")
		outcome.analysisState = dataService.getAnalysisStateByName("No Repro")
		dataService.saveDomainObject outcome

		outcome = to.getTestOutcome(tc, testRun)
		outcome.testResult = dataService.result("fail")
		outcome.analysisState = dataService.getAnalysisStateByName("Other")
		dataService.saveDomainObject outcome

		outcome = to.getTestOutcome(tc, testRun)
		outcome.testResult = dataService.result("error")
		outcome.analysisState = dataService.getAnalysisStateByName("Other")
		dataService.saveDomainObject outcome

		Map analysisStats = dataService.getAnalysisStateStats(testRun)
		assertEquals "2", analysisStats.bug
		assertEquals "2", analysisStats.environment
		assertEquals "2", analysisStats.harness
		assertEquals "2", analysisStats.'no repro'
		assertEquals "2", analysisStats.other
	}


	public void testGetTestOutcomes() {
		Project proj = to.project
		proj.testType = TestType.findByName("JUnit")
		proj.save()

		TestRun testRun = to.getTestRun(proj)
		if (!testRun.save()) {
			reportError testRun
		}

		TestCase tc = to.getTestCase(proj)
		tc.packageName = "a.b.c"
		dataService.saveDomainObject tc

		def outcomes = []

		TestOutcome outcome

		for (x in 1..8) {
			outcome = to.getTestOutcome(tc, testRun)
			outcome.analysisState = null
			outcome.testResult = dataService.result("pass")
			dataService.saveDomainObject outcome
			outcomes << outcome
		}

		def idsToGet = outcomes[0..3].collect {out ->
			out.id
		}

		def fetchedOutcomes = dataService.getTestOutcomes(idsToGet)
		assertEquals "Wrong number of outcomes retrieved", idsToGet.size(), fetchedOutcomes.size()
		fetchedOutcomes.eachWithIndex {it, indx ->
			assertEquals "Wrong id", idsToGet[indx], it.id
		}
	}


	public void testGetTestOutcomeAnalyses() {
		Project proj = to.project
		proj.testType = TestType.findByName("JUnit")
		proj.save()

		TestCase tc = to.getTestCase(proj)
		dataService.saveDomainObject tc

		def outcomes = []

		for (x in 1..8) {
			TestRun testRun = to.getTestRun(proj)
			if (!testRun.save()) {
				reportError testRun
			}
			def outcome = to.getTestOutcome(tc, testRun)
			outcome.analysisState = null
			outcome.testResult = dataService.result("pass")
			dataService.saveDomainObject outcome

			outcomes << outcome
			sleep(SYSTEM_SLEEP)
		}

		outcomes[1].testResult = dataService.result("fail")
		outcomes[2].testResult = dataService.result("error")
		outcomes[4].testResult = dataService.result("error")
		outcomes[5].testResult = dataService.result("fail")
		outcomes[6].testResult = dataService.result("error")
		outcomes[7].testResult = dataService.result("fail")

		outcomes[1].analysisState = dataService.getAnalysisStateByName("Investigate")
		outcomes[2].analysisState = dataService.getAnalysisStateByName("Investigate")
		outcomes[4].analysisState = dataService.getAnalysisStateByName("Unanalyzed")
		outcomes[5].analysisState = dataService.getAnalysisStateByName("Unanalyzed")
		outcomes[6].analysisState = dataService.getAnalysisStateByName("No Repro")
		outcomes[7].analysisState = dataService.getAnalysisStateByName("Bug")

		outcomes.each {out ->
			if (!out.save()) {
				reportError out
			}
		}

		def outcomesWithAnalysis = dataService.getTestOutcomeAnalyses(tc)
		assertEquals("Wrong number of analyses", 2, outcomesWithAnalysis.size())
		assertEquals("Wrong analysis", outcomes[7], outcomesWithAnalysis[0])
		assertEquals("Wrong analysis", outcomes[6], outcomesWithAnalysis[1])
	}


	public void testGetExistingBugWithUrl() {
		def bugs = []
		1.upto(3) {
			bugs << new Bug(title: to.wordGen.getCamelWords(3), url: "http://" + to.wordGen.getCamelWords(3))
		}
		bugs.each {bug ->
			if (!bug.save()) {
				reportError bug
			}
		}
		assertEquals "Wrong number of bugs saved", 3, Bug.list().size()
		bugs.each {bug ->
			def foundBug = dataService.getBug(null, bug.url)
			assertNotNull "bug not found", foundBug
			assertEquals "Wrong bug title", bug.title, foundBug.title
			assertEquals "Wrong url", bug.url, foundBug.url
		}

		bugs.each {bug ->
			def foundBug = dataService.getBug("", bug.url)
			assertNotNull "bug not found", foundBug
			assertEquals "Wrong bug title", bug.title, foundBug.title
			assertEquals "Wrong url", bug.url, foundBug.url
		}
	}


	public void testGetExistingBugWithOnlyTitle() {
		def bugs = []
		1.upto(3) {
			bugs << new Bug(title: to.wordGen.getCamelWords(3))
		}
		bugs.each {bug ->
			if (!bug.save()) {
				reportError bug
			}
		}
		assertEquals "Wrong number of bugs saved", 3, Bug.list().size()
		bugs.each {bug ->
			def foundBug = dataService.getBug(bug.title, null)
			assertNotNull "bug not found", foundBug
			assertEquals "Wrong bug title", bug.title, foundBug.title
			assertEquals "Wrong url", bug.url, foundBug.url
		}

		bugs.each {bug ->
			def foundBug = dataService.getBug(bug.title, "")
			assertNotNull "bug not found", foundBug
			assertEquals "Wrong bug title", bug.title, foundBug.title
			assertEquals "Wrong url", bug.url, foundBug.url
		}
	}


	public void testGetNonExistingBugWithUrlOnly() {
		def bugs = []
		1.upto(6) {
			bugs << new Bug(url: "http://" + to.wordGen.getCamelWords(3))
		}
		bugs[0..2].each {bug ->
			def foundBug = dataService.getBug(bug.title, bug.url)
			assertNotNull "bug not found", foundBug
			assertEquals "Wrong bug title", bug.title, foundBug.title
			assertEquals "Wrong url", bug.url, foundBug.url
		}
		assertEquals "Wrong number of bugs saved", 3, Bug.list().size()

		bugs[3..5].each {bug ->
			def foundBug = dataService.getBug("", bug.url)
			assertNotNull "bug not found", foundBug
			assertEquals "Wrong bug title", bug.title, foundBug.title
			assertEquals "Wrong url", bug.url, foundBug.url
		}

		assertEquals "Wrong number of bugs saved", 6, Bug.list().size()
	}


	public void testGetNonExistingBugWithTitleOnly() {
		def bugs = []
		1.upto(6) {
			bugs << new Bug(title: to.wordGen.getCamelWords(3))
		}
		bugs[0..2].each {bug ->
			def foundBug = dataService.getBug(bug.title, bug.url)
			assertNotNull "bug not found", foundBug
			assertEquals "Wrong bug title", bug.title, foundBug.title
			assertEquals "Wrong url", bug.url, foundBug.url
		}
		assertEquals "Wrong number of bugs saved", 3, Bug.list().size()

		bugs[3..5].each {bug ->
			def foundBug = dataService.getBug(bug.title, "")
			assertNotNull "bug not found", foundBug
			assertEquals "Wrong bug title", bug.title, foundBug.title
			assertEquals "Wrong url", bug.url, foundBug.url
		}

		assertEquals "Wrong number of bugs saved", 6, Bug.list().size()
	}


	public void testGetBugWithoutTitleOrUrl() {
		def expectedMsg = "Neither a bug title or URL was provided"
		def msg = shouldFail(CuantoException) {
			dataService.getBug(null, null)
		}
		assertEquals "Wrong exception message", expectedMsg, msg

		msg = shouldFail(CuantoException) {
			dataService.getBug("", "")
		}
		assertEquals "Wrong exception message", expectedMsg, msg
	}


	public void testDeleteTestRunWithStats() {
		Project proj = to.project
		proj.testType = TestType.findByName("JUnit")
		proj.save()

		TestCase tc = to.getTestCase(proj)
		dataService.saveDomainObject tc

		def outcomes = []
		def testRun = to.getTestRun(proj)

		if (!testRun.save()) {
			reportError testRun
		}

		for (x in 1..8) {
			def outcome = to.getTestOutcome(tc, testRun)
			outcome.analysisState = null
			outcome.testResult = dataService.result("pass")
			dataService.saveDomainObject outcome
			outcomes << outcome
		}

		TestRunStats stats = new TestRunStats(passed: 8, failed: 0, tests: 8)
        stats.testRun = testRun
        dataService.saveDomainObject stats

		if (!testRun.save()) {
			reportError testRun
		}

		testRunService.deleteTestRun(testRun)

		assertNull "Test run not deleted", TestRun.get(testRun.id)
		assertEquals "Test outcomes not deleted", 0, TestOutcome.list().size()
		assertEquals "Test run statistics not deleted", 0, TestRunStats.list().size()
	}


	public void testDeleteTestRunWithoutStats() {
		Project proj = to.project
		proj.testType = TestType.findByName("JUnit")
		proj.save()

		TestCase tc = to.getTestCase(proj)
		dataService.saveDomainObject tc

		def outcomes = []
		def testRun = to.getTestRun(proj)
		if (!testRun.save()) {
			reportError testRun
		}

		for (x in 1..8) {
			def outcome = to.getTestOutcome(tc, testRun)
			outcome.analysisState = null
			outcome.testResult = dataService.result("pass")
			dataService.saveDomainObject outcome
			outcomes << outcome
		}

		if (!testRun.save()) {
			reportError testRun
		}

		testRunService.deleteTestRun(testRun)

		assertNull "Test run not deleted", TestRun.get(testRun.id)
		assertEquals "Test outcomes not deleted", 0, TestOutcome.list().size()
		assertEquals "Test run statistics not deleted", 0, TestRunStats.list().size()
	}


	void testGetProject() {
		Project proj = to.project
		proj.save()
		assertNotNull proj.id
		assertEquals proj.id, dataService.getProject(proj.id).id
		assertNull dataService.getProject("foo")
	}


	void testGetTestRunsByProject() {
		Project targetProject = to.project
		dataService.saveDomainObject(targetProject)
		Project otherProject = to.project
		dataService.saveDomainObject(otherProject)

		def targetTestRuns = []
		1.upto(4) {
			def tr = to.getTestRun(targetProject)
			dataService.saveDomainObject(tr)
			statisticService.queueTestRunStats(tr)
			targetTestRuns << tr
		}

		1.upto(4) {
			def tr = to.getTestRun(otherProject)
			dataService.saveDomainObject(tr)
			statisticService.queueTestRunStats(tr)
		}

		def fetchedTestRuns = dataService.getTestRunsByProject(targetProject,
			[sort: "t.dateExecuted", order: "asc", max: 3, offset: 0])

		assertEquals "Wrong number of test runs returned", 3, fetchedTestRuns.size()

		fetchedTestRuns.each { run ->
			assertTrue "incorrect test run found", targetTestRuns.any { it.id == run.id}
		}

		fetchedTestRuns = dataService.getTestRunsByProject(targetProject)
		assertEquals "Wrong number of test runs returned", 4, fetchedTestRuns.size()

		fetchedTestRuns.each { run ->
			assertTrue "incorrect test run found", targetTestRuns.any { it.id == run.id}
		}

		assertEquals "Wrong test run count", 4, TestRun.countByProject(targetProject)
	}


	void testAddTestCases() {
		Project proj = to.project
		dataService.saveDomainObject(proj)
		assertEquals 0, TestCase.list().size()

		def testCases = []
		1.upto(10) {
			testCases << to.getTestCase(proj)
		}

		dataService.addTestCases(proj, testCases)

		assertEquals "Wrong number of total test cases", 10, TestCase.list().size()
		assertEquals "Wrong number of project test cases", 10, dataService.countTestCases(proj)
	}


	void testSaveTestOutcomes() {
		Project proj = to.project
		dataService.saveDomainObject(proj)
		assertEquals 0, TestCase.list().size()

		def testCases = []
		1.upto(10) {
			testCases << to.getTestCase(proj)
		}

		dataService.addTestCases(proj, testCases)

		TestRun testRun = to.getTestRun(proj)
		dataService.saveDomainObject(testRun)

		def outcomes = []
		testCases.each { testCase ->
			outcomes << to.getTestOutcome(testCase, testRun)
			outcomes << to.getTestOutcome(testCase, testRun)
		}

		dataService.saveTestOutcomes(outcomes)

		assertEquals "Wrong number of test outcomes", outcomes.size(), TestOutcome.list().size()
		def fetched = TestOutcome.findAllByTestRun(testRun)
		assertEquals "Wrong number of test outcomes", outcomes.size(), fetched.size()

		assertEquals "Wrong total outcome count for test case", 2,
			dataService.countTestOutcomes(new TestOutcomeQueryFilter(testCase: testCases[0] as TestCase))
		assertEquals "Wrong Test Case total for project", testCases.size(), dataService.countTestCases(proj)
	}


	void testGetAllTestResultsMap() {
		Map resultMap = dataService.allTestResultsMap
		assertTrue "No TestResults", resultMap.size() > 0
		assertEquals "Wrong total", TestResult.list().size(), resultMap.size()
		TestResult.list().each { result ->
			resultMap.values().contains(result)
			resultMap.keySet().contains(result.name.toLowerCase())
		}
	}


	void testGetAllAnalysisStatesMap() {
		Map analysisStateMap = dataService.allAnalysisStatesMap
		assertTrue "No AnalysisStates", AnalysisState.list().size() > 0
		assertEquals "Wrong total", AnalysisState.list().size(), analysisStateMap.size()
	}


	void testDeleteUnusedBug() {
		def unusedBug = new Bug(title: to.wordGen.getCamelWords(3), url: "http://" + to.wordGen.getCamelWords(3))
		dataService.saveDomainObject(unusedBug)
		assertNotNull "No Bug ID", unusedBug.id
		assertEquals "Wrong bug total", 1, Bug.list().size()
		dataService.deleteBugIfUnused(unusedBug)
		assertEquals "Wrong bug total", 0, Bug.list().size()
	}


	void testDeleteBugUsedOnce() {
		def usedOnceBug = new Bug(title: to.wordGen.getCamelWords(3), url: "http://" + to.wordGen.getCamelWords(3))
		dataService.saveDomainObject(usedOnceBug)
		assertNotNull "No Bug ID", usedOnceBug.id
		assertEquals "Wrong bug total", 1, Bug.list().size()

		def proj = to.project
		dataService.saveDomainObject(proj)

		def testCase = to.getTestCase(proj)
		dataService.saveDomainObject testCase

		def testRun = to.getTestRun(proj)
		dataService.saveDomainObject(testRun)

		def outcome = to.getTestOutcome(testCase, testRun)
		outcome.bug = usedOnceBug;
		dataService.saveDomainObject(outcome)

		dataService.deleteBugIfUnused(usedOnceBug)
		assertNotNull "No Bug ID", usedOnceBug.id
		assertEquals "Wrong bug total", 1, Bug.list().size()
		assertEquals "bug deleted?", outcome.bug, usedOnceBug

		// now don't use the bug
		outcome.bug = null
		dataService.saveDomainObject(outcome)
		dataService.deleteBugIfUnused(usedOnceBug)
		assertEquals "Wrong bug total", 0, Bug.list().size()
	}


	void testDeleteBugUsedTwice() {
		def usedTwiceBug = new Bug(title: to.wordGen.getCamelWords(3), url: "http://" + to.wordGen.getCamelWords(3))
		dataService.saveDomainObject(usedTwiceBug)
		assertNotNull "No Bug ID", usedTwiceBug.id
		assertEquals "Wrong bug total", 1, Bug.list().size()

		def proj = to.project
		dataService.saveDomainObject(proj)

		def testCases = []
		1.upto(2) {
			def testCase = to.getTestCase(proj)
			dataService.saveDomainObject testCase
			testCases << testCase
		}

		def testRun = to.getTestRun(proj)
		dataService.saveDomainObject(testRun)

		def outcomes = []
		testCases.each { testCase ->
			def outcome = to.getTestOutcome(testCase, testRun)
			outcome.bug = usedTwiceBug;
			dataService.saveDomainObject(outcome)
			outcomes << outcome
		}

		dataService.deleteBugIfUnused(usedTwiceBug)
		assertNotNull "No Bug ID", usedTwiceBug.id
		assertEquals "Wrong bug total", 1, Bug.list().size()

		// get rid of one usage
		outcomes[0].bug = null
		dataService.saveDomainObject(outcomes[0])
		dataService.deleteBugIfUnused(usedTwiceBug)
		assertNotNull "No Bug ID", usedTwiceBug.id
		assertEquals "Wrong bug total", 1, Bug.list().size()

		// get rid of other usage
		outcomes[1].bug = null
		dataService.saveDomainObject(outcomes[1])
		dataService.deleteBugIfUnused(usedTwiceBug)
		assertEquals "Wrong bug total", 0, Bug.list().size()

		dataService.deleteBugIfUnused(null)
	}


	void testSaveUnvalidatedDomainObject() {
		def groupName = to.wordGen.getCamelWords(3)
		def groupOne = new ProjectGroup(name: groupName)
		dataService.saveDomainObject groupOne

		shouldFail(CuantoException) {
			def dupeGroup = new ProjectGroup(name: groupName)
			dataService.saveDomainObject dupeGroup
		}
	}


	void testGetTestOutcome() {
		Project proj = to.project
		dataService.saveDomainObject(proj)
		assertEquals 0, TestCase.list().size()

		def testCases = []
		1.upto(10) {
			testCases << to.getTestCase(proj)
		}

		dataService.addTestCases(proj, testCases)

		TestRun testRun = to.getTestRun(proj)
		dataService.saveDomainObject(testRun)

		def outcomes = []
		testCases.each { testCase ->
			outcomes << to.getTestOutcome(testCase, testRun)
			outcomes << to.getTestOutcome(testCase, testRun)
		}

		dataService.saveTestOutcomes(outcomes)

		outcomes.each { outcome ->
			assertEquals "Wrong outcome", outcome.id, dataService.getTestOutcome(outcome.id).id
			assertEquals "Wrong outcome by string", outcome.id,
				dataService.getTestOutcome(outcome.id.toString()).id
		}

		assertNull "Bad response for null outcome", dataService.getTestOutcome(null)
		assertNull "Bad response for blank outcome", dataService.getTestOutcome("")
	}


	void testGetAnalysisStateMethods() {
		def allStates = AnalysisState.list()
		allStates.each { state ->
			assertEquals "Wrong state", state.id, dataService.getAnalysisState(state.id).id
			assertEquals "Wrong state by string", state.id,
				dataService.getAnalysisState(state.id.toString()).id
		}

		def defaultState = AnalysisState.findByName("Unanalyzed")
		assertEquals "Wrong default state", defaultState, dataService.defaultAnalysisState
		assertEquals "There should be one default state", 1, AnalysisState.findAllByIsDefault(true).size()

		def bugState = AnalysisState.findByName("Bug")
		assertEquals "Wrong bug state", bugState, dataService.analysisStateForBug
		assertEquals "There should be one bug state", 1, AnalysisState.findAllByIsBug(true).size()
	}


	void testNewTestType() {
		def numTypes = TestType.list().size()
		def typeName = to.wordGen.getCamelWords(3)
		def testType = dataService.createTestType(typeName)
		assertEquals "Wrong number of test types after creation", numTypes + 1, TestType.list().size()
		assertEquals "New test type not found", testType, TestType.findByName(typeName)

		dataService.createTestType(dataService.getTestType("JUnit").name)
		assertEquals "Wrong number of test types", numTypes + 1, TestType.list().size()
	}


	void testGetFieldByFriendlyName() {
		assertEquals "Wrong field name", "testCase.fullName", dataService.getFieldByFriendlyName(null)
		assertEquals "Wrong field name", "testCase.fullName", dataService.getFieldByFriendlyName("")
		assertEquals "Wrong field name", "testCase.fullName", dataService.getFieldByFriendlyName("foobar")
		assertEquals "Wrong field name", "testCase.fullName", dataService.getFieldByFriendlyName("NAME")
		assertEquals "Wrong field name", "testCase.fullName", dataService.getFieldByFriendlyName("TestCase")
		assertEquals "Wrong field name", "testResult", dataService.getFieldByFriendlyName("result")
		assertEquals "Wrong field name", "analysisState.name", dataService.getFieldByFriendlyName("state")
		assertEquals "Wrong field name", "duration", dataService.getFieldByFriendlyName("duration")
		//assertEquals "Wrong field name", "bug.title", dataService.getFieldByFriendlyName("bug")
		assertEquals "Wrong field name", "owner", dataService.getFieldByFriendlyName("owner")
		assertEquals "Wrong field name", "note", dataService.getFieldByFriendlyName("note")
		assertEquals "Wrong field name", "testOutput", dataService.getFieldByFriendlyName("output")
	}


	void testGetMostRecentTestRunForProjectKey() {
		def project = to.project
		dataService.saveDomainObject project
		assertNull "No runs should've been returned", dataService.getMostRecentTestRunForProjectKey(project.projectKey)

		def mostRecent = to.getTestRun(project)
		dataService.saveDomainObject mostRecent
		assertEquals "Wrong test run", mostRecent, dataService.getMostRecentTestRunForProjectKey(project.projectKey)
		sleep(SYSTEM_SLEEP)

		mostRecent = to.getTestRun(project)
		dataService.saveDomainObject mostRecent
		assertEquals "Wrong test run", mostRecent, dataService.getMostRecentTestRunForProjectKey(project.projectKey)
		sleep(SYSTEM_SLEEP)

		mostRecent = to.getTestRun(project)
		dataService.saveDomainObject mostRecent
		assertEquals "Wrong test run", mostRecent, dataService.getMostRecentTestRunForProjectKey(project.projectKey)
		sleep(SYSTEM_SLEEP)

	}


	void testGetPreviousOutcome() {
		def project = to.project
		dataService.saveDomainObject project

		def testCase = to.getTestCase(project)
		dataService.saveDomainObject testCase

		def outcomes = []
		def testRuns = []
		1.upto(4) {
			def testRun = to.getTestRun(project)
			dataService.saveDomainObject testRun
			testRuns << testRun
			def testOutcome = to.getTestOutcome(testCase, testRun)
			dataService.saveDomainObject testOutcome
			outcomes << testOutcome
			if (it != 4) {
				sleep(SYSTEM_SLEEP)
			}
		}

		assertEquals "Wrong outcome returned", outcomes[2], dataService.getPreviousOutcome(outcomes[3])
		assertEquals "Wrong outcome returned", outcomes[1], dataService.getPreviousOutcome(outcomes[2])
		assertEquals "Wrong outcome returned", outcomes[0], dataService.getPreviousOutcome(outcomes[1])
	}

	void testGetNextOutcome() {
		def project = to.project
		dataService.saveDomainObject project

		def testCase = to.getTestCase(project)
		dataService.saveDomainObject testCase

		def outcomes = []
		def testRuns = []
		1.upto(4) {
			def testRun = to.getTestRun(project)
			dataService.saveDomainObject testRun
			testRuns << testRun
			def testOutcome = to.getTestOutcome(testCase, testRun)
			dataService.saveDomainObject testOutcome
			outcomes << testOutcome
			if (it != 4) {
				sleep(SYSTEM_SLEEP)
			}
		}

		assertEquals "Wrong outcome returned", outcomes[3], dataService.getNextOutcome(outcomes[2])
		assertEquals "Wrong outcome returned", outcomes[2], dataService.getNextOutcome(outcomes[1])
		assertEquals "Wrong outcome returned", outcomes[1], dataService.getNextOutcome(outcomes[0])
	}


	void testGetPreviousOutcomeFinishedAt() {
		def project = to.project
		dataService.saveDomainObject project

		def testCase = to.getTestCase(project)
		dataService.saveDomainObject testCase

		def outcomes = []
		1.upto(4) {
			def testOutcome = to.getTestOutcome(testCase, null)

			testOutcome.finishedAt = new Date()
			dataService.saveDomainObject testOutcome
			outcomes << testOutcome
			if (it != 4) {
				sleep(SYSTEM_SLEEP)
			}
		}

		assertEquals "Wrong outcome returned", outcomes[2], dataService.getPreviousOutcome(outcomes[3])
		assertEquals "Wrong outcome returned", outcomes[1], dataService.getPreviousOutcome(outcomes[2])
		assertEquals "Wrong outcome returned", outcomes[0], dataService.getPreviousOutcome(outcomes[1])
	}


	void testGetNextOutcomeFinishedAt() {
		def project = to.project
		dataService.saveDomainObject project

		def testCase = to.getTestCase(project)
		dataService.saveDomainObject testCase

		def outcomes = []
		1.upto(4) {
			def testOutcome = to.getTestOutcome(testCase, null)

			testOutcome.finishedAt = new Date()
			dataService.saveDomainObject testOutcome
			outcomes << testOutcome
			if (it != 4) {
				sleep(SYSTEM_SLEEP)
			}
		}

		assertEquals "Wrong outcome returned", outcomes[3], dataService.getNextOutcome(outcomes[2])
		assertEquals "Wrong outcome returned", outcomes[2], dataService.getNextOutcome(outcomes[1])
		assertEquals "Wrong outcome returned", outcomes[1], dataService.getNextOutcome(outcomes[0])
	}


	void testGetPreviousOutcomeStartedAt() {
		def project = to.project
		dataService.saveDomainObject project

		def testCase = to.getTestCase(project)
		dataService.saveDomainObject testCase

		def outcomes = []
		1.upto(4) {
			def testOutcome = to.getTestOutcome(testCase, null)
			testOutcome.startedAt = new Date()
			dataService.saveDomainObject testOutcome
			outcomes << testOutcome
			if (it != 4) {
				sleep(SYSTEM_SLEEP)
			}
		}

		assertEquals "Wrong outcome returned", outcomes[2], dataService.getPreviousOutcome(outcomes[3])
		assertEquals "Wrong outcome returned", outcomes[1], dataService.getPreviousOutcome(outcomes[2])
		assertEquals "Wrong outcome returned", outcomes[0], dataService.getPreviousOutcome(outcomes[1])
	}


	void testGetNextOutcomeStartedAt() {
		def project = to.project
		dataService.saveDomainObject project

		def testCase = to.getTestCase(project)
		dataService.saveDomainObject testCase

		def outcomes = []
		1.upto(4) {
			def testOutcome = to.getTestOutcome(testCase, null)
			testOutcome.startedAt = new Date()
			dataService.saveDomainObject testOutcome
			outcomes << testOutcome
			if (it != 4) {
				sleep(SYSTEM_SLEEP)
			}
		}

		assertEquals "Wrong outcome returned", outcomes[3], dataService.getNextOutcome(outcomes[2])
		assertEquals "Wrong outcome returned", outcomes[2], dataService.getNextOutcome(outcomes[1])
		assertEquals "Wrong outcome returned", outcomes[1], dataService.getNextOutcome(outcomes[0])
	}


	void testGetPreviousOutcomeDateCreated() {
		def project = to.project
		dataService.saveDomainObject project

		def testCase = to.getTestCase(project)
		dataService.saveDomainObject testCase

		def outcomes = []
		1.upto(4) {
			def testOutcome = to.getTestOutcome(testCase, null)
			dataService.saveDomainObject testOutcome
			outcomes << testOutcome
			if (it != 4) {
				sleep(SYSTEM_SLEEP)
			}
		}

		assertEquals "Wrong outcome returned", outcomes[2], dataService.getPreviousOutcome(outcomes[3])
		assertEquals "Wrong outcome returned", outcomes[1], dataService.getPreviousOutcome(outcomes[2])
		assertEquals "Wrong outcome returned", outcomes[0], dataService.getPreviousOutcome(outcomes[1])
	}


	void testGetNextOutcomeDateCreated() {
		def project = to.project
		dataService.saveDomainObject project

		def testCase = to.getTestCase(project)
		dataService.saveDomainObject testCase

		def outcomes = []
		1.upto(4) {
			def testOutcome = to.getTestOutcome(testCase, null)
			dataService.saveDomainObject testOutcome
			outcomes << testOutcome
			if (it != 4) {
				sleep(SYSTEM_SLEEP)
			}
		}

		assertEquals "Wrong outcome returned", outcomes[3], dataService.getNextOutcome(outcomes[2])
		assertEquals "Wrong outcome returned", outcomes[2], dataService.getNextOutcome(outcomes[1])
		assertEquals "Wrong outcome returned", outcomes[1], dataService.getNextOutcome(outcomes[0])
	}


	void testGetTestOutcomesForNextTestRun() {
		// create my project
		Project proj1 = to.project
		dataService.saveDomainObject(proj1)

		// create some other project
		Project proj2 = to.project
		dataService.saveDomainObject(proj2)

		// create test cases for my project
		def testCasesForProj1 = []
		1.upto(10) {
			testCasesForProj1 << to.getTestCase(proj1)
		}
		dataService.addTestCases(proj1, testCasesForProj1)

		// create test cases for other project
		def testCasesForProj2 = []
		1.upto(10) {
			testCasesForProj2 << to.getTestCase(proj2)
		}
		dataService.addTestCases(proj2, testCasesForProj2)

		// create a test run for proj 1
		def outcomesForRun1 = createNewTestRun(proj1, new Date(1000), testCasesForProj1)
		def testRun1 = outcomesForRun1[0].testRun

		// create a test run for proj 2
		createNewTestRun(proj2, new Date(2000), testCasesForProj2)

		// create the next test run for proj 1
		def outcomesForRun2 = createNewTestRun(proj1, new Date(3000), testCasesForProj1)
		def testRun2 = outcomesForRun2[0].testRun

		// create the next test run for proj 2
		createNewTestRun(proj2, new Date(4000), testCasesForProj2)

		// create the next test run for proj 1
		def outcomesForRun3 = createNewTestRun(proj1, new Date(5000), testCasesForProj1)
		def testRun3 = outcomesForRun3[0].testRun

		// create the next test run for proj 2
		createNewTestRun(proj2, new Date(6000), testCasesForProj2)

		// find outcomes for the run after testRun1
		def testRunAfterRun1 = dataService.getNextTestRun(testRun1)
		assertEquals "Unexpected TestRun.", testRun2.id, testRunAfterRun1.id
		assertEquals "Unexpected TestOutcomes for the next TestRun",
			outcomesForRun2, dataService.getTestOutcomesForTestRun(testRunAfterRun1.id, 1000, 0)

		// find outcomes for the run after testRun2
		def testRunAfterRun2 = dataService.getNextTestRun(testRun2)
		assertEquals "Unexpected TestRun.", testRun3.id, testRunAfterRun2.id
		assertEquals "Unexpected TestOutcomes for the next TestRun",
			outcomesForRun3, dataService.getTestOutcomesForTestRun(testRunAfterRun2.id, 1000, 0)

		// find outcomes for the run after testRun3
		def testRunAfterRun3 = dataService.getNextTestRun(testRun3)
		assertNull "The last TestRun in this project should not have any next TestRuns.", testRunAfterRun3
	}

	List<TestOutcome> createNewTestRun(Project proj, Date dateExecuted, List<TestCase> testCases) {
		TestRun testRun = to.getTestRun(proj)
		testRun.dateExecuted = dateExecuted
		dataService.saveDomainObject(testRun)

		def testOutcomes = []
		testCases.each { testCase ->
			testOutcomes << to.getTestOutcome(testCase, testRun)
			testOutcomes << to.getTestOutcome(testCase, testRun)
		}

		dataService.saveTestOutcomes(testOutcomes)
		return testOutcomes
	}


	def reportError(domainObj) {
		def errMsg = ""
		domainObj.errors.allErrors.each {
			errMsg += it.toString()
		}
		log.warning errMsg
		fail(errMsg)
	}
}
