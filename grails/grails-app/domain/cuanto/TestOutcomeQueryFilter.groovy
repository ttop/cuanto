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
		isSkip(nullable: true)
		testResult(nullable:true)
		testCaseFullName(nullable: true)
		testCaseParameters(nullable: true)
		testCasePackage(nullable: true)
		project(nullable: true)
		testResultIncludedInCalculations(nullable: true)
		isAnalyzed(nullable: true)
		analysisState(nullable: true)
		bug(nullable: true)
		owner(nullable: true)
		testCase(nullable: true)
		note(nullable: true)
		testOutput(nullable: true)
		dateCriteria(nullable: true)
		sorts(nullable: true)
		queryOffset(nullable: true)
		queryMax(nullable: true)
		isFailureStatusChanged(nullable: true)
		hasAllTestOutcomeProperties(nullable: true)
	}

	TestOutcomeQueryFilter() {}

	TestOutcomeQueryFilter(TestOutcomeQueryFilter filterToCopy) {
		["testRun", "isFailure", "isSkip", "testResult", "testCaseFullName", "testCaseParameters", "testCasePackage", "project",
			"testResultIncludedInCalculations", "isAnalyzed", "analysisState", "bug", "owner", "testCase", "note",
			"testOutput", "dateCriteria", "sorts", "queryOffset", "queryMax", "isFailureStatusChanged",
			"hasAllTestOutcomeProperties", "successRate"].each {
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
	 * If true then all returned outcomes that are considered skips will be returned.
	 * If false, then all returned outcomes that are not considered skips will be returned.
	 * If null, outcomes will not be returned based on isSkip status
	 */
	Boolean isSkip


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
	Integer queryOffset


	/**
	 * The maximum number of TestOutcomes to return
	 */
	Integer queryMax


    /**
     * If not null, only TestOutcomes that contain at least one of these tag names will be returned.
     * @param tags A list of tag names.
     */
    List tags


    /**
    * If not null, only TestOutcomes that either have tags or don't have tags will be returned, depending on the
    * boolean value.
    */
    Boolean hasTags


	/**
	 * If true, only TestOutcomes with their isFailureStatusChanged == true will be returned.
	 * If false, only TestOutcomes with their isFailureStatusChanged == false will be returned.
	 * If null, TestOutcomes will not be filtered based on their isFailureStatusChanged state.
	 */
	Boolean isFailureStatusChanged

	/**
	 * If not null or empty, only TestOutcomes that have testProperties which contain all of the specified properties
	 * will be returned.
	 */
	List hasAllTestOutcomeProperties

	

    String selectClause() {
        def select = "select distinct t"
        if (sorts) {
            def newList = sorts.collect{"t." + it.sort}
            select += ", " + newList.join(", ")
        }
        return select
    }

	String fromClause() {
		"from cuanto.TestOutcome t"
	}


	String countClause() {
        "select count(distinct t) "
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
		} else if (field == "properties") {
			def delim = searchTerm.indexOf("|")
			def propName = searchTerm.substring(0, delim)
			def propVal = searchTerm.substring(delim + 1)
			def prop = new TestOutcomeProperty(propName, propVal)
			this.hasAllTestOutcomeProperties = [prop]
		}
	}


	static String getSortNameForFriendlyName(String friendlyName) {
		def name = friendlyName?.toLowerCase()?.replaceAll("_", "")

		def nameMap = [:]
		nameMap.name = "testCase.fullName"
		nameMap.testcase = "testCase.fullName"
		nameMap.fullname = "testCase.fullName"
		nameMap.result = "testResult"
		nameMap.testresult = "testResult.name"
        nameMap.streak = "testOutcomeStats.streak"
        nameMap.successrate = "testOutcomeStats.successRate"
		nameMap.state = "analysisState"
		nameMap.analysisstate = "analysisState"
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
		nameMap.startdate = "startedAt"
        nameMap.dateexecuted = "testRun.dateExecuted"
		nameMap.testproperties = "prop_0.name"
		def resolvedValue = nameMap[name]
		if (!resolvedValue) {
			resolvedValue = "testCase.fullName"
		}
		return resolvedValue as String
	}


    public List resultTransform(List results) {
        if (sorts) {
            return results.collect{it[0]}
        } else {
            return results
        }
    }
}