import cuanto.test.TestObjects
import cuanto.TestCase
import cuanto.Project
import cuanto.ProjectGroup
import cuanto.TestType


/**
 * User: Todd Wells
 * Date: Sep 19, 2009
 * Time: 9:22:28 AM
 * 
 */

public class TestCaseUnitTests extends GroovyTestCase{

	TestObjects to
	cuanto.test.WordGenerator wordGen

	void setUp() {
		to = new TestObjects()
		wordGen = TestObjects.wordGen
	}

	void testToParsableTestCase() {
		def projectGroup = new ProjectGroup(name: wordGen.word)
		def proj = new Project(name: wordGen.word, 'projectGroup': projectGroup, 'projectKey': wordGen.word,
			testType: new TestType(name: "JUnit"))

		TestCase testCase = new TestCase(project: proj)
		testCase.testName = wordGen.getCamelWords(3)
		testCase.packageName = wordGen.getCamelWords(3)
		testCase.fullName = testCase.packageName + "." + testCase.testName
		testCase.description = wordGen.getSentence(8)

		def parsableTestCase = testCase.toParsableTestCase()

		["testName", "packageName", "fullName", "description"].each { field ->
			assertEquals "Wrong value for ${field}", testCase.getProperty(field), parsableTestCase.getProperty(field)
		}

		assertEquals "Wrong project", proj.projectGroup.name + ": " + proj.name, parsableTestCase.project

	}
}