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
public class TestResultIsNonPassingQueryModule implements QueryModule {

    /**
     * Based on the value of queryFilter.isNonPassing:
     * If true then all returned outcomes that are considered failures or skips will be returned.
     * If null, outcomes will not be returned based on failure status.
     */
    public Map getQueryParts(QueryFilter queryFilter) {
        if (queryFilter.isNonPassing != null) {
            return [where: " (t.testResult.isFailure = ? OR t.testResult.isSkip = ?) ", params: [queryFilter.isNonPassing, queryFilter.isNonPassing]]
        } else {
            return [:]
        }
    }


    public List<Class> getObjectTypes() {
        return [TestOutcome.class]
    }

}