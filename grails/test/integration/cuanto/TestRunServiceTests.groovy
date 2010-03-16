package cuanto

import cuanto.test.TestObjects
import cuanto.test.WordGenerator
import cuanto.api.TestCase as TestCaseApi
import cuanto.api.TestOutcome as TestOutcomeApi
import cuanto.api.TestRun as TestRunApi
import cuanto.api.Link as LinkApi
import cuanto.api.TestProperty as TestPropertyApi

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
			if (x == 2) {
				outcome.testResult = dataService.result("fail")
			} else if (x == 3) {
				outcome.testResult = dataService.result("error")
			} else if (x == 4) {
				outcome.testResult = dataService.result("ignore")
			}

			dataService.saveDomainObject outcome
		}

		statisticService.calculateTestRunStats(testRun.id)

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
		assertEquals 0, Link.list().size()
		params.link = links

		def props = ["CustomProp1||Custom Value 1", "CustomProp2||Custom Value 2"]
		assertEquals 0, TestProperty.list().size()
		params.testProperty = props

		TestRun createdTr = testRunService.createTestRun(params)

		assertEquals 2, Link.list().size()
		assertEquals 2, TestProperty.list().size()

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

		dataService.deleteTestRun(fetchedTr)
		assertNull TestRun.get(fetchedTr.id)
		assertEquals 0, Link.list().size()
	}


	void testUpdateTestRun() {
		Project proj = to.project
		dataService.saveDomainObject proj, true

		def params = [:]
		params.project = proj.projectKey
		params.note = to.wordGen.getSentence(5)

		def links = ["http://gurdy||hurdy", "http://easy||squeezy", "malformed"]
		assertEquals 0, Link.list().size()
		params.link = links

		def props = ["CustomProp1||Custom Value 1", "CustomProp2||Custom Value 2"]
		assertEquals 0, TestProperty.list().size()
		params.testProperty = props

		TestRun createdTr = testRunService.createTestRun(params)

		TestRunApi updatedTr = new TestRunApi(id: createdTr.id)
		updatedTr.note = to.wordGen.getSentence(5)
		updatedTr.valid = false
		updatedTr.links = [new LinkApi("newlink", "http://newlink"), new LinkApi("hurdy", "http://foo")]
		updatedTr.testProperties = [new TestPropertyApi("CustomProp1", "Custom Value 1"),
			new TestPropertyApi("newprop", "new value")]
		testRunService.update(updatedTr)

		TestRun fetchedTr = TestRun.get(createdTr.id)
		assertNotNull fetchedTr

		assertEquals "note", updatedTr.note, fetchedTr.note
		assertEquals "valid", updatedTr.valid, fetchedTr.valid
		assertEquals "links length", 2, fetchedTr.links.size()

		def link0 = fetchedTr.links.find {it.description == updatedTr.links[0].description }
		assertNotNull link0
		assertEquals "link 0 url", updatedTr.links[0].url, link0.url

		def link1 = fetchedTr.links.find { it.description == updatedTr.links[1].description }
		assertNotNull link1
		assertEquals "link 1 url", updatedTr.links[1].url, link1.url

		def prop0 = fetchedTr.testProperties.find { it.name == updatedTr.testProperties[0].name }
		assertNotNull prop0
		assertEquals "prop 0 value", updatedTr.testProperties[0].value, prop0.value
		
		def prop1 = fetchedTr.testProperties.find { it.name == updatedTr.testProperties[1].name }
		assertNotNull prop1
		assertEquals "prop 1 value", updatedTr.testProperties[1].value, prop1.value
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
		1.upto(5) {
			props << to.testProperty
		}

		testRuns[0].addToTestProperties(props[0])
		testRuns[0].addToTestProperties(props[1])
		testRuns[0].addToTestProperties(props[2])
		testRuns[1].addToTestProperties(props[0])
		testRuns[1].addToTestProperties(props[1])
		testRuns[1].addToTestProperties(props[3])
		testRuns[2].addToTestProperties(props[1])

		testRuns.each {
			dataService.saveTestRun(it)
		}

		assertEquals "Wrong number of test runs", 0, testRunService.getTestRunsWithProperties(proj, [props[4]])?.size()

		def fetchedRuns = testRunService.getTestRunsWithProperties(proj, props[0..2])
		assertEquals "Wrong number of test runs", 1, fetchedRuns?.size()
		assertEquals "Wrong test run retrieved", testRuns[0].id, fetchedRuns[0].id

		fetchedRuns = testRunService.getTestRunsWithProperties(proj, props[0..1])
		assertEquals "Wrong number of test runs", 2, fetchedRuns.size()
		assertNotNull "Couldn't find test run", fetchedRuns.find { it.id == testRuns[0].id }
		assertNotNull "Couldn't find test run", fetchedRuns.find { it.id == testRuns[1].id }
	}

}

