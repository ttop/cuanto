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
		queryBuilder.queryModules = [new TestRunQueryModule(), new TestResultIsFailureQueryModule(),
		new TestResultQueryModule(), new TestCaseFullNameQueryModule(), new TestCaseParametersQueryModule(),
		new TestCasePackageQueryModule(), new ProjectQueryModule(), new TestResultIncludedInCalculationsQueryModule(),
		new IsAnalyzedQueryModule()]
	}

	void testTestRun() {
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "from cuanto.TestOutcome t where t.testRun = ? order by t.testCase.fullName asc"
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
		expectedQuery.hql = "from cuanto.TestOutcome t where t.testRun = ? order by t.testCase.fullName desc, t.testCase.parameters desc"
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
		expectedQuery.hql = "from cuanto.TestOutcome t where t.testRun = ? and t.testResult.isFailure = ? order by t.testCase.fullName desc"
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
		expectedQuery.hql = "from cuanto.TestOutcome t where t.testRun = ? and t.testResult.isFailure = ? order by t.testCase.fullName desc"
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
		expectedQuery.hql = "from cuanto.TestOutcome t where t.testRun = ? and t.testResult = ? order by t.testCase.fullName asc"
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
		expectedQuery.hql = "from cuanto.TestOutcome t where t.testRun = ? and upper(t.testCase.fullName) like ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.testRun, "%${qf.testCaseFullName}%"]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "TestResult", expectedQuery, actualQuery
	}

	void testTestCaseParametersAndTestRun(){
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
	    qf.testCaseParameters = "foo, bar"

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "from cuanto.TestOutcome t where t.testRun = ? and upper(t.testCase.parameters) like ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.testRun, qf.testCaseParameters.toUpperCase()]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "TestResult", expectedQuery, actualQuery
	}

	void testTestCaseParametersWildcardAndTestRun(){
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
	    qf.testCaseParameters = "foo, *, bar"

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "from cuanto.TestOutcome t where t.testRun = ? and upper(t.testCase.parameters) like ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.testRun, "FOO, %, BAR"]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "TestResult", expectedQuery, actualQuery
	}

	void testTestCasePackageAndTestRun(){
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
	    qf.testCasePackage = "my.pack.age"

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "from cuanto.TestOutcome t where t.testRun = ? and t.testCase.package like ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.testRun, qf.testCasePackage]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "TestResult", expectedQuery, actualQuery
	}


	void testTestCasePackageWildcardAndTestRun(){
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
	    qf.testCasePackage = "my.pack.age.*"

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "from cuanto.TestOutcome t where t.testRun = ? and t.testCase.package like ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.testRun, qf.testCasePackage.replaceAll("\\*", "%")]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "TestResult", expectedQuery, actualQuery
	}


	void testProject(){
		def qf = new TestOutcomeQueryFilter()
		qf.project = new Project("foobar")

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "from cuanto.TestOutcome t where t.testCase.project = ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.project]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "TestResult", expectedQuery, actualQuery
	}


	void testIncludeIgnoredAndTestRun(){
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
	    qf.testResultIncludedInCalculations = true

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "from cuanto.TestOutcome t where t.testRun = ? and t.testResult.includeInCalculations = ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.testRun, qf.testResultIncludedInCalculations]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "TestResult", expectedQuery, actualQuery
	}

	void testIsAnalyzedAndTestRun(){
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
	    qf.isAnalyzed = true

		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "from cuanto.TestOutcome t where t.testRun = ? and t.analysisState.isAnalyzed = ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.testRun, qf.isAnalyzed]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = queryBuilder.buildQuery(qf)
		assertEquals "TestResult", expectedQuery, actualQuery
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