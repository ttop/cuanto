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

import cuanto.*
import cuanto.api.TestOutcome as TestOutcomeApi

class TestOutcomeService {

	boolean transactional = false

	def dataService
	def testRunService
	def bugService
	def statisticService
	def testCaseFormatterRegistry

	def updateTestOutcome(Map params) {
		def outcome = dataService.getTestOutcome(params.id)
		if (outcome) {
			applyTestResultToTestOutcome(outcome, dataService.result(params.testResult))
			applyBugParametersToTestOutcome(outcome, params)
			applyAnalysisStateToTestOutcome(outcome, params)
			outcome.note = Sanitizer.escapeHtmlScriptTags(params.note)
			outcome.owner =  Sanitizer.escapeHtmlScriptTags(params.owner)
			dataService.saveDomainObject(outcome)
			dataService.deleteBugIfUnused(outcome.bug)
		}
	}


	def updateTestOutcome(TestOutcomeApi pOutcome) {
		if (pOutcome) {
			TestOutcome origOutcome = dataService.getTestOutcome(pOutcome.id)
			TestOutcome outcome = dataService.getTestOutcome(pOutcome.id)
			if (outcome) {
				applyTestResultToTestOutcome(outcome, dataService.result(pOutcome.testResult))

				if (pOutcome.analysisState) {
					outcome.analysisState = dataService.getAnalysisStateByName(pOutcome.analysisState)
				}

				def origBug = outcome.bug
				outcome.bug = bugService.getBug(pOutcome.bug?.title, pOutcome.bug?.url)
				outcome.note = pOutcome.note
				outcome.owner = pOutcome.owner
				outcome.testOutput = pOutcome.testOutput
				dataService.saveDomainObject outcome
				dataService.deleteBugIfUnused origBug

				if (origOutcome.testResult != outcome.testResult && outcome.testRun != null) {
					statisticService.queueTestRunStats outcome.testRun.id
				}
			}
		}
	}


	def applyTestResultToTestOutcome(testOutcome, testResult) {
		if (testOutcome && testResult) {
			boolean recalc = false
			if (testResult != testOutcome.testResult && testOutcome.testRun != null) {
				recalc = true
			}

			testOutcome.testResult = testResult
			if (recalc) {
				statisticService.queueTestRunStats(testOutcome.testRun)
			}
		}
	}


	def applyBugParametersToTestOutcome(outcome, params) {
		def newBug
		if (outcome && params) {
			if (params?.bug) {
				def bugInfo = outcome.testCase.project.extractBugInfo(Sanitizer.escapeHtmlScriptTags(params?.bug))
				newBug = bugService.getBug(bugInfo.title, bugInfo.url)
			} else {
				newBug = bugService.getBug(params?.bugTitle, params?.bugUrl)
			}
			outcome.bug = newBug
		}
	}


	def applyAnalysisStateToTestOutcome(outcome, params) {
		def analysisState
		def unanalyzed = dataService.getDefaultAnalysisState()

		if (params.analysisState) {
			analysisState = dataService.getAnalysisState(params.analysisState)
		} else if (params.analysisStateName) {
			analysisState = dataService.getAnalysisStateByName(params.analysisStateName)
		} else {
			def result = dataService.result(params.testResult)
			if (result == dataService.result("Pass") || !result) {
				analysisState = null
			} else {
				analysisState = unanalyzed
			}
		}

		// if there's a bug specified but the analysis state is unanalyzed, then assign it
		// the analysis state "Bug"

		if (analysisState ==  unanalyzed && outcome.bug) {
			analysisState = dataService.getAnalysisStateForBug()
		}

		if (analysisState != outcome.analysisState) {
			outcome.analysisState = analysisState
			statisticService.calculateAnalysisStats(outcome.testRun)
		}
	}


	List <TestOutcome> getTestOutcomeHistory(TestCase testCase, int startIndex, int maxOutcomes, String sortVal, String order) {
		def sortOptions = getSortOptions()
		dataService.getTestOutcomeHistory(testCase, startIndex, maxOutcomes, sortOptions[sortVal], order)
	}


	List<TestOutcome> getTestOutcomeFailureHistory(TestCase testCase, int startIndex, int maxOutcomes, String sortVal, String order) {
		def sortOptions = getSortOptions()
		return dataService.getTestOutcomeFailureHistory(testCase, startIndex, maxOutcomes, sortOptions[sortVal], order)
	}


	Map getSortOptions(){
		return [result:"testResult.name", analysisState:"analysisState", duration:"duration", bug:"bug.title",
			owner:"owner", note:"note", date:"testRun.dateExecuted", dateExecuted:"testRun.dateExecuted"]
	}


	List <TestCase> findTestCaseByName(name, proj) {
		Project project = Project.get(proj)
		dataService.findTestCaseByName(name, project)
	}


	def countOutcomes(params) {
		def testRun = TestRun.get(params.id)

		def queryParams = [:]
		def possibleQueryParams = ["sort", "order", "max", "offset", "filter"]
		possibleQueryParams.each {possibleParam ->
			if (params.containsKey(possibleParam)) {
				queryParams[possibleParam] = params[possibleParam]
			}
		}

		def totalCount

		if (params.filter?.equalsIgnoreCase("allFailures")) {
			totalCount = dataService.countFailuresForTestRun(testRun)
		} else if (params.filter?.equalsIgnoreCase("newFailures")){
			totalCount = testRunService.countNewFailuresForTestRun(testRun)
		} else if (params.outcome) {
			/* todo: why is this parameter here? it smells bad */
			totalCount = params.totalCount
		} else {
			totalCount = testRunService.countOutcomes(testRun)
		}
		return totalCount
	}


	def applyAnalysis(sourceTestOutcome, targetTestOutcomes, fieldsToApply) {
		def fields = []
		if (fieldsToApply) {
			fields = fieldsToApply
		} else {
			fields = getValidAnalysisFields()
		}

		targetTestOutcomes.each { targetOutcome ->
			fields.each { field ->
				targetOutcome.setProperty(field, sourceTestOutcome.getProperty(field))
			}
			dataService.saveDomainObject(targetOutcome)
		}
		statisticService.queueTestRunStats(sourceTestOutcome.testRun)
	}


	def applyAnalysis(targetTestoutcomes, Map fieldsToApply) {
		def validFields = validAnalysisFields
		targetTestoutcomes.each { targetOutcome ->
			fieldsToApply.each { field ->
				if (field.key in validFields) {
					targetOutcome.setProperty(field.key, field.value)
				}
			}
			dataService.saveDomainObject(targetOutcome)
		}
		statisticService.queueTestRunStats(sourceTestOutcome.testRun)
	}


	def getValidAnalysisFields() {
		return ["testResult", "analysisState", "bug", "owner", "note"]
	}


	def getTestCaseFormatter(formatterDescription) {
		def formatter = null
		if (formatterDescription) {
			formatter = testCaseFormatterRegistry.getFormatMap()[formatterDescription]
		}

		if (!formatter){
			formatter = testCaseFormatterRegistry.formatterList[0]
		}
		return formatter
	}

	
	def deleteTestOutcome(testOutcome) {
		TestOutcome.withTransaction {
			testOutcome.delete(flush:true)
		}
	}
}
