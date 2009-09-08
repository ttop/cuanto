package cuanto
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
	def projectName
	def projectId


	@Override
	void setUp() {
		projectName = wordGen.getSentence(3)
		def projectKey = wordGen.getSentence(3).replaceAll("\\s+", "")
		projectId = client.createProject(projectName, projectKey, 'JUnit')
	}


	@Override
	void tearDown() {
		client.deleteProject(projectId)
	}


	void testOutputWithNewLinesAndNodes() {
		def testRunId = client.getTestRunId(projectName, null, wordGen.getSentence(2), null, null)

		ParsableTestCase testCase = new ParsableTestCase()
		testCase.packageName = "foo.bar.blah"
		testCase.testName = wordGen.getSentence(3).replaceAll(" ", "")

		ParsableTestOutcome outcome = new ParsableTestOutcome()
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