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
	Long projectId
	String projectKey

	@Override
	void setUp() {
		projectName = wordGen.getSentence(3).trim()
		projectKey = wordGen.getSentence(3).replaceAll("\\s+", "").trim()
		if (projectKey.length() > 25) {
			projectKey = projectKey.substring(0, 24)
		}
		projectId = client.createProject(new Project(name: projectName, 'projectKey': projectKey, testType: "JUnit"))
	}


	@Override
	void tearDown(){
		client.deleteProject(projectId)
	}


	void testCreateAndDeleteProject() {
		def localName = wordGen.getSentence(3)
		def projKey = wordGen.getSentence(2).replaceAll("\\s+", "")
		def localProjectId = client.createProject(new Project(name:localName, 'projectKey': projKey, testType: "JUnit"))
		def project = client.getProject(localProjectId)
		assertEquals "wrong project name", localName, project.name
		assertEquals "wrong project key", projKey, project.projectKey

		project = client.getProject(projKey)
		assertEquals "wrong project name", localName, project.name
		assertEquals "wrong project key", projKey, project.projectKey

		client.deleteProject(localProjectId)
		assertNull client.getProject(localProjectId)
		assertNull client.getProject(projKey)
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
			client.createProject(new Project(name: "Long name", 'projectKey': "abcdefghijklmnopqrstuvwxyz",
				testType: "JUnit"))
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

		TestRun runTwo = new TestRun(projectName)
		runTwo.testProperties << new TestProperty("Artist", "Da Vinci")
		runTwo.testProperties << new TestProperty("Musician", "Muddy Waters")
		client.createTestRun(runTwo)

		List<TestRun> runs = client.getTestRunsWithProperties(projectId, runTwo.testProperties)
		assertEquals "Wrong number of test runs", 1, runs.size()
		assertEquals "Wrong test run", runTwo.id, runs[0].id

		runs = client.getTestRunsWithProperties(projectId, testRun.testProperties)
		assertEquals "Wrong number of test runs", 1, runs.size()
		assertEquals "Wrong test run", testRun.id, runs[0].id

		runs = client.getTestRunsWithProperties(projectId, [run.testProperties[0]])
		assertEquals "Wrong number of test runs", 2, runs.size()
		assertNotNull runs.find { it.id == testRun.id }
		assertNotNull runs.find { it.id == runTwo.id }

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


	void testGetTestCase() {
		Long testRunId = client.createTestRun(new TestRun(projectName))
		File fileToSubmit = getFile("junitReport_single_suite.xml")
		client.submitFile(fileToSubmit, testRunId)
		def stats = waitForTestRunStats(client, testRunId, "34")

		def fetchedCase = client.getTestCase(projectId, "cuanto.test.packageOne.SampleTest", "Stephenson")
		assertNotNull "No test case fetched", fetchedCase
		assertEquals "Wrong package", "cuanto.test.packageOne.SampleTest", fetchedCase.packageName
		assertEquals "Wrong test name", "Stephenson", fetchedCase.testName

		assertNull client.getTestCase(projectId, "non.existent.TestCase", "Stephenson")
		assertNull client.getTestCase(projectId, "cuanto.test.packageOne.SampleTest", "Stephenson", "foo")
		
		shouldFail(CuantoClientException) {
			client.getTestCase(3434L, "blah", "blah")
		}

		def testCases = []
		1.upto(3) {
			def tc = new TestCase(packageName: wordGen.getSentence(3).replaceAll(" ", "."),
				testName: wordGen.getSentence(2), parameters: wordGen.getSentence(3))
			testCases << tc
			def testOutcome = new TestOutcome(testCase: tc, testResult: "Pass")
			client.createTestOutcomeForProject(testOutcome, projectId)
		}

		testCases.each { TestCase tc ->
			def fetchedTc = client.getTestCase(projectId, tc.packageName, tc.testName,  tc.parameters)
			assertEquals "package name", tc.packageName, fetchedTc.packageName
			assertEquals "test name", tc.testName, fetchedTc.testName
			assertEquals "parameters", tc.parameters, fetchedTc.parameters
		}
	}

	
	void testSubmitSingleSuite() {
		Long testRunId = client.createTestRun(new TestRun(projectName))
		File fileToSubmit = getFile("junitReport_single_suite.xml")
		client.submitFile(fileToSubmit, testRunId)
		def stats = waitForTestRunStats(client, testRunId, 34)
		assertEquals 34, stats.tests
		assertEquals 3, stats.failed
		assertEquals 31, stats.passed
	}

	
	void testSubmitMultipleSuite() {
		Long testRunId = client.createTestRun(new TestRun(projectName))
		File fileToSubmit = getFile("junitReport_multiple_suite.xml")
		client.submitFile(fileToSubmit, testRunId)
		def stats = waitForTestRunStats(client, testRunId, 56)
		assertEquals 56, stats.tests
		assertEquals 15, stats.failed
		assertEquals 41, stats.passed
	}


	void testSubmitMultipleFiles() {
		Long testRunId = client.createTestRun(new TestRun(projectName))
		def filesToSubmit = []
		filesToSubmit << getFile("junitReport_single_suite.xml")
		filesToSubmit << getFile("junitReport_single_suite_2.xml")

		client.submitFiles(filesToSubmit, testRunId)
		def stats = waitForTestRunStats(client, testRunId, 68)
		assertEquals 68, stats.tests
		assertEquals 6, stats.failed
		assertEquals 62, stats.passed
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

		def stats = waitForTestRunStats(client, testRunId, 1)
		assertNotNull stats
		assertEquals "Wrong total tests", 1, stats?.tests
		assertEquals "Wrong passed", 1, stats?.passed
		assertEquals "Wrong failed", 0, stats?.failed

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


	void testGetTestOutcome() {
		TestOutcome origApiOutcome = new TestOutcome()
		origApiOutcome.testCase = new TestCase(project: projectKey, packageName: "foo.bar", testName: "testUpdate")
		origApiOutcome.setDuration 200
		origApiOutcome.setTestResult "Pass"
		origApiOutcome.setNote wordGen.getSentence(13)

		def testRunId = client.createTestRun(new TestRun(projectKey))
		def outcomeId = client.createTestOutcomeForTestRun(origApiOutcome, testRunId)
		def testCase = client.getTestCase(projectId, origApiOutcome.testCase.packageName, origApiOutcome.testCase.testName)
		def fetchedOutcomes = client.getTestOutcomes(testRunId, testCase.id)

		assertNotNull fetchedOutcomes
		assertEquals 1, fetchedOutcomes.size()
		assertEquals "Outcome ID", outcomeId, fetchedOutcomes[0].id
	}

	
	void testUpdateTestRun() {
		shouldFail(NullPointerException) {
			client.updateTestRun(null) 
		}
		
		def createdTestRun = new TestRun(projectKey)
		createdTestRun.links << new Link("Code Coverage", "http://cobertura")
		createdTestRun.links << new Link("Info", "http://projectInfo")
		createdTestRun.testProperties << new TestProperty("Artist", "Da Vinci")
		createdTestRun.testProperties << new TestProperty("Musician", "Paul McCartney")
		createdTestRun.note = wordGen.getSentence(3)

		Long testRunId = client.createTestRun(createdTestRun)
		assertNotNull "No testRunId returned", testRunId

		TestRun testRun = new TestRun(projectKey)
		testRun.links << new Link("Info", "http://projectInfo2")
		testRun.testProperties << new TestProperty("Musician", "John Lennon")
		testRun.note = wordGen.getSentence(3)
		testRun.id = testRunId

		client.updateTestRun(testRun)

		TestRun fetchedRun = client.getTestRun(testRunId)
		assertEquals "links size", 1, fetchedRun.links?.size()
		assertEquals "link", testRun.links[0].url, fetchedRun.links[0].url
	    assertEquals "testProperties size", 1, fetchedRun.testProperties?.size()
		assertEquals "testProperty", testRun.testProperties[0].value, fetchedRun.testProperties[0].value
		assertEquals "note", testRun.note, fetchedRun.note
	}


	File getFile(String filename) {
		def path = "grails/test/resources"
		File myFile = new File("${path}/${filename}")
		assertTrue "file not found: ${myFile.absoluteFile}", myFile.exists()
		return myFile
	}


	TestRunStats waitForTestRunStats(CuantoClient client, Long testRunId, totalTests) {
		def secToWait = 30
		def interval = 500
		def start = new Date().time
		TestRunStats stats = client.getTestRunStats(testRunId)
		while (stats.tests != totalTests && new Date().time - start < secToWait * 1000) {
			sleep interval
			stats = client.getTestRunStats(testRunId)
		}
		return stats
	}


	void testGetAllTestOutcomes() {
		Long testRunId = client.createTestRun(new TestRun(projectName))
		File fileToSubmit = getFile("junitReport_multiple_suite.xml")
		client.submitFile(fileToSubmit, testRunId)

		def stats = waitForTestRunStats(client, testRunId, 56)
		assertEquals 56, stats.tests

		def outcomes = client.getAllTestOutcomes(testRunId)
		assertEquals "Wrong number of outcomes", 56, outcomes.size()

		def failed = 0
		def passed = 0
		outcomes.each { TestOutcome out ->
			if (out.testResult == "Fail" || out.testResult == "Error") {
				failed++
			} else if (out.testResult == "Pass") {
				passed++
			}
		}
		assertEquals "failed count", 15, failed
		assertEquals "passed count", 41, passed
	}


}