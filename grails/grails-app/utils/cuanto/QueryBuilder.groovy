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

	List processors = [this.&getTestRunClause]

	CuantoQuery buildQueryForTestOutcomeFilter(TestOutcomeQueryFilter queryFilter) {
		String query = "from cuanto.TestOutcome t "
		List params = []

		processors.each { queryProcessor ->
			def details = queryProcessor(queryFilter)
			query += details.clause
			params += details.params
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
			return [clause: "where t.testRun = ? ", params: [queryFilter.testRun]]
		} else {
			return [:]
		}
	}
}