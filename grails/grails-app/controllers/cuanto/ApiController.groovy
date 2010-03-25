package cuanto

import cuanto.TestOutcome
import cuanto.TestRun
import cuanto.formatter.TestNameFormatter
import grails.converters.JSON

class ApiController {

	def testRunService
	def testOutcomeService
	def parsingService
	def dataService
	def projectService
	def statisticService


    def index = { }

	
	def addTestRun = {
		TestRun testRun = parsingService.parseTestRun(request.JSON)
		dataService.saveTestRun(testRun)
		response.status = response.SC_CREATED
		render testRun.toJSONMap() as JSON
	}


	def updateTestRun = {
		try {
			TestRun testRun = parsingService.parseTestRun(request.JSON)
			if (testRun) {
				testRunService.update(testRun)
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

	}


	def addTestOutcome = {
		TestOutcome testOutcome = parsingService.parseTestOutcome(request.JSON)
		dataService.saveTestOutcomes([testOutcome])

		if (testOutcome.testRun) {
			statisticService.queueTestRunStats(testOutcome.testRun)
		}

		response.status = response.SC_CREATED
		render testOutcome.toJSONmap() as JSON
	}


	def updateTestOutcome = {
		try {
			TestOutcome testOutcome = parsingService.parseTestOutcome(request.JSON)
			if (testOutcome) {
				testOutcomeService.updateTestOutcome(testOutcome)
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
				jsonArray << it.toJSONmap()
			}
			def jsonMap = [testOutcomes: jsonArray]
			render jsonMap as JSON
		}
	}


	def getAllTestOutcomes = {
		Map results

		try {
			results = testOutcomeService.getTestOutcomeQueryResultsForParams(params)
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


	def getProject = {

	}

}
