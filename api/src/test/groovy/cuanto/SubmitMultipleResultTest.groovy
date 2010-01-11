package cuanto

import cuanto.api.CuantoClient
import cuanto.WordGenerator
import cuanto.api.TestCase
import cuanto.api.TestOutcome
import cuanto.api.TestOutcome
import cuanto.api.TestRun
import cuanto.api.TestRun

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
		sleep 5000
		client.deleteProject(projectId)
	}

	void testSubmitThousands() {
		def testRunId = client.createTestRun(new TestRun(projectKey))
		Long submitTime = 0
		int idx = 0
		int numThreads = 5
		def totalStart = new Date().time
		while (idx < 2000) {
			def th = []
			1.upto(numThreads){
				th << Thread.start {
					TestCase testCase = new TestCase()
					testCase.packageName = "foo.bar.blah"
					testCase.testName = wordGen.getSentence(3).replaceAll(" ", "")

					TestOutcome outcome = new TestOutcome()
					outcome.testCase = testCase
					outcome.testResult = "Pass"

					//System.out.print("Submitting cuanto result... ");
					long start = System.currentTimeMillis();
					def outcomeId = client.submit(outcome, testRunId)
					long duration = System.currentTimeMillis() - start;

					synchronized(idx) {
						submitTime += duration;
						System.out.println(String.format("${idx + 1} done in %d ms - Total lapse: %d ms", duration, submitTime));
						idx++
					}
				}
			}
			th.each { Thread it ->
				it.join() 
			}
		}
		def totalEnd = new Date().time
		println "total time is ${totalEnd - totalStart}"
	}
}