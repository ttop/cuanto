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
import grails.converters.XML
import java.text.SimpleDateFormat

class TestRunController {
    def bucketService
	def parsingService
	def dataService
	def testOutcomeService
	def testRunService
	def testCaseFormatterRegistry
	def statisticService

	// the delete, save, update and submit actions only accept POST requests
	static def allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', submit: 'POST', create: 'POST',
		submitFile: 'POST', createXml: 'POST', deleteProperty: 'POST', deleteLink: 'POST']

	def index = { redirect(action: 'mason', controller: 'project', params: params) }


	def delete = {
		def testRun = TestRun.get(params.id)
		def myJson = [:]
		if (testRun) {
			testRunService.deleteTestRun(testRun)
		} else {
			response.status = response.SC_NOT_FOUND
			myJson.error = "Test Run ${params.id} was not found"
		}
		render myJson as JSON
	}


	def bulkDelete = {
		def myJson = [deleted: testRunService.deleteTestRuns(request.JSON)]
		render myJson as JSON
	}

	def deleteProperty = {
		def propToDelete = TestRunProperty.get(params.id)
		def testRun = TestRun.get(params.testRun)

		if (propToDelete && testRun) {
			testRunService.deleteTestRunProperty(propToDelete)
			render "OK"
		} else {
			response.setStatus(response.SC_NOT_FOUND)
			render "TestProperty ID ${params?.id} or Test Run ID ${params?.testRun} not found"
		}
	}


	def deleteLink = {
		def linkToDelete = TestRunLink.get(params.id)
		def testRun = TestRun.get(params.testRun)

		if (linkToDelete && testRun) {
			testRunService.deleteTestRunLink(linkToDelete)
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


	def outcomes = {
		Map results = testOutcomeService.getTestOutcomeQueryResultsForParams(params)

		withFormat {
			json {
				def formatter = testOutcomeService.getTestCaseFormatter(params.tcFormat)
				def jsonOutcomes = []
				results?.testOutcomes?.each { outcome ->
					jsonOutcomes << outcome.toJSONmap(true, 180, formatter, false)
				}

				def myJson = ['totalCount': results?.totalCount, count: results?.testOutcomes?.size(), testOutcomes: jsonOutcomes,
					'offset': results?.offset, testProperties: results?.testProperties, links: results?.links]
				render myJson as JSON
			}
			xml {
				if (results?.testOutcomes) {
					response.contentType = "text/xml"
					render results.testOutcomes as XML
				} else {
					response.status = response.SC_NO_CONTENT
					render "No test outcomes in this test run."
				}
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


	def groupedOutput = {
		if (!params.id) {
			response.status = response.SC_BAD_REQUEST
			render "No id parameter was found"
		} else {
			def testRun = TestRun.get(params.id)
			if (!testRun) {
				response.status = response.SC_NOT_FOUND
				render "TestRun ${params.id} was not found"
			} else {
				def filter = "AllFailures"
				if (params.filter) {
					filter = params.filter.replaceAll(" ", "")
				}
				def totalCount = testOutcomeService.countGroupedOutputSummaries(testRun, filter)
				def offset = 0
				def max = 20
				def sort = "failures"
				def order = "desc"

				if (params.offset) {
					offset = Integer.valueOf(params.offset)
				}
				if (params.max) {
					max = Integer.valueOf(params.max)
				}
				if (params.sort) {
					sort = params.sort
				}
				if (params.order) {
					order = params.order
				}

				def groupedResults = testOutcomeService.getGroupedOutputSummaries(testRun, filter, offset, max, sort, order)

				def jsonArray = groupedResults.collect {
					def output
					if (it[1]) {
						output = it[1].encodeAsHTML()
					} else {
						output = ""
					}
					return [failures: it[0], output: output]
				}

				def jsonMap = [groupedOutput: jsonArray, totalCount: totalCount, offset: offset]
				render jsonMap as JSON
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

    def testNgBuckets = {
        def testRunId = getTestRunIdFromFilename(params.id)
        if (testRunId) {
            def testRun = TestRun.get(testRunId)
            def bucket = bucketService.getTestNgSuiteForPassFailBuckets(testRun)
            response.contentType = "text/xml"
            render bucket
        } else {
            response.status = response.SC_BAD_REQUEST
            render "Couldn't parse TestRun ID for XML"
        }
    }


	def outcomeCount = {
		render testOutcomeService.countOutcomes(params)
	}


	def recalc = {
		def testRun = TestRun.get(params.id)
		if (!testRun) {
			response.status = response.SC_NOT_FOUND
			render "testRun ${params.id} not found"
		} else {
			statisticService.queueTestRunStats(testRun)
			render "OK"
		}
	}

	
	def statistics = {
		def testRun = TestRun.get(params.id)
		if (!testRun) {
			redirect(controller: 'project', action: 'mason')
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
					TestRunStats stats = TestRunStats.findByTestRun(testRun)
					if (!stats) {
						statisticService.queueTestRunStats(testRun)
					}
					myJson['results'] += stats.toJsonMap()
				}

				render myJson as JSON
			}
			/*      TODO: DELETE? I think this code is unused
			text {
				def testRunMap = [:]
				TestRunStats stats = TestRunStats.findByTestRun(testRun)

				if (stats) {
					testRunMap.tests = stats.tests
					testRunMap.passed = stats.passed
					testRunMap.failed = stats.failed
					testRunMap.skipped = stats.skipped
				}
				render(view: 'get', model: ['testRunMap': testRunMap])
			}
			*/
		}
	}


	def get = {
		SimpleDateFormat dateFormat = new SimpleDateFormat(Defaults.dateFormat)
		def testRun = TestRun.get(Long.valueOf(params.id))
		def testRunMap = testRun?.toJSONWithDateFormat(dateFormat)
		withFormat {
			text {
				['testRunMap': testRunMap]
			}
			json {
				render testRunMap as JSON
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
			def stats = TestRunStats.findByTestRun(testRun)
			return ['testRun': testRun, 'filter': getDefaultFilter(testRun), 'filterList': getFilterList(),
				'outputFilter': "All Failures", 'outputFilterList': getOutputFilterList(),
				'testResultList': getJavascriptList(TestResult.listOrderByName()),
				'analysisStateList': getJavascriptList(analysisStates), 'pieChartUrl': pieChartUrl,
				'bugSummary': bugSummary, 'formatters': tcFormatList,
				'project': testRun?.project, 'tcFormat': tcFormatList[0].key, 'stats': stats]
		} else {
			redirect(controller: 'project', action: 'mason')
		}
	}


	def failureChart = {
		if (params.id) {
			def testRun = TestRun.get(Long.valueOf(params.id))
			def pieChartUrl = testRunService.getGoogleChartUrlForTestRunFailures(testRun)
			render(template: "pieChart", model: ['pieChartUrl': pieChartUrl])
		} else {
			redirect(controller: 'project', action: 'mason')
		}
	}

	def summaryTable = {
		if (params.id) {
			def testRun = TestRun.get(Long.valueOf(params.id))
			def stats = TestRunStats.findByTestRun(testRun)
			render(template: "summaryTable", model: ['stats': stats])
		} else {
			redirect(controller: 'project', action: 'mason')
		}
	}

	def bugSummary = {
		if (params.id) {
			def testRun = TestRun.get(Long.valueOf(params.id))
			def bugSummary = testRunService.getBugSummary(testRun)
			render(template: 'bugSummary', model: ['bugSummary': bugSummary])
		} else {
			redirect(controller: 'project', action: 'mason')
		}
	}

	private getDefaultFilter(testRun) {
		def filter
		def stats = TestRunStats.findByTestRun(testRun)
		if (stats?.failed == 0 || testRun.project.testType.name == "Manual") {
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
		filterList += [id: "allskipped", value: "All Skipped"]
		filterList += [id: "newpasses", value: "New Passes"]
		filterList += [id: "allresults", value: "All Results"]
        filterList += [id: "allquarantined", value: "All Quarantined"]
		return filterList
	}


	private getOutputFilterList() {
		def filterList = []
		filterList += [id: "allfailures", value: "All Failures"]
		filterList += [id: "newfailures", value: "New Failures"]
		filterList += [id: "unanalyzedfailures", value: "Unanalyzed Failures"]
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


	def createManual = {
		if (!params.id) {
			flash.message = "Unable to determine project for Test Case"
			redirect(controller: 'project', action: 'mason')
		}
		def project = dataService.getProject(Long.valueOf(params.id))
		['project': project]
	}

	def manual = {
		if (!params.id) {
			flash.message = "Unable to determine project"
			redirect(controller: 'project', action: 'mason')
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



	def export = {
		[testRun: TestRun.get(params.id)]
	}


	def getTestRunIdFromFilename(filename) {
		def matcher = filename =~/.+_(\d+).*/
		if (matcher.matches()) {
			def match = matcher[0][1]
			return Long.parseLong(match.toString())
		} else {
			return null
		}
	}
}
