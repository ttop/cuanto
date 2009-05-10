package cuanto

import cuanto.ProjectController
import cuanto.TestType
import cuanto.test.TestObjects
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

class ProjectControllerTests extends GroovyTestCase {

	def dataService
	def initializationService

	TestObjects to

	@Override
	void setUp() {
		initializationService.initializeAll()
		to = new TestObjects()
		to.dataService = dataService
	}

	void testGet() {
		def proj = to.getProject()
		proj.projectGroup = to.getProjectGroup("Sample")
		proj.projectKey = "foo&bar!"
		proj.bugUrlPattern = "http://foo.com:1337/bar?bug={BUG}"
		proj.testCases = [to.getTestCase(proj), to.getTestCase(proj)]
		proj.testCaseFormatKey = "a.b.c"
		proj.testType = TestType.findByName("JUnit")
		proj.save()

		def projectController = new ProjectController()

		projectController.params.id = proj.id
		projectController.params.format = 'json'
		projectController.get()

		def response = JSON.parse(projectController.response.contentAsString)
		assertJsonEquals proj.id, response.id
		assertJsonEquals proj.name, response.name
		assertJsonEquals proj.projectGroup.id, response.projectGroup.id
		assertJsonEquals proj.projectGroup.name, response.projectGroup.name
		assertJsonEquals proj.projectKey, response.projectKey
		assertJsonEquals proj.bugUrlPattern,  response.bugUrlPattern
//		assertJsonEquals proj.testCases,  response.testCases
		assertJsonEquals proj.testCaseFormatKey, response.testCaseFormatKey
		assertJsonEquals proj.testType.id, response.testType.id
		assertJsonEquals proj.testType.name, response.testType.name
	}

	private void assertJsonEquals(expected, actual) {
		def normalizedExpected = expected ?: JSONObject.NULL
		def normalizedActual = actual ?: JSONObject.NULL
		assertEquals normalizedExpected, normalizedActual
	}
}
