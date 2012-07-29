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
	QueryBuilder queryBuilder

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


	List getRawTestRunStats(TestRun run) { /* return the count, total test time and average test time for the test run*/
		def queryResults = TestRun.executeQuery("select count(*), sum(duration), avg(duration) from cuanto.TestOutcome t where t.testRun = ? and t.testResult.includeInCalculations = true",
			[run])
		return queryResults[0]
	}


	def deleteOutcomesForTestRun(TestRun run) {
		TestOutcome.executeUpdate("delete cuanto.TestOutcome t where t.testRun = ?", [run])
	}

	
	def getProject(id) {
		try {
			return Project.find("from Project p where p.id = ? and p.deleted = false", [Long.valueOf(id)])
		} catch (NumberFormatException e) {
			return null
		}
	}


	def getProject(groupName, projectName) throws CuantoException {
		def queryString
		def queryArgs = []

		if (groupName) {
			queryString = "from cuanto.Project p where p.projectGroup.name = ? and p.name = ? and p.deleted = false"
			queryArgs << groupName
		} else {
			queryString = "from cuanto.Project p where p.projectGroup is null and p.name = ? and p.deleted = false"
		}

		queryArgs << projectName
		return Project.find(queryString, queryArgs)
	}


	def getTestRunsByProject(proj, final Map queryParams) {
		if (queryParams.sort?.startsWith("prop|")) {
			def propName = queryParams.sort.substring(queryParams.sort.indexOf("|") + 1)
			def result = TestRun.executeQuery(
				"from cuanto.TestRun t left outer join t.testProperties as prop with prop.name = ? where t.project = ? order by prop.value ${queryParams.order}",
				[propName, proj], [max: Integer.valueOf(queryParams.max), offset: Integer.valueOf(queryParams.offset)])

			def realResult = result.collect { it[0] }
			return realResult
		} else {
			def paginateParams = [:]
			if (queryParams.max != null) {
				paginateParams['max'] = Integer.valueOf(queryParams.max)
			}
			if (queryParams.offset != null) {
				paginateParams['offset'] = Integer.valueOf(queryParams.offset)
			}

            def q = null
            def joinTrs = queryParams.sort?.startsWith('trs')
            if (joinTrs)
                q = "from cuanto.TestRun t, cuanto.TestRunStats trs where trs.testRun = t and t.project = ? order by ${queryParams.sort} ${queryParams.order}"
            else
                q = "from cuanto.TestRun t where t.project = ? order by ${queryParams.sort} ${queryParams.order}"

			// use the paginate params if applicable; otherwise, don't paginate
			if (paginateParams.size() > 0){
				def results = TestRun.executeQuery(q, [proj], paginateParams)
				return joinTrs ? results.collect{it[0]} : results
			} else {
				def results = TestRun.executeQuery(q, [proj])
				return joinTrs ? results.collect{it[0]} : results
			}
		}
	}


	def getTestRunsByProject(proj) {
		TestRun.findAllByProject(proj, [sort: "dateExecuted", order: "desc"])
	}


	def countTestRunsByProject(proj) {
		TestRun.countByProject(proj)
	}

	def getNextTestRun(testRun) {
		TestRun.createCriteria().get {
			and {
				eq("project.id", testRun.project.id)
				gt("dateExecuted", testRun.dateExecuted)
			}
			order("dateExecuted", "asc")
			maxResults(1)
		}
	}

	def getTestOutcomesForTestRun(Long testRunId, Long limit, Long offset) {
		if (!testRunId)
			return []
		TestOutcome.findAll("from cuanto.TestOutcome where testRun.id = ?", [testRunId], [max: limit, offset: offset])
	}




	def findMatchingTestCaseForProject(Project project, TestCase testcase) {
		if (testcase) {
			if (!testcase.fullName) {
				testcase.fullName = testcase.packageName + "." + testcase.testName
			}
			def query = "from cuanto.TestCase as tc where tc.project=? and tc.fullName=? "
			if (testcase.parameters == null) {
				query += "and tc.parameters is null"
				return TestCase.find(query, [project, testcase.fullName])
			} else {
				query += "and tc.parameters=?"
				return TestCase.find(query,	[project, testcase.fullName, testcase.parameters])
			}
		} else {
			throw new CuantoException("No test case specified")
		}
	}


	def addTestCases(Project project, List testCases) {
		for (testCase in testCases) {
			testCase.project = project
			saveDomainObject testCase
		}
	}


	def saveTestOutcomes(List outcomes) {
		for (TestOutcome outcome in outcomes) {
			updateAnalysisCountForTestCase(outcome.testCase)
			saveDomainObject outcome
		}
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
		TestOutcomeQueryFilter filter = getTestOutcomeQueryFilterWithOptions(sort, order, paging)
		filter.testRun = run
		return getTestOutcomes(filter)
	}


	String processSort(String sort) {
		if (!sort) {
			sort = "testCase.fullName"
		} else {
			sort = sort.replaceAll("^t\\.", "") //todo: remove once sorts with "t." are out of the code -- see TestRunService
		}
		return sort
	}


	String processOrder(String order) {
		if (!order) {
			order = "asc"
		}
		if (order.toLowerCase() != "asc" && order.toLowerCase() != "desc") {
			throw new IllegalArgumentException("${order} is not a valid sort order")
		}
		return order
	}

	// paging is an optional Map containing values for "max" or "offset" -- pass null or an empty map to ignore
	// sort is the field name to sort by, order is asc or desc
	List getTestOutcomeFailuresByTestRun(TestRun run, String sort, String order, Map paging) {
		TestOutcomeQueryFilter filter = getTestOutcomeQueryFilterWithOptions(sort, order, paging)
		filter.testRun = run
		filter.isFailure = true
		return getTestOutcomes(filter)
	}

	TestOutcomeQueryFilter getTestOutcomeQueryFilterWithOptions(String sort, String order, Map paging) {
		TestOutcomeQueryFilter filter = new TestOutcomeQueryFilter()
		filter.sorts = [new SortParameters('sort': processSort(sort), sortOrder: processOrder(order))]
		if (paging && paging.max) {
			filter.queryMax = paging.max
		}
		if (paging && paging.offset) {
			filter.queryOffset = paging.offset
		}
		return filter
	}

	// paging is an optional Map containing values for "max" or "offset" -- pass null or an empty map to ignore
	// sort is the field name to sort by, order is asc or desc
	List getTestOutcomeUnanalyzedFailuresByTestRun(TestRun run, String sort, String order, Map paging) {
		TestOutcomeQueryFilter filter = getTestOutcomeQueryFilterWithOptions(sort, order, paging)
		filter.testRun = run
		filter.isFailure = true
		filter.isAnalyzed = false
		return getTestOutcomes(filter)
	}


	TestOutcome getPreviousOutcome(TestOutcome outcome) {

		return getAdjacentOutcome(outcome, true)
	}

	TestOutcome getNextOutcome(TestOutcome outcome) {

		return getAdjacentOutcome(outcome, false)
	}

	private TestOutcome getAdjacentOutcome(TestOutcome outcome, boolean previous) {
		TestOutcomeQueryFilter filter = new TestOutcomeQueryFilter()
		filter.testCase = outcome.testCase
		def operator = previous ? '<' : '>'
		def sortOrder = previous ? 'desc' : 'asc'

		if (outcome.finishedAt) {
			filter.dateCriteria = [new DateCriteria(field: 'finishedAt', date: outcome.finishedAt, operator: operator)]
			filter.sorts = [new SortParameters(sort: 'finishedAt', sortOrder: sortOrder)]
		} else if (outcome.startedAt) {
			filter.dateCriteria = [new DateCriteria(field: 'startedAt', date: outcome.startedAt, operator: operator)]
			filter.sorts = [new SortParameters(sort: 'startedAt', sortOrder: sortOrder)]
		} else if (outcome.testRun?.dateExecuted) {
			filter.dateCriteria = [new DateCriteria(field: 'testRun', date: outcome.testRun?.dateExecuted, operator: operator)]
			filter.sorts = [new SortParameters(sort: 'testRun.dateExecuted', sortOrder: sortOrder)]
		} else {
			filter.dateCriteria = [new DateCriteria(field: 'dateCreated', date: outcome.dateCreated, operator: operator)]
			filter.sorts = [new SortParameters(sort: 'dateCreated', sortOrder: sortOrder)]
		}

		filter.queryOffset = 0
		filter.queryMax = 1
		List outcomes = getTestOutcomes(filter)
		TestOutcome outcome1 = outcomes[0] as TestOutcome

		return outcome1
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


	List<TestOutcome> getTestOutcomes(TestCase testCase, TestRun testRun) {
		TestOutcomeQueryFilter queryFilter = new TestOutcomeQueryFilter()
		queryFilter.testCase = testCase
		queryFilter.testRun = testRun
		queryFilter.sorts = [new SortParameters(sort:"finishedAt", sortOrder: "desc"),
			new SortParameters(sort:"dateCreated", sortOrder: "desc")]
		getTestOutcomes(queryFilter)
	}


	List<TestOutcome> getTestOutcomes(List outcomeIds) {
		TestOutcome.findAll("from cuanto.TestOutcome out where out.id in (:outList)", [outList: outcomeIds])
	}


	List<TestOutcome> getTestOutcomes(TestOutcomeQueryFilter queryFilter) throws CuantoException {
		CuantoQuery cuantoQuery = queryBuilder.buildQuery(queryFilter)
        def results
		if (cuantoQuery.paginateParameters) {
			results = TestOutcome.executeQuery(cuantoQuery.hql, (Collection) cuantoQuery.positionalParameters, cuantoQuery.paginateParameters)
		} else {
			results = TestOutcome.executeQuery(cuantoQuery.hql, (Collection) cuantoQuery.positionalParameters)
		}
        def transformed = queryFilter.resultTransform(results)
        return transformed
	}


	Long countTestOutcomes(TestOutcomeQueryFilter queryFilter) {
		CuantoQuery cuantoQuery = queryBuilder.buildCount(queryFilter)
		def results
		if (cuantoQuery.paginateParameters) {
			results = TestOutcome.executeQuery(cuantoQuery.hql, cuantoQuery.positionalParameters, cuantoQuery.paginateParameters)
		} else if (cuantoQuery.positionalParameters) {
			results = TestOutcome.executeQuery(cuantoQuery.hql, cuantoQuery.positionalParameters)
		} else {
			results = TestOutcome.executeQuery(cuantoQuery.hql)
		}
		return results[0]
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


	List<TestOutcome> getTestOutcomeHistory(TestCase testCase, int startIndex, int maxOutcomes, String sort, String order) {
		TestOutcomeQueryFilter filter = getTestOutcomeQueryFilterWithOptions(sort, order, [max: maxOutcomes, offset: startIndex])
		filter.testCase = testCase
		return getTestOutcomes(filter)
	}


	List<TestOutcome> getTestOutcomeFailureHistory(TestCase testCase, int startIndex, int maxOutcomes, String sort, String order) {
		TestOutcomeQueryFilter filter = getTestOutcomeQueryFilterWithOptions(sort, order, [max: maxOutcomes, offset: startIndex])
		filter.testCase = testCase
		filter.isFailure = true
		return getTestOutcomes(filter)
	}


	List<TestOutcome> getTestOutcomeAnalyses(TestCase testCase) {
		TestOutcomeQueryFilter filter = new TestOutcomeQueryFilter()
		filter.testCase = testCase
		filter.isFailure = true
		filter.isAnalyzed = true

		filter.sorts = []
		filter.sorts << new SortParameters('sort': "finishedAt", sortOrder: "desc")
		filter.sorts << new SortParameters('sort': "testRun.dateExecuted", sortOrder: "desc")
		filter.sorts << new SortParameters('sort': "dateCreated", sortOrder: "desc")
		return getTestOutcomes(filter)
	}


	Integer countTestCases(project) {
		TestCase.countByProject(project)
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
			cuanto.TestOutcome t where t.testRun = ? and t.analysisState is not null and t.testResult.isFailure = true group by t.analysisState""",
			[testRun])
		def stats = []
		for (rawData in total) {
			stats << new AnalysisStatistic(state: anStates[rawData[0]], qty: rawData[1])
		}
		return stats
	}

	


	List<TestCase> findTestCaseByName(String name, Project project) {
		TestCase.findAllByFullNameIlikeAndProject("%${name}%", project)
	}

	List<TestCase> findExactTestCaseByName(String name, Project project) {
		TestCase.findAllByFullNameAndProject("${name}", project)
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
		def allWithGroup = Project.findAll("from cuanto.Project as proj where proj.projectGroup is not null and proj.deleted = false order by proj.projectGroup.name asc, proj.name asc")
		def allWithoutGroup = Project.findAll("from cuanto.Project as proj where proj.projectGroup is null and proj.deleted = false order by proj.name asc")
		return [allWithGroup, allWithoutGroup].flatten()
	}


	def getProjectsByGroupName(groupName) {
		def gname = groupName?.replaceAll("%20", " ")?.replaceAll(/\+/, " ")
		Project.findAll("from cuanto.Project as proj WHERE proj.projectGroup.name = ? and proj.deleted = false order by proj.name asc",
			[gname], [sort: 'name'])
	}


	def getProjectsByGroup(group) {
		Project.findAllByProjectGroupAndDeleted(group, false, [sort: 'name'])
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
			"from cuanto.TestCase as tc where tc.project = ? order by tc.packageName asc, tc.testName asc, tc.parameters asc", [project],
			['max': max, 'offset': offset])
		return testCases
	}


	def getTestCases(project) {
		TestCase.findAllByProject(project)
	}

	def deleteTestCase(testCase) {
		TestOutcome.executeUpdate("delete cuanto.TestOutcome out where out.testCase = ?", [testCase])
		testCase.delete(flush: true)
	}


	def deleteTestCasesForProject(project) {
		def cases = getTestCases(project)
		cases.each { TestCase tc ->
			TestOutcome.executeUpdate("delete cuanto.TestOutcome tout where tout.testCase = ?", [tc])
		}

	    TestCase.withSession {
		    it.flush()
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


	TestOutcomeQueryFilter getTestOutcomeQueryFilterForSearch(testRun, params, searchTerm, searchField) {
		TestOutcomeQueryFilter outFilter = new TestOutcomeQueryFilter()

		def qOrder = getSortOrder(params.order)
		def qSort = getFieldByFriendlyName(params.sort) //todo: use TestOutcomeQueryFilter.getSortNameForFriendlyName()?
		outFilter.sorts = [new SortParameters(sort: qSort, sortOrder: qOrder)]

		outFilter.testRun = testRun
		outFilter.setForSearchTerm(searchField, searchTerm)
		return outFilter
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
			qSort = "testResult"
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


    def findLastOutcomeForTestCase(testCase) {
        List<TestOutcome> lastTestOutcomes = TestOutcome.findAll(
                "from cuanto.TestOutcome outcome where outcome.testCase = ? order by id desc", [testCase], [max: 1])
        return lastTestOutcomes ? lastTestOutcomes.get(0) : null
    }


	def countAnalysesForTestCase(testCase) {
		return TestOutcome.executeQuery("select count(*) from cuanto.TestOutcome t where t.testCase = ? and t.analysisState.isAnalyzed = true",
			[testCase])[0]
	}

	def updateAnalysisCountForTestCase(TestCase testCase) {
		testCase.lock()
		testCase.analysisCount = countAnalysesForTestCase(testCase) as Integer
		saveDomainObject(testCase)
	}
}


