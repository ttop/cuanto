package cuanto

import cuanto.CuantoClient
import cuanto.WordGenerator
import groovy.mock.interceptor.StubFor
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.PostMethod

/**
 * User: Todd Wells
 * Date: Sep 29, 2008
 * Time: 5:54:29 PM
 * 
 */
class CuantoClientTest extends GroovyTestCase{

	def serverUrl = "http://localhost:8080/cuanto"
	CuantoClient client = new CuantoClient(serverUrl)

	WordGenerator wordGen = new WordGenerator()
	def projectName
	def projectId

	def testUser = "admin"
	def testPassword = "admin"

	@Override
	void setUp() {
		client.userId = testUser
		client.password = testPassword
		
		projectName = wordGen.getSentence(3).trim()
		def projectKey = wordGen.getSentence(3).replaceAll("\\s+", "").trim()
		if (projectKey.length() > 25) {
			projectKey = projectKey.substring(0, 24)
		}
		projectId = client.createProject(projectName, projectKey, "JUnit")
	}


	@Override
	void tearDown(){
		client.deleteProject(projectId)
	}


	void testCreateAndDeleteProject() {
		def localName = wordGen.getSentence(3)
		def projKey = wordGen.getSentence(2).replaceAll("\\s+", "")
		def localProjectId = client.createProject(localName, projKey, "JUnit")
		def projectInfo = client.getProject(localProjectId)
		assertEquals "wrong project name", localName, projectInfo.name
		assertEquals "wrong project key", projKey, projectInfo.key
		client.deleteProject(localProjectId)
		projectInfo = client.getProject(localProjectId)
		assertNull projectInfo.id
	}


	void testCreateAndDeleteProjectStubbed() {
		def localName = wordGen.getSentence(3)
		def projKey = wordGen.getSentence(3).replaceAll("\\s+", "")
		def fakedId = 12345

		def stubHttpClient = new StubFor(HttpClient)
		stubHttpClient.demand.executeMethod { 200 }

		def stubPost = new StubFor(PostMethod)
		stubPost.demand.addRequestHeader(1..1) { name, value ->	}
		stubPost.demand.addParameter(3..3) { name, value ->	}
		stubPost.demand.releaseConnection { }
		stubPost.demand.getResponseBodyAsStream {
			def exp = new Expando()
			exp.text = fakedId.toString()
			return exp
		}

		stubHttpClient.use {
			stubPost.use {
				def localProjectId = client.createProject(localName, projKey, "JUnit")
				assertEquals "Wrong id", fakedId, localProjectId
			}
		}
	}


	void testCreateProjectTooBig() {
		def message = shouldFail(CuantoClientException) {
			client.createProject("Long name", "abcdefghijklmnopqrstuvwxyz", "JUnit")
		}
		assertTrue "Wrong error message: $message", message.contains("projectKey.maxSize")
	}

	
	void testGetTestRunId() {
		/* getTestRunID(String project, String dateExecuted, String milestone, String build, String
			 targetEnv) //everything but project can be null */

		Long testRunId = client.getTestRunId(projectName, null, null, null, null)
		assertNotNull "No testRunId returned", testRunId

		Map runInfo = client.getTestRunInfo(testRunId)
		assertEquals projectName, runInfo.project
		assertNotNull runInfo.dateExecuted
		assertTrue runInfo != ""
		assertNull  runInfo.milestone
		assertNull runInfo.build
		assertNull runInfo.targetEnv
	}


	void testGetTestRunIdWithoutProject() {
		def msg = shouldFail(IllegalArgumentException) {
			client.getTestRunId(null, null, null, null, null)
		}
		assertEquals "Wrong error message", "Project argument must be a valid cuanto project", msg
	}


	void testGetTestRunIdWithBogusProject() {
		def projName = "Bogus Foobar"
		def msg = shouldFail(IllegalArgumentException) {
			client.getTestRunId(projName, null, null, null, null)
		}
		assertEquals "Wrong error message", "Couldn't find project named ${projName}", msg 
	}


	void testGetTestRunWithParams() {
		def milestone = wordGen.getSentence(2)
		def build = wordGen.word
		def targetEnv = wordGen.getSentence(2)
		Long testRunId = client.getTestRunId(projectName, null, milestone, build, targetEnv)
		assertNotNull "No testRunId returned", testRunId

		Map runInfo = client.getTestRunInfo(testRunId)
		assertEquals projectName, runInfo.project
		assertNotNull runInfo.dateExecuted
		assertEquals milestone, runInfo.milestone
		assertEquals build, runInfo.build
		assertEquals targetEnv, runInfo.targetEnv
	}


	/*  boolean submit(File file, Long testRunId)  */
	void testSubmitSingleSuite() {
		Long testRunId = client.getTestRunId(projectName, null, "test milestone", "test build", "test env")
		File fileToSubmit = getFile("junitReport_single_suite.xml")
		client.submit(fileToSubmit, testRunId)
		def stats = client.getTestRunStats(testRunId)
		assertEquals "34", stats.tests
		assertEquals "3", stats.failed
		assertEquals "31", stats.passed
	}

	
	void testSubmitMultipleSuite() {
		Long testRunId = client.getTestRunId(projectName, null, "test milestone", "test build", "test env")
		File fileToSubmit = getFile("junitReport_multiple_suite.xml")
		client.submit(fileToSubmit, testRunId)
		def stats = client.getTestRunStats(testRunId)
		assertEquals "56", stats.tests
		assertEquals "15", stats.failed
		assertEquals "41", stats.passed
	}


	void testSubmitMultipleFiles() {
		Long testRunId = client.getTestRunId(projectName, null, "test milestone", "test build", "test env")
		def filesToSubmit = []
		filesToSubmit << getFile("junitReport_single_suite.xml")
		filesToSubmit << getFile("junitReport_single_suite_2.xml")

		client.submit(filesToSubmit, testRunId)
		def stats = client.getTestRunStats(testRunId)
		assertEquals "68", stats.tests
		assertEquals "6", stats.failed
		assertEquals "62", stats.passed
	}

	
	void testDefaultDateFormat() {
		def expDate = "2008-03-05 20:55:00"
		Long testRunId = client.getTestRunId(projectName, expDate, "test milestone", "test build", "test env")
		def runInfo = client.getTestRunInfo(testRunId)
		assertEquals "Wrong date", expDate, runInfo.dateExecuted
	}


	void testDifferentDateFormat() {
		CuantoClient altDateClient = new CuantoClient(dateFormat: "MM-dd-yyyy HH:mm:ss", cuantoUrl: serverUrl,
			userId: testUser, password: testPassword)
		def altFormattedDate = "03-05-2008 20:55:00"
		Long testRunId = altDateClient.getTestRunId(projectName, altFormattedDate, "test milestone", "test build", "test env")
		def runInfo = client.getTestRunInfo(testRunId)
		assertEquals "Wrong date", "2008-03-05 20:55:00", runInfo.dateExecuted
	}


	void testUpdateResults() {
		Long testRunId = client.getTestRunId(projectName, null, "test milestone", "test build", "test env")
		
		ParsableTestOutcome outcomeOne = new ParsableTestOutcome()
		def pkgName = "cuanto.test.packageOne.SampleTest"
		def testName = "LeCarre"
		outcomeOne.testCase = new ParsableTestCase(packageName: pkgName, 'testName': testName)
		outcomeOne.testResult = "Fail"
		def outcomeOneId = client.submit(outcomeOne, testRunId)
		def retrievedOutcomeOne = client.getTestOutcome(outcomeOneId)
		assertEquals "Wrong result", "Fail", retrievedOutcomeOne.testResult
		assertEquals "Wrong test pkg", pkgName, retrievedOutcomeOne.testCase.packageName
		assertEquals "Wrong test name", testName, retrievedOutcomeOne.testCase.testName
		assertNull "Wrong test output", retrievedOutcomeOne.testOutput

		File fileToSubmit = getFile("junitReport_multiple_suite.xml")
		client.submit(fileToSubmit, testRunId)
		def stats = client.getTestRunStats(testRunId)
		assertEquals "Wrong number of total tests", "56", stats.tests
		assertEquals "Wrong number of total failures", "15", stats.failed
		assertEquals "Wrong number of passing tests", "41", stats.passed
		def retrievedOutcomeTwo = client.getTestOutcome(outcomeOneId)

		assertEquals "Wrong result", "Fail", retrievedOutcomeTwo.testResult
		assertEquals "Wrong test pkg", pkgName, retrievedOutcomeTwo.testCase.packageName
		assertEquals "Wrong test name", testName, retrievedOutcomeTwo.testCase.testName
		assertTrue "Wrong test output: ${retrievedOutcomeTwo.testOutput}",
			retrievedOutcomeTwo.testOutput.contains("junit.framework.AssertionFailedError: LeCarre failed")

		// submit all results again, just to be sure they haven't changed
		client.submit(fileToSubmit, testRunId)
		stats = client.getTestRunStats(testRunId)
		assertEquals "Wrong number of total tests", "56", stats.tests
		assertEquals "Wrong number of total failures", "15", stats.failed
		assertEquals "Wrong number of passing tests", "41", stats.passed
	}

	
	File getFile(String filename) {
		def path = "grails/test/resources"
		File myFile = new File("${path}/${filename}")
		assertTrue "file not found: ${myFile.absoluteFile}", myFile.exists()
		return myFile
	}
}