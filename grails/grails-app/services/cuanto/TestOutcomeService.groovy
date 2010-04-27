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


	def updateTestOutcome(pOutcome) {
		if (pOutcome) {
			TestOutcome origOutcome = dataService.getTestOutcome(pOutcome.id)
			TestOutcome outcome = dataService.getTestOutcome(pOutcome.id)
			if (outcome) {
				applyTestResultToTestOutcome(outcome, dataService.result(pOutcome.testResult.toString()))

				if (pOutcome.analysisState) {
					outcome.analysisState = dataService.getAnalysisStateByName(pOutcome.analysisState.toString())
				}

				def origBug = outcome.bug
				outcome.bug = bugService.getBug(pOutcome.bug?.title, pOutcome.bug?.url)
				outcome.note = pOutcome.note
				outcome.owner = pOutcome.owner
				outcome.testOutput = pOutcome.testOutput
				outcome.duration = pOutcome.duration

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

		if (params.filter) {
			totalCount = dataService.countTestOutcomes(getTestOutcomeQueryFilterForParams(params))
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
	

	Integer getTestOutcomeCountForParams(Map params) throws CuantoException {
		TestOutcomeQueryFilter testOutcomeFilter = getTestOutcomeQueryFilterForParams(params)
		return dataService.countTestOutcomes(testOutcomeFilter)
	}


	/**
	* Returns a map with [testOutcomes: List<TestOutcome>, totalCount: integer, offset: long, outputChars: integer]
	*/
	Map getTestOutcomeQueryResultsForParams(Map params) throws CuantoException {
		TestOutcomeQueryFilter testOutcomeFilter = getTestOutcomeQueryFilterForParams(params)
		def outputChars = params.outputChars ? params.outputChars : 180
		def offset
		def totalCount
		def testOutcomes

		// todo: hunt down this difference in parameters and unify the usage
		if (params.containsKey("recordStartIndex")) {
			offset = Integer.valueOf(params.recordStartIndex)
		} else if (params.containsKey("offset")) {
			offset = Integer.valueOf(params.offset)
		} else {
			offset = 0
		}

		if (params.filter?.equalsIgnoreCase("newFailures")) {
			TestRun testRun = TestRun.get(params.id)
			if (!testRun) {
				throw new RuntimeException("TestRun must be specified to retrieve new ")
			}
			def newFailureQueryFilter = new TestOutcomeQueryFilter(
				testRun: testRun,
				isFailure: true,
				isFailureStatusChanged: true)
			testOutcomes = dataService.getTestOutcomes(newFailureQueryFilter)
			totalCount = testOutcomes.size()
		} else if (params.outcome) { // todo: hmm, look at this
			testOutcomes = [dataService.getTestOutcome(params.outcome)]
			offset = (Integer.valueOf(params.recordStartIndex))
			totalCount = params.totalCount
		} else {
			testOutcomes = dataService.getTestOutcomes(testOutcomeFilter)
			totalCount = dataService.countTestOutcomes(testOutcomeFilter)
		}
			
		return ['testOutcomes': testOutcomes, 'totalCount': totalCount, 'offset': offset, 'outputChars': outputChars]
	}


	Map parseQueryFromString(String rawQuery) {
		// returns a map with 'searchField' and 'searchTerms'
		String query
		String searchField
		def delim = rawQuery.indexOf("|")
		if (delim == -1) {
			searchField = "Name"
			query = rawQuery
		} else {
			searchField = rawQuery.substring(0, delim)
			query = rawQuery.substring(delim + 1)
		}
		return ['searchField': searchField.toLowerCase(), 'searchTerms': query]
	}


	TestOutcomeQueryFilter getTestOutcomeQueryFilterForParams(Map params) throws CuantoException {
		TestOutcomeQueryFilter filter = new TestOutcomeQueryFilter()

		if (params.id) {
			def testRun = TestRun.get(params.id) as TestRun
			if (!testRun) {
				throw new CuantoException("Test Run ${params.id} not found")
			}
			filter.testRun = testRun
		}

		if (params.testCase) {
			def testCase = TestCase.get(params.testCase) as TestCase
			if (!testCase) {
				throw new CuantoException("Test Case ${params.testCase} not found")
			}
			filter.testCase = testCase
		}

		filter.sorts = getSortParametersFromParamStrings(params.sort, params.order)

		if (params.max) {
			filter.queryMax = Integer.valueOf(params.max)
		}

		if (params.offset) {
			filter.queryOffset = Integer.valueOf(params.offset)
		} else if (params.recordStartIndex) {
			filter.queryOffset = Integer.valueOf(params.recordStartIndex ) // todo: hunt down this difference in parameters and unify the usage
		}

		if (params.qry) {
			Map searchDetails = parseQueryFromString(params.qry)
			filter.setForSearchTerm(searchDetails.searchField, searchDetails.searchTerms)
		}

		if (params.filter?.equalsIgnoreCase("allFailures")) {
			filter.isFailure = true
		} else if (params.filter?.equalsIgnoreCase("newFailures")) {
			filter.isFailure = true
			filter.isFailureStatusChanged = true
		} else if (params.filter?.equalsIgnoreCase("newPasses")) {
			filter.isFailure = false
			filter.isFailureStatusChanged = true
		} else if (params.filter?.equalsIgnoreCase("unanalyzedFailures")) {
			filter.isFailure = true
			filter.isAnalyzed = false
		}

        if (params.tag) {
            filter.tags = [params.tag].flatten()
        }

        if (params.hasTags != null) {
            filter.hasTags = Boolean.valueOf(params.hasTags)
        }

		return filter
	}


	List<SortParameters> getSortParametersFromParamStrings(sortParam,  orderParam) {
		def sorts = []
		def sortParams = [sortParam].flatten()
		def orderParams = [orderParam].flatten()

		if (sortParams.size() != orderParams.size()) {
			throw new CuantoException("Number of sorts and number of orders don't match")
		}

		sortParams.eachWithIndex{ sort, indx ->
			final String sortName = TestOutcomeQueryFilter.getSortNameForFriendlyName(sort)
			def primarySort = new SortParameters(sort: sortName, sortOrder: orderParams[indx])

			sorts << primarySort

			if (sortName == "testCase.fullName") {
				def secondarySort = new SortParameters(sort: "testCase.parameters")
				if (orderParam) {
					secondarySort.sortOrder = primarySort.sortOrder
				}
				sorts << secondarySort
			}
		}
		return sorts as List<SortParameters>

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
	

	 /**
	  * Determines whether the failure status for a given TestOutcome has changed
	  * from the last (if applicable) TestOutcome for the same TestCase.
	  *
	  * @param testOutcome to determine failure status change
	  * @return true if the failure status changed or false otherwise
	  */
	def isFailureStatusChanged(TestOutcome testOutcome) {
		def previousOutcome = dataService.getPreviousOutcome(testOutcome)

		if (testOutcome.testResult?.isFailure) {
			// if the previous test outcome does not exist or it was not a failure,
			// the current failed test outcome is a change in failure status.
			return !previousOutcome || !previousOutcome.testResult?.isFailure
		} else {
			// if the previous test outcome does not exist,
			// this successful test outcome is not a change in failure status.
			// but if it exists and was a failure, this successful test outcome is a change in failure status.
			return previousOutcome && previousOutcome.testResult?.isFailure
		}
	}
}
