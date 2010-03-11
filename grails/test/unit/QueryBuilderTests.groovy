import cuanto.TestOutcomeQueryFilter
import cuanto.TestRun
import cuanto.CuantoQuery
import cuanto.QueryBuilder
import cuanto.TestOutcomeQueryFilter
import cuanto.test.WordGenerator
import cuanto.TestResult
import cuanto.SortParameters

/**
 * User: Todd Wells
 * Date: Mar 10, 2010
 * Time: 6:34:59 PM
 * 
 */

public class QueryBuilderTests extends GroovyTestCase {

	WordGenerator wordGen = new WordGenerator()

	void testTestRun() {
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "asc")

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "from cuanto.TestOutcome t where t.testRun = ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.testRun]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = new QueryBuilder().buildQueryForTestOutcomeFilter(qf)
		assertEquals "TestRun", expectedQuery, actualQuery
	}

	void testPaginateTestRun() {
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: wordGen.getSentence(10))
		qf.sorts = []
		qf.sorts << new SortParameters(sort: "testCase.fullName", sortOrder: "desc")
		qf.queryMax = 10
		qf.queryOffset = 5

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "from cuanto.TestOutcome t where t.testRun = ? order by t.testCase.fullName desc"
		expectedQuery.positionalParameters = [qf.testRun]
		expectedQuery.paginateParameters = [max: qf.queryMax, offset: qf.queryOffset]

		CuantoQuery actualQuery = new QueryBuilder().buildQueryForTestOutcomeFilter(qf)
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

		CuantoQuery actualQuery = new QueryBuilder().buildQueryForTestOutcomeFilter(qf)
		assertEquals "TestRun", expectedQuery, actualQuery
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

		CuantoQuery actualQuery = new QueryBuilder().buildQueryForTestOutcomeFilter(qf)
		assertEquals "TestRun", expectedQuery, actualQuery
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