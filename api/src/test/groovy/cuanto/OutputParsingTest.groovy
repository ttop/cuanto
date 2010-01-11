package cuanto

import cuanto.api.TestCase
import cuanto.api.TestCase
import cuanto.api.TestOutcome
import cuanto.api.TestRun
import cuanto.api.TestRun
import cuanto.api.CuantoClient

/**
 * User: Todd Wells
 * Date: Sep 3, 2009
 * Time: 8:08:27 AM
 *
 */

public class OutputParsingTest extends GroovyTestCase {

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
	void tearDown() {
		client.deleteProject(projectId)
	}


	void testOutputWithNewLinesAndNodes() {
		def testRunId = client.createTestRun(new TestRun(projectKey))
		TestCase testCase = new TestCase()
		testCase.packageName = "foo.bar.blah"
		testCase.testName = wordGen.getSentence(3).replaceAll(" ", "")

		TestOutcome outcome = new TestOutcome()
		outcome.testCase = testCase
		outcome.testResult = "Pass"
		outcome.testOutput = """
<error message="Error attempting to activate model on host
A network error occurred. (Error code: 8)" type="package.ModelActivationException">package.ModelActivationException: Error attempting to activate model on host
A network error occurred. (Error code: 8)
at package.class.method(class.java:151)
at package.class.method(class.java:197)
at package.class.method(class.java:78)
</error>
		"""
		def outcomeId = client.submit(outcome, testRunId)
	}
}