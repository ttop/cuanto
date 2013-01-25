package cuanto

import cuanto.test.TestObjects
import java.math.MathContext

/**
 * User: Todd Wells
 * Date: Jan 7, 2010
 * Time: 8:28:46 AM
 * 
 */

public class StatisticServiceTests extends GroovyTestCase {

	DataService dataService
	StatisticService statisticService
	def initializationService

	TestObjects to = new TestObjects()
	Project proj

	@Override
	void setUp() {
		initializationService.initializeAll()
		to = new TestObjects()
		to.dataService = this.dataService
		statisticService.dataService = this.dataService
		proj = to.project
		dataService.saveDomainObject proj
	}


	void testSuccessRateChangeCalculation() {
		Project proj = to.project
		dataService.saveDomainObject proj

		def numCases = 11

		TestRun testRun1 = to.getTestRun(proj)

		if (!testRun1.save()) {
			dataService.reportSaveError testRun1
		}

		for (x in 1..numCases) {
			TestCase tc = to.getTestCase(proj)
			tc.packageName = "a.b.c"
			dataService.saveDomainObject tc

			TestOutcome outcome = to.getTestOutcome(tc, testRun1)
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

		statisticService.calculateTestRunStats(testRun1.id)

		def testRunStatistics = TestRunStats.findByTestRun(testRun1)
		assertNotNull "results not found", testRunStatistics
		TestRunStats result = testRunStatistics
		assertEquals "wrong total tests", 10, result.tests
		assertEquals "wrong failures", 2, result.failed
		assertEquals "wrong passed", 8, result.passed
		assertEquals "wrong avg duration", 1, result.averageDuration
		assertEquals "wrong total duration", 10, result.totalDuration
		assertEquals "wrong successRate", 80, result.successRate
		assertEquals "wrong successRateChange", null, testRunStatistics.successRateChange

		
		def testRun2 = to.getTestRun(proj)
		testRun2.dateExecuted = new Date() + 2

		if (!testRun2.save()) {
			dataService.reportSaveError testRun2
		}

		for (x in 1..numCases) {
			TestCase tc = to.getTestCase(proj)
			tc.packageName = "a.b.c"
			dataService.saveDomainObject tc

			TestOutcome outcome = to.getTestOutcome(tc, testRun2)
			outcome.duration = 1
			outcome.isFailureStatusChanged = false

			if (x == 2 || x == 5 || x == 6) {
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

		statisticService.calculateTestRunStats(testRun2.id)
		MathContext fourDigitRounding = new MathContext(4)
		testRunStatistics = TestRunStats.findByTestRun(testRun2)
		assertNotNull "results not found", testRunStatistics
		result = testRunStatistics
		assertEquals "wrong total tests", 10, result.tests
		assertEquals "wrong failures", 4, result.failed
		assertEquals "wrong passed", 6, result.passed
		assertEquals "wrong avg duration", 1, result.averageDuration
		assertEquals "wrong total duration", 10, result.totalDuration
		assertEquals "Wrong successRate", (6 / 10 * 100).round(fourDigitRounding), testRunStatistics.successRate
		assertEquals "Wrong successRate", 60.00, testRunStatistics.successRate
		assertEquals "wrong successRateChange", -20, testRunStatistics.successRateChange
		
		
		def testRun3 = to.getTestRun(proj)
		testRun3.dateExecuted = new Date() + 2

		if (!testRun3.save()) {
			dataService.reportSaveError testRun3
		}

		for (x in 1..numCases) {
			TestCase tc = to.getTestCase(proj)
			tc.packageName = "a.b.c"
			dataService.saveDomainObject tc

			TestOutcome outcome = to.getTestOutcome(tc, testRun3)
			outcome.duration = 1
			outcome.isFailureStatusChanged = false

			if (x == 2 ) {
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

		statisticService.calculateTestRunStats(testRun3.id)
		testRunStatistics = TestRunStats.findByTestRun(testRun3)
		assertNotNull "results not found", testRunStatistics
		result = testRunStatistics
		assertEquals "wrong total tests", 10, result.tests
		assertEquals "wrong failures", 2, result.failed
		assertEquals "wrong passed", 8, result.passed
		assertEquals "wrong avg duration", 1, result.averageDuration
		assertEquals "wrong total duration", 10, result.totalDuration
		assertEquals "Wrong successRate", (8 / 10 * 100).round(fourDigitRounding), testRunStatistics.successRate
		assertEquals "Wrong successRate", 80.00, testRunStatistics.successRate
		assertEquals "wrong successRateChange", 20, testRunStatistics.successRateChange
		
		
		def testRun4 = to.getTestRun(proj)
		testRun4.dateExecuted = new Date() + 3

		if (!testRun4.save()) {
			dataService.reportSaveError testRun4
		}

		for (x in 1..numCases) {
			TestCase tc = to.getTestCase(proj)
			tc.packageName = "a.b.c"
			dataService.saveDomainObject tc

			TestOutcome outcome = to.getTestOutcome(tc, testRun4)
			outcome.duration = 1
			outcome.isFailureStatusChanged = false

			if (x == 2 || x == 5 || x == 6) {
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

		statisticService.calculateTestRunStats(testRun4.id)
		testRunStatistics = TestRunStats.findByTestRun(testRun4)
		assertNotNull "results not found", testRunStatistics
		result = testRunStatistics
		assertEquals "wrong total tests", 10, result.tests
		assertEquals "wrong failures", 4, result.failed
		assertEquals "wrong passed", 6, result.passed
		assertEquals "wrong avg duration", 1, result.averageDuration
		assertEquals "wrong total duration", 10, result.totalDuration
		assertEquals "Wrong successRate", (6 / 10 * 100).round(fourDigitRounding), testRunStatistics.successRate
		assertEquals "Wrong successRate", 60.00, testRunStatistics.successRate
		assertEquals "wrong successRateChange", -20, testRunStatistics.successRateChange
		
	}
}