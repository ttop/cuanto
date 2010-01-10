package cuanto

import cuanto.CuantoClient
import cuanto.WordGenerator
import groovy.mock.interceptor.StubFor
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.PostMethod
import cuanto.api.Link
import cuanto.api.TestProperty

/**
 * User: Todd Wells
 * Date: Sep 29, 2008
 * Time: 5:54:29 PM
 * 
 */
class CuantoClientTest extends GroovyTestCase{

	String serverUrl = "http://localhost:8080/cuanto"
	CuantoClient client = new CuantoClient(serverUrl)

	WordGenerator wordGen = new WordGenerator()
	String projectName
	def projectId
	String projectKey

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

	
	void testGetTestRun() {
		Long testRunId = client.createTestRun(new ParsableTestRun(projectKey: projectName))
		assertNotNull "No testRunId returned", testRunId
		def fetchedTestRun = client.getTestRun(testRunId)
		assertEquals projectKey, fetchedTestRun.projectKey
		assertNotNull fetchedTestRun.dateExecuted
	}


	void testCreateTestRunWithoutProject() {
		def msg = shouldFail(IllegalArgumentException) {
			client.createTestRun(new ParsableTestRun())
		}
		assertEquals "Wrong error message", "Project argument must be a valid cuanto project key", msg
	}


	void testCreateTestRunWithBogusProject() {
		def projName = "Bogus Foobar"
		def msg = shouldFail(IllegalArgumentException) {
			client.createTestRun(new ParsableTestRun(projName))
		}
		assertEquals "Wrong error message", "Unable to locate project with the project key or full title of ${projName}", msg 
	}


	void testGetTestRunWithPropertiesAndLinks() {
		ParsableTestRun run = new ParsableTestRun(projectName)
		run.links << new Link("Info", "http://projectInfo")
		run.links << new Link("Code Coverage", "http://cobertura")

		run.testProperties << new TestProperty("Artist", "Da Vinci")
		run.testProperties << new TestProperty("Musician", "Paul McCartney")

		Long testRunId = client.createTestRun(run)
		assertNotNull "No testRunId returned", testRunId

		ParsableTestRun testRun = client.getTestRun(testRunId)
		assertEquals projectKey, testRun.projectKey
		assertNotNull testRun.dateExecuted
		assertTrue "Valid", testRun.valid

		assertNotNull testRun.links
		assertEquals 2, testRun.links.size()

		testRun.links.eachWithIndex { link, index ->
			assertEquals run.links[index].description, link.description
			assertEquals run.links[index].url, link.url
		}

		assertEquals 2, testRun.testProperties?.size()
		testRun.testProperties.eachWithIndex { prop, index ->
			assertEquals run.testProperties[index].name, prop.name
			assertEquals run.testProperties[index].value, prop.value
		}
	}


	void testCreateTestRun() {
		def createdTestRun = new ParsableTestRun(projectKey)
		createdTestRun.links << new Link("Info", "http://projectInfo")
		createdTestRun.links << new Link("Code Coverage", "http://cobertura")
		createdTestRun.testProperties << new TestProperty("Artist", "Da Vinci")
		createdTestRun.testProperties << new TestProperty("Musician", "Paul McCartney")

		Long testRunId = client.createTestRun(createdTestRun)
		assertNotNull "No testRunId returned", testRunId

		ParsableTestRun testRun = client.getTestRun(testRunId)
		assertEquals projectKey, testRun.projectKey
		assertNotNull testRun.dateExecuted
		assertTrue "Valid", testRun.valid

		assertEquals 2, testRun.links?.size()
		testRun.links.eachWithIndex {link, index ->
			assertEquals createdTestRun.links[index].description, link.description
			assertEquals createdTestRun.links[index].url, link.url
		}

		assertEquals 2, testRun.testProperties?.size()
		testRun.testProperties.eachWithIndex {prop, index ->
			assertEquals createdTestRun.testProperties[index].name, prop.name
			assertEquals createdTestRun.testProperties[index].value, prop.value
		}
	}


	void testSubmitSingleSuite() {
		Long testRunId = client.createTestRun(new ParsableTestRun(projectName))
		File fileToSubmit = getFile("junitReport_single_suite.xml")
		client.submit(fileToSubmit, testRunId)
		def stats = waitForTestRunStats(client, testRunId, "34")
		assertEquals "34", stats.tests
		assertEquals "3", stats.failed
		assertEquals "31", stats.passed
	}

	
	void testSubmitMultipleSuite() {
		Long testRunId = client.createTestRun(new ParsableTestRun(projectName))
		File fileToSubmit = getFile("junitReport_multiple_suite.xml")
		client.submit(fileToSubmit, testRunId)
		def stats = waitForTestRunStats(client, testRunId, "56")
		assertEquals "56", stats.tests
		assertEquals "15", stats.failed
		assertEquals "41", stats.passed
	}


	void testSubmitMultipleFiles() {
		Long testRunId = client.createTestRun(new ParsableTestRun(projectName))
		def filesToSubmit = []
		filesToSubmit << getFile("junitReport_single_suite.xml")
		filesToSubmit << getFile("junitReport_single_suite_2.xml")

		client.submit(filesToSubmit, testRunId)
		def stats = waitForTestRunStats(client, testRunId, "68")
		assertEquals "68", stats.tests
		assertEquals "6", stats.failed
		assertEquals "62", stats.passed
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