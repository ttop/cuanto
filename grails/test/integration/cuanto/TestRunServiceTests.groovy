package cuanto

import cuanto.test.TestObjects
import cuanto.test.WordGenerator

class TestRunServiceTests extends GroovyTestCase {

	def dataService
	def initializationService
	def testRunService
	def testResultService
	TestObjects to
	WordGenerator wordGen = new WordGenerator()

	@Override
	void setUp() {
		initializationService.initializeAll()
		to = new TestObjects()
		to.dataService = dataService
	}



	void testCalculateTestRunTotals() {
		Project proj = to.getProject()
		proj.testType = TestType.findByName("JUnit")
		proj.save()

		def numCases = 11

		TestRun testRun = to.getTestRun(proj, "foobar")

		if (!testRun.save()) {
			dataService.reportSaveError testRun
		}

		for (x in 1..numCases) {
			TestCase tc = to.getTestCase(proj)
			tc.packageName = "a.b.c"

			if (!proj.addToTestCases(tc).save()) {
				dataService.reportSaveError proj
			}

			TestOutcome outcome = to.getTestOutcome(tc, testRun)
			outcome.duration = 1
			if (x == 2) {
				outcome.testResult = dataService.result("fail")
			} else if (x == 3) {
				outcome.testResult = dataService.result("error")
			} else if (x == 4) {
				outcome.testResult = dataService.result("ignore")
			}

			if (!testRun.addToOutcomes(outcome).save()) {
				dataService.reportSaveError testRun
			}
		}

		testRunService.calculateTestRunStats(testRun)

		assertNotNull "results not found", testRun.testRunStatistics
		TestRunStats result = testRun.testRunStatistics
		assertEquals "wrong total tests", 10, result.tests
		assertEquals "wrong failures", 2, result.failed
		assertEquals "wrong passed", 8, result.passed
		assertEquals "wrong avg duration", 1, result.averageDuration
		assertEquals "wrong total duration", 10, result.totalDuration
	}

	void testSearchByTestNote() {
		Project proj = to.project
		proj.testType = TestType.findByName("JUnit")
		if (!proj.save()) {
			dataService.reportSaveError proj
		}

		final int numTests = 10

		def testCases = []
		for (x in 1..numTests) {
			TestCase testCase = to.getTestCase(proj)
			if (!proj.addToTestCases(testCase).save()) {
				dataService.reportSaveError proj
			}
			testCases.add(testCase)
		}

		def testRunOne = to.getTestRun(proj, "mile1")
		testRunOne.save()

		for (testCase in testCases) {
			TestOutcome out = to.getTestOutcome(testCase, testRunOne)
			if (!testRunOne.addToOutcomes(out).save()) {
				dataService.reportSaveError testRunOne
			}
		}

		def testRunTwo = to.getTestRun(proj, "mile1")
		testRunTwo.save()

		for (testCase in testCases) {
			TestOutcome out = to.getTestOutcome(testCase, testRunTwo)
			if (!testRunTwo.addToOutcomes(out).save()) {
				dataService.reportSaveError testRunTwo
			}
		}

		def runOneOutcomes = testRunService.getOutcomesForTestRun(testRunOne, [includeIgnored: false])
		runOneOutcomes[0].note = "Pacific Ocean Blue"
		runOneOutcomes[0].save()
		runOneOutcomes[1].note = "Lost in the Pacific"
		runOneOutcomes[1].save()

		def runTwoOutcomes = testRunService.getOutcomesForTestRun(testRunTwo, [includeIgnored: false])
		runTwoOutcomes[0].note = "Pacific Lake Blue"
		runTwoOutcomes[0].save()
		runTwoOutcomes[1].note = "Found in the Pacific"
		runTwoOutcomes[1].save()

		def sort = "note"
		def order = "asc"
		def max = 10
		def offset = 0

		def runOneParams = ['sort': sort, 'order': order, 'max': max, 'offset': offset, 'id': testRunOne.id,
			'qry': 'note|Pacific']
		def runTwoParams = ['sort': sort, 'order': order, 'max': max, 'offset': offset, 'id': testRunTwo.id,
			'qry': 'note|Pacific']

		def count = testRunService.countTestOutcomesBySearch(runOneParams)
		assertEquals "Wrong count", 2, count
		count = testRunService.countTestOutcomesBySearch(runTwoParams)
		assertEquals "Wrong count", 2, count

		def searchResults = testRunService.searchTestOutcomes(runOneParams)
		assertEquals "Wrong number of outcomes returned", 2, searchResults.size()
		assertEquals "Wrong outcome", runOneOutcomes[1], searchResults[0]
		assertEquals "Wrong outcome", runOneOutcomes[0], searchResults[1]

		runOneParams['order'] = "desc"
		searchResults = testRunService.searchTestOutcomes(runOneParams)
		assertEquals "Wrong number of outcomes returned", 2, searchResults.size()
		assertEquals "Wrong outcome", runOneOutcomes[0], searchResults[0]
		assertEquals "Wrong outcome", runOneOutcomes[1], searchResults[1]
	}


	void testSearchByTestName() {
		Project proj = to.project
		proj.testType = TestType.findByName("JUnit")
		if (!proj.save()) {
			dataService.reportSaveError proj
		}

		final int numTests = 10

		def testCases = []
		for (x in 1..numTests) {
			TestCase testCase = to.getTestCase(proj)
			if (!proj.addToTestCases(testCase).save()) {
				dataService.reportSaveError proj
			}
			testCases.add(testCase)
		}

		def testRunOne = to.getTestRun(proj, "mile1")
		testRunOne.save()

		for (testCase in testCases) {
			TestOutcome out = to.getTestOutcome(testCase, testRunOne)
			if (!testRunOne.addToOutcomes(out).save()) {
				dataService.reportSaveError testRunOne
			}
		}

		def testRunTwo = to.getTestRun(proj, "mile1")
		testRunTwo.save()

		for (testCase in testCases) {
			TestOutcome out = to.getTestOutcome(testCase, testRunTwo)
			if (!testRunTwo.addToOutcomes(out).save()) {
				dataService.reportSaveError testRunTwo
			}
		}

		def runOneOutcomes = testRunService.getOutcomesForTestRun(testRunOne, [includeIgnored: false])
		runOneOutcomes[0].testCase.fullName = "a Pacific Ocean Blue"
		runOneOutcomes[0].testCase.save()
		runOneOutcomes[1].testCase.fullName = "b Lost in the Pacific"
		runOneOutcomes[1].testCase.save()

		def runTwoOutcomes = testRunService.getOutcomesForTestRun(testRunTwo, [includeIgnored: false])
		runTwoOutcomes[0].testCase.fullName = "a Pacific Lake Blue"
		runTwoOutcomes[0].testCase.save()
		runTwoOutcomes[1].testCase.fullName = "b Found in the Pacific"
		runTwoOutcomes[1].testCase.save()

		def runOneParams = ['sort': 'name', 'order': 'asc', 'max': 10, 'offset': 0, 'id': testRunOne.id,
			'qry': 'name|Pacific']
		def searchResults = testRunService.searchTestOutcomes(runOneParams)
		assertEquals "Wrong number of outcomes returned", 2, searchResults.size()
		assertEquals "Wrong outcome", runOneOutcomes[0], searchResults[0]
		assertEquals "Wrong outcome", runOneOutcomes[1], searchResults[1]

		runOneParams['order'] = "desc"
		searchResults = testRunService.searchTestOutcomes(runOneParams)
		assertEquals "Wrong number of outcomes returned", 2, searchResults.size()
		assertEquals "Wrong outcome", runOneOutcomes[1], searchResults[0]
		assertEquals "Wrong outcome", runOneOutcomes[0], searchResults[1]
	}


	void testSearchByTestOwner() {
		Project proj = to.project
		proj.testType = TestType.findByName("JUnit")
		if (!proj.save()) {
			dataService.reportSaveError proj
		}

		final int numTests = 10

		def testCases = []
		for (x in 1..numTests) {
			TestCase testCase = to.getTestCase(proj)
			if (!proj.addToTestCases(testCase).save()) {
				dataService.reportSaveError proj
			}
			testCases.add(testCase)
		}

		def testRunOne = to.getTestRun(proj, "mile1")
		testRunOne.save()

		for (testCase in testCases) {
			TestOutcome out = to.getTestOutcome(testCase, testRunOne)
			if (!testRunOne.addToOutcomes(out).save()) {
				dataService.reportSaveError testRunOne
			}
		}

		def testRunTwo = to.getTestRun(proj, "mile1")
		testRunTwo.save()

		for (testCase in testCases) {
			TestOutcome out = to.getTestOutcome(testCase, testRunTwo)
			if (!testRunTwo.addToOutcomes(out).save()) {
				dataService.reportSaveError testRunTwo
			}
		}

		def runOneOutcomes = testRunService.getOutcomesForTestRun(testRunOne, [includeIgnored: false])
		runOneOutcomes[0].owner = "Pacific Ocean Blue"
		runOneOutcomes[0].save()
		runOneOutcomes[1].owner = "Lost in the Pacific"
		runOneOutcomes[1].save()

		def runTwoOutcomes = testRunService.getOutcomesForTestRun(testRunTwo, [includeIgnored: false])
		runTwoOutcomes[0].owner = "Pacific Lake Blue"
		runTwoOutcomes[0].save()
		runTwoOutcomes[1].owner = "Found in the Pacific"
		runTwoOutcomes[1].save()

		def runOneParams = ['sort': 'owner', 'order': 'asc', 'max': 10, 'offset': 0, 'id': testRunOne.id,
			'qry': "owner|Pacific"]

		def searchResults = testRunService.searchTestOutcomes(runOneParams)
		assertEquals "Wrong number of outcomes returned", 2, searchResults.size()
		assertEquals "Wrong outcome", runOneOutcomes[1], searchResults[0]
		assertEquals "Wrong outcome", runOneOutcomes[0], searchResults[1]

		runOneParams['order'] = 'desc'
		searchResults = testRunService.searchTestOutcomes(runOneParams)
		assertEquals "Wrong number of outcomes returned", 2, searchResults.size()
		assertEquals "Wrong outcome", runOneOutcomes[0], searchResults[0]
		assertEquals "Wrong outcome", runOneOutcomes[1], searchResults[1]
	}	
}

