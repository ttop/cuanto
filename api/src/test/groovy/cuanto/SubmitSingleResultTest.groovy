package cuanto

import cuanto.CuantoClient
import cuanto.WordGenerator

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
	def projectName
	def projectId

	@Override
	void setUp() {
		projectName = wordGen.getSentence(3)
		def projectKey = wordGen.getSentence(3).replaceAll("\\s+", "")
		projectId = client.createProject(projectName, projectKey, 'JUnit')
	}


	@Override
	void tearDown(){
		client.deleteProject(projectId)
	}

	void testSubmitOneResult() {
		def testRunId = client.getTestRunId(projectName, null, wordGen.getSentence(2), null, null)

		ParsableTestCase testCase = new ParsableTestCase()
		testCase.packageName = "foo.bar.blah"
		testCase.testName = "submitOneTest"

		ParsableTestOutcome outcome = new ParsableTestOutcome()
		outcome.testCase = testCase
		outcome.testResult = "Pass"

		def outcomeId = client.submit(outcome, testRunId)
		assertNotNull outcomeId
		
		def stats = client.getTestRunStats(testRunId)
		assertNotNull stats
		assertEquals "Wrong total tests", "1", stats?.tests
		assertEquals "Wrong passed", "1", stats?.passed
		assertEquals "Wrong failed", "0", stats?.failed
	}


	void testUpdateSingleResult() {
		def testRunId = client.getTestRunId(projectName, null, wordGen.getSentence(2), null, null)

		ParsableTestCase testCase = new ParsableTestCase()
		testCase.packageName = "foo.bar.blah"
		testCase.testName = "submitOneTest"

		ParsableTestOutcome outcome = new ParsableTestOutcome()
		outcome.testCase = testCase
		outcome.testResult = "Fail"

		def outcomeId = client.submit(outcome, testRunId)
		assertNotNull outcomeId

		def stats = client.getTestRunStats(testRunId)
		assertNotNull stats
		assertEquals "Wrong total tests", "1", stats?.tests
		assertEquals "Wrong passed", "0", stats?.passed
		assertEquals "Wrong failed", "1", stats?.failed

		outcome.testOutput = "This is the test output"
		def updatedOutcomeId = client.submit(outcome, testRunId)
		assertEquals "Test outcome IDs not the same", outcomeId, updatedOutcomeId

		def updatedStats = client.getTestRunStats(testRunId)
		assertNotNull updatedStats
		assertEquals "Wrong total tests", "1", updatedStats?.tests
		assertEquals "Wrong passed", "0", updatedStats?.passed
		assertEquals "Wrong failed", "1", updatedStats?.failed
	}
}