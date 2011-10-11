
/*
 Copyright (c) 2010 Todd Wells.

This file is part of Cuanto, a test results repository and analysis program.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package cuanto

import cuanto.TestOutcome
import cuanto.TestRun
import cuanto.formatter.TestNameFormatter
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import cuanto.parsers.ParsableProject
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

class ApiController {

	def testRunService
	def testOutcomeService
	def parsingService
	def dataService
	def projectService
	def statisticService

	static def allowedMethods = [deleteTestRun: 'POST', addProject: 'POST', deleteProject: 'POST']

    def index = { }


	def addTestRun = {
		try {

			TestRun testRun = parsingService.parseTestRun(request.JSON)
			dataService.saveTestRun(testRun)
			statisticService.queueTestRunStats(testRun)
			// todo: wait for stats to be calculated before returning?
			response.status = response.SC_CREATED
			render testRun.toJSONMap() as JSON
		} catch (CuantoException e) {
			response.status = response.SC_INTERNAL_SERVER_ERROR
			render "${e.getClass().canonicalName}: ${e.getMessage()}"

		} catch (Exception e) {
			response.status = response.SC_INTERNAL_SERVER_ERROR
			render "Unknown error: ${e.getMessage()}"
		}
	}


	def updateTestRun = {
		try {
			TestRun testRun = parsingService.parseTestRun(request.JSON)
			if (testRun) {
				testRunService.update(testRun)
				statisticService.queueTestRunStats(testRun)
				render "TestRun updated"
			} else {
				response.status = response.SC_INTERNAL_SERVER_ERROR
				render "Didn't successfully parse TestRun"
			}
		} catch (Exception e) {
		    response.status = response.SC_INTERNAL_SERVER_ERROR
			render "Unknown error: ${e.getMessage()}"
		}
	}


	def getTestRun = {
		TestRun tr = TestRun.get(params.id)
		if (tr) {
			render tr.toJSONMap() as JSON
		} else {
			response.status = response.SC_NOT_FOUND
			render "TestRun ${params.id} not found."
		}
	}


	def getTestRunsWithProperties = {
		JSONObject incomingJson = request.JSON

		if (!incomingJson.containsKey("projectKey")) {
			throw new CuantoException("No projectKey parameter was specified.")
		}

		Project project = projectService.getProject(incomingJson.getString("projectKey"))
		JSONObject jsonTestProperties = incomingJson.getJSONObject("testProperties")

		def testProperties = []
		jsonTestProperties.each { key, value ->
			testProperties << new TestRunProperty(key, value)
		}
		def testRuns = testRunService.getTestRunsWithProperties(project, testProperties)

		def jsonMap = [:]
		def testRunsToRender = []
		testRuns.each {
			testRunsToRender << it.toJSONMap()
		}
		jsonMap["testRuns"] = testRunsToRender
		render jsonMap as JSON
	}


	def getAllTestRuns = {
		if (!params.projectKey) {
			response.status = response.SC_BAD_REQUEST
			render "No projectKey parameter was specified"
		} else {
			Project project = projectService.getProject(params.projectKey)
			if (project) {
				def testRuns = dataService.getTestRunsByProject(project)
				def jsonMap = [:]
				def testRunsToRender = []
				testRuns.each {
					testRunsToRender << it.toJSONMap()
				}
				jsonMap["testRuns"] = testRunsToRender
				render jsonMap as JSON
			} else {
				response.status = response.SC_NOT_FOUND
				render "Project was not found for projectKey ${params.projectKey}"
			}
		}
	}
    

	def addTestOutcome = {
        try {
            TestOutcome testOutcome = testOutcomeService.addTestOutcome(request)
            response.status = response.SC_CREATED
            render testOutcome.toJSONmap() as JSON
        } catch (Exception e) {
			response.status = response.SC_INTERNAL_SERVER_ERROR
			render "Unknown error: ${e.getMessage()}"
		}
	}


	def updateTestOutcome = {
		try {
			TestOutcome testOutcome = testOutcomeService.apiUpdateTestOutcome(request)
			if (testOutcome) {
				response.status = response.SC_CREATED
				render "TestOutcome updated"
			} else {
				response.status = response.SC_INTERNAL_SERVER_ERROR
				render "Didn't successfully parse TestOutcome"
			}
		} catch (Exception e) {
			response.status = response.SC_INTERNAL_SERVER_ERROR
			render "Unknown error: ${e.getMessage()}"
		}
	}


	def getTestOutcome = {
		def testOutcome = TestOutcome.get(params.id) as TestOutcome
		if (!testOutcome) {
			response.status = response.SC_NOT_FOUND
			render "TestOutcome ${params.id} not found"
		}
		else {
			render testOutcome.toJSONmap() as JSON
		}
	}


	def getTestOutput = {
		def testOutcome = TestOutcome.get(params.id) as TestOutcome
		if (!testOutcome) {
			response.status = response.SC_NOT_FOUND
			render "TestOutcome ${params.id} not found"
		}
		else {
			render testOutcome.testOutput
		}
	}


	def getTestCaseOutcomesForTestRun = {
		if (!params.testCase) {
			response.SC_BAD_REQUEST
			render "No testCase parameter was specified."
		}

		TestCase testCase = TestCase.get(params.testCase)

		if (!testCase) {
			response.SC_NOT_FOUND
			render "TestCase ${params.testCase} was not found"
		}

		if (!params.testRun) {
			response.SC_BAD_REQUEST
			render "No testRun parameter was specified."
		}

		TestRun testRun = TestRun.get(params.testRun)

		if (!testRun) {
			response.SC_NOT_FOUND
			render "TestRun ${params.testRun} was not found"
		}

		List<TestOutcome> out = dataService.getTestOutcomes(testCase, testRun)
		if (!out) {
			response.status = response.SC_NOT_FOUND
			render "Test outcome not found for test case ${params.testCase} and test run ${params.testRun}"
		} else {

			def jsonArray = []
			out.each {
				jsonArray << it.toJSONmap(false, null, null, false)
			}
			def jsonMap = [testOutcomes: jsonArray]
			render jsonMap as JSON
		}
	}


	def getTestOutcomes = {
		try {
			Map results = testOutcomeService.getTestOutcomeQueryResultsForParams(params)
			def jsonArray = []
			results?.testOutcomes?.each {
				jsonArray << it.toJSONmap()
			}
			def jsonMap = [testOutcomes: jsonArray]
			render jsonMap as JSON
		} catch (CuantoException e) {
			response.status = response.SC_INTERNAL_SERVER_ERROR
			render e.message
		}
	}


	def countTestOutcomes = {
		try {
			Integer count = testOutcomeService.getTestOutcomeCountForParams(params)
			def jsonMap = ['count': count]
			render jsonMap as JSON
		} catch (CuantoException e) {
			response.status = response.SC_INTERNAL_SERVER_ERROR
			render e.message
		}
	}


	def getTestCase = {
		Project project = projectService.getProject(params.projectKey)
		if (!project) {
			response.status = response.SC_BAD_REQUEST
			render "ProjectKey ${params.projectKey} was not found"
		} else {
			TestCase testCase = new TestCase(packageName: params.packageName, testName: params.testName, parameters: params.parameters)
			TestCase foundTestCase = dataService.findMatchingTestCaseForProject(project, testCase)
			if (foundTestCase) {
				render foundTestCase as JSON
			} else {
				response.status = response.SC_NOT_FOUND
				render "Test case not found for packageName '${params.packageName}', testName: '${params.testName}, " +
					"parameters: ${params.parameters}"
			}
		}
	}

	def deleteTestRun = {
		TestRun testRun = TestRun.get(params.id)
		if (!testRun) {
			response.status = response.SC_NOT_FOUND
			render "TestRun ${params.id} not found"
		} else {
			testRunService.deleteTestRun(testRun)
			render "Deleted TestRun ${params.id}"
		}
	}


	def addProject = {
		try {
			Project project = parsingService.parseProject(request.JSON)
			response.status = response.SC_CREATED
			render project.toJSONMap() as JSON
		} catch (CuantoException e) {
			response.status = response.SC_INTERNAL_SERVER_ERROR
			render e.message
		}
	}


	def getProject = {
		if (!(params.projectKey || params.id)) {
			response.status = response.SC_BAD_REQUEST
			render "No projectKey or id parameter was provided on the request."
		} else {
			Project project = getProjectFromParams(params)

			if (project) {
				render project.toJSONMap() as JSON
			} else {
				response.status = response.SC_NOT_FOUND
				render "A Project matching the projectkey or id was not found."
			}
		}
	}


	Project getProjectFromParams(params) {
		def project = null
		if (params?.projectKey) {
			project = projectService.getProject(params.projectKey)
		} else if (params?.id) {
			project = dataService.getProject(params.id)
		}
		return project
	}


	def getAllProjects = {
		def allProjects = dataService.getAllProjects()
		def allJsonProjects = allProjects.collect {
			it.toJSONMap()
		}
		render allJsonProjects as JSON
	}


	def getProjectsForGroup = {
		if (params.name) {
			def projects = dataService.getProjectsByGroupName(params.name)
			if (projects) {
				def allJsonProjects = projects.collect {
					it.toJSONMap()
				}
				render allJsonProjects as JSON
			} else {
				response.status = response.SC_NOT_FOUND
				render "No group by the name ${params.name} was found."
			}
		} else {
			response.status = response.SC_BAD_REQUEST
			render "No name parameter was provided on the request."
		}
	}


	def deleteProject = {
		if (!(params.projectKey || params.id)) {
			response.status = response.SC_BAD_REQUEST
			render "No projectKey or id parameter was provided on the request."
		} else {
			Project project

			if (params.projectKey) {
				project = projectService.getProject(params.projectKey)
			} else {
				project = dataService.getProject(params.id)
			}

			if (project) {
				def projectName = project.name
				def projectGroup = project.projectGroup?.name
				projectService.queueForDeletion(project)

				def msg
				if (projectGroup) {
					msg = "Queued Project ${project.id}, ${projectGroup}: ${projectName} for deletion"
				} else {
					msg = "Queued Project ${project.id}, ${projectName} for deletion."
				}
				render msg
			} else {
				response.status = response.SC_NOT_FOUND
				render "A Project matching the projectkey or id was not found."
			}
		}
	}

}
