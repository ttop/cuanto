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

public class TestOutcomeQueryFilter {

	static hasMany = [sorts: SortParameters]
	static constraints = {
		testRun(nullable: true)
		isFailure(nullable: true)
		sorts(nullable: true)
		queryOffset(nullable: true)
		queryMax(nullable:true)
	}

	TestRun testRun
	Boolean isFailure
	
	List sorts
	Long queryOffset
	Long queryMax

	/* TODO:

     Filters for:
      Project (if it's not with a TestRun)
	  TestCase (which would return history)
	  TestCase name
	  DateRange - start and end dates, operator (lt, gt, <=, >=)
	  TestResult
	  IncludeIgnored
	  AnalysisState
	  duration
	  bug
	  note
	  owner
	  testoutput

	*/

}