import cuanto.TestOutcomeQueryFilter
import cuanto.TestRun
import cuanto.CuantoQuery
import cuanto.QueryBuilder
import cuanto.test.WordGenerator
import cuanto.SortParameters
import cuanto.TestResult
import cuanto.Project
import cuanto.queryprocessor.TestRunQueryModule
import cuanto.queryprocessor.TestResultIsFailureQueryModule
import cuanto.queryprocessor.TestResultQueryModule
import cuanto.queryprocessor.TestCaseFullNameQueryModule
import cuanto.queryprocessor.TestCaseParametersQueryModule
import cuanto.queryprocessor.TestCasePackageQueryModule
import cuanto.queryprocessor.ProjectQueryModule
import cuanto.queryprocessor.TestResultIncludedInCalculationsQueryModule
import cuanto.queryprocessor.IsAnalyzedQueryModule
import cuanto.queryprocessor.AnalysisStateQueryModule
import cuanto.AnalysisState
import cuanto.queryprocessor.BugQueryModule
import cuanto.Bug
import cuanto.queryprocessor.OwnerQueryModule
import cuanto.TestCase
import cuanto.queryprocessor.TestCaseQueryModule
import cuanto.queryprocessor.NoteQueryModule
import cuanto.queryprocessor.TestOutputQueryModule
import cuanto.queryprocessor.DateExecutedQueryModule
import cuanto.DateCriteria
import cuanto.queryprocessor.TagNameQueryModule
import cuanto.queryprocessor.HasTagsQueryModule
import cuanto.queryprocessor.FailureStatusChangedQueryModule

/**
 * User: Todd Wells
 * Date: Mar 10, 2010
 * Time: 6:34:59 PM
 * 
 */

public class QueryBuilderTests extends GroovyTestCase {

	WordGenerator wordGen = new WordGenerator()
	QueryBuilder queryBuilder

	void setUp() {
		queryBuilder = new QueryBuilder()
		queryBuilder.queryModules = [
			new TestRunQueryModule(), new TestResultIsFailureQueryModule(), new TestResultQueryModule(),
			new TestCaseFullNameQueryModule(), new TestCaseParametersQueryModule(), new TestCasePackageQueryModule(),
			new ProjectQueryModule(), new TestResultIncludedInCalculationsQueryModule(), new IsAnalyzedQueryModule(),
			new AnalysisStateQueryModule(), new BugQueryModule(), new OwnerQueryModule(),
			new TestCaseQueryModule(), new NoteQueryModule(), new TestOutputQueryModule(),
			new DateExecutedQueryModule(), new TagNameQueryModule(), new HasTagsQueryModule(),
			new FailureStatusChangedQueryModule()]
	}

	void testTestRun() {
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "select distinct t, t.testCase.fullName from cuanto.TestOutcome t where t.testRun = ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.testRun]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "TestRun", expectedQuery, actualQuery
	}

	void testPaginateTestRun() {
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: wordGen.getSentence(10))
		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "desc")
		qf.sorts << new SortParameters(sort: "testCase.parameters", sortOrder: "desc")
		qf.queryMax = 10
		qf.queryOffset = 5

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "select distinct t, t.testCase.fullName, t.testCase.parameters from cuanto.TestOutcome t where t.testRun = ? order by t.testCase.fullName desc, t.testCase.parameters desc"
		expectedQuery.positionalParameters = [qf.testRun]
		expectedQuery.paginateParameters = [max: qf.queryMax, offset: qf.queryOffset]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "TestRun", expectedQuery, actualQuery
	}

	void testTestResultIsFailureAndTestRun() {
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: wordGen.getSentence(10))
		qf.isFailure = true

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "desc")
		qf.queryMax = 10
		qf.queryOffset = 5

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "select distinct t, t.testCase.fullName from cuanto.TestOutcome t where t.testRun = ? and t.testResult.isFailure = ? order by t.testCase.fullName desc"
		expectedQuery.positionalParameters = [qf.testRun, true]
		expectedQuery.paginateParameters = [max: qf.queryMax, offset: qf.queryOffset]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "IsFailure", expectedQuery, actualQuery
	}

	void testTestResultIsNotFailureAndTestRun() {
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: wordGen.getSentence(10))
		qf.isFailure = false

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "desc")
		qf.queryMax = 10
		qf.queryOffset = 5

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "select distinct t, t.testCase.fullName from cuanto.TestOutcome t where t.testRun = ? and t.testResult.isFailure = ? order by t.testCase.fullName desc"
		expectedQuery.positionalParameters = [qf.testRun, false]
		expectedQuery.paginateParameters = [max: qf.queryMax, offset: qf.queryOffset]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "IsNotFailure", expectedQuery, actualQuery
	}

	void testTestResultAndTestRun(){
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
		qf.testResult = new TestResult(name: "Fail", isFailure: true, includeInCalculations: true)

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "select distinct t, t.testCase.fullName from cuanto.TestOutcome t where t.testRun = ? and t.testResult = ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.testRun, qf.testResult]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "TestResult", expectedQuery, actualQuery
	}

	void testTestCaseFullNameAndTestRun(){
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
		qf.testCaseFullName = "my.sample.testcase"

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "select distinct t, t.testCase.fullName from cuanto.TestOutcome t where t.testRun = ? and upper(t.testCase.fullName) like ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.testRun, "%${qf.testCaseFullName.toUpperCase()}%"]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "TestCaseFullName and TestRun", expectedQuery, actualQuery
	}

	void testTestCaseParametersAndTestRun(){
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
	    qf.testCaseParameters = "foo, bar"

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "select distinct t, t.testCase.fullName from cuanto.TestOutcome t where t.testRun = ? and upper(t.testCase.parameters) like ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.testRun, qf.testCaseParameters.toUpperCase()]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "TestCaseParameters and TestRun", expectedQuery, actualQuery
	}

	void testTestCaseParametersWildcardAndTestRun(){
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
	    qf.testCaseParameters = "foo, *, bar"

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "select distinct t, t.testCase.fullName from cuanto.TestOutcome t where t.testRun = ? and upper(t.testCase.parameters) like ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.testRun, "FOO, %, BAR"]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "TestCaseParametersWildcard and TestRun", expectedQuery, actualQuery
	}

	void testTestCasePackageAndTestRun(){
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
	    qf.testCasePackage = "my.pack.age"

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql =	"""select distinct t, t.testCase.fullName from cuanto.TestOutcome t where t.testRun = ? and (t.testCase.packageName like ? or
			t.testCase.packageName like ?) order by t.testCase.fullName asc"""
		expectedQuery.positionalParameters = [qf.testRun, qf.testCasePackage, qf.testCasePackage + "%"]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "TestCasePackage and TestRun", expectedQuery, actualQuery
	}


	void testTestCasePackageWildcardAndTestRun(){
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
	    qf.testCasePackage = "my.pack.age.*"

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = """select distinct t, t.testCase.fullName from cuanto.TestOutcome t where t.testRun = ? and
			(t.testCase.packageName like ? or t.testCase.packageName like ?) order by t.testCase.fullName asc"""
		String expectedPkg = qf.testCasePackage.replaceAll("\\*", "%")
		expectedQuery.positionalParameters = [qf.testRun, expectedPkg, "${expectedPkg}%"]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "TestCasePackageWildcard and TestRun", expectedQuery, actualQuery
	}


	void testProject(){
		def qf = new TestOutcomeQueryFilter()
		qf.project = new Project("foobar")

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "select distinct t, t.testCase.fullName from cuanto.TestOutcome t where t.testCase.project = ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.project]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "Project", expectedQuery, actualQuery
	}


	void testIncludeIgnoredAndTestRun(){
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
	    qf.testResultIncludedInCalculations = true

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "select distinct t, t.testCase.fullName from cuanto.TestOutcome t where t.testRun = ? and t.testResult.includeInCalculations = ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.testRun, qf.testResultIncludedInCalculations]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "includeIgnored and TestRun", expectedQuery, actualQuery
	}


	void testIsAnalyzedAndTestRun(){
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
	    qf.isAnalyzed = true

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "select distinct t, t.testCase.fullName from cuanto.TestOutcome t where t.testRun = ? and t.analysisState.isAnalyzed = ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.testRun, qf.isAnalyzed]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "IsAnalyzedAndTestRun", expectedQuery, actualQuery
	}


	void testAnalysisStateAndTestRun(){
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
	    qf.analysisState = new AnalysisState(name: "Fail")

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "select distinct t, t.testCase.fullName from cuanto.TestOutcome t where t.testRun = ? and t.analysisState = ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.testRun, qf.analysisState]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "AnalysisState and TestRun", expectedQuery, actualQuery
	}


	void testBugAndTestRun(){
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
	    qf.bug = new Bug(title: "bar", url: "http://foobar")

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "select distinct t, t.testCase.fullName from cuanto.TestOutcome t where t.testRun = ? and t.bug = ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.testRun, qf.bug]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "Bug and TestRun", expectedQuery, actualQuery
	}


	void testOwnerAndTestRun(){
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
	    qf.owner = "Bob"

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "select distinct t, t.testCase.fullName from cuanto.TestOutcome t where t.testRun = ? and upper(t.owner) like ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.testRun, "%${qf.owner.toUpperCase()}%"]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "Owner and TestRun", expectedQuery, actualQuery
	}


	void testTestCase(){
		def qf = new TestOutcomeQueryFilter()
		qf.testCase = new TestCase(fullName: wordGen.getSentence(3))

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testRun.dateExecuted", sortOrder: "desc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "select distinct t, t.testRun.dateExecuted from cuanto.TestOutcome t where t.testCase = ? order by t.testRun.dateExecuted desc"
		expectedQuery.positionalParameters = [qf.testCase]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "TestCase", expectedQuery, actualQuery
	}

	
	void testNoteQuery(){
		def qf = new TestOutcomeQueryFilter()
		qf.note = wordGen.getSentence(5)

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testRun.dateExecuted", sortOrder: "desc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "select distinct t, t.testRun.dateExecuted  from cuanto.TestOutcome t where upper(t.note) like ? order by t.testRun.dateExecuted desc"
		expectedQuery.positionalParameters = ["%${qf.note.toUpperCase()}%"]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "Note", expectedQuery, actualQuery
	}


	void testOutputQuery(){
		def qf = new TestOutcomeQueryFilter()
		qf.testOutput = wordGen.getSentence(5)

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testRun.dateExecuted", sortOrder: "desc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "select distinct t, t.testRun.dateExecuted from cuanto.TestOutcome t where upper(t.testOutput) like ? order by t.testRun.dateExecuted desc"
		expectedQuery.positionalParameters = ["%${qf.testOutput.toUpperCase()}%"]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "TestOutput", expectedQuery, actualQuery
	}


	void testDateRangeQuery(){
		def qf = new TestOutcomeQueryFilter()
		qf.dateCriteria =[new DateCriteria(date: new Date(), operator: ">")]

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testRun.dateExecuted", sortOrder: "desc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "select distinct t, t.testRun.dateExecuted from cuanto.TestOutcome t where t.testRun.dateExecuted > ? order by t.testRun.dateExecuted desc"
		expectedQuery.positionalParameters = [qf.dateCriteria[0].date]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "DateRange", expectedQuery, actualQuery
	}


    void testMultipleDateRangeQuery(){
		def qf = new TestOutcomeQueryFilter()
		qf.dateCriteria =[
			new DateCriteria(date: new Date(), operator: ">"),
			new DateCriteria(date: new Date() + 2, operator: "<")
		]

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testRun.dateExecuted", sortOrder: "desc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "select distinct t, t.testRun.dateExecuted from cuanto.TestOutcome t where t.testRun.dateExecuted > ? and t.testRun.dateExecuted < ? order by t.testRun.dateExecuted desc"
		expectedQuery.positionalParameters = [qf.dateCriteria[0].date, qf.dateCriteria[1].date]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "MultipleDateRange", expectedQuery, actualQuery
	}


    void testTagQuery() {
        def qf = new TestOutcomeQueryFilter()
        qf.testRun = new TestRun(note: "foo")
        
        qf.dateCriteria =[
            new DateCriteria(date: new Date(), operator: ">"),
            new DateCriteria(date: new Date() + 2, operator: "<")
        ]

        qf.tags = ["john", "paul"]
        qf.sorts = []
        qf.sorts << new SortParameters(sort: "testRun.dateExecuted", sortOrder: "desc")

        CuantoQuery expectedQuery = new CuantoQuery()
        expectedQuery.hql = "select distinct t, t.testRun.dateExecuted from cuanto.TestOutcome t inner join t.tags tag_0 where t.testRun = ? and  t.testRun.dateExecuted > ? and t.testRun.dateExecuted < ? and (upper(tag_0.name) like ? or upper(tag_0.name) like ?) order by t.testRun.dateExecuted desc"
        expectedQuery.positionalParameters = [qf.testRun, qf.dateCriteria[0].date, qf.dateCriteria[1].date, qf.tags[0].toUpperCase(), qf.tags[1].toUpperCase()]
        expectedQuery.paginateParameters = [:]

        CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
        assertEquals "Tag", expectedQuery, actualQuery
    }


    void testHasTagsTrueQuery() {
        def qf = new TestOutcomeQueryFilter()
        qf.testRun = new TestRun(note: "foo")
        qf.hasTags = true
        qf.sorts = []
        qf.sorts << new SortParameters(sort: "testRun.dateExecuted", sortOrder: "desc")

        CuantoQuery expectedQuery = new CuantoQuery()
        expectedQuery.hql = "select distinct t, t.testRun.dateExecuted from cuanto.TestOutcome t where t.testRun = ? and t.tags is not empty order by t.testRun.dateExecuted desc"
        expectedQuery.positionalParameters = [qf.testRun]
        expectedQuery.paginateParameters = [:]

        CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
        assertEquals "Tag", expectedQuery, actualQuery
    }
    

    void testHasTagsFalseQuery() {
        def qf = new TestOutcomeQueryFilter()
        qf.testRun = new TestRun(note: "foo")
        qf.hasTags = false
        qf.sorts = []
        qf.sorts << new SortParameters(sort: "testRun.dateExecuted", sortOrder: "desc")

        CuantoQuery expectedQuery = new CuantoQuery()
        expectedQuery.hql = "select distinct t, t.testRun.dateExecuted from cuanto.TestOutcome t where t.testRun = ? and t.tags is empty order by t.testRun.dateExecuted desc"
        expectedQuery.positionalParameters = [qf.testRun]
        expectedQuery.paginateParameters = [:]

        CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
        assertEquals "Tag", expectedQuery, actualQuery
    }


	void assertEquals(String message, CuantoQuery expected, CuantoQuery actual) {
		assertEqualsIgnoringWhitespace "${message}: Wrong hql", expected.hql,  actual.hql
		assertEquals "${message}: Wrong positional parameters",
			expected.positionalParameters, actual.positionalParameters
		assertEquals "${message}: Wrong positional parameters",
			expected.paginateParameters, actual.paginateParameters
	}

	void assertEqualsIgnoringWhitespace(String message, String expected, String actual) {
		assertEquals message, expected.replaceAll(/\s+/, " ").trim(), actual.replaceAll(/\s+/, " ").trim();
	}

}