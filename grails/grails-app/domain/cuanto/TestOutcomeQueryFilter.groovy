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

	static hasMany = [sorts: SortParameters, dateCriteria: DateCriteria]

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
		bug(nullable:true)
		owner(nullable:true)
		testCase(nullable:true)
		note(nullable:true)
		testOutput(nullable:true)
		dateCriteria(nullable:true)
		sorts(nullable: true)
		queryOffset(nullable: true)
		queryMax(nullable:true)
	}

	TestOutcomeQueryFilter() {}

	TestOutcomeQueryFilter(TestOutcomeQueryFilter filterToCopy) {
		["testRun", "isFailure", "testResult", "testCaseFullName", "testCaseParameters", "testCasePackage", "project",
		"testResultIncludedInCalculations", "isAnalyzed", "analysisState", "bug", "owner", "testCase", "note",
		"testOutput", "dateCriteria", "sorts", "queryOffset", "queryMax"].each {
			this.setProperty(it, filterToCopy.getProperty(it))
		}
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
	 * If not null, then all returned outcomes must have this exact package string or start with this package string.
	 * '*' may be used as a wildcard.
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
	 * If not null, only TestOutcomes that reference this test case will be returned. 
	 */
	TestCase testCase


	/**
	 * If not null, only TestOutcomes that have a note which contains this text will be returned.
	 */
	String note

	/**
	 * If not null, only TestOutcomes that have testOutput containing this string will be returned.
	 */
	String testOutput

	/**
	 * If not null, only TestOutcomes that meet all of these dateCriteria will be returned.
	 */
	List<DateCriteria> dateCriteria


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
	  duration ?
	*/


	String fromClause() {
		return "from cuanto.TestOutcome t where "
	}


	String countClause() {
		return "select count(*) from cuanto.TestOutcome t where "
	}


	Class appliesToClass() {
		return TestOutcome.class
	}

	void setForSearchTerm (String searchField, String searchTerm) {
		/* 'Name', 'Note', 'Output', 'Owner' */
		String field = searchField.toLowerCase()

		if (field == "name" ) {
			this.testCaseFullName = searchTerm
		} else if (field == "note") {
			this.note = searchTerm
		} else if (field == "output") {
			this.testOutput = searchTerm
		} else if (field == "owner") {
			this.owner = searchTerm
		}
	}


	static String getSortNameForFriendlyName(String friendlyName) {
		def name = friendlyName?.toLowerCase()?.replaceAll("_", "")

		def nameMap = [:]
		nameMap.name = "testCase.fullName"
		nameMap.testcase = "testCase.fullName"
		nameMap.fullname = "testCase.fullName"
		nameMap.result = "testResult"
		nameMap.testresult = "testResult"
		nameMap.state = "analysisState.name"
		nameMap.analysisstate = "analysisState.name"
		nameMap.duration = "duration"
		nameMap.bug = "bug.title"
		nameMap.owner = "owner"
		nameMap.note = "note"
		nameMap.output = "testOutput"
		nameMap.testoutput = "testOutput"
		nameMap.datecreated = "dateCreated"
		nameMap.finishedat = "finishedAt"
		nameMap.id = "id"
		nameMap.lastupdated = "lastUpdated"
		nameMap.startedat = "startedAt"

		def resolvedValue = nameMap[name]
		if (!resolvedValue) {
			resolvedValue = "testCase.fullName"
		}
		return resolvedValue as String
	}
}