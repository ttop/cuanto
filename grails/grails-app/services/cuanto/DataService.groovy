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


class DataService {

	boolean transactional = true


	Bug getBug(String title, String url) throws CuantoException {
		if (!title && !url) {
			throw new CuantoException("Neither a bug title or URL was provided")
		}

		Bug bug = null
		if (url) {
			bug = Bug.findByUrl(url)
		} else {
			bug = Bug.findByTitle(title)
		}

		if (!bug) {
			bug = new Bug('title': title, 'url': url)
			saveDomainObject(bug)
		}
		return bug
	}


	TestRun getTestRun(id) {
		return TestRun.get(id)
	}


	List getRawTestRunStats(TestRun run) { /* return the count, total test time and average test time for the test run*/
		def queryResults = TestRun.executeQuery("select count(*), sum(duration), avg(duration) from cuanto.TestOutcome t where t.testRun = ? and t.testResult.includeInCalculations = true",
			[run])
		return queryResults[0]
	}


	def getTestRunFailureCount(TestRun run) {
		TestOutcome.executeQuery("select count(*) from cuanto.TestOutcome t where t.testRun = ? and t.testResult.isFailure = true",
			[run])[0]
	}


	/**
	 Delete all outcomes for a testrun, then delete the testrun
	 */

	def deleteTestRun(TestRun run) {
		TestOutcome.executeUpdate("delete cuanto.TestOutcome t where t.testRun = ?", [run])
		run.delete()
	}

	def deleteOutcomesForTestRun(TestRun run) {
		TestOutcome.executeUpdate("delete cuanto.TestOutcome t where t.testRun = ?", [run])
	}


	def deleteStatisticsForTestRun(TestRun testRun) {
		if (testRun?.testRunStatistics) {
			testRun.testRunStatistics.delete()
			testRun.testRunStatistics = null
			saveDomainObject testRun
		}
	}

	def deleteTestRunsForProject(Project project) {
		TestRun.executeUpdate("delete cuanto.TestRun t where t.project = ?", [project])
	}

	def getProject(id) {
		Project.get(id)
	}


	def getProject(groupName, projectName) throws CuantoException {  //todo: optimize
		def project = null
		def group = findProjectGroupByName(groupName)
		project = Project.findByProjectGroupAndName(group, projectName)
		return project
	}


	def getProjectByKey(projectKey) {
		Project.findByProjectKey(projectKey)
	}


	def getTestRunsByProject(proj, final Map queryParams) {
		TestRun.executeQuery("from cuanto.TestRun t where t.project = ? order by ${queryParams.sort} ${queryParams.order}",
			[proj], [max: Integer.valueOf(queryParams.max), offset: Integer.valueOf(queryParams.offset)])
	}


	def getTestRunsByProject(proj) {
		TestRun.findAllByProject(proj)
	}


	def countTestRunsByProject(proj) {
		TestRun.countByProject(proj)
	}


	def getTestRunsWithoutAnalysisStatistics() {
		def criteria = TestRunStats.createCriteria()
		def trStats = criteria.list {
			and {
				testRun {
					isNotEmpty("outcomes")
				}
				isEmpty("analysisStatistics")
				gt("failed", 0)
			}
			order("id")
		}
		def runs = trStats.collect { it.testRun }
		return runs 
	}


	def findMatchingTestCaseForProject(Project project, TestCase testcase) {
		def query = "from cuanto.TestCase as tc where tc.project=? and tc.fullName=? "
		if (testcase.parameters == null) {
			query += "and is null (tc.parameters)"
			return TestCase.find("from cuanto.TestCase as tc where tc.project=? and tc.fullName=?",
				[project, testcase.fullName])
		} else {
			query += "and tc.parameters=?"
			return TestCase.find("from cuanto.TestCase as tc where tc.project=? and tc.fullName=? and tc.parameters=?",
				[project, testcase.fullName, testcase.parameters])
		}
	}


	def addTestCases(Project project, List testCases) {
		for (testCase in testCases) {
			project.addToTestCases(testCase)
		}
		saveDomainObject project
	}


	def saveTestOutcomes(TestRun testRun, List outcomes) {
		for (outcome in outcomes) {
			testRun.addToOutcomes outcome
		}

		saveDomainObject testRun, true
	}


	def saveTestRun(TestRun testRun) {
		saveDomainObject testRun, true
		return testRun
	}


	def getTestType(String name) {
		if (name) {
			return TestType.findByNameIlike(name)
		} else {
			return null
		}
	}


	Map getAllTestResultsMap() {
		Map testResultMap = new HashMap()
		TestResult.list().each { testResult ->
			testResultMap[testResult.name.toLowerCase()] = testResult
		}
		return testResultMap
	}


	Map<String, AnalysisState> getAllAnalysisStatesMap() {
		Map analysisStateMap = [:]
		AnalysisState.findAll("from cuanto.AnalysisState").each {analysisState ->
			analysisStateMap[analysisState.getName().toLowerCase()] = analysisState
		}
		return analysisStateMap
	}


	void deleteBugIfUnused(Bug bug) {
		// delete if no TestOutcome is referencing this bug
		if (bug) {
			def bugRefs = TestOutcome.countByBug(bug)
			if (bugRefs == 0) {
				bug.delete()
			}
		}
	}


	void deleteProject(Project project) {
		if (project) {
			def group = project.projectGroup
			project.delete(flush: true)
			if (group && Project.countByProjectGroup(group) == 0) {
				group.delete(flush: true)
			}
		}
	}


	void reportSaveError(domainObj) throws CuantoException {
		def errMsg = ""
		domainObj.errors.allErrors.each {
			log.warn it.toString()
			errMsg += it.toString()
		}
		throw new CuantoException("Failed saving: ${errMsg}")
	}


	// paging is an optional Map containing values for "max" or "offset" -- pass null or an empty map to ignore
	// sort is the field name to sort by, order is asc or desc
	List getTestOutcomesByTestRun(TestRun run, String sort, String order, Map paging) {
		if (!order) {
			order = "asc"
		}
		if (order.toLowerCase() != "asc" && order.toLowerCase() != "desc") {
			throw new IllegalArgumentException("${order} is not a valid sort order")
		}

		if (!sort) {
			sort = "t.testCase.fullName"
		}

		def query = "from cuanto.TestOutcome t where t.testRun = ? order by ${sort} ${order}"

		def results
		if (paging) {
			results = TestOutcome.executeQuery(query, [run], paging)
		} else {
			results = TestOutcome.executeQuery(query, [run])
		}
		return results
	}
	

	// paging is an optional Map containing values for "max" or "offset" -- pass null or an empty map to ignore
	// sort is the field name to sort by, order is asc or desc
	List getTestOutcomeFailuresByTestRun(TestRun run, String sort, String order, Map paging) {
		return TestOutcome.executeQuery("from cuanto.TestOutcome t where t.testRun = ? and t.testResult.isFailure = true order by ${sort} ${order}",
			[run], paging)
	}


	// paging is an optional Map containing values for "max" or "offset" -- pass null or an empty map to ignore
	// sort is the field name to sort by, order is asc or desc
	List getTestOutcomeUnanalyzedFailuresByTestRun(TestRun run, String sort, String order, Map paging) {
		return TestOutcome.executeQuery("""from cuanto.TestOutcome t where t.testRun = ? and t.testResult.isFailure = true
and t.analysisState.isAnalyzed = false order by ${sort} ${order}""",
			[run], paging)
	}


	List getTestOutcomesByTestRun(TestRun run, String pkg, String order, Map paging, boolean includeIgnored) {
		def queryArgs = [run]
		def query = "from cuanto.TestOutcome t where t.testRun = ? "

		if (pkg) {
			queryArgs << pkg
			queryArgs << pkg + ".%"
			query += " and (t.testCase.packageName = ? or t.testCase.packageName like ?)"
		}

		if (!includeIgnored) {
			query += "and t.testResult.includeInCalculations = true "
		}
		if (!order) {
			order = "asc"
		}

		query += "order by t.testCase.fullName ${order} "
		def outcomes
		if (paging) {
			outcomes = TestOutcome.executeQuery(query, queryArgs, paging)
		} else {
			outcomes = TestOutcome.executeQuery(query, queryArgs)
		}
		return outcomes
	}


	List getNewFailures(testOutcomes, priorToDate) {
		// return a list of the outcomes where the test case outcome's result immediately prior to priorToDate was a
		// pass
		def newFails = testOutcomes.findAll { currentOutcome ->
			if (!currentOutcome.testResult.isFailure){
				return false
			} else {
				def previousOutcome = getPreviousOutcome(currentOutcome.testCase, priorToDate)
				if (previousOutcome) {
					return !previousOutcome?.testResult?.isFailure
				} else {
					return true
				}
			}
		}
		return newFails
	}


	TestOutcome getPreviousOutcome(testCase, priorToDate) {
		def qry = "from cuanto.TestOutcome tout where tout.testCase = ? and tout.testRun.dateExecuted < ? order by \
                      tout.testRun.dateExecuted desc"
		def out = TestOutcome.find(qry, [testCase, priorToDate])
		return out
	}


	void saveDomainObject(domObj, flushDb = false) throws CuantoException {
		if (!domObj.save(flush: flushDb)) {
			reportSaveError domObj
		}
	}


	TestOutcome getTestOutcome(outcomeId) {
		if (outcomeId) {
			def out = Long.valueOf(outcomeId)
			return TestOutcome.get(out)
		} else {
			return null
		}
	}


	List<TestOutcome> getTestOutcomes(outcomeIds) {
		TestOutcome.findAll("from cuanto.TestOutcome out where out.id in (:outList)", [outList: outcomeIds])
	}


	TestResult result(String nameStartsWith) {
		TestResult.findByNameIlike(nameStartsWith + "%")
	}


	AnalysisState getAnalysisStateByName(String name) {
		AnalysisState.findByNameIlike(name)
	}


	AnalysisState getAnalysisState(id) {
		def analysisId = Long.valueOf(id)
		AnalysisState.get(analysisId)
	}


	AnalysisState getDefaultAnalysisState() {
		AnalysisState.findByIsDefault(true)
	}


	AnalysisState getAnalysisStateForBug() {
		AnalysisState.findByIsBug(true)
	}


	Bug findBugByUrl(url) {
		Bug.findByUrl(url)
	}


	Bug findBugByTitle(title) {
		Bug.findByTitle(title)
	}


	def countFailuresForTestRun(TestRun run) {
		def total = TestOutcome.executeQuery("select count(*) from cuanto.TestOutcome t where t.testRun = ? and t.testResult.isFailure = true  ", [run])
		return total[0]
	}

	def countUnanalyzedFailuresForTestRun(TestRun run) {
		def total = TestOutcome.executeQuery("select count(*) from cuanto.TestOutcome t where t.testRun = ? and " +
			"t.analysisState is not null and t.analysisState.isAnalyzed = false and t.testResult.isFailure = true  ", [run])
		return total[0]
	}


	List<TestOutcome> getTestOutcomeHistory(TestCase testCase, int startIndex, int maxOutcomes, String sort, String order) {
		def qry = "from cuanto.TestOutcome t where t.testCase = ?"
		if (sort) {
			qry += "order by t.${sort} "
			if (order) {
				qry += " ${order} "
			}
		}

		def results = TestOutcome.executeQuery(qry, [testCase], [max: maxOutcomes, offset: startIndex])
		return results
	}


	List<TestOutcome> getTestOutcomeFailureHistory(TestCase testCase, int startIndex, int maxOutcomes, String sort, String order) {
		def qry = """from cuanto.TestOutcome t where t.testCase = ? and
t.testResult.isFailure = true and t.testResult.includeInCalculations = true """
		if (sort) {
			qry += "order by t.${sort} "
			if (order) {
				qry += " ${order} "
			}
		}
		def results = TestOutcome.executeQuery(qry, [testCase], [max: maxOutcomes, offset: startIndex])
		return results
	}


	List<TestOutcome> getTestOutcomeAnalyses(testCase) {
		def qry = """from cuanto.TestOutcome t where t.testCase = ? and
			t.testResult.isFailure = true and t.analysisState.isAnalyzed = true order by t.testRun.dateExecuted desc"""
		def results = TestOutcome.executeQuery(qry, [testCase])
		return results
	}


	Integer countTestCaseOutcomes(TestCase testCase) {
		TestOutcome.countByTestCase(testCase)
	}


	Integer countTestCases(project) {
		TestCase.countByProject(project)
	}


	Integer countTestCaseFailures(TestCase testCase) {
		def results = TestOutcome.executeQuery("""select count(*) from cuanto.TestOutcome t where t.testCase = ? and
			t.testResult.isFailure = true and t.testResult.includeInCalculations = true""",
			[testCase])
		return results[0]
	}


	Map getAnalysisStateStats(TestRun testRun) {
		def total = TestOutcome.executeQuery("""select t.analysisState.name, count(t.analysisState) from
			cuanto.TestOutcome t where t.testRun = ? group by t.analysisState.name""", [testRun])
		def stats = [:]
		for (item in total) {
			stats[item[0].toLowerCase()] = item[1].toString()
		}
		return stats
	}


	List getAnalysisStatistics(TestRun testRun) {       
		def anStates = [:]
		AnalysisState.list().each { anStates[it.id] = it }
		def total = TestOutcome.executeQuery("""select t.analysisState.id, count(t.analysisState) from
			cuanto.TestOutcome t where t.testRun = ? and t.analysisState is not null group by t.analysisState""",
			[testRun])
		def stats = []
		for (rawData in total) {
			stats << new AnalysisStatistic(state: anStates[rawData[0]], qty: rawData[1])
		}
		return stats
	}

	
	def clearAnalysisStatistics(TestRun testRun) {
		def statsToDelete = testRun?.testRunStatistics?.analysisStatistics?.collect {it}
		statsToDelete?.each { stat ->
			testRun.testRunStatistics.removeFromAnalysisStatistics(stat)
			stat.delete()
		}
		testRun?.save()
	}


	List<TestCase> findTestCaseByName(String name, Project project) {
		TestCase.findAllByFullNameIlikeAndProject("%${name}%", project)
	}


	TestType createTestType(testTypeName) {
		def testType = getTestType(testTypeName)
		if (!testType) {
			testType = new TestType(name: testTypeName)
			saveDomainObject testType
		}
		return testType
	}


	def getAllProjects() {
		// have to do this in two queries because hibernate does include results without a projectGroup if
		// ordering by proj.projectGroup.name
		def allWithGroup = Project.findAll("from cuanto.Project as proj where proj.projectGroup is not null order by proj.projectGroup.name asc, proj.name asc")
		def allWithoutGroup = Project.findAll("from cuanto.Project as proj where proj.projectGroup is null order by proj.name asc")
		return [allWithGroup, allWithoutGroup].flatten()
	}


	def getProjectsByGroupName(groupName) {
		Project.findAll("from cuanto.Project as proj WHERE proj.projectGroup.name = ? order by proj.name asc",
			[groupName], [sort: 'name'])
	}


	def getProjectsByGroup(group) {
		Project.findAllByProjectGroup(group, [sort: 'name'])
	}


	def getAllGroups() {
		ProjectGroup.listOrderByName()
	}


	def findProjectGroupByName(name) {
		if (name) {
			return ProjectGroup.findByNameIlike(name)
		} else {
			return null
		}
	}


	def getProjectGroupsWithPrefix(prefix) {
		return ProjectGroup.findAllByNameIlike("${prefix}%", [sort: 'name'])
	}


	def getTestCases(project, offset, max) {
		def testCases = TestCase.executeQuery(
			"from cuanto.TestCase as tc where tc.project = ? order by tc.packageName asc, tc.testName asc", [project],
			['max': max, 'offset': offset])
		return testCases
	}


	def getTestCases(project) {
		TestCase.findAllByProject(project)
	}


	def deleteEmptyTestRuns() {
		def now = new Date().getTime()
		def TEN_MINUTES = 1000 * 60 * 10
		def cutoffDate = new Date(now - TEN_MINUTES)

		def runs = TestRun.executeQuery("from cuanto.TestRun as tr where tr.testRunStatistics is null and " +
			"tr.dateExecuted < ?", [cutoffDate])

		if (runs.size() > 0) {
			log.info "found ${runs.size()} empty test runs to delete"
			TestRun.executeUpdate("delete cuanto.TestRun as tr where tr.testRunStatistics is null and " +
				"tr.dateExecuted < ?", [cutoffDate])
		}
	}


	def deleteTestCase(testCase) {
		TestOutcome.executeUpdate("delete cuanto.TestOutcome out where out.testCase = ?", [testCase])
		testCase.project.removeFromTestCases(testCase).save()
		testCase.delete(flush: true)
	}


	def deleteTestCasesForProject(project) {
		def testCases = TestCase.findAllByProject(project)
		testCases.each {testCase ->
			project.removeFromTestCases(testCase)
		}
		TestCase.executeUpdate("delete cuanto.TestCase tc where tc.project = ?", [project])
	}


	def getBugSummary(testRun) {
		def bugStats = TestOutcome.executeQuery("""select distinct outc.bug, (select count(outb) from
				cuanto.TestOutcome outb	where outb.testRun = ? and outb.bug = outc.bug) from cuanto.TestOutcome
                outc where outc.testRun = ? and outc.bug is not null""", [testRun, testRun])

		Collections.sort bugStats as List, new BugQuantityComparator()
		return bugStats
	}


	def countTestOutcomesBySearch(search, searchTerm, testRun, params) {
		def queries = ""
		def qField = "out." + getFieldByFriendlyName(search)

		def queryList = []
		searchTerm.tokenize().each {term ->
			queries += " and upper($qField) like ? "
			queryList << "%${term.toUpperCase()}%".toString()
		}

		def filter = params.filter?.toLowerCase()

		if (filter == "allfailures" || filter == "newfailures") {
			queries += " and out.testResult.isFailure = true "
		}

		def qArgs = [testRun, queryList].flatten()

		def count = 0
		def outCount = TestOutcome.executeQuery(
			"select count(*)from cuanto.TestOutcome as out where out.testRun = ? ${queries.toString()}", qArgs)


		if (filter == "newfailures") {
			def outcomes = searchTestOutcomes(search, searchTerm, testRun, params)
			def newFailures = getNewFailures(outcomes, testRun.dateExecuted)
			count = newFailures.size()
		} else {
			count = outCount[0]
		}
		return count
	}


	def searchTestOutcomes(search, searchTerm, testRun, params) {
		// valid params are sort, order, offset, max plus an optional filter
		def qOrder = getSortOrder(params.order)
		def qSort = getFieldByFriendlyName(params.sort)
		def queries = ""
		def qField = "out." + getFieldByFriendlyName(search)

		def queryList = []
		searchTerm.tokenize().each {term ->
			queries += " and upper($qField) like ? "
			queryList << "%${term.toUpperCase()}%"
		}

		def filter = params?.filter?.toLowerCase()

		if (filter == "allfailures" || filter == "newfailures") {
			queries += " and out.testResult.isFailure = true "
		}

		def qArgs = [testRun, queryList].flatten()
		def outcomesToReturn
		def outs = TestOutcome.findAll(
			"from cuanto.TestOutcome as out where out.testRun = ? $queries order by out.${qSort} ${qOrder}",
			qArgs, ['max': Integer.valueOf(params.max), 'offset': Integer.valueOf(params.offset)])
		if (filter == "newfailures") {
			outcomesToReturn = getNewFailures(outs, testRun.dateExecuted)
		} else {
			outcomesToReturn = outs
		}
		return outcomesToReturn
	}


	private def getSortOrder(order) {
		def qOrder = "asc"
		if (order.toLowerCase() == "desc") {
			qOrder = "desc"
		}
		return qOrder
	}


	def getFieldByFriendlyName(friendlyName) {
		def qSort
		def name = friendlyName?.toLowerCase()
		if (name == "name" || name == "testcase") {
			qSort = "testCase.fullName"
		} else if (name == "result") {
			qSort = "testResult.name"  
		} else if (name == "state") {
			qSort = "analysisState.name"
		} else if (name == "duration") {
			qSort = "duration"
		} else if (name == "bug") {
			qSort = "bug.title"
		} else if (name == "owner") {
			qSort = "owner"
		} else if (name == "note") {
			qSort = "note"
		} else if (name == "output") {
			qSort = "testOutput"
		} else {
			qSort = "testCase.fullName"
		}
		return qSort
	}


	def createProjectGroup(groupName) {
		def grp = new ProjectGroup(name: groupName)
		saveDomainObject(grp)
		return grp
	}


	def getMostRecentTestRunForProjectKey(projectKey) {
		def recent = TestRun.executeQuery("from cuanto.TestRun tr where tr.project.projectKey like ? order by tr.dateExecuted desc",
			[projectKey], [max: 1])
		if (recent) {
			return recent[0]
		} else {
			return null
		}
	}


	def findOutcomeForTestCase(testCase, testRun) {
		TestOutcome.findWhere('testCase': testCase, 'testRun': testRun)
	}
}


