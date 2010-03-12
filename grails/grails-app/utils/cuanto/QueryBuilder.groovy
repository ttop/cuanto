/*

Copyright (c) 2010 Todd Wells

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

public class QueryBuilder {

	List processors = [this.&getTestRunClause, this.&getIsFailureClause, this.&getTestResultClause,
		this.&getTestCaseFullNameClause, this.&getTestCaseParametersClause, this.&getTestCasePackageClause]

	CuantoQuery buildQueryForTestOutcomeFilter(TestOutcomeQueryFilter queryFilter) {
		String query = "from cuanto.TestOutcome t where "
		List params = []
		List queryClauses = []

		processors.each { queryProcessor ->
			def details = queryProcessor(queryFilter)
			if (details.clause?.trim()) {
				queryClauses << details.clause
				params += details.params
			}
		}

		queryClauses.eachWithIndex { clause, idx ->
			query += " ${clause}"
			if (idx < queryClauses.size() - 1) {
				query += "and "
			} else {
				query += " "
			}
		}

		// add sort & sortOrder if specified
		if (queryFilter.sorts) {
			query += " order by "
			queryFilter.sorts.eachWithIndex { it, idx ->
				def order
				if (it.sortOrder) {
					order = it.sortOrder
				} else {
					order = "asc"
				}
				query += "t.${it.sort} ${order}"
				if (idx < queryFilter.sorts.size() - 1) {
					query += ", "
				}
			}
		}

		def pagination = [:]
		if (queryFilter.queryMax) {
			pagination.max = queryFilter.queryMax
		}
		if (queryFilter.queryOffset) {
			pagination.offset = queryFilter.queryOffset
		}

		def cuantoQuery = new CuantoQuery(hql: query, positionalParameters: params.flatten() as List,
			paginateParameters:pagination )
		return cuantoQuery
	}

	Map getTestRunClause(TestOutcomeQueryFilter queryFilter) {
		if (queryFilter.testRun) {
			return [clause: " t.testRun = ? ", params: [queryFilter.testRun]]
		} else {
			return [:]
		}
	}
	
	Map getIsFailureClause(TestOutcomeQueryFilter queryFilter) {
		if (queryFilter.isFailure != null) {
			return [clause: " t.testResult.isFailure = ? ", params: [queryFilter.isFailure]]
		} else {
			return [:]
		}
	}

	Map getTestResultClause(TestOutcomeQueryFilter queryFilter) {
		if (queryFilter.testResult != null) {
			return [clause: " t.testResult = ? ", params: [queryFilter.testResult]]
		} else {
			return [:]
		}
	}

	Map getTestCaseFullNameClause(TestOutcomeQueryFilter queryFilter) {
		if (queryFilter.testCaseFullName != null) {
			return [clause: " upper(t.testCase.fullName) like ? ", params: "%${queryFilter.testCaseFullName}%"]
		} else {
			return [:]
		}
	}

	Map getTestCaseParametersClause(TestOutcomeQueryFilter queryFilter) {
		def map = [:]
		if (queryFilter.testCaseParameters != null) {
			map = [clause: " upper(t.testCase.parameters) like ? ",
				params: queryFilter.testCaseParameters.toUpperCase().replaceAll("\\*", "%")]
		}
		return map
	}

	Map getTestCasePackageClause(TestOutcomeQueryFilter queryFilter) {
		def map = [:]
		if (queryFilter.testCasePackage != null) {
			map = [clause: " t.testCase.package like ? ",
				params: queryFilter.testCasePackage.replaceAll("\\*", "%")]
		}
		return map
	}

}