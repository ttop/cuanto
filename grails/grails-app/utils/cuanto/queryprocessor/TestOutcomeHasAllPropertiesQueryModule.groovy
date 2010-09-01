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

package cuanto.queryprocessor

import cuanto.QueryFilter
import cuanto.TestOutcome


class TestOutcomeHasAllPropertiesQueryModule implements QueryModule {

	// todo: make case-insensitive?
	def Map getQueryParts(QueryFilter queryFilter) {
		if (queryFilter.hasAllTestOutcomeProperties) {
			def fromClauses = []
			def whereClauses = []
			def qryArgs = []

			queryFilter.hasAllTestOutcomeProperties.eachWithIndex { prop, indx ->
				fromClauses << " left join t.testProperties prop_${indx} "
				whereClauses << " upper(prop_${indx}.name) = ? and upper(prop_${indx}.value) like ? "
				qryArgs << prop.name.toUpperCase()
				qryArgs << "%${prop.value.toUpperCase()}%"
			}
			
			def fromText = fromClauses.join(" ")
			def whereText = "(" + whereClauses.join(" and ") + ")"
			return [from: fromText, where: whereText, 'params': qryArgs]
		} else {
			return [:]
		}
	}


	def List<Class> getObjectTypes() {
		[TestOutcome.class]
	}
}
