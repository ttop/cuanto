package cuanto

import cuanto.Project
import cuanto.TestCase
import cuanto.TestRun
import cuanto.TestType
import cuanto.test.TestObjects
import cuanto.ParsingException

class ParsingServiceTests extends GroovyTestCase {
	def parsingService
	def dataService
	def initializationService
	def testOutcomeService
	def testRunService
	def testResultService

	TestObjects fakes = new TestObjects()


	@Override
	void setUp() {
		initializationService.initializeAll()
		fakes.dataService = dataService
	}


	void testFindCases() {
		Project project = new Project(name: "ParsingServiceTestProject", projectKey: fakes.getProjectKey())
		project.testType = TestType.findByName("JUnit")
		if (!project.save()) {
			reportErrors project
		}

		TestCase tCase = new TestCase()
		tCase.packageName = "foo"
		tCase.testName = "Bar"
		tCase.fullName = tCase.packageName + "." + tCase.testName

		tCase.project = project

		if (!project.addToTestCases(tCase).save()) {
			reportErrors project
		}

		if (!project.addToTestCases(tCase).save(flush: true)) {
			reportErrors project
		}

		TestCase t2 = new TestCase()
		t2.packageName = "foo"

		t2.testName = "Bar"
		t2.fullName = t2.packageName + "." + t2.testName
		t2.project = project

		TestCase myCase = dataService.findMatchingTestCaseForProject(project, tCase)
		assertEquals("wrong fullName", tCase.fullName, myCase.fullName)
		myCase = dataService.findMatchingTestCaseForProject(project, t2)
		assertEquals("wrong fullName", tCase.fullName, myCase.fullName)
	}

	
	void testParsingRegistryInjection() {
		assertTrue "No parsers found", parsingService.testParserRegistry?.parsers?.size() > 0
	}	

	void testParseFileFromStream()
	{
		Project proj = fakes.getProject()
		proj.testType = TestType.findByName("JUnit")
		proj.save()

		TestRun testRun = fakes.getTestRun(proj, "foobar")

		if (!testRun.save()) {
			reportError testRun
		}

		getFile("junitReport_single_suite.xml").withInputStream {
			TestRun tr = parsingService.parseFileFromStream(it, testRun.id)
			println tr
		}
	}


	void testGetParser() {
		shouldFail(ParsingException) {
			parsingService.getParser(new TestType(name: "foobar"))
		}
	}

	private File getFile(filename) {
		File file = new File("test/resources/${filename}")
		assertTrue("Couldn't find file: ${file.toString()}", file.exists())
		return file
	}

	def reportErrors(obj) {
		def message = ""
		obj.errors.allErrors.each {
			message += it.toString() + "\n"
		}
		fail message
	}
}