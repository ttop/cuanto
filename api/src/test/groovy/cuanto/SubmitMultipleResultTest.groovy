package cuanto

import cuanto.CuantoClient
import cuanto.WordGenerator

/**
 * User: Todd Wells
 * Date: Mar 3, 2009
 * Time: 6:02:02 PM
 * 
 */
class SubmitMultipleResultTest extends GroovyTestCase {

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

	void testSubmitThousands() {
		def testRunId = client.getTestRunId(projectName, null, wordGen.getSentence(2), null, null)

		Long submitTime = 0

		1.upto(3000) { idx ->
			ParsableTestCase testCase = new ParsableTestCase()
			testCase.packageName = "foo.bar.blah"
			testCase.testName = wordGen.getSentence(3).replaceAll(" ", "")

			ParsableTestOutcome outcome = new ParsableTestOutcome()
			outcome.testCase = testCase
			outcome.testResult = "Pass"

			//System.out.print("Submitting cuanto result... ");
			long start = System.currentTimeMillis();
			def outcomeId = client.submit(outcome, testRunId)
			long duration = System.currentTimeMillis() - start;
			submitTime += duration;
			System.out.println(String.format("${idx} done in %d ms - Total lapse: %d ms", duration, submitTime));
		}

	}
}