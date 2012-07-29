/*
 Copyright (c) 2008 thePlatform, Inc.

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

import java.text.SimpleDateFormat
import cuanto.formatter.TestNameFormatter

class TestOutcome {

	static constraints = {
		testCase(nullable: false)
		testResult(nullable: false)
		testRun(nullable: true)
		analysisState(nullable: true)
		duration(nullable: true)
		bug(nullable: true)
		note(blank: true, nullable: true)
		owner(blank: true, nullable: true)
		testOutput(nullable: true, blank: true, maxSize: 10000)
        testOutputSummary(nullable: true, blank: true, maxSize: 255)
		startedAt(nullable: true)
		finishedAt(nullable: true)
		dateCreated(nullable: true)
		lastUpdated(nullable: true)
        tags(nullable :true)
		isFailureStatusChanged(nullable: true)
        testOutcomeStats(nullable: true)
		//testOutcomeLink(nullable: true)
		//testOutcomeProperty(nullable: true)
	}

	static mapping = {
		cache usage: 'read-write'
		analysisState lazy: false
        tags fetch: "join", lazy: false
		isFailureStatusChanged lazy: false, index: 'is_failure_status_changed_idx'
		testOutputSummary index:'test_output_summary_idx'
		links fetch: "join", lazy: false, cascade: "all-delete-orphan"
		testProperties fetch: "join", lazy: false, cascade: "all-delete-orphan"
		version false
	}

    static hasMany = [tags: Tag, testProperties: TestOutcomeProperty, links: TestOutcomeLink]

	TestCase testCase
	TestRun testRun
	TestResult testResult
	AnalysisState analysisState
	String testOutput
    String testOutputSummary
	Long duration
	String owner
	Bug bug
	String note
	Date startedAt // when the test started
	Date finishedAt // when the test finished
	Date dateCreated  // this is the timestamp for when the database record was created
	Date lastUpdated // timestamp for when the database record was last updated
	Boolean isFailureStatusChanged
	List<TestOutcomeLink> links
	List<TestOutcomeProperty> testProperties
    TestOutcomeStats testOutcomeStats

	Map toJSONmap(Boolean includeTestOutput = false, Integer truncateOutput = null, TestNameFormatter testCaseFormatter = null,
		Boolean includeTestRunDetails = true) {
		def outcome = this
		final SimpleDateFormat dateFormatter = new SimpleDateFormat(Defaults.fullDateFormat)
        def successRate = outcome.testOutcomeStats?.successRate ?: 0.0f
        def formattedSuccessRate = null
        if (successRate != null)
        {
            formattedSuccessRate = String.format("%.2f", successRate)
        }

		def myJson = [
			id: outcome.id,
			analysisState: outcome.analysisState?.name,
			result: outcome.testResult?.name,
            streak: outcome.testOutcomeStats?.streak ?: 0,
            successRate: formattedSuccessRate,
			owner: outcome.owner,
			note: outcome.note,
			duration: outcome.duration,
			isFailureStatusChanged: outcome.isFailureStatusChanged
		]

		if (includeTestRunDetails) {
			myJson.testRun = outcome.testRun?.toJSONMap()
		} else {
			myJson.testRun = ["id": outcome.testRun?.id]
		}

		def testCaseJson
		if (testCaseFormatter) {
			testCaseJson = [name: testCaseFormatter.getTestName(outcome.testCase), id: outcome.testCase.id,
				analysisCount: outcome.testCase?.analysisCount]
			if (outcome.testCase.parameters) {
				testCaseJson.parameters = outcome.testCase.parameters
			}
		} else {
			testCaseJson = [testName: outcome.testCase?.testName, packageName: outcome.testCase?.packageName,
				parameters: outcome.testCase?.parameters, description: outcome.testCase?.description,
				fullName: outcome.testCase?.fullName, analysisCount: outcome.testCase?.analysisCount]
		}

		myJson.testCase = testCaseJson


		if (outcome.testCase?.id) {
			myJson.testCase.id = outcome.testCase.id
		}

		if (includeTestOutput) {
		    if (truncateOutput && outcome.testOutput) {
			    def maxChars = outcome.testOutput?.size() > truncateOutput ? truncateOutput : outcome.testOutput?.size() 
			    myJson.testOutput = outcome.testOutput[0..maxChars - 1]
		    } else {
			    myJson.testOutput = outcome.testOutput
		    }
		} else {
			myJson.testOutput = null
		}

		myJson.bug = outcome.bug == null ? null : [title: outcome.bug?.title, url: outcome.bug?.url, 'id': outcome.bug?.id]
		myJson.dateCreated = outcome.dateCreated == null ? null : dateFormatter.format(outcome.dateCreated)
		myJson.lastUpdated = outcome.lastUpdated == null ? null : dateFormatter.format(outcome.lastUpdated)
		myJson.startedAt = outcome.startedAt == null ? null : dateFormatter.format(outcome.startedAt)
		myJson.finishedAt = outcome.finishedAt == null ? null : dateFormatter.format(outcome.finishedAt)

        if (outcome.tags) {
            myJson.tags = outcome.tags.collect{it.name}.sort()
        }

		if (outcome.testProperties) {
			def propJson = [:]

			outcome.testProperties.each {
				propJson[it.name] = it.value
			}
			myJson["testProperties"] = propJson
		}

		if (outcome.links) {
			def linkJson = [:]
			outcome.links.each {
				linkJson[it.url] = it.description
			}
			myJson["links"] = linkJson
		}
        
		return myJson
	}

    void applyAnalysisFrom(TestOutcome source)
    {
        this.owner = source.owner
        this.note = source.note
        this.bug = source.bug
        this.analysisState = source.analysisState
    }
}
