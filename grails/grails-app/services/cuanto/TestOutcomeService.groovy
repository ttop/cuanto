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
import java.text.SimpleDateFormat

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

			SimpleDateFormat formatter = new SimpleDateFormat(Defaults.dateFormat)
			["startedAt", "finishedAt"].each {
				if (params.it) {
					Date candidate = formatter.parse(params.it)
					if (candidate != null &&
						(outcome.getProperty(it) == null) ||
						Math.abs(candidate.time - outcome.getProperty(it)) > 1000) {
						outcome.setProperty(it, candidate)
					}
				}
			}

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

				["startedAt", "finishedAt"].each {
					if (pOutcome.getProperty(it)) {
						Date candidate = pOutcome.getProperty(it)
						if (candidate != null && 
							(origOutcome.getProperty(it) == null) ||
							Math.abs(candidate.time - origOutcome.getProperty(it).time) > 1000) {
							outcome.setProperty(it, candidate)
						}
					}
				}

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
	
	
	


	/**
	* Returns a map with [testOutcomes: List<TestOutcome>, totalCount: integer, offset: long, outputChars: integer]
	*/
	Map getTestOutcomes(Map params) {
		
		def queryParams = [:]

		def possibleQueryParams = ["sort", "order", "max", "offset", "filter", "qry"]
		possibleQueryParams.each {possibleParam ->
			if (params.containsKey(possibleParam)) {
				queryParams[possibleParam] = params[possibleParam]
			}
		}

		def testRun = TestRun.get(params.id)
		if (!testRun) {
			throw new CuantoException("Test Run ${params.id} not found")
		}
		
		def outputChars = params.outputChars ? params.outputChars : 180

		def offset
		def totalCount
		def testOutcomes
		
		if (params.containsKey("recordStartIndex")) {
			offset = Integer.valueOf(params.recordStartIndex)
		} else if (params.containsKey("offset")) {
			offset = Integer.valueOf(params.offset)
		} else {
			offset = 0
		}

		def filter = params.filter

		if (params.qry) {
			totalCount = testRunService.countTestOutcomesBySearch(params)
			testOutcomes = testRunService.searchTestOutcomes(params)
		} else if (filter?.equalsIgnoreCase("allFailures")) {
			testOutcomes = testRunService.getOutcomesForTestRun(testRun, queryParams)
			totalCount = dataService.countFailuresForTestRun(testRun)
		} else if (filter?.equalsIgnoreCase("unanalyzedFailures")) {
			testOutcomes = testRunService.getOutcomesForTestRun(testRun, queryParams)
			totalCount = dataService.countUnanalyzedFailuresForTestRun(testRun)
		} else if (filter?.equalsIgnoreCase("newFailures")) {
			testOutcomes = testRunService.getNewFailures(testRun, queryParams)
			totalCount = testRunService.countNewFailuresForTestRun(testRun)
		} else if (params.outcome) {
			testOutcomes = [dataService.getTestOutcome(params.outcome)]
			offset = (Integer.valueOf(params.recordStartIndex))
			totalCount = params.totalCount
		} else {
			testOutcomes = testRunService.getOutcomesForTestRun(testRun, queryParams)
			totalCount = testRunService.countOutcomes(testRun)
		}

		return ['testOutcomes': testOutcomes, 'totalCount': totalCount, 'offset': offset, 'outputChars': outputChars]
	}


	String getCsvForTestOutcomes(List<TestOutcome> outcomes) {
		return getDelimitedTextForTestOutcomes(outcomes, ",")
/*		StringBuffer buff = new StringBuffer()
		buff << 'Test Outcome ID,Test Result,Analysis State,Started At,Finished At, Duration, Bug Title, Bug URL, Note\n'

		outcomes.each{ outcome ->
			def renderList = []
			renderList << outcome.id
			renderList << outcome.testResult.name
			renderList << outcome.analysisState?.name
			renderList << outcome.startedAt
			renderList << outcome.finishedAt
			renderList << outcome.duration
			renderList << outcome.bug?.title
			renderList << outcome.bug?.url
			renderList << outcome.note
			renderList.eachWithIndex { it, indx ->
				if (it == null) {
					buff << ''
				} else {
					buff << it.toString().replaceAll(',', '\\,')
				}
				//buff << it != null ? it : ''
				if (indx != renderList.size() - 1) {
					buff << ','
				}
			}
			buff << '\n'
		}

		return buff.toString()*/
	}
	
	String getDelimitedTextForTestOutcomes(List<TestOutcome> outcomes, String delimiter) {
		StringBuffer buff = new StringBuffer()
		String d = delimiter
		buff << "Test Outcome ID${d}Test Result${d}Analysis State${d}Started At${d}Finished At${d}Duration${d}Bug Title${d}Bug URL${d}Note\n"

		outcomes.each{ outcome ->
			def renderList = []
			renderList << outcome.id
			renderList << outcome.testResult.name
			renderList << outcome.analysisState?.name
			renderList << outcome.startedAt
			renderList << outcome.finishedAt
			renderList << outcome.duration
			renderList << outcome.bug?.title
			renderList << outcome.bug?.url
			renderList << outcome.note
			renderList.eachWithIndex { it, indx ->
				if (it == null) {
					buff << ''
				} else {
					buff << it.toString().replaceAll(delimiter, "\\${delimiter}")
				}
				//buff << it != null ? it : ''
				if (indx != renderList.size() - 1) {
					buff << delimiter
				}
			}
			buff << '\n'
		}

		return buff.toString()
	}
	
	
}
