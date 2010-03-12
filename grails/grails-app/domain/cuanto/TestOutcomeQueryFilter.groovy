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

public class TestOutcomeQueryFilter implements QueryFilter {

	static hasMany = [sorts: SortParameters]

	static constraints = {
		testRun(nullable: true)
		isFailure(nullable: true)
		testResult(nullable:true)
		testCaseFullName(nullable:true)
		testCaseParameters(nullable:true)
		testCasePackage(nullable:true)
		project(nullable:true)
		testResultIncludedInCalculations(nullable:true)
		isAnalyzed(nullable:true)
		analysisState(nullable:true)
		sorts(nullable: true)
		queryOffset(nullable: true)
		queryMax(nullable:true)
	}

	/**
	 * If not null, all returned outcomes will be associated with this TestRun.
	 */
	TestRun testRun


	/**
	 * If true then all returned outcomes that are considered failures will be returned.
	 * If false, then all returned outcomes that are not considered failures will be returned.
	 * If null, outcomes will not be returned based on failure status.
	 */
	Boolean isFailure


	/**
	 * If not null, then all returned outcomes must have this exact TestResult.
	 */
	TestResult testResult


	/**
	 * If not null, then all returned outcomes must contain this string as part of the associated TestCase's fullName.
	 */
	String testCaseFullName


	/**
	 * If not null, then all returned outcomes must have this exact parameters string. '*' may be used as a wildcard.
	 */
	String testCaseParameters


	/**
	 * If not null, then all returned outcomes must have this exact package string. '*' may be used as a wildcard.
	 */
	String testCasePackage


	/**
	 * If not null, then all returned outcomes must be from this project. This would be used in the case where
	 * TestRun or TestCase was not provided.
	 */
	Project project


	/**
	 * If true, the TestOutcomes returned will include TestResults that are ignored when calculating statistics, like
	 * "Ignore" or "Unexecuted".
	 * If false, all TestOutcomes will only contain un-ignored values.
	 */
	Boolean testResultIncludedInCalculations

	/**
	 * If true, only TestOutcomes with their AnalysisState.isAnalyzed == true will be returned.
	 * If false, only TestOutcomes with their AnalysisState.isAnalyzed == false will be returned.
	 * If null, TestOutcomes will not be filtered based on their isAnalyzed state.
	 */
	Boolean isAnalyzed


	/**
	 * If not null, only TestOutcomes with this AnalysisState will be returned. 
	 */
	AnalysisState analysisState



	/**
	 * If not null, only TestOutcomes that reference this bug will be returned. 
	 */
	Bug bug


	/**
	 * If not null, only TestOutcomes that reference this owner will be returned. Case-insensitive match.
	 */
	String owner
	

    /**
	 * A list of SortParameters to be applied to the TestOutcomes in order of precedence.
	 */
	List sorts


	/**
	 * The offset of the first TestOutcome to return.
	 */
	Long queryOffset


	/**
	 * The maximum number of TestOutcomes to return
	 */
	Long queryMax



	/* TODO:

     Filters for:
	  TestCase (which would return history)
	  DateRange - start and end dates, operator (lt, gt, <=, >=)
	  duration
	  note
	  owner
	  testoutput

	*/


	public String fromClause() {
		return "from cuanto.TestOutcome t where "
	}


	public Class appliesToClass() {
		return TestOutcome.class
	}
}