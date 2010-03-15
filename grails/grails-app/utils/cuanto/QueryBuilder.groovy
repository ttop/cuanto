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

import cuanto.queryprocessor.QueryModule

public class QueryBuilder {

	List<QueryModule> queryModules
	private Map <Class, List<QueryModule>> moduleProcessors

	CuantoQuery buildCount(QueryFilter queryFilter) {
		String query = " ${queryFilter.countClause()} "
		Map base = buildQueryForBaseQuery(query, queryFilter)
		return new CuantoQuery(hql: base.hql as String, positionalParameters: base.params as List)
	}


	CuantoQuery buildQuery(QueryFilter queryFilter) {
		Map base = buildQueryForBaseQuery(" ${queryFilter.fromClause()} ", queryFilter)
		String query = base.hql as String

		if (queryFilter.sorts) {
			query += " order by "
			queryFilter.sorts.eachWithIndex {it, idx ->
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
		def cuantoQuery = new CuantoQuery(hql: query.toString(), positionalParameters: base.params as List,
			paginateParameters: pagination)
		return cuantoQuery
	}


	def buildQueryForBaseQuery(String fromClause, QueryFilter queryFilter) {
		List params = []
		List whereClauses = []

		List<QueryModule> processors = getProcessors(queryFilter.appliesToClass())
		String query = fromClause;
		
		processors.each {QueryModule queryProcessor ->
			def details = queryProcessor.getQueryParts(queryFilter)

			if (details.where?.trim()) {
				whereClauses << " ${details.where} "
				params += details.params
			}
		}

		whereClauses.eachWithIndex {clause, idx ->
			query += " ${clause}"
			if (idx < whereClauses.size() - 1) {
				query += " and "
			} else {
				query += " "
			}
		}

		return [hql: query.toString(), 'params': params.flatten()]
	}


	List<QueryModule> getProcessors(Class clazz) {
		if (!moduleProcessors) {
			moduleProcessors = new HashMap<Class, List<QueryModule>>();
			queryModules.each { QueryModule module ->
				module.objectTypes.each { Class modClass ->
					if (!moduleProcessors.containsKey(modClass)) {
						moduleProcessors[modClass] = new ArrayList<QueryModule>();
					}
					moduleProcessors[modClass] << module
				}
			}
		}
		List<QueryModule> modToReturn = moduleProcessors[clazz]
		if (!modToReturn) {
			throw new IllegalArgumentException("No QueryModule found for ${clazz.canonicalName}")
		}
		return modToReturn
	}
}