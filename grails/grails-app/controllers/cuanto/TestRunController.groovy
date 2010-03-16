/*
 Copyright (c) 2008 thePlatform, Inc.

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

import grails.converters.JSON
import java.text.SimpleDateFormat
import com.thoughtworks.xstream.XStream
import cuanto.api.TestRun as TestRunApi
import cuanto.api.TestRunStats as TestRunStatsApi

class TestRunController {
	def parsingService
	def dataService
	def testOutcomeService
	def testRunService
	def testCaseFormatterRegistry
	def statisticService

	XStream xstream = new XStream()

	// the delete, save, update and submit actions only accept POST requests
	static def allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', submit: 'POST', create: 'POST',
		submitFile: 'POST', createXml:'POST', deleteProperty: 'POST', deleteLink: 'POST']

	SimpleDateFormat dateFormat = new SimpleDateFormat(Defaults.dateFormat)

	def index = { redirect(action: 'list', controller: 'project', params: params) }

	
	def delete = {
		def testRun = TestRun.get(params.id)
		def myJson = [:]
		if (testRun) {
			dataService.deleteTestRun(testRun)
		} else {
			response.status = response.SC_NOT_FOUND
			myJson.error = "Test Run ${params.id} was not found"
		}
		render myJson as JSON
	}


	def deleteProperty = {
		def propToDelete = TestProperty.get(params.id)
		def testRun = TestRun.get(params.testRun)

		if (propToDelete && testRun) {
			testRun.removeFromTestProperties(propToDelete)
			dataService.saveDomainObject testRun 
			render "OK"
		} else {
			response.setStatus(response.SC_NOT_FOUND)
			render "TestProperty ID ${params?.id} or Test Run ID ${params?.testRun} not found"
		}
	}

	def deleteLink = {
		def linkToDelete = Link.get(params.id)
		def testRun = TestRun.get(params.testRun)

		if (linkToDelete && testRun) {
			testRun.removeFromLinks(linkToDelete)
			dataService.saveDomainObject testRun
			render "OK"
		} else {
			response.setStatus(response.SC_NOT_FOUND)
			render "Link ID ${params?.id} or Test Run ID ${params?.testRun} not found"
		}
	}

	
	def edit = {
		def testRun = TestRun.get(params.id)

		if (!testRun) {
			flash.message = "TestRun not found with id ${params.id}"
			redirect(controller: project, action: select)
		}
		else {
			return [testRun: testRun]
		}
	}


	def update = {
		if (request.format == 'form') {
			def testRun = TestRun.get(params.id)
			if (testRun) {
				testRun = testRunService.update(testRun, params)
				flash.message = "Test Run updated."
			}
			redirect(controller: "testRun", action: edit, id: testRun?.id)
		} else if (request.format == 'xml') {
			try {
				def testRunApi = (TestRunApi) xstream.fromXML(request.inputStream)
				if (testRunApi) {
					testRunService.update(testRunApi)
					render "OK"
				} else {
					response.status = response.SC_NOT_FOUND
					render "Did not parse test run"
				}
			} catch (Exception e) {
				response.status = response.SC_INTERNAL_SERVER_ERROR
				render e.message
			}
		} else {
			response.status = response.SC_INTERNAL_SERVER_ERROR
			render "Unable to process update" 
		}
	}


	def updateNote = {
		def testRun = TestRun.get(params.id)
		def myJson = [:]
		if (!params.keySet().contains("note")) {
			myJson.error = "The note field was missing"
		} else if (testRun) {
			testRunService.updateNote(testRun, params.note) 
		} else {
			myJson.error = "The test run id field was missing or invalid."
		}
		withFormat {
			json {
				if (myJson.error) {
					response.status = response.SC_NOT_FOUND
					render myJson as JSON
				} else {
					render myJson as JSON
				}
			}
		}
	}


	def submitFile = {
		def testRunId = Long.valueOf(request.getHeader("Cuanto-TestRun-Id"))

		for (fileName in request.getFileNames()) {
			log.info "Parsing ${fileName}"
			def multipartFileRequest = request.getFile(fileName)
			parsingService.parseFileFromStream(multipartFileRequest.getInputStream(), testRunId)
		}
		render ""
	}


	def submitSingleTest = {
		def testRunId = null
		def projectId = null

		String testRunHeader = request.getHeader("Cuanto-TestRun-Id")
		if (testRunHeader) {
			testRunId = Long.valueOf(testRunHeader)
		}

		String projectHeader = request.getHeader("Cuanto-Project-Id")
		if (projectHeader) {
			projectId = Long.valueOf(projectHeader)
		}

		def testOutcome
		try {
			testOutcome = parsingService.parseTestOutcome(request.getInputStream(), testRunId, projectId)
			render testOutcome?.id

		} catch (ParsingException e) {
		    response.status = response.SC_BAD_REQUEST
			render e.message
		} catch (CuantoException e) {
			response.status = response.SC_INTERNAL_SERVER_ERROR
			render e.message
		}
	}


	def outcomes = {
		Map results = testOutcomeService.getTestOutcomeQueryResultsForParams(params)

		withFormat {
			json {
				def formatter = testOutcomeService.getTestCaseFormatter(params.tcFormat)
				def jsonOutcomes = []
				results?.testOutcomes?.each {outcome ->
					def currentOutcome = [
						result: outcome.testResult.name, analysisState: outcome.analysisState?.name,
						duration: outcome.duration, owner: outcome.owner, startedAt: outcome.startedAt, finishedAt: outcome.finishedAt,
						bug: [title: outcome.bug?.title, url: outcome.bug?.url], note: outcome.note, id: outcome.id,
					]

					if (outcome.testOutput) {
						def maxChars = outcome.testOutput.size() > results?.outputChars ? results?.outputChars : outcome.testOutput.size()
						currentOutcome.output = outcome.testOutput[0..maxChars - 1]
					} else {
						currentOutcome.output = null
					}

					def currentTestCase = [name:formatter.getTestName(outcome.testCase), id:outcome.testCase.id]

					if (outcome.testCase.parameters) {
						currentTestCase.parameters = outcome.testCase.parameters
					}

					currentOutcome.testCase = currentTestCase
					jsonOutcomes << currentOutcome
				}

				def myJson = ['totalCount': results?.totalCount, count: results?.testOutcomes?.size(), testOutcomes: jsonOutcomes,
					'offset': results?.offset]
				render myJson as JSON
			}
			xml {
				def outcomesToRender = results?.testOutcomes.collect{it.toTestOutcomeApi()}
				response.contentType = "text/xml"
			    render xstream.toXML(outcomesToRender)
			}
			csv {
				response.contentType = "text/csv"
				render testOutcomeService.getDelimitedTextForTestOutcomes(results?.testOutcomes, ",")
			}
			tsv {
				response.contentType = 'text/tab-separated-values'
				render testOutcomeService.getDelimitedTextForTestOutcomes(results?.testOutcomes, "\t")
			}
		}
	}
	

	def csv = {
		def testRunId = getTestRunIdFromFilename(params.id)
		if (testRunId) {
			params.format = 'csv'
			params.id = testRunId
			forward(action: 'outcomes', params: params)
		} else {
			response.status = response.SC_BAD_REQUEST
			render "Couldn't parse TestRun ID for CSV"
		}
	}


	def tab = {
		def testRunId = getTestRunIdFromFilename(params.id)
		if (testRunId) {
			params.format = 'tsv'
			params.id = testRunId
			forward(action: 'outcomes', params: params)
		} else {
			response.status = response.SC_BAD_REQUEST
			render "Couldn't parse TestRun ID for CSV"
		}
	}


	def xml = {
		def testRunId = getTestRunIdFromFilename(params.id)
		if (testRunId) {
			params.format = 'xml'
			params.id = testRunId
			forward(action: 'outcomes', params: params)
		} else {
			response.status = response.SC_BAD_REQUEST
			render "Couldn't parse TestRun ID for XML"
		}
	}
	

	def outcomeCount = {
		render testOutcomeService.countOutcomes(params)
	}


	def statistics = {
		def testRun = TestRun.get(params.id)
		if (!testRun) {
			redirect(controller: 'project', action: 'list')
		}

		if (params.calculate) {
			statisticService.queueTestRunStats(testRun)
		}

		withFormat {
			json {
				def myJson = [:]
				if (params.insertIndex) {
					myJson['recordInsertIndex'] = Integer.valueOf(params.insertIndex)
					myJson['insertIndex'] = Integer.valueOf(params.insertIndex)
				}

				myJson['results'] = []
				if (params.header) {
					if (!testRun.testRunStatistics) {
						statisticService.queueTestRunStats(testRun)
					}
					myJson['results'] += testRun.testRunStatistics.toJsonMap()
				}

				render myJson as JSON
			}
			text {
				def testRunMap = [:]
				TestRunStats stats = testRun.testRunStatistics
				if (stats) {
					testRunMap.tests = stats.tests
					testRunMap.passed = stats.passed
					testRunMap.failed = stats.failed
				}
				render(view: 'get', model: ['testRunMap': testRunMap])
			}
			xml {
				def stats = testRun.testRunStatistics?.toTestRunStatsApi()
				response.contentType = 'application/xml'
				render(xstream.toXML(stats))
			}
		}
	}


	def get = {
		def testRun = TestRun.get(Long.valueOf(params.id))
		def testRunMap = testRun?.toJSONWithDateFormat(dateFormat)
		withFormat {
			text {
				['testRunMap': testRunMap]
			}
			json {
				render testRunMap as JSON
			}
			xml {
				if (testRun) {
					render xstream.toXML(testRun.toTestRunApi())
				} else {
					response.status = response.SC_NOT_FOUND
					render "Test Run with id parameter of ${params.id} was not found"
				}
			}
		}
	}

	
	def results = {
		def testRun = null
		if (params.id) {
			testRun = TestRun.get(params.id)
		} else if (params.projectKey) {
			testRun = dataService.getMostRecentTestRunForProjectKey(params.projectKey)
		}

		if (testRun) {
			def analysisStates = ["-None-"]
			analysisStates.addAll(AnalysisState.listOrderByName())

			def pieChartUrl = testRunService.getGoogleChartUrlForTestRunFailures(testRun)
			def bugSummary = testRunService.getBugSummary(testRun)
			def tcFormatList = testCaseFormatterRegistry.formatterList
			return ['testRun': testRun, 'filter': getDefaultFilter(testRun), 'filterList': getFilterList(),
				'testResultList': getJavascriptList(TestResult.listOrderByName()),
				'analysisStateList': getJavascriptList(analysisStates), 'pieChartUrl': pieChartUrl,
				'bugSummary': bugSummary, 'formatters': tcFormatList,
				'project': testRun?.project, 'tcFormat': tcFormatList[0].key]
		} else {
			redirect(controller: 'project', action: 'list')
		}
	}


	def failureChart = {
		if (params.id) {
			def testRun = TestRun.get(Long.valueOf(params.id))
			def pieChartUrl = testRunService.getGoogleChartUrlForTestRunFailures(testRun)
			render(template: "pieChart", model: ['pieChartUrl': pieChartUrl])
		} else {
			redirect(controller: 'project', action: 'list')
		}
	}

	def summaryTable = {
		if (params.id) {
			def testRun = TestRun.get(Long.valueOf(params.id))
			render(template: "summaryTable", model: ['testRun': testRun])
		} else {
			redirect(controller: 'project', action: 'list')
		}
	}

	def bugSummary = {
		if (params.id) {
			def testRun = TestRun.get(Long.valueOf(params.id))
			def bugSummary = testRunService.getBugSummary(testRun)
			render(template: 'bugSummary', model: ['bugSummary': bugSummary])
		} else {
			redirect(controller: 'project', action: 'list')
		}
	}

	private getDefaultFilter(testRun) {
		def filter
		if (testRun.testRunStatistics?.failed == 0 || testRun.project.testType.name == "Manual") {
			filter = "All Results"
		} else {
			filter = "All Failures"
		}
		return filter
	}


	private getFilterList() {
		def filterList = []
		filterList += [id: "allfailures", value: "All Failures"]
		filterList += [id: "newfailures", value: "New Failures"]
		filterList += [id: "unanalyzedfailures", value: "Unanalyzed Failures"]
		filterList += [id: "allresults", value: "All Results"]
		return filterList
	}


	def create = {
		if (!params.project) {
			response.status = response.SC_NOT_FOUND // todo is this the right response code? how about invalid request?
			render "project parameter is required"
		} else {
			try {
				def run = testRunService.createTestRun(params)
				render(view: "create", model: ['testRunId': run.id])
			} catch (CuantoException e) {
				response.status = response.SC_INTERNAL_SERVER_ERROR
				render e.getMessage()
			}
		}
	}

	def createXml = {
		def testRun = (TestRunApi) xstream.fromXML(request.inputStream)
		try {
			def parsedTestRun = testRunService.createTestRun(testRun)
			render(view: "create", model: ['testRunId': parsedTestRun.id])
		} catch (CuantoException e) {
			response.status = response.SC_INTERNAL_SERVER_ERROR
			render e.getMessage()
		}
	}

	def createManual = {
		if (!params.id) {
			flash.message = "Unable to determine project for Test Case"
			redirect(controller: 'project', action: 'list')
		}
		def project = dataService.getProject(Long.valueOf(params.id))
		['project': project]
	}

	def manual = {
		if (!params.id) {
			flash.message = "Unable to determine project"
			redirect(controller: 'project', action: 'list')
		}
		def run = testRunService.createManualTestRun(params)
		flash.message = "Created Test Run ${run.id}."
		redirect(controller: 'project', action: 'history', id: params.id)
	}


	def getJavascriptList(libObj) {
		def javascriptList = "["
		libObj.eachWithIndex {item, idx ->
			javascriptList += "\"${item}\""
			if (idx < libObj.size() - 1) {
				javascriptList += ","
			}
		}
		javascriptList += "]"
		return javascriptList
	}


	def getWithProperties = {
		def testProps = []
		Project project = Project.get(params.project)
		if (project) {
			params.each { String paramName, String paramValue ->
				def propMatcher = (paramName =~ /^prop\[(\d+)]/)
				if (propMatcher.matches()) {
					def propIndex = propMatcher[0][1] as Integer
					def propValue = params["propValue[${propIndex}]"]
					testProps << new TestProperty(paramValue, propValue)
				}
			}
			List testRuns = testRunService.getTestRunsWithProperties(project, testProps)
			def toRender = testRuns.collect {it.toTestRunApi()}
			render xstream.toXML(toRender)
		} else {
			response.status = response.SC_BAD_REQUEST
			render "Couldn't find project ${params?.project}"
		}

	}

	def export = {
		[testRun: TestRun.get(params.id)]
	}


	private def getTestRunIdFromFilename(filename) {
		def matcher = filename =~ /.+(\d+).*/
		if (matcher.matches()) {
			return matcher[0][1]
		} else {
			return null
		}
	}
}
