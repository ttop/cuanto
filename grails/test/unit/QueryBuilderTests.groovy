import cuanto.TestOutcomeQueryFilter
import cuanto.TestRun
import cuanto.CuantoQuery
import cuanto.QueryBuilder
import cuanto.TestOutcomeQueryFilter

/**
 * User: Todd Wells
 * Date: Mar 10, 2010
 * Time: 6:34:59 PM
 * 
 */

public class QueryBuilderTests extends GroovyTestCase {


	void assertEqualsIgnoringWhitespace(String message, String expected, String actual) {
		assertEquals message, expected.replaceAll(/\s+/, " ").trim(), actual.replaceAll(/\s+/, " ").trim();
	}

	void testTestRun() {
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
		qf.sorts = []
		qf.sorts << [sort: "testCase.fullName", order: "asc"]

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "from cuanto.TestOutcome t where t.testRun = ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.testRun]
		expectedQuery.paginateParameters = [:]

		CuantoQuery actualQuery = new QueryBuilder().buildQueryForTestOutcomeFilter(qf)
		assertEquals "TestRun", expectedQuery, actualQuery
	}

	void testPaginateTestRun() {
		def qf = new TestOutcomeQueryFilter()
		qf.testRun = new TestRun(note: "foo")
		qf.sorts = []
		qf.sorts << [sort: "testCase.fullName", order: "asc"]
		qf.queryMax = 10
		qf.queryOffset = 5

		CuantoQuery expectedQuery = new CuantoQuery()
		expectedQuery.hql = "from cuanto.TestOutcome t where t.testRun = ? order by t.testCase.fullName asc"
		expectedQuery.positionalParameters = [qf.testRun]
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



}