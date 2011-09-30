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

import cuanto.Defaults
import cuanto.Project
import cuanto.TestCase
import cuanto.TestOutcome
import grails.converters.JSON
import java.text.SimpleDateFormat
import javax.servlet.http.HttpServletResponse
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.json.JSONArray

class TestCaseController {

	def testOutcomeService
	def projectService
	def dataService

	// the delete, save and update actions only accept POST requests
	static def allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', create: 'POST', doRename: 'POST']

	def index = { redirect(action: list, params: params) }



	def history = {
		def testCase = TestCase.get(params.id)
		withFormat {
			html {
				if (!testCase) {
					flash.message = "Test Case not found with id ${params?.id}"
					redirect(controller: project, action: select)
				} else {
					return ['testCase': testCase, 'project': testCase.project]
				}
			}
			json {
				def startIndex
				if (params.offset) {
					startIndex = Integer.valueOf(params.offset)
				} else if (params.recordStartIndex) {
					startIndex = Integer.valueOf(params.recordStartIndex)
				}
				def numOutcomes
				if (params.max) {
					numOutcomes = Integer.valueOf(params.max)
				} else {
					numOutcomes = 1
				}

				Integer totalCount
				List<TestOutcome> outcomes

				if (params.filter == "allresults") {
					//totalCount = dataService.countTestCaseOutcomes(testCase)
					totalCount = dataService.countTestOutcomes(new TestOutcomeQueryFilter(testCase: testCase))
					outcomes = testOutcomeService.getTestOutcomeHistory(testCase, startIndex, numOutcomes, params.sort, params.order)
				} else {
					totalCount = dataService.countTestOutcomes(new TestOutcomeQueryFilter(testCase: testCase, isFailure:true,
						testResultIncludedInCalculations: true))
					outcomes = testOutcomeService.getTestOutcomeFailureHistory(testCase, startIndex, numOutcomes, params.sort, params.order)
				}
				def myJson = getJsonForOutcomes(outcomes, testCase, totalCount, startIndex)
				render myJson as JSON
			}
		}
	}


	def show = {
		def proj
		if (params.id) {
			proj = Project.get(params.id)
			return [project: proj]
		} else if (!params.id) {
			flash.message = "Please select a project."
			redirect(controller: "project", action: "mason")
		}
	}


	def get = {
		Project proj = Project.get(params.project)
		TestCase testCase
		if (proj) {
			def packageName = params.packageName
			def testName = params.testName
			def parameters = params.parameters
			def candidateTestCase = new TestCase('packageName': packageName, 'testName': testName,
				'parameters': parameters)
			testCase = dataService.findMatchingTestCaseForProject(proj, candidateTestCase)
			if (!testCase) {
				response.status = response.SC_NOT_FOUND
				render "Test case not found"
			} else {
				render xstream.toXML(testCase.toTestCaseApi())
			}
		} else {
			response.status = response.SC_NOT_FOUND
			render "Project not found"
		}

	}


	def list = {
		if (!params.id) {
			flash.message = "Unable to determine project"
			redirect(action: 'show')
		} else {
			def project = Project.get(Long.valueOf(params.id))
			def offset, max
			if (params.offset) {
				offset = Integer.valueOf(params.offset)
			} else {
				offset = 0
			}
			if (params.max) {
				max = Integer.valueOf(params.max)
			} else {
				max = 0
			}

			if (!params.max) {
				params.max = 10
			}
			def testCases = dataService.getTestCases(project, offset, max)

			def casesToRender = []
			testCases.each {tc ->
				casesToRender << [pkg: tc.packageName, name: tc.testName, descr: tc.description,
					id: tc.id, fullName: tc.fullName]
			}

			def json = ['testCases': casesToRender, recordStartIndex: offset, startIndex: offset,
				totalCases: dataService.countTestCases(project)]

			render json as JSON
		}
	}


	def update = {
		if (params.id) {
			def testCase
			testCase = TestCase.get(Long.valueOf(params.id))
			if (params.containsKey("packageName")) {
				testCase.packageName = params.packageName
			}
			if (params.containsKey("testName")) {
				testCase.testName = params.testName
			}
			if (params.containsKey("description")) {
				testCase.description = params.description
			}

			dataService.saveDomainObject(testCase)
			flash.message = "Test Case ${testCase.fullName} updated."
			render(view: 'show', model: ['project': testCase.project])

		} else {
			flash.message = "Unable to determine Test Case"
			redirect(action: 'show')
		}
	}


	def create = {
		validateTestCaseFields(params)
		def project = dataService.getProject(Long.valueOf(params.project))

		def fullName = ""
		if (params.pkg) {
			fullName = "${params.pkg}."
		}
		fullName += params.testName

		def existingTestCase = dataService.findExactTestCaseByName(fullName, project)
		if (existingTestCase) {
			flash.message = "A Test Case with that name already exists"
			params.testCase = existingTestCase.id
			redirect(action: 'enterNew', 'params': params)
		} else {
			projectService.createTestCase(params)
			flash.message = "Test Case ${params.testName} created"
		}
		if (params._action_create == "Create and Add Another") {
			redirect(action: 'enterNew', params: ['id': project.id])
		} else {
			render(view: 'show', model: ['project': project])
		}
	}


	def edit = {
		if (!params.id) {
			flash.message = "Unknown test case"
			redirect(action: 'show')
		}

		def testCase = TestCase.get(Long.valueOf(params.id))
		[project: testCase?.project, 'testCase': testCase]
	}


	def enterNew = {
		if (!params.id) {
			flash.message = "Unable to determine project"
			redirect(action: 'show')
		}
		def proj = dataService.getProject(Long.valueOf(params.id))

		def testCase = null
		if (params.testCase) {
			testCase = TestCase.get(Long.valueOf(params.testCase))
		}
		[project: proj, 'testCase': testCase]
	}


	def confirmDelete = {
		if (!params.id) {
			flash.message = "Unknown test case"
			redirect(action: 'show')
		}
		def testCase = TestCase.get(Long.valueOf(params.id))
		['testCase': testCase]
	}


	def delete = {
		if (!params.id) {
			flash.message = "Unknown test case"
			redirect(action: 'show')
		}
		def testCase = TestCase.get(Long.valueOf(params.id))
		def proj = testCase.project
		def msg = "Test Case ${testCase?.fullName} deleted."
		dataService.deleteTestCase(testCase)
		flash.message = msg
		render(view: 'show', model: ['project': proj, 'flash': flash])
	}


	def analysis = {
		def currentOutcome = TestOutcome.get(params.id)
		def outcomesToReturn = dataService.getTestOutcomeAnalyses(currentOutcome.testCase)
		def currentIndex = outcomesToReturn.indexOf(currentOutcome)
		if (currentIndex == -1) {
			currentIndex = 0
			def tempOutcomeList =[currentOutcome]
			tempOutcomeList.addAll(outcomesToReturn)
			outcomesToReturn = tempOutcomeList
		}
		def jsonOutcomes = []
		outcomesToReturn.each {
			def outcome = [testRun: it.testRun.dateExecuted.toString(), testResult: it.testResult?.name,
				analysisState: it.analysisState?.name, bug: [title: it.bug?.title, url: it.bug?.url], owner: it.owner,
				note: it.note, id: it.id]
			jsonOutcomes << outcome
		}
		def formatter = testOutcomeService.getTestCaseFormatter("methodonly")
		def shortName = formatter.getTestName(currentOutcome.testCase)
		def myJson = [testCase: [id: currentOutcome.testCase.id, name: currentOutcome.testCase.fullName,
			'shortName': shortName], 'outcomes': jsonOutcomes, 'count': jsonOutcomes.size(), 'offset': currentIndex]
		render myJson as JSON
	}


	def rename = {
		def project = null
		if (params.id) {
			project = Project.get(params.id)
		} else if (params.project) {
			project = projectService.getProject(params.project)
		}

		if (!project) {
			flash.message = "Unknown project"
			redirect(controller: 'project', action: 'mason')
		}

		['project': project]
	}


	def renamePreview = {
		def project = null
		def error = null
		if (params.id) {
			project = Project.get(params.id)
		}

		if (!project) {
			response.status = response.SC_NOT_FOUND
			error = "Project ${params.id} not found"
		}

		if (!params.searchTerm) {
			response.status = response.SC_BAD_REQUEST
			error = "Missing searchTerm parameter"
		}

		if (!params.replaceName) {
			response.status = response.SC_BAD_REQUEST
			error = "Missing replaceName parameter"
		}

		if (error) {
			render error
		} else {
			def renameList = testOutcomeService.previewTestRename(project, params.searchTerm, params.replaceName)
			def myJson = ["renameList": renameList]
			render myJson as JSON
		}
	}


	def doRename = {
		JSONArray json = request.JSON
		def renameArray = []
		json.each { JSONObject jsonObj ->
			def id = jsonObj.getInt("id")
			def newName = jsonObj.getString("newName")
			renameArray << ['id': id, 'newName': newName]
		}
		def myJson = [renamed: testOutcomeService.bulkTestCaseRename(renameArray)]
		render myJson as JSON
	}


	private getJsonForOutcomes(List<TestOutcome> outcomes, TestCase testCase, Integer totalCount, Integer offset) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(Defaults.dateFormat)
		def jsonOutcomes = []
		outcomes.each {TestOutcome outcome ->
			jsonOutcomes += [result: outcome.testResult.name, date: dateFormat.format(outcome.testRun.dateExecuted),
				analysisState: outcome.analysisState?.name, duration: outcome.duration, owner: outcome.owner,
				bug: [title: outcome.bug?.title, url: outcome.bug?.url], note: outcome.note, id: outcome.id]
		}

		def myJson = [testCase: testCase.fullName, totalOutcomes: totalCount, count: outcomes.size(),
			testOutcomes: jsonOutcomes, 'recordStartIndex': offset]
		return myJson
	}


	private validateTestCaseFields(params) {
		if (!params.project) {
			flash.message = "Unable to determine project for Test Case"
			redirect(action: 'show')
		}
		if (!params.testName) {
			flash.message = "Test Name is a required field for Test Case"
			params.id = params.project
			redirect(action: 'enterNew', 'params': params)
		}
	}

}
