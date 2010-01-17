package cuanto

import cuanto.WordGenerator
import cuanto.api.*

/**
 * User: Todd Wells
 * Date: Sep 29, 2008
 * Time: 5:54:29 PM
 * 
 */
class CuantoClientTest extends GroovyTestCase {

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
		def project = client.getProject(localProjectId)
		assertEquals "wrong project name", localName, project.name
		assertEquals "wrong project key", projKey, project.projectKey
		client.deleteProject(localProjectId)
		assertNull client.getProject(localProjectId)
	}


	void testCreateAndDeleteProjectTwo() {
		def localName = wordGen.getSentence(3)
		def projKey = wordGen.getSentence(2).replaceAll("\\s+", "")
		def proj = new Project(name:localName, projectKey: projKey, testType: "JUnit")
		def localProjectId = client.createProject(proj)
		def fetchedProject = client.getProject(localProjectId)
		assertEquals "wrong project name", localName, fetchedProject.name
		assertEquals "wrong project key", projKey, fetchedProject.projectKey
		client.deleteProject(localProjectId)
		assertNull client.getProject(localProjectId)
	}


	void testCreateProjectTooBig() {
		def message = shouldFail(CuantoClientException) {
			client.createProject("Long name", "abcdefghijklmnopqrstuvwxyz", "JUnit")
		}
		assertTrue "Wrong error message: $message", message.contains("projectKey.maxSize")
	}

	
	void testGetTestRun() {
		Long testRunId = client.createTestRun(new TestRun(projectKey: projectName))
		assertNotNull "No testRunId returned", testRunId
		def fetchedTestRun = client.getTestRun(testRunId)
		assertEquals projectKey, fetchedTestRun.projectKey
		assertNotNull fetchedTestRun.dateExecuted
	}


	void testCreateTestRunWithoutProject() {
		def msg = shouldFail(IllegalArgumentException) {
			client.createTestRun(new TestRun())
		}
		assertEquals "Wrong error message", "Project argument must be a valid cuanto project key", msg
	}


	void testCreateTestRunWithBogusProject() {
		def projName = "Bogus Foobar"
		def msg = shouldFail(IllegalArgumentException) {
			client.createTestRun(new TestRun(projName))
		}
		assertEquals "Wrong error message", "Unable to locate project with the project key or full title of ${projName}", msg 
	}


	void testGetTestRunWithPropertiesAndLinks() {
		TestRun run = new TestRun(projectName)
		run.links << new Link("Code Coverage", "http://cobertura")
		run.links << new Link("Info", "http://projectInfo")

		run.testProperties << new TestProperty("Artist", "Da Vinci")
		run.testProperties << new TestProperty("Musician", "Paul McCartney")

		Long testRunId = client.createTestRun(run)
		assertNotNull "No testRunId returned", testRunId

		TestRun testRun = client.getTestRun(testRunId)
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


	void testCreateTestAndDeleteTestRun() {
		def createdTestRun = new TestRun(projectKey)
		createdTestRun.links << new Link("Code Coverage", "http://cobertura")
		createdTestRun.links << new Link("Info", "http://projectInfo")
		createdTestRun.testProperties << new TestProperty("Artist", "Da Vinci")
		createdTestRun.testProperties << new TestProperty("Musician", "Paul McCartney")

		Long testRunId = client.createTestRun(createdTestRun)
		assertNotNull "No testRunId returned", testRunId

		TestRun testRun = client.getTestRun(testRunId)
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

		client.deleteTestRun testRun.id
		def fetchedRun = client.getTestRun(testRun.id)
		assertNull fetchedRun
	}


	void testSubmitSingleSuite() {
		Long testRunId = client.createTestRun(new TestRun(projectName))
		File fileToSubmit = getFile("junitReport_single_suite.xml")
		client.submitFile(fileToSubmit, testRunId)
		def stats = waitForTestRunStats(client, testRunId, "34")
		assertEquals "34", stats.tests
		assertEquals "3", stats.failed
		assertEquals "31", stats.passed
	}

	
	void testSubmitMultipleSuite() {
		Long testRunId = client.createTestRun(new TestRun(projectName))
		File fileToSubmit = getFile("junitReport_multiple_suite.xml")
		client.submitFile(fileToSubmit, testRunId)
		def stats = waitForTestRunStats(client, testRunId, "56")
		assertEquals "56", stats.tests
		assertEquals "15", stats.failed
		assertEquals "41", stats.passed
	}


	void testSubmitMultipleFiles() {
		Long testRunId = client.createTestRun(new TestRun(projectName))
		def filesToSubmit = []
		filesToSubmit << getFile("junitReport_single_suite.xml")
		filesToSubmit << getFile("junitReport_single_suite_2.xml")

		client.submitFiles(filesToSubmit, testRunId)
		def stats = waitForTestRunStats(client, testRunId, "68")
		assertEquals "68", stats.tests
		assertEquals "6", stats.failed
		assertEquals "62", stats.passed
	}


	void testSubmitOneOutcomeWithTestRun() {
		def testRunId = client.createTestRun(new TestRun(projectKey))

		TestCase testCase = new TestCase()
		testCase.packageName = "foo.bar.blah"
		testCase.testName = "submitOneTest"

		TestOutcome outcome = new TestOutcome()
		outcome.testCase = testCase
		outcome.testResult = "Pass"

		def outcomeId = client.createTestOutcomeForTestRun(outcome, testRunId)
		assertNotNull outcomeId

		def stats = waitForTestRunStats(client, testRunId, "1")
		assertNotNull stats
		assertEquals "Wrong total tests", "1", stats?.tests
		assertEquals "Wrong passed", "1", stats?.passed
		assertEquals "Wrong failed", "0", stats?.failed

		client.deleteTestOutcome outcomeId
		def fetchedOutcome = client.getTestOutcome(outcomeId)
		assertEquals "Outcome not deleted", null, fetchedOutcome
	}


	void testUpdateOneOutcome() {
		TestOutcome origApiOutcome = new TestOutcome()
		origApiOutcome.testCase = new TestCase(project: projectKey, packageName: "foo.bar", testName: "testUpdate")
		origApiOutcome.setDuration 200
		origApiOutcome.setTestResult "Pass"
		origApiOutcome.setNote wordGen.getSentence(13)

		def testRunId = client.createTestRun(new TestRun(projectKey))
		def outcomeId = client.createTestOutcomeForTestRun(origApiOutcome, testRunId)

		TestOutcome changedApiOutcome = new TestOutcome()
		["testCase", "duration", "testResult", "note"].each {
			changedApiOutcome.setProperty it, origApiOutcome.getProperty(it)
		}

		changedApiOutcome.note = "New Note!"
		changedApiOutcome.testOutput = wordGen.getSentence(15)
		changedApiOutcome.id = outcomeId
		changedApiOutcome.testResult = "Fail"

		client.updateTestOutcome(changedApiOutcome)

		TestOutcome fetchedApiOutcome = client.getTestOutcome(outcomeId)
		assertEquals "Note", changedApiOutcome.note, fetchedApiOutcome.note
		assertEquals "output", changedApiOutcome.testOutput, fetchedApiOutcome.testOutput
		assertEquals "test result", changedApiOutcome.testResult, fetchedApiOutcome.testResult 
	}
	

	void testSubmitOneOutcomeWithoutTestRun() {
		TestCase testCase = new TestCase()
		testCase.packageName = "foo.bar.blah"
		testCase.testName = "submitOneTest"
		testCase.fullName = testCase.packageName + "." + testCase.testName

		TestOutcome outcome = new TestOutcome()
		outcome.testCase = testCase
		outcome.testResult = "Pass"

		def outcomeId = client.createTestOutcomeForProject(outcome, projectId)
		assertNotNull outcomeId

		def fetchedOutcome = client.getTestOutcome(outcomeId)
		assertEquals outcome.testCase.packageName, fetchedOutcome.testCase.packageName
		assertEquals outcome.testCase.testName, fetchedOutcome.testCase.testName
		assertEquals outcome.testResult, fetchedOutcome.testResult
	}


	void testSubmitManyMultiThreaded() {
		final int numSubmissions = 100
		final int numThreads = 5

		def testRunId = client.createTestRun(new TestRun(projectKey))
		Long submitTime = 0
		int idx = 0
		def totalStart = new Date().time
		while (idx < numSubmissions) {
			def th = []
			1.upto(numThreads){
				th << Thread.start {
					TestCase testCase = new TestCase()
					testCase.packageName = "foo.bar.blah"
					testCase.testName = wordGen.getSentence(3).replaceAll(" ", "")

					TestOutcome outcome = new TestOutcome()
					outcome.testCase = testCase
					outcome.testResult = "Pass"

					long start = System.currentTimeMillis();
					client.createTestOutcomeForTestRun(outcome, testRunId)
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