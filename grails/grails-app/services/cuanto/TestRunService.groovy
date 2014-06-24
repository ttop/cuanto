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
import org.hibernate.StaleObjectStateException
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException
import org.springframework.dao.OptimisticLockingFailureException

class TestRunService {

	def dataService
	def projectService
	def statisticService
	def failureStatusService
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
			sortMap["tests"] = "trs.tests"
			sortMap["passed"] = "trs.passed"
			sortMap["failed"] = "trs.failed"
            sortMap["quarantined"] = "trs.quarantined"
			sortMap["totalDuration"] = "trs.totalDuration"
			sortMap["averageDuration"] = "trs.averageDuration"
			sortMap["successRate"] = "trs.successRate"
			sortMap["dateExecuted"] = "t.dateExecuted"

			if (sortMap.containsKey(params.sort)) {
				queryParams.sort = sortMap[params.sort]
			} else {
				queryParams.sort = params.sort
			}
		} else {
			queryParams.sort = "t.dateExecuted"
		}

		Project proj = null
		if (params.id) {
			proj = dataService.getProject(params.id)
		} else if (params.projectKey) {
			proj = projectService.getProject(params.projectKey)
		}
		List<TestRun> testRuns = dataService.getTestRunsByProject(proj, queryParams)
		return testRuns
	}

	List<TestRun> getTestRunsForGroupName(group) {
		def testRuns = []
		def projects = dataService.getProjectsByGroupName(group)
		projects.each { project ->
			testRuns += dataService.getTestRunsByProject(project, [max: 1, order: 'desc', offset: 0, sort: 't.dateExecuted'])
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
				TestRunStats stats = TestRunStats.findByTestRun(testRun)

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


	private synchronized String getDateLegend(List testRuns) {
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



	def getGoogleChartUrlForTestRunFailures(testRun) {
		TestRunStats stats = TestRunStats.findByTestRun(testRun)

		def analysisStats = stats?.analysisStatistics
		if (analysisStats) {
			def chartString = new StringBuffer()
			chartString.append "cht=p" // chart style
			chartString.append "&chtt=Failures" // title
			chartString.append "&chs=350x200" // chart size
			chartString.append "&chf=bg,s,EDF5FF" // fill color

			def labels = new StringBuffer()
			def data = new StringBuffer()
			analysisStats.eachWithIndex {cause, idx ->
				def pct
				if (stats?.failed == 0) {
					pct = 0
				} else {
					pct = (cause.qty / stats.failed * 100).intValue().toString()
				}
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
	* Find the TestRun chronologically before this test run and return the successRate of that testRun.
	* If there is no previous TestRun or if the previous TestRun has a successRate of null, null will be returned.
	*/
	def getPreviousTestRunSuccessRate(TestRun testRun) {
		if (testRun == null) {
			throw new NullPointerException("Cannot fetch a null TestRun")
		}

		if (testRun.dateExecuted == null) {
			throw new CuantoException("TestRun does not have a valid dateExecuted")
		}

		def previousStats = null

		List<TestRun> previousTestRuns = TestRun.findAllByProjectAndDateExecutedLessThan(testRun.project, testRun.dateExecuted,
			[max: 1, sort: "dateExecuted", order:"desc"])

		if (previousTestRuns) {
			previousStats = TestRunStats.findByTestRun(previousTestRuns[0])
		}
		return previousStats?.successRate
	}

	def countOutcomes(TestRun testRun) {
		if (testRun) {
			return TestOutcome.countByTestRun(testRun)
		} else {
			return 0
		}
	}

	// given a List of TestOutcomes, return a Set of TestCases

	Set getTestCasesOfTestOutcomes(List<TestOutcome> outcomes) {
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
			if (params.pinRun == "on") {
				testRun.allowPurge = false
			}
			dataService.saveDomainObject testRun
		}
		return testRun
	}


	def update(testRun) {
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


	void updatePropertiesOfTestRun(TestRun origTestRun, testRun) {
		testRun.testProperties?.each { tp ->
			TestRunProperty origProp = origTestRun.testProperties?.find { it.name == tp.name }
			if (origProp) {
				if (tp.value != origProp.value) {
					origProp.value = tp.value
					dataService.saveDomainObject origProp
				}
			} else {
				// property not found in original, so add it
				origTestRun.addToTestProperties(new TestRunProperty(tp.name, tp.value))
				dataService.saveDomainObject origTestRun
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
			dataService.saveDomainObject origTestRun
			it.delete()
		}
	}


	void updateLinksOfTestRun(TestRun origTestRun, testRun) {
		testRun.links?.each { link ->
			TestRunLink origLink = origTestRun.links?.find { it.description == link.description }
			if (origLink) {
				if (origLink.url != link.url) {
					origLink.url = link.url
					dataService.saveDomainObject origLink
				}
			} else {
				// link not found in original, so add it
				origTestRun.addToLinks(new TestRunLink(link.url, link.description))
			}
		}

		def linksToRemove = []
		origTestRun.links?.each { TestRunLink origLink ->
			if (!testRun.links?.find {it.description == origLink.description}) {
				linksToRemove << origLink
			}
		}

		linksToRemove.each {
			origTestRun.removeFromLinks it
			dataService.saveDomainObject origTestRun
			it.delete()
		}
	}


	void processLinksFromParameters(Map params, TestRun testRun) {
		params.each {String paramName, String paramValue ->
			// is this parameter an existing Link?
			def existingLinkMatcher = (paramName =~ /^linkId\[(\d+)]/)
			if (existingLinkMatcher.matches()) {
				def linkIndex = existingLinkMatcher[0][1] as Integer
				def linkId = params["linkId[${linkIndex}]"] as Integer
				if (linkId) {
					TestRunLink existingLink = TestRunLink.get(linkId)
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
						def link = new TestRunLink(linkUrl, descr)
						testRun.addToLinks(link)
					}
				}
			}
		}
	}


	void processTestPropertiesFromParameters(Map params, TestRun testRun) {
		params.each {String paramName, String paramValue ->
			// is this parameter an existing TestProperty?
			def existingPropertyMatcher = (paramName =~ /^prop\[(\d+)]/)
			if (existingPropertyMatcher.matches()) {
				def propIndex = existingPropertyMatcher[0][1] as Integer
				def propId = params["propId[${propIndex}]"] as Integer
				if (propId) {
					TestRunProperty existingProp = TestRunProperty.get(propId)
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
						def testProperty = new TestRunProperty(paramValue.trim(), propValue.trim())
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


	def getFeedText(TestRun run, TestRunStats stats) {
		SimpleDateFormat formatter = new SimpleDateFormat(Defaults.dateFormat)
		String dateTxt = formatter.format(run.dateExecuted)

		def text = """<table>
<thead>
	<tr><th>Date</th><th>Tests</th><th>Passed</th><th>Failed</th><td>Quarantined</td><th>Success</th><th>Total Duration</th>
		<th>Average Duration</th></tr>
</thead>
<tbody>
	<tr><td>${dateTxt}</td><td>${stats?.tests}</td><td>${stats?.passed}</td><td>${stats?.failed}</td><td>${stats?.quarantined}</td>
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
			if (params.dateFormatter) {
				dateFormat = params.dateFormatter
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

		dataService.saveTestRun(testRun)
		statisticService.queueTestRunStats testRun
		return testRun
	}


	List parseLinksFromParams(Map params) {
		def linksToAdd = []
		if (params.link) {
			def links = [params.link].flatten()
			links.each {String linkParam ->
				try {
					TestRunLink link = new TestRunLink()
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
					propsToAdd << new TestRunProperty(propSplit[0], propSplit[1])
				}
			}
		}
		return propsToAdd
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
            testOutcome.testRun = testRun
			testOutcomesToSave << testOutcome
		}
		dataService.saveTestOutcomes(testOutcomesToSave)
		statisticService.queueTestRunStats testRun
		return testRun
	}


	def getBugSummary(testRun) {
		def bugList = []

		def rawBs = dataService.getBugSummary(testRun)
		rawBs.each {
			def entry = [bug: it[0], total: it[1]]
			bugList << entry
		}
		return bugList
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


	List<TestRun> getTestRunsWithProperties(Project proj, List<TestRunProperty> props) {
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

		final String fullQuery = qryFromClause + qryWhereClause + " order by tr.dateExecuted desc"
		def results = TestRun.executeQuery(fullQuery, qryArgs)
		if (results == null || results?.size() == 0) {
			return []
		} else {
			return results.collect { it[0] }
		}
	}


	List<String> getTestRunPropertiesByProject(Project project) {
		TestRunProperty.executeQuery("select name from cuanto.TestRunProperty trp where trp.testRun.project = ? group by name order by name", [project])
	}


	/**
	 * Delete all TestOutcomes for a TestRun, then delete the TestRun,
	 * after which FailureStatusUpdateTasks are queued for re-initialization of the isFailureStatusChanged field
	 * for all TestOutcomes in the next TestRun.
	 */
	def deleteTestRun(TestRun run, recalc = true) {
			try {
				TestRun testRun = TestRun.get(run.id)
				TestRun nextRun = null

				 if (recalc) {
					 nextRun = dataService.getNextTestRun(run)
				 }

				statisticService.dequeueTestRunStats(run.id)
				statisticService.deleteStatsForTestRun(run)

				if (testRun.tags) {
					def testRunTagsToRemove = new ArrayList(testRun.tags)
					testRunTagsToRemove.each {tag ->
						testRun.removeFromTags(tag)
					}
				}

				def outcomes = TestOutcome.findAllByTestRun(testRun, [max: 500])
				while (outcomes) {
					outcomes.each {outcome ->
						if (outcome.tags) {
							def testOutcomeTagsToRemove = new ArrayList(outcome.tags)
							testOutcomeTagsToRemove.each {tag ->
								outcome.removeFromTags(tag)
							}
						}

						outcome.delete()
					}

					TestOutcome.withSession {
						it.flush()
					}
					outcomes = TestOutcome.findAllByTestRun(testRun, [max: 500])
				}

				testRun.save()
				testRun.delete()

				if (recalc && nextRun) {
					failureStatusService.queueFailureStatusUpdateForRun(nextRun)
					nextRun?.discard()
				}
			} catch (OptimisticLockingFailureException e) {
				log.error "OptimisticLockingFailureException for test run ${run.id}"
			} catch (HibernateOptimisticLockingFailureException e) {
				log.error "HibernateOptimisticLockingFailureException for test run ${run.id}"
			} catch (StaleObjectStateException e) {
				log.error "StaleObjectStateException for test run ${run.id}"
			}
	}


	Integer deleteTestRuns(testRunIds, recalc = true) {
		testRunIds.each { runId ->
			deleteTestRun(TestRun.get(runId), recalc)
		}
		return testRunIds.size()
	}


	def deleteTestRunProperty(TestRunProperty propToDelete) throws CuantoException {
		if (propToDelete) {
			TestRun.withTransaction {
				TestRun testRun = propToDelete.testRun
				testRun.removeFromTestProperties(propToDelete)
				dataService.saveDomainObject testRun
				propToDelete.delete()
			}
		}
	}

	def deleteTestRunLink(TestRunLink linkToDelete) throws CuantoException {
		if (linkToDelete) {
			TestRun.withTransaction {
				TestRun testRun = linkToDelete.testRun
				testRun.removeFromLinks(linkToDelete)
				dataService.saveDomainObject testRun
				linkToDelete.delete()
			}
		}
	}
}
