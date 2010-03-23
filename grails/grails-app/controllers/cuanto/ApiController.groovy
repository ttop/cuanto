package cuanto

import cuanto.TestOutcome
import cuanto.TestRun
import cuanto.formatter.TestNameFormatter
import grails.converters.JSON

class ApiController {

	def testRunService
	def testOutcomeService
	def parsingService

    def index = { }

	
	def addTestRun = {
		TestRun testRun = testRunService.createTestRun(request.JSON)
		response.status = response.SC_CREATED
		render testRun.toJSONMap() as JSON
	}


	def updateTestRun = {

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
		response.status = response.SC_CREATED
		render testOutcome.toJSONmap() as JSON
	}


	def updateTestOutcome = {

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


	def getTestOutcomes = {

	}


	def getAllTestOutcomes = {

	}


	def getTestCase = {

	}


	def getProject = {

	}

}
