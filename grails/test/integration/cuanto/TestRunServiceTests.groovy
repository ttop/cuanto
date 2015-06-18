package cuanto

import cuanto.test.TestObjects
import cuanto.test.WordGenerator

class TestRunServiceTests extends GroovyTestCase {

	DataService dataService
	InitializationService initializationService
	TestRunService testRunService
	StatisticService statisticService
	ParsingService parsingService
	TestOutcomeService testOutcomeService

	TestObjects to
	WordGenerator wordGen = new WordGenerator()

	@Override
	void setUp() {
		initializationService.initializeAll()
		to = new TestObjects()
		to.dataService = dataService
	}


	void testCalculateTestRunTotals() {
		Project proj = to.project
		dataService.saveDomainObject proj

		def numCases = 11

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
			outcome.isFailureStatusChanged = false

			if (x == 2) {
				outcome.testResult = dataService.result("fail")
				outcome.isFailureStatusChanged = true
			} else if (x == 3) {
				outcome.testResult = dataService.result("error")
				outcome.isFailureStatusChanged = true
			} else if (x == 4) {
				outcome.testResult = dataService.result("ignore")
			}

			dataService.saveDomainObject outcome
		}

		statisticService.calculateTestRunStats(testRun.id)

		def testRunStatistics = TestRunStats.findByTestRun(testRun)
		assertNotNull "results not found", testRunStatistics
		TestRunStats result = testRunStatistics
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

		def runOneOutcomes = dataService.getTestOutcomes(
			new TestOutcomeQueryFilter(testRun: testRunOne, testResultIncludedInCalculations: true))
		runOneOutcomes[0].note = "Pacific Ocean Blue"
		runOneOutcomes[0].save()
		runOneOutcomes[1].note = "Lost in the Pacific"
		runOneOutcomes[1].save()

		//def runTwoOutcomes = testRunService.getOutcomesForTestRun(testRunTwo, [includeIgnored: false])
		def runTwoOutcomes = dataService.getTestOutcomes(
			new TestOutcomeQueryFilter(testRun: testRunTwo, testResultIncludedInCalculations: true))

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

		def searchResults = testOutcomeService.getTestOutcomeQueryResultsForParams(runOneParams).testOutcomes
		assertEquals "Wrong number of outcomes returned", 2, searchResults.size()
		assertEquals "Wrong outcome", runOneOutcomes[1], searchResults[0]
		assertEquals "Wrong outcome", runOneOutcomes[0], searchResults[1]

		runOneParams['order'] = "desc"
		searchResults = testOutcomeService.getTestOutcomeQueryResultsForParams(runOneParams).testOutcomes
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

		def runOneOutcomes = dataService.getTestOutcomes(
			new TestOutcomeQueryFilter(testRun: testRunOne, testResultIncludedInCalculations: true))

		runOneOutcomes[0].testCase.fullName = "a Pacific Ocean Blue"
		runOneOutcomes[0].testCase.save()
		runOneOutcomes[1].testCase.fullName = "b Lost in the Pacific"
		runOneOutcomes[1].testCase.save()

		def runTwoOutcomes = dataService.getTestOutcomes(
			new TestOutcomeQueryFilter(testRun: testRunTwo, testResultIncludedInCalculations: true))
		runTwoOutcomes[0].testCase.fullName = "a Pacific Lake Blue"
		runTwoOutcomes[0].testCase.save()
		runTwoOutcomes[1].testCase.fullName = "b Found in the Pacific"
		runTwoOutcomes[1].testCase.save()

		def runOneParams = ['sort': 'name', 'order': 'asc', 'max': 10, 'offset': 0, 'id': testRunOne.id,
			'qry': 'name|Pacific']
		def searchResults = testOutcomeService.getTestOutcomeQueryResultsForParams(runOneParams).testOutcomes
		assertEquals "Wrong number of outcomes returned", 2, searchResults.size()
		assertEquals "Wrong outcome", runOneOutcomes[0], searchResults[0]
		assertEquals "Wrong outcome", runOneOutcomes[1], searchResults[1]

		runOneParams['order'] = "desc"
		searchResults = testOutcomeService.getTestOutcomeQueryResultsForParams(runOneParams).testOutcomes
		assertEquals "Wrong number of outcomes returned", 2, searchResults.size()
		assertEquals "Wrong outcome", runOneOutcomes[1], searchResults[0]
		assertEquals "Wrong outcome", runOneOutcomes[0], searchResults[1]
	}


	void testSearchByTestOwner() {
		Project proj = to.project
		dataService.saveDomainObject proj

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

		def runOneOutcomes = dataService.getTestOutcomes(
			new TestOutcomeQueryFilter(testRun: testRunOne, testResultIncludedInCalculations: true)
		)
		runOneOutcomes[0].owner = "Pacific Ocean Blue"
		runOneOutcomes[0].save()
		runOneOutcomes[1].owner = "Lost in the Pacific"
		runOneOutcomes[1].save()

		def runTwoOutcomes = dataService.getTestOutcomes(
			new TestOutcomeQueryFilter(testRun: testRunTwo, testResultIncludedInCalculations: true)
		)
		runTwoOutcomes[0].owner = "Pacific Lake Blue"
		runTwoOutcomes[0].save()
		runTwoOutcomes[1].owner = "Found in the Pacific"
		runTwoOutcomes[1].save()

		def runOneParams = ['sort': 'owner', 'order': 'asc', 'max': 10, 'offset': 0, 'id': testRunOne.id,
			'qry': "owner|Pacific"]

		def searchResults = testOutcomeService.getTestOutcomeQueryResultsForParams(runOneParams).testOutcomes
		assertEquals "Wrong number of outcomes returned", 2, searchResults.size()
		assertEquals "Wrong outcome", runOneOutcomes[1], searchResults[0]
		assertEquals "Wrong outcome", runOneOutcomes[0], searchResults[1]

		runOneParams['order'] = 'desc'
		searchResults = testOutcomeService.getTestOutcomeQueryResultsForParams(runOneParams).testOutcomes
		assertEquals "Wrong number of outcomes returned", 2, searchResults.size()
		assertEquals "Wrong outcome", runOneOutcomes[0], searchResults[0]
		assertEquals "Wrong outcome", runOneOutcomes[1], searchResults[1]
	}

	void testGetProject() {
		def groupNames = ["aa", "bb", "cc"]
		def projectsPerGroup = 3
		def projects = []

		groupNames.each { groupName ->
			def group = to.getProjectGroup(groupName)
			dataService.saveDomainObject(group)
			1.upto(projectsPerGroup) {
				def proj = new Project(name: to.wordGen.getSentence(3), projectKey: to.getProjectKey(),
					projectGroup: group, 'testType': TestType.findByName("JUnit"))
				dataService.saveDomainObject(proj)
				projects << proj
			}
		}

		projects.each { proj ->
			def foundProj = testRunService.getProject(proj.projectKey)
			assertNotNull foundProj
			assertEquals proj, foundProj
		}
	}


	void testCreateAndDeleteTestRun() {
		Project proj = to.project
		dataService.saveDomainObject proj, true

		def params = [:]
		params.project = proj.projectKey
		params.note = to.wordGen.getSentence(5)

		def links = ["http://gurdy||hurdy", "http://easy||squeezy", "malformed"]
		assertEquals 0, TestRunLink.list().size()
		params.link = links

		def props = ["CustomProp1||Custom Value 1", "CustomProp2||Custom Value 2"]
		assertEquals 0, TestRunProperty.list().size()
		params.testProperty = props

		TestRun createdTr = testRunService.createTestRun(params)

		assertEquals 2, TestRunLink.list().size()
		assertEquals 2, TestRunProperty.list().size()

		TestRun fetchedTr = TestRun.get(createdTr.id)
		assertEquals "Wrong note", params.note, fetchedTr.note

		assertEquals "Wrong number of links", 2, fetchedTr.links.size()
		assertEquals "Wrong number of test properties", 2, fetchedTr.testProperties.size()

		def listOfLinks = new ArrayList(fetchedTr.links as List)
		Collections.sort(listOfLinks)

		assertEquals "http://gurdy", listOfLinks[0].url
		assertEquals "hurdy", listOfLinks[0].description
		assertEquals "http://easy", listOfLinks[1].url
		assertEquals "squeezy", listOfLinks[1].description

		def listOfProps = new ArrayList(fetchedTr.testProperties as List)
		Collections.sort(listOfProps)
		assertEquals "CustomProp1", listOfProps[0].name
		assertEquals "Custom Value 1", listOfProps[0].value
		assertEquals "CustomProp2", listOfProps[1].name
		assertEquals "Custom Value 2", listOfProps[1].value

		testRunService.deleteTestRun(fetchedTr)
		assertNull TestRun.get(fetchedTr.id)
		assertEquals 0, TestRunLink.list().size()
	}


	void testGetTestRunsWithProperties() {
		Project proj = to.project
		dataService.saveDomainObject proj, true

		def testRuns = []

		1.upto(3) {
			testRuns << to.getTestRun(proj)
		}
		assertEquals 3, testRuns.size()


		def props = []
		props << [name: to.wordGen.getCamelWords(4), value: to.wordGen.getSentence(3)]
		props << [name: to.wordGen.getCamelWords(4), value: to.wordGen.getSentence(3)]
		props << [name: to.wordGen.getCamelWords(4), value: to.wordGen.getSentence(3)]
		props << [name: to.wordGen.getCamelWords(4), value: to.wordGen.getSentence(3)]
		props << [name: to.wordGen.getCamelWords(4), value: to.wordGen.getSentence(3)]

		testRuns[0].addToTestProperties(new TestRunProperty(props[0].name, props[0].value))
		testRuns[0].addToTestProperties(new TestRunProperty(props[1].name, props[1].value))
		testRuns[0].addToTestProperties(new TestRunProperty(props[2].name, props[2].value))
		testRuns[1].addToTestProperties(new TestRunProperty(props[0].name, props[0].value))
		testRuns[1].addToTestProperties(new TestRunProperty(props[1].name, props[1].value))
		testRuns[1].addToTestProperties(new TestRunProperty(props[3].name, props[2].value))
		testRuns[2].addToTestProperties(new TestRunProperty(props[1]))

		testRuns.each {
			dataService.saveTestRun(it)
		}

		assertEquals "Wrong number of test runs", 0, testRunService.getTestRunsWithProperties(proj,
			[new TestRunProperty(props[4].name, props[4].value)])?.size()

		def fetchedRuns = testRunService.getTestRunsWithProperties(proj, testRuns[0].testProperties)
		assertEquals "Wrong number of test runs", 1, fetchedRuns?.size()
		assertEquals "Wrong test run retrieved", testRuns[0].id, fetchedRuns[0].id

		fetchedRuns = testRunService.getTestRunsWithProperties(proj, testRuns[0].testProperties[0..1])
		assertEquals "Wrong number of test runs", 2, fetchedRuns.size()
		assertNotNull "Couldn't find test run", fetchedRuns.find { it.id == testRuns[0].id }
		assertNotNull "Couldn't find test run", fetchedRuns.find { it.id == testRuns[1].id }
	}


	void testGetTestRunPropertiesByProject() {
		Project proj = to.project
		dataService.saveDomainObject proj, true

		def testRuns = []

		1.upto(3) {
			testRuns << to.getTestRun(proj)
		}
		assertEquals 3, testRuns.size()


		def props = []
		props << [name: to.wordGen.getCamelWords(4), value: to.wordGen.getSentence(3)]
		props << [name: to.wordGen.getCamelWords(4), value: to.wordGen.getSentence(3)]
		props << [name: to.wordGen.getCamelWords(4), value: to.wordGen.getSentence(3)]
		props << [name: to.wordGen.getCamelWords(4), value: to.wordGen.getSentence(3)]
		props << [name: to.wordGen.getCamelWords(4), value: to.wordGen.getSentence(3)]

		testRuns[0].addToTestProperties(new TestRunProperty(props[0].name, props[0].value))
		testRuns[0].addToTestProperties(new TestRunProperty(props[1].name, props[1].value))
		testRuns[0].addToTestProperties(new TestRunProperty(props[2].name, props[2].value))
		testRuns[1].addToTestProperties(new TestRunProperty(props[0].name, props[0].value))
		testRuns[1].addToTestProperties(new TestRunProperty(props[1].name, props[1].value))
		testRuns[1].addToTestProperties(new TestRunProperty(props[3].name, props[2].value))
		testRuns[2].addToTestProperties(new TestRunProperty(props[1]))

		testRuns.each {
			dataService.saveTestRun(it)
		}

		List results = testRunService.getTestRunPropertiesByProject(proj)
		assertEquals "Wrong number of results returned", 4, results.size()
		assertTrue "Couldn't find property", results.contains(props[0].name)
		assertTrue "Couldn't find property", results.contains(props[1].name)
		assertTrue "Couldn't find property", results.contains(props[2].name)
		assertTrue "Couldn't find property", results.contains(props[3].name)
	}


	void testUpdatePropertiesOfTestRun() {
		Project proj = to.project
		dataService.saveDomainObject proj, true
		TestRun origTestRun = to.getTestRun(proj)

		def props = []
		props << [name: to.wordGen.getCamelWords(4), value: to.wordGen.getSentence(3)]
		props << [name: to.wordGen.getCamelWords(4), value: to.wordGen.getSentence(3)]
		props << [name: to.wordGen.getCamelWords(4), value: to.wordGen.getSentence(3)]
		props << [name: to.wordGen.getCamelWords(4), value: to.wordGen.getSentence(3)]
		props << [name: to.wordGen.getCamelWords(4), value: to.wordGen.getSentence(3)]

		origTestRun.addToTestProperties(new TestRunProperty(props[0].name, props[0].value))
		origTestRun.addToTestProperties(new TestRunProperty(props[1].name, props[1].value))
		origTestRun.addToTestProperties(new TestRunProperty(props[2].name, props[2].value))

		dataService.saveTestRun(origTestRun)

		TestRun updateTestRun = new TestRun(id: origTestRun.id)
		updateTestRun.addToTestProperties(new TestRunProperty(props[0].name, "updated!"))
		updateTestRun.addToTestProperties(new TestRunProperty(props[3].name, "added!"))

		testRunService.updatePropertiesOfTestRun origTestRun, updateTestRun

		TestRun fetchedTestRun = TestRun.get(origTestRun.id)
		assertEquals "Wrong number of test run properties", 2, fetchedTestRun.testProperties.size()

		def fetchedProps = TestRunProperty.findAllByTestRun(origTestRun)
		assertEquals "Wrong number of fetched properties", 2, fetchedProps.size()
	}


	void testUpdateSinglePropertyOfTestRun() {
		Project proj = to.project
		dataService.saveDomainObject proj, true
		TestRun origTestRun = to.getTestRun(proj)
		origTestRun.addToTestProperties(new TestRunProperty("one", "orig value"))
		dataService.saveTestRun(origTestRun)

		TestRun updateTestRun = new TestRun(id: origTestRun.id)
		updateTestRun.addToTestProperties(new TestRunProperty("one", "updated"))

		testRunService.updatePropertiesOfTestRun origTestRun, updateTestRun
		TestRun fetchedTestRun = TestRun.get(origTestRun.id)
		assertEquals "Wrong number of test run properties", 1, fetchedTestRun.testProperties.size()

		def fetchedProps = TestRunProperty.findAllByTestRun(origTestRun)
		assertEquals "Wrong number of fetched properties", 1, fetchedProps.size()

		assertEquals "Wrong property value", "updated", fetchedTestRun.testProperties[0].value
		assertEquals "Wrong property value", "updated", fetchedProps[0].value
	}


	void testRemoveSinglePropertyOfTestRun() {
		Project proj = to.project
		dataService.saveDomainObject proj, true
		TestRun origTestRun = to.getTestRun(proj)
		origTestRun.addToTestProperties(new TestRunProperty("one", "orig value"))
		dataService.saveTestRun(origTestRun)

		TestRun updateTestRun = new TestRun(id: origTestRun.id)

		testRunService.updatePropertiesOfTestRun origTestRun, updateTestRun
		TestRun fetchedTestRun = TestRun.get(origTestRun.id)
		assertEquals "Wrong number of test run properties", 0, fetchedTestRun.testProperties.size()

		def fetchedProps = TestRunProperty.findAllByTestRun(origTestRun)
		assertEquals "Wrong number of fetched properties", 0, fetchedProps.size()
	}


	void testGetPreviousTestRunSuccessRate() {
		Project proj = to.project
		dataService.saveDomainObject proj, true

		TestRun testRunFirst = to.getTestRun(proj)
		testRunFirst.dateExecuted = new Date() - 1
		dataService.saveTestRun(testRunFirst)
		TestRunStats testRunFirstStats = new TestRunStats(successRate: 90, testRun: testRunFirst)
		dataService.saveDomainObject(testRunFirstStats)

		TestRun testRunSecond = to.getTestRun(proj)
		dataService.saveTestRun(testRunSecond)
		TestRunStats testRunSecondStats = new TestRunStats(successRate: 95, testRun: testRunSecond)
		dataService.saveDomainObject(testRunSecondStats)

		TestRun testRunThird = to.getTestRun(proj)
		testRunThird.dateExecuted = new Date() + 1
		dataService.saveTestRun(testRunThird)
		TestRunStats testRunThirdStats = new TestRunStats(successRate: 85, testRun: testRunThird)
		dataService.saveDomainObject(testRunThirdStats)

		assertEquals "Previous successRate should've been null for first TestRun",
			null, testRunService.getPreviousTestRunSuccessRate(testRunFirst)

		assertEquals "Wrong successRate returned", testRunFirstStats.successRate,
			testRunService.getPreviousTestRunSuccessRate(testRunSecond)

		assertEquals "Wrong successRate returned", testRunSecondStats.successRate,
			testRunService.getPreviousTestRunSuccessRate(testRunThird)

	    TestRun testRunWithoutStats = to.getTestRun(proj)
		testRunWithoutStats.dateExecuted = new Date() - 2
		dataService.saveTestRun(testRunWithoutStats)

		assertEquals "Previous successRate for a TestRun without stats should've been null",
			null, testRunService.getPreviousTestRunSuccessRate(testRunFirst)
	}
}

