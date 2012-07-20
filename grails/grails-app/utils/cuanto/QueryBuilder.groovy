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
        Map<Object,Object> result
        List fromClauses = []
        List whereClauses = []
        List params = []
        List<QueryModule> processors = getProcessors(queryFilter.appliesToClass())
        processors.each {QueryModule queryProcessor ->
            def details = queryProcessor.getQueryParts(queryFilter)

            if (details.from?.trim()) {
                fromClauses << details.from.trim()
            }

            if (details.where?.trim()) {
                whereClauses << " ${details.where} "
                if (details.params) {
                    params += details.params
                }
            }
        }
        String fromClause = queryFilter.fromClause()
        fromClauses.each {
            fromClause += " ${it} "
        }
        if (whereClauses.size() == 0) {
            throw new CuantoException("No filter options were specified for query")
        }
        String whereClause = ""
        whereClauses.eachWithIndex {clause, idx ->
            whereClause += " ${clause}"
            if (idx < whereClauses.size() - 1) {
                whereClause += " and "
            } else {
                whereClause += " "
            }
        }
        String selectClause = queryFilter.countClause()
        String query = selectClause + " " +  fromClause + " where " + whereClause
        result = [hql: query.toString(), 'params': params.flatten()]
        return new CuantoQuery(hql: result.hql as String, positionalParameters: result.params as List)
    }


	CuantoQuery buildQuery(QueryFilter queryFilter) throws CuantoException {

		Map base = buildQueryForBaseQuery(queryFilter)
		String query = base.hql as String

		if (queryFilter.sorts) {
			query += " order by "
			queryFilter.sorts.eachWithIndex {it, idx ->
				def order
				if (it.sortOrder) {
					if (it.sortOrder.toLowerCase() == "asc" || it.sortOrder.toLowerCase() == "desc") {
						order = it.sortOrder
					} else {
						throw new IllegalArgumentException("${it.sortOrder} is not a valid sort order")
					}
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


	def buildQueryForBaseQuery(QueryFilter queryFilter) {
        List fromClauses = []
		List whereClauses = []
		List params = []

		List<QueryModule> processors = getProcessors(queryFilter.appliesToClass())

        processors.each {QueryModule queryProcessor ->
			def details = queryProcessor.getQueryParts(queryFilter)

            if (details.from?.trim()) {
                fromClauses << details.from.trim()
            }

			if (details.where?.trim()) {
				whereClauses << " ${details.where} "
                if (details.params) {
                    params += details.params
                }
			}
		}

        String selectClause = queryFilter.selectClause()

        String fromClause = queryFilter.fromClause()
        fromClauses.each {
            fromClause += " ${it} "
        }

		if (whereClauses.size() == 0) {
			throw new CuantoException("No filter options were specified for query")
		}

        String whereClause = ""
		whereClauses.eachWithIndex {clause, idx ->
			whereClause += " ${clause}"
			if (idx < whereClauses.size() - 1) {
				whereClause += " and "
			} else {
				whereClause += " "
			}
		}

        String query = selectClause + " " +  fromClause + " where " + whereClause
		return [hql: query.toString(), 'params': params.flatten()]
	}


	synchronized List<QueryModule> getProcessors(Class clazz) {
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