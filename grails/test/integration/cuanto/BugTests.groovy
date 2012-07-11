package cuanto

import cuanto.test.TestObjects
import cuanto.*

class BugTests extends GroovyTestCase {

	DataService dataService
	def initializationService
	def testRunService
	def bugService

	TestObjects to


	@Override
	void setUp() {
		initializationService.initializeAll()
		to = new TestObjects()
		to.dataService = dataService
	}


	void testBugSummary() {
		Project proj = to.getProject()
		proj.testType = TestType.findByName("JUnit")
		proj.save()

		def numCases = 25

		TestRun testRun = to.getTestRun(proj)

		if (!testRun.save()) {
			dataService.reportSaveError testRun
		}

		def bugs = []
		bugs << ["bug1", "http://bug1"]
		bugs << ["bug2", "http://bug2"]
		bugs << ["bug3", "http://bug3"]
		bugs << ["bug4", "http://bug4"]

		for (x in 1..numCases) {
			TestCase tc = to.getTestCase(proj)
			tc.packageName = "a.b.c"
			dataService.saveDomainObject tc 

			TestOutcome outcome = to.getTestOutcome(tc, testRun)
			outcome.duration = 1
			if (x < 5) { // 4 total
				outcome.testResult = dataService.result("fail")
				outcome.bug = bugService.getBug(bugs[0][0], bugs[0][1])
			}

			if (x >= 5 && x < 13) { // 8 total
				outcome.testResult = dataService.result("fail")
				outcome.bug = bugService.getBug(bugs[1][0], bugs[1][1])
			}

			if (x == 15) {  // 1 bug
				outcome.testResult = dataService.result("fail")
				outcome.bug = bugService.getBug(bugs[2][0], bugs[2][1])
			}

			if (x > 19) { // 6 bugs
				outcome.testResult = dataService.result("fail")
				outcome.bug = bugService.getBug(bugs[3][0], bugs[3][1])
			}

			dataService.saveDomainObject outcome
		}

		def bugSummary = testRunService.getBugSummary(testRun)
		assertEquals "Wrong number of bugs", 4, bugSummary.size()

		assertEquals "Wrong bug", bugService.getBug(bugs[1][0], bugs[1][1]), bugSummary[0].bug
		assertEquals "Wrong total", 8, bugSummary[0].total

		assertEquals "Wrong bug", bugService.getBug(bugs[3][0], bugs[3][1]), bugSummary[1].bug
		assertEquals "Wrong total", 6, bugSummary[1].total

		assertEquals "Wrong bug", bugService.getBug(bugs[0][0], bugs[0][1]), bugSummary[2].bug
		assertEquals "Wrong total", 4, bugSummary[2].total

		assertEquals "Wrong bug", bugService.getBug(bugs[2][0], bugs[2][1]), bugSummary[3].bug
		assertEquals "Wrong total", 1, bugSummary[3].total

	}


	void testGetBug() {

		Bug bugA = bugService.getBug("Foo", "http://bar")
		Bug bugB = bugService.getBug("Foo", "http://bar")

		assertEquals("Wrong bug total", 1, Bug.count())
		assertEquals("Bugs not equal", bugA, bugB)

		Bug bugC = bugService.getBug("Foo", "http://blah")
		assertEquals("Wrong bug total", 2, Bug.count())

		Bug bugD = bugService.getBug("Bar", "http://foo")
		assertEquals("Wrong bug total", 3, Bug.count())

		Bug bugE = bugService.getBug("Baz", "http://bar")
		assertEquals("Wrong bug total", 4, Bug.count())

	}
}
