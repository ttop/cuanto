package cuanto

import cuanto.api.CuantoClient
import cuanto.WordGenerator
import cuanto.api.TestCase
import cuanto.api.TestOutcome
import cuanto.api.TestOutcome
import cuanto.api.TestRun

/**
 * User: Todd Wells
 * Date: Mar 3, 2009
 * Time: 6:02:02 PM
 * 
 */
class SubmitSingleResultTest extends GroovyTestCase {

	def serverUrl = "http://localhost:8080/cuanto"
	CuantoClient client = new CuantoClient(serverUrl)

	WordGenerator wordGen = new WordGenerator()
	String projectName
	String projectKey
	Long projectId

	@Override
	void setUp() {
		projectName = wordGen.getSentence(3)
		projectKey = wordGen.getSentence(3).replaceAll("\\s+", "")
		projectId = client.createProject(projectName, projectKey, 'JUnit')
	}


	@Override
	void tearDown(){
		client.deleteProject(projectId)
	}

	void testSubmitOneResult() {
		def testRunId = client.createTestRun(new TestRun(projectKey))

		TestCase testCase = new TestCase()
		testCase.packageName = "foo.bar.blah"
		testCase.testName = "submitOneTest"

		TestOutcome outcome = new TestOutcome()
		outcome.testCase = testCase
		outcome.testResult = "Pass"

		def outcomeId = client.submit(outcome, testRunId)
		assertNotNull outcomeId
		
		def stats = waitForTestRunStats(client, testRunId, "1")
		assertNotNull stats
		assertEquals "Wrong total tests", "1", stats?.tests
		assertEquals "Wrong passed", "1", stats?.passed
		assertEquals "Wrong failed", "0", stats?.failed
	}

	Map waitForTestRunStats(CuantoClient client, Long testRunId, String totalTests) {
		def secToWait = 30
		def interval = 500
		def start = new Date().time
		Map stats = client.getTestRunStats(testRunId)
		while (stats.tests != totalTests && new Date().time - start < secToWait * 1000) {
			sleep interval
			stats = client.getTestRunStats(testRunId)
		}
		return stats
	}
}