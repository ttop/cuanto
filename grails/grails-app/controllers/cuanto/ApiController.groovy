package cuanto

import grails.converters.JSON

class ApiController {

    def index = { }

	
	def createTestRun = {

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


	def createTestOutcome = {

	}


	def updateTestOutcome = {

	}


	def getTestOutcome = {

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
