package cuanto

import cuanto.CuantoClient
import cuanto.CuantoTestResult
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
		projectId = client.createProject(projectName, wordGen.getSentence(3).replaceAll("\\s+", ""), 'JUnit')
	}


	@Override
	void tearDown(){
		client.deleteProject(projectId)
	}

	void testSubmitOneResult() {
		def testRunId = client.getTestRunId(projectName, null, wordGen.getSentence(2), null, null)

		CuantoTestResult result = new CuantoTestResult(testRunId)
		result.packageName = "foo.bar"
		result.testName = "myTestName"
		result.description = "my description"
		result.testResult = "Pass"
		result.testOutput = wordGen.getSentence(50)
		result.owner = "Me!"
		result.bug = "http://jira/CUANTO-5"
		result.note = "this is my note"
		result.analysisState = "Bug"

		// TODO: does JUnit RunListener expose test output?

		client.submit(result)

		def stats = client.getTestRunStats(testRunId)
		assertNotNull stats
		assertEquals "Wrong total tests", 1, stats?.tests
		assertEquals "Wrong passed", 1, stats?.passed
		assertEquals "Wrong failed", 0, stats?.failed
		
	}
}