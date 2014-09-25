/*

Copyright (c) 2010 thePlatform, Inc.

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


public class TagNameQueryModule implements QueryModule {

    public Map getQueryParts(QueryFilter queryFilter) {
        def combineWith = "and"
        def parts = [:]
        if (queryFilter.tags) {
            if (combineWith.equals("or")) {
                def whereClauses = []
                def params = []
                queryFilter.tags.each { tagName->
                    whereClauses << "upper(tag_0.name) like ?"
                    params << tagName.toUpperCase()
                }
                def whereText = "(" + whereClauses.join(" OR ") + ")"
                parts = [from: "inner join t.tags tag_0", where: whereText,
                        'params': params ]
            } else {
                def having = []
                def params = []
                queryFilter.tags.each { tagName->
                    //having << "find_in_set(?, upper(col_3_0_))"
                    //params << tagName.toUpperCase()
                    having << "upper(col_3_0_) like ?"
                    params << "%" + tagName.toUpperCase() + "%"
                }
                parts = [from: "inner join t.tags tag_0",
                         group: "t.id",
                         having: "(" + having.join(" AND ") + ")",
                         params: params]
            }

        }
        return parts
    }


    public List<Class> getObjectTypes() {
        [TestOutcome.class]
    }
}