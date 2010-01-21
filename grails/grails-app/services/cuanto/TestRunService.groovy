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
import cuanto.api.Link as ApiLink
import cuanto.api.TestProperty as ApiProperty
import cuanto.api.TestRun as TestRunApi

class TestRunService {

	def dataService
	def projectService
	def statisticService
	def sessionFactory

	boolean transactional = false

	SimpleDateFormat chartDateFormat = new SimpleDateFormat(Defaults.chartDateFormat)



	/**
	 * valid params are offset, max, sort and order.  id is the project id
	 */

	List<TestRun> getTestRunsForProject(Map params) {

		def queryParams = [:]

		["offset", "max", "order"].each {
			if (params.containsKey(it)) {
				queryParams[it] = params[it]
			}
		}

		if (!queryParams.containsKey("order")) {
			queryParams.order = "desc"
		}

		if (params.containsKey("sort")) {
			def sortMap = [:]
			sortMap["tests"] = "testRunStatistics.tests"
			sortMap["passed"] = "testRunStatistics.passed"
			sortMap["failed"] = "testRunStatistics.failed"
			sortMap["totalDuration"] = "testRunStatistics.totalDuration"
			sortMap["averageDuration"] = "testRunStatistics.averageDuration"
			sortMap["successRate"] = "testRunStatistics.successRate"

			if (sortMap.containsKey(params.sort)) {
				queryParams.sort = sortMap[params.sort]
			} else {
				queryParams.sort = params.sort
			}
		} else {
			queryParams.sort = "dateExecuted"
		}


		Project proj = null
		if (params.id) {
			proj = dataService.getProject(params.id)
		} else if (params.projectKey) {
			proj = dataService.getProjectByKey(params.projectKey)
		}
		List<TestRun> testRuns = dataService.getTestRunsByProject(proj, queryParams)
		return testRuns
	}

	List<TestRun> getTestRunsForGroupName(group) {
		def testRuns = []
		def projects = dataService.getProjectsByGroupName(group)
		projects.each { project ->
			testRuns += dataService.getTestRunsByProject(project, [max: 1, order: 'desc', offset: 0, sort: 'dateExecuted'])
		}
		return testRuns.flatten()
	}


	def getGoogleChartUrlForProject(proj) {
		def numColumns = 20
		def minimumResultsForXAxisLabels = 6
		def testRuns = getTestRunsForProject([id: proj?.id, max: numColumns, offset: 0, order: 'desc']).reverse()
		if (testRuns) {
			def chartString = new StringBuffer()
			chartString.append "cht=bvs" // chart style
			chartString.append "&chtt=Test+Totals" // title
			chartString.append "&chdl=Passed|Failed" // legend
			chartString.append "&chco=00BA1FCC,C33B00" // chart colors
			chartString.append "&chs=500x150" // chart size
			chartString.append "&chbh=15" //bar width
			if (testRuns.size() >= minimumResultsForXAxisLabels) {
				chartString.append "&chxt=x,y,r" // axes
			} else {
				chartString.append "&chxt=y,r" // axes
			}

			// chart dataset
			def maxTests = 0

			def passedDataSet = new StringBuffer()
			def failedDataSet = new StringBuffer()
			testRuns.eachWithIndex {testRun, indx ->
				def stats = testRun.testRunStatistics

				if (stats) {

					passedDataSet.append stats.passed?.toString()
					failedDataSet.append stats.failed?.toString()

					if (indx < (testRuns.size() - 1)) {
						passedDataSet.append ","
						failedDataSet.append ","
					}

					if (stats.tests > maxTests) {
						maxTests = stats.tests
					}
				}
			}

			def topYScale = (maxTests * 1.1).intValue() // todo, round up to nearest multiple of 10
			while (topYScale % 10 != 0) {
				topYScale++
			}

			chartString.append("&chd=t:").append(passedDataSet).append("|").append(failedDataSet)
			chartString.append("&chds=0,${topYScale},0,${topYScale}")

			if (passedDataSet.size() >= minimumResultsForXAxisLabels) {
				chartString.append("&chxl=0:").append(getDateLegend(testRuns)) // date legend
				chartString.append "|1:|0|${topYScale}"
				chartString.append "|2:|0|${topYScale}"
			} else {
				chartString.append("&chxl=")
				chartString.append "0:|0|${topYScale}"
				chartString.append "|1:|0|${topYScale}"
			}


			return "http://chart.apis.google.com/chart?" + chartString.toString()
		} else {
			return null
		}
	}


	private String getDateLegend(List testRuns) {
		if (testRuns.size() == 1) {
			return "|" + chartDateFormat.format(testRuns[0].dateExecuted) + "|"
		} else {

			def mid = (testRuns.size() / 2).intValue() - 1

			def firstDate = chartDateFormat.format(testRuns[0].dateExecuted)
			def middleDate = chartDateFormat.format(testRuns[mid].dateExecuted)
			def lastDate = chartDateFormat.format(testRuns[testRuns.size() - 1].dateExecuted)

			def dates = "|$firstDate"
			if (mid > 0 && (mid + 1 < testRuns.size() - 1)) {
				1.upto(mid) {
					dates += "|"
				}
				dates += "$middleDate"
				(mid + 1).upto(testRuns.size() - 1) {
					dates += "|"
				}
			}
			dates += "$lastDate|"

			dates = dates.replaceAll("/", "%2f")
			return dates
		}
	}


	def capitalize(String str) {        //todo: delete? unused?
		def words = str.tokenize(" ")
		StringBuffer buf = new StringBuffer()
		words.each {
			buf.append(it[0].toUpperCase() + it[1..it.size() - 1] + " ")
		}
		buf.toString().trim()
	}


	def getGoogleChartUrlForTestRunFailures(testRun) {
		def analysisStats = testRun?.testRunStatistics?.analysisStatistics
		if (analysisStats) {
			def chartString = new StringBuffer()
			chartString.append "cht=p" // chart style
			chartString.append "&chtt=Failures" // title
			chartString.append "&chs=350x200" // chart size
			chartString.append "&chf=bg,s,EDF5FF" // fill color

			def labels = new StringBuffer()
			def data = new StringBuffer()
			analysisStats.eachWithIndex {cause, idx ->
				def pct = (cause.qty / testRun.testRunStatistics.failed * 100).intValue().toString()
				labels.append("$pct% ").append(cause.state.name)
				data.append cause.qty
				if (idx < analysisStats.size() - 1) {
					labels.append "|"
					data.append ","
				}
			}

			def labelStr = labels.toString().replaceAll(" ", "+")
			chartString.append "&chl=$labelStr"
			chartString.append "&chd=t:$data"
			return "http://chart.apis.google.com/chart?" + chartString.toString()
		} else {
			return null
		}
	}

	/*
		 params contains sorting and paging info
		 sort = sort field,
		 max = max results
		 offset = first result
		 filter = "allfailures" or blank for all results
		 includeIgnored determines whether or not results that have the "includeInCalculations"
		 value set to false (e.g. Ignored, Unexecuted, etc) are included in the search.
	*/
	def getOutcomesForTestRun(TestRun testRun, Map params) {
		//todo: move logic into service
		def order
		def sort

		if (!params?.order || params?.order?.equalsIgnoreCase("asc")) {
			order = ""
		} else {
			order = params.order
		}

		def sortMap = [:]
		sortMap.testCase = "t.testCase.fullName"
		sortMap.result = "t.testResult.name"
		sortMap.analysisState = "t.analysisState.name"
		sortMap.duration = "duration"
		sortMap.bug = "t.bug.title"
		sortMap.owner = "t.owner"
		sortMap.note = "t.note"

		if (params?.sort == null) {
			sort = sortMap.testCase
		} else if (!sortMap.containsKey(params.sort)) {
			throw new CuantoException("Unknown sort option: ${params.sort}")
		} else {
			sort = sortMap[params.sort]
		}

		def paging = [:]
		if (params?.max) {
			paging.max = Integer.valueOf(params.max)
		}
		if (params?.offset) {
			paging.offset = Integer.valueOf(params.offset)
		}

		def outcomes
		if (params?.filter?.equalsIgnoreCase("allfailures")) {
			outcomes = dataService.getTestOutcomeFailuresByTestRun(testRun, sort, order, paging)
		} else if (params?.filter?.equalsIgnoreCase("unanalyzedfailures")) {
			outcomes = dataService.getTestOutcomeUnanalyzedFailuresByTestRun(testRun, sort, order, paging)
		}
		else {
			outcomes = dataService.getTestOutcomesByTestRun(testRun, sort, order, paging)
		}

		return outcomes
	}


	def getOutcomesForTestRun(TestRun testRun, String pkg, Map params, boolean includeIgnored) {
		def order
		if (!params?.order || params?.order?.equalsIgnoreCase("asc")) {
			order = ""
		} else {
			order = params.order
		}

		def paging = [:]
		if (params?.max) {
			paging.max = Integer.valueOf(params.max)
		}
		if (params?.offset) {
			paging.offset = Integer.valueOf(params.offset)
		}

		def outcomes = dataService.getTestOutcomesByTestRun(testRun, pkg, order, paging, includeIgnored)
		return outcomes
	}


	def countOutcomes(TestRun testRun) {
		return testRun.outcomes.size()
	}


	def countNewFailuresForTestRun(TestRun testRun) {
		return getNewFailures(testRun, null).size()
	}


	def getNewFailures(TestRun testRun, Map params) {
		def queryParams = [filter: "allfailures"]
		if (params?.order) {
			queryParams.order = params.order
		}
		if (params?.sort) {
			queryParams.sort = params.sort
		}

		def currentFailedOutcomes = getOutcomesForTestRun(testRun, queryParams)
		def newFailedOutcomes = dataService.getNewFailures(currentFailedOutcomes, testRun.dateExecuted)

		def startRange
		if (params?.offset) {
			startRange = Integer.valueOf(params.offset)
		} else {
			startRange = 0
		}

		def endRange
		if (params?.max) {
			endRange = startRange + Integer.valueOf(params.max)
			if (endRange > newFailedOutcomes.size() - 1) {
				endRange = newFailedOutcomes.size() - 1
			}
		} else {
			endRange = newFailedOutcomes.size() - 1
			if (endRange < 0) {
				endRange = 0
			}
		}

		if (newFailedOutcomes.size() == 0) {
			return []
		} else {
			return newFailedOutcomes[startRange..endRange]
		}
	}


	// given a List of TestOutcomes, return a List of TestCases
	def getTestCasesOfTestOutcomes(List<TestOutcome> outcomes) {
		def tCases = new HashSet()
		outcomes.each {	tCases += it.testCase }
		return tCases
	}


	def update(TestRun testRun, Map params) {
		if (testRun) {
			testRun.note = params.note
			if (!params.valid) {
				testRun.valid = false;
			}

			processTestPropertiesFromParameters(params, testRun)
			processLinksFromParameters(params, testRun)

			dataService.saveDomainObject testRun
		}
		return testRun
	}


	def update(TestRunApi testRun) {
		TestRun.withTransaction {
			TestRun origTestRun = TestRun.get(testRun.id)
			if (!origTestRun) {
				throw new CuantoException("Test run ID ${testRun.id} not found")
			}

			// update the TestRun values that can be updated
			def valuesToUpdate = ["note", "valid"]
			valuesToUpdate.each { String val ->
				origTestRun.setProperty(val, testRun.getProperty(val))
			}

			updatePropertiesOfTestRun origTestRun, testRun
			updateLinksOfTestRun origTestRun, testRun
			dataService.saveDomainObject origTestRun
		}
	}


	void updatePropertiesOfTestRun(TestRun origTestRun, TestRunApi testRun) {
		testRun.testProperties?.each { tp ->
			TestProperty origProp = origTestRun.testProperties?.find{ it.name == tp.name }
			if (origProp) {
				if (tp.value != origProp.value) {
					origProp.value = tp.value
					dataService.saveDomainObject origProp
				}
			} else {
				// property not found in original, so add it
				origTestRun.addToTestProperties(new TestProperty(tp.name, tp.value))
			}
		}

		def propsToRemove = []
		origTestRun.testProperties?.each { origProp ->
			if (!testRun.testProperties?.find { it.name == origProp.name }) {
				propsToRemove << origProp
			}
		}

		propsToRemove.each {
			origTestRun.removeFromTestProperties it
		}
	}


	void updateLinksOfTestRun(TestRun origTestRun, TestRunApi testRun) {
		testRun.links?.each { link ->
			Link origLink = origTestRun.links?.find { it.description == link.description }
			if (origLink) {
				if (origLink.url != link.url) {
					origLink.url = link.url
					dataService.saveDomainObject origLink
				}
			} else {
				// link not found in original, so add it
				origTestRun.addToLinks(new Link(link.description, link.url))
			}
		}

		def linksToRemove = []
		origTestRun.links?.each { Link origLink ->
			if (!testRun.links?.find {it.description == origLink.description}) {
				linksToRemove << origLink
			}
		}

		linksToRemove.each {
			origTestRun.removeFromLinks it
		}
	}


	Map processLinksFromParameters(Map params, TestRun testRun) {
		return params.each {String paramName, String paramValue ->
			// is this parameter an existing Link?
			def existingLinkMatcher = (paramName =~ /^linkId\[(\d+)]/)
			if (existingLinkMatcher.matches()) {
				def linkIndex = existingLinkMatcher[0][1] as Integer
				def linkId = params["linkId[${linkIndex}]"] as Integer
				if (linkId) {
					Link existingLink = Link.get(linkId)
					def descr = params["linkDescr[${linkIndex}]"] as String
					def url = params["linkUrl[${linkIndex}]"] as String
					if (existingLink.description != descr || existingLink.url != url) {
						existingLink.description = descr
						existingLink.url = url
						dataService.saveDomainObject existingLink
					}
				}
			} else {
				// is this parameter a new Link?
				def newLinkMatcher = (paramName =~ /^newLinkDescr\[(\d+)]/)
				if (newLinkMatcher.matches()) {
					def linkIndex = newLinkMatcher[0][1] as Integer
					def linkUrl = params["newLinkUrl[${linkIndex}]"] as String
					linkUrl = linkUrl?.trim()
					if (linkUrl) {
						def descr = paramValue.trim()
						if (!descr) {
							descr = linkUrl
						}
						def link = new Link(descr, linkUrl)
						testRun.addToLinks(link)
					}
				}
			}
		}
	}


	Map processTestPropertiesFromParameters(Map params, TestRun testRun) {
		return params.each {String paramName, String paramValue ->
			// is this parameter an existing TestProperty?
			def existingPropertyMatcher = (paramName =~ /^prop\[(\d+)]/)
			if (existingPropertyMatcher.matches()) {
				def propIndex = existingPropertyMatcher[0][1] as Integer
				def propId = params["propId[${propIndex}]"] as Integer
				if (propId) {
					TestProperty existingProp = TestProperty.get(propId)
					if (existingProp.value != paramValue) {
						existingProp.value = paramValue
						dataService.saveDomainObject existingProp
					}
				}
			} else {
				// is this parameter a new TestProperty?
				def newPropertyMatcher = (paramName =~ /^newPropName\[(\d+)]/)
				if (newPropertyMatcher.matches()) {
					def propIndex = newPropertyMatcher[0][1] as Integer
					def propValue = params["newPropValue[${propIndex}]"] as String
					if (propValue) {
						def testProperty = new TestProperty(paramValue.trim(), propValue.trim())
						testRun.addToTestProperties(testProperty)
					}
				}
			}
		}
	}


	def updateNote(testRun, note) {
		testRun.note = note
		dataService.saveDomainObject(testRun) 
		return testRun
	}


	void reportError(domainObj) {
		def errMsg = ""
		domainObj.errors.allErrors.each {
			errMsg += it.toString() + "\n"
		}
		log.warning errMsg
		throw new CuantoException("Error saving domain object: ${errMsg}")
	}


	def getFeedText(TestRun run) {
		SimpleDateFormat formatter = new SimpleDateFormat(Defaults.dateFormat)
		TestRunStats stats = run.testRunStatistics
		String dateTxt = formatter.format(run.dateExecuted)

		def text = """<table>
<thead>
	<tr><th>Date</th><th>Tests</th><th>Passed</th><th>Failed</th><th>Success</th><th>Total Duration</th>
		<th>Average Duration</th></tr>
</thead>
<tbody>
	<tr><td>${dateTxt}</td><td>${stats?.tests}</td><td>${stats?.passed}</td><td>${stats?.failed}</td>
		<td>${stats?.successRate}%</td><td>${stats?.totalDuration}</td><td>${stats?.averageDuration}</td></tr>
</tbody></table>"""
		return text.toString()
	}


	TestRun createTestRun(Map params) {
		def project
		if (params.containsKey("project")) {
			project = projectService.getProject(params.project)
			if (!project) {
				throw new CuantoException("Unable to locate project with the project key or full title of ${params.project}")
			}
		} else if (params.containsKey("id")) {
			project = dataService.getProject(Long.valueOf(params.id))
			if (!project) {
				throw new CuantoException("Unable to locate project with the id ${params.id}")
			}
		} else {
			throw new CuantoException("project or id parameter is required")
		}

		def testRun = new TestRun('project': project)
		testRun.note = params.note
		if (params.valid) {
			testRun.valid = Boolean.valueOf(params.valid)
		}

		// TODO: consolidate dateExecuted and userDateExecuted handling
		if (params.dateExecuted) {
			Date dateExecuted
			String dateFormat
			if (params.dateFormat) {
				dateFormat = params.dateFormat
			} else {
				dateFormat = Defaults.dateFormat
			}

			def dateFormatter = new SimpleDateFormat(dateFormat)
			dateExecuted = dateFormatter.parse(params.dateExecuted)
			testRun.dateExecuted = dateExecuted
		}
		else {
			testRun.dateExecuted = new Date()
		}

		if (params.userDateExecuted) {
			try {
				SimpleDateFormat formatter = new SimpleDateFormat(Defaults.dateFormat)
				Date candidate = formatter.parse(params.userDateExecuted)
				// only update the dateExecuted this if it's changed by more than a second
				if (Math.abs(candidate.time - testRun.dateExecuted.time) > 1000) {
					testRun.dateExecuted = candidate
				}
			} catch (java.text.ParseException e) {
				testRun.errors.reject('date.not.parseable.message', [params.userDateExecuted, Defaults.dateFormat] as Object[],
					"${params.userDateExecuted} is not a parsable date matching the format ${Defaults.dateFormat}")
			}
		}

		parseLinksFromParams(params).each {
			testRun.addToLinks(it)
		}

		parseTestPropertiesFromParams(params).each {
			testRun.addToTestProperties(it)
		}

		testRun.testRunStatistics = new TestRunStats()  //todo: is this needed?
		dataService.saveTestRun(testRun)
		return testRun
	}


	List parseLinksFromParams(Map params) {
		def linksToAdd = []
		if (params.link) {
			def links = [params.link].flatten()
			links.each {String linkParam ->
				try {
					Link link = new Link()
					def linkSplit = linkParam.split('\\|\\|', 3)
					link.url = getUrlFromString(linkSplit[0])

					if (linkSplit.length > 1) {
						link.description = linkSplit[1]
					} else {
						link.description = linkSplit[0]
					}
					linksToAdd << link

				} catch (MalformedURLException e) {
					// Don't persist the link, just log an error
					log.error "Malformed URL: ${e.message}"
				}
			}
		}
		return linksToAdd
	}

	
	List parseTestPropertiesFromParams(Map params) {
		def propsToAdd = []
		if (params.testProperty) {
			def props = [params.testProperty].flatten()
			props.each { String propParam ->
				def propSplit = propParam.split('\\|\\|')
				if (propSplit.length > 1) {
					propsToAdd << new TestProperty(propSplit[0], propSplit[1])
				}
			}
		}
		return propsToAdd
	}


	TestRun createTestRun(TestRunApi pTestRun) {

		def project = projectService.getProject(pTestRun.projectKey)
		if (!project) {
			throw new CuantoException("Unable to locate project with the project key or full title of ${pTestRun.projectKey}")
		}

		def testRun = new TestRun('project': project)
		testRun.note = pTestRun.note
		if (pTestRun.valid != null) {
			testRun.valid = pTestRun.valid
		}

		if (pTestRun.dateExecuted) {
			testRun.dateExecuted = pTestRun.dateExecuted
		} else {
			testRun.dateExecuted = new Date()
		}

		pTestRun.links?.each {ApiLink aLink->
			try {
				// todo: make description and link HTML-safe?
				testRun.addToLinks(new Link(aLink.description, aLink.url))
			} catch (MalformedURLException e) {
				// Don't persist the link, just log an error
				log.error "Malformed URL: ${e.message}"
			}
		}

		pTestRun.testProperties?.each { ApiProperty aProperty ->
			// todo: make name and value HTML-safe?
			testRun.addToTestProperties(new TestProperty(aProperty.name, aProperty.value))
		}

		testRun.testRunStatistics = new TestRunStats()  //todo: is this needed?
		dataService.saveTestRun(testRun)
		return testRun
	}

	def getUrlFromString(String urlString) {
		return new URL(urlString).toString()
	}

	def getProject(projectString) {
		def project = Project.findByProjectKey(projectString)
		if (!project) {
			project = projectService.getProjectByFullName(projectString)
		}
		return project

	}
	
	def createManualTestRun(params) {
		def testRun = createTestRun(params)
		def testCases = projectService.getSortedTestCases(testRun.project)
		def testOutcomesToSave = []
		testCases.each {tc ->
			def testOutcome = new TestOutcome()
			testOutcome.testCase = tc
			testOutcome.testResult = dataService.result("Unexecuted")
			testOutcomesToSave << testOutcome
		}
		dataService.saveTestOutcomes(testRun, testOutcomesToSave)
		statisticService.queueTestRunStats testRun
		return testRun
	}


	def getBugSummary(testRun) {
		def bugList = []

		def rawBs= dataService.getBugSummary(testRun)
		rawBs.each {
			def entry = [bug: it[0], total: it[1]]
			bugList << entry
		}
		return bugList
	}



	def countTestOutcomesBySearch(Map params) {
		def queryDetails = parseQueryFromParams(params)
		def searchField = queryDetails['searchField']
		def searchTerms = queryDetails['searchTerms']

		def count = 0
		def testRun = TestRun.get(params.id)
		if (testRun && allowedSearches.contains(searchField)) {
			def validParams = extractValidParams(params)
			count = dataService.countTestOutcomesBySearch(searchField, searchTerms, testRun, validParams)
		}
		return count 
	}
	

	def searchTestOutcomes(Map params) {
		def queryDetails = parseQueryFromParams(params)
		def searchField = queryDetails['searchField']
		def searchTerms = queryDetails['searchTerms']

		def testOutcomes = []
		def testRun = TestRun.get(params.id)
		if (testRun && allowedSearches.contains(searchField)) {
			def validParams = extractValidParams(params)
			testOutcomes = dataService.searchTestOutcomes(searchField, searchTerms, testRun, validParams)
		}
		return testOutcomes
	}

	def parseQueryFromParams(params) {
		// returns a map with 'searchField' and 'searchTerms'
		def searchField, query
		if (params.qry) {
			def delim = params?.qry?.indexOf("|")
			if (delim == -1) {
				searchField = "Name"
				query = params.qry
			} else {
				searchField = params.qry.substring(0, delim)
				query = params.qry.substring(delim + 1)
			}

			return ['searchField': searchField.toLowerCase(), 'searchTerms': query]
		} else {
			throw new CuantoException("No query parameter was provided")
		}
	}


	def getAllowedSearches() {
		return ["note", "name", "owner", "output"]
	}


	def extractValidParams(Map params) {
		def validParamKeys = ["max", "order", "offset", "sort", "filter"]
		def newPagingMap = [:]
		validParamKeys.each { paramKey ->
			if (params.containsKey(paramKey)) {
				newPagingMap[paramKey] = params[paramKey]
			}
		}
		return newPagingMap
	}


	List<TestRun> getTestRunsWithProperties(Project proj, List<TestProperty> props) {
		if (!props) {
			throw new CuantoException("No properties were specified")
		}

		def qryFromClause = "from cuanto.TestRun tr left join tr.testProperties prop_0 "
		def qryWhereClause = "where tr.project = ? and prop_0.name=? and prop_0.value=? "
		def qryArgs = [proj, props[0].name, props[0].value]
		for (def idx = 1; idx < props.size(); idx++) {
		    qryFromClause += "left join tr.testProperties prop_${idx} "
			qryWhereClause += "and prop_${idx}.name=? and prop_${idx}.value=? "
			qryArgs << props[idx].name
			qryArgs << props[idx].value
		}

		def results = TestRun.executeQuery(qryFromClause + qryWhereClause + " order by tr.dateExecuted desc", qryArgs)
		if (results == null || results?.size() == 0) {
			return []
		} else {
			return results.collect { it[0] }
		}
	}

}
