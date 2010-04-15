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

public interface QueryFilter {


    /**
     *@return the HQL 'select' clauses that is the base select clause for the query this filter will use
     */
    String selectClause()

	/**
	 * @return the HQL 'from' clause that is the basis of queries that this QueryFilter will use.  
	 */
    String fromClause()


	/**
	 * @return the HQL 'from' clause that is the basis of count queries using this QueryFilter.
	 */
	String countClause()

	
	Class appliesToClass()




    /**
    * A transform on the results if needed
    */
    List resultTransform(List results)
}