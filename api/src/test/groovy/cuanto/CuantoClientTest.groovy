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
	def projectKey

	@Override
	void setUp() {
		projectName = wordGen.getSentence(3).trim()
		projectKey = wordGen.getSentence(3).replaceAll("\\s+", "").trim()
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
		assertEquals "Wrong error message", "Unable to locate project with the project key or full title of ${projName}", msg 
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

		Map<String, String> links = ["Info":"http://projectInfo",
			"Code Coverage": "http://cobertura"] as Map<String, String>;
		testRunId = client.getTestRunId(projectName, null, milestone, build, targetEnv, links)
		runInfo = client.getTestRunInfo(testRunId)

		ParsableTestRun testRun = client.getTestRun(testRunId)
		assertEquals projectKey, testRun.project
		assertNotNull testRun.dateExecuted
		assertEquals milestone, testRun.milestone
		assertEquals build, testRun.build
		assertEquals targetEnv, testRun.targetEnv
		assertNotNull testRun.links
		assertTrue "Valid", testRun.valid
		assertEquals 2, testRun.links.size()

		def codeCov = testRun.links.find {
			it.key == "Code Coverage"
		}
		assertNotNull codeCov
		assertEquals "http://cobertura", codeCov.value

		def info = testRun.links.find {
			it.key == "Info"
		}
		assertNotNull info
		assertEquals "http://projectInfo", info.value
	}


	/*  boolean submit(File file, Long testRunId)  */
	void testSubmitSingleSuite() {
		Long testRunId = client.getTestRunId(projectName, null, "test milestone", "test build", "test env")
		File fileToSubmit = getFile("junitReport_single_suite.xml")
		client.submit(fileToSubmit, testRunId)
		def stats = waitForTestRunStats(client, testRunId, "34")
		assertEquals "34", stats.tests
		assertEquals "3", stats.failed
		assertEquals "31", stats.passed
	}

	
	void testSubmitMultipleSuite() {
		Long testRunId = client.getTestRunId(projectName, null, "test milestone", "test build", "test env")
		File fileToSubmit = getFile("junitReport_multiple_suite.xml")
		client.submit(fileToSubmit, testRunId)
		def stats = waitForTestRunStats(client, testRunId, "56")
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
		def stats = waitForTestRunStats(client, testRunId, "68")
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
		CuantoClient altDateClient = new CuantoClient(dateFormat: "MM-dd-yyyy HH:mm:ss", cuantoUrl: serverUrl)
		def altFormattedDate = "03-05-2008 20:55:00"
		Long testRunId = altDateClient.getTestRunId(projectName, altFormattedDate, "test milestone", "test build", "test env")
		def runInfo = client.getTestRunInfo(testRunId)
		assertEquals "Wrong date", "2008-03-05 20:55:00", runInfo.dateExecuted
	}

	
	File getFile(String filename) {
		def path = "grails/test/resources"
		File myFile = new File("${path}/${filename}")
		assertTrue "file not found: ${myFile.absoluteFile}", myFile.exists()
		return myFile
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