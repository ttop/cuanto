package cuanto

import cuanto.test.TestObjects
import cuanto.test.WordGenerator

/**
 * User: Todd Wells
 * Date: Apr 28, 2009
 * Time: 10:55:43 PM
 * 
 */
class DeleteEmptyTestRunsTests extends GroovyTestCase {
	TestRunService testRunService
	InitializationService initializationService
	DataService dataService
	StatisticService statisticService

	TestObjects to
	WordGenerator wordGen
	TestType testType


	@Override
	void setUp() {
		initializationService.initializeAll()
		to = new TestObjects()
		to.dataService = dataService
		wordGen = new WordGenerator()
		testType = TestType.findByName("JUnit")
	}

	void testDeleteEmptyTestRuns() {
		def proj = to.project
		dataService.saveDomainObject proj

		def numTestRuns = 10

		def runs = []
		1.upto(numTestRuns) {
			def testRun = to.getTestRun(proj, "my milestone")
			dataService.saveDomainObject testRun
			statisticService.calculateTestRunStats(testRun)
			runs << testRun
		}

		dataService.deleteEmptyTestRuns()
		assertEquals "Wrong number of test runs", numTestRuns, TestRun.list().size()

		runs.each { testRun ->
			def olderTime = new Date().time - (60 * 1000 * 11)
			testRun.dateExecuted = new Date(olderTime)
			dataService.saveDomainObject testRun
		}
		dataService.deleteEmptyTestRuns()
		assertEquals "Wrong number of test runs", numTestRuns, TestRun.list().size()
		
		0.upto(2) { indx ->
			runs[indx].testRunStatistics = null
			dataService.saveDomainObject runs[indx]
		}

		dataService.deleteEmptyTestRuns()

		assertEquals "Wrong number of test runs", 7, TestRun.list().size()
	}
}