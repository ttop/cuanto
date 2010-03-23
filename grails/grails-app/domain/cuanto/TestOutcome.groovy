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

import cuanto.testapi.TestOutcome as TestOutcomeApi

import java.text.SimpleDateFormat

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
		startedAt(nullable: true)
		finishedAt(nullable: true)
		dateCreated(nullable: true)
		lastUpdated(nullable: true)
	}
	static mapping = {
		cache true
		analysisState lazy: false
	}

	TestCase testCase
	TestRun testRun
	TestResult testResult
	AnalysisState analysisState
	String testOutput
	Long duration
	String owner
	Bug bug
	String note
	Date startedAt // when the test started
	Date finishedAt // when the test finished
	Date dateCreated  // this is the timestamp for when the database record was created
	Date lastUpdated // timestamp for when the database record was last updated


	TestOutcomeApi toTestOutcomeApi() {
		TestOutcomeApi out = new TestOutcomeApi()
		out.testCase = this.testCase?.toTestCaseApi()
		out.testResult = this.testResult?.toString()
		out.testOutput = this.testOutput
		out.duration = this.duration
		out.owner = this.owner
		out.bug = this.bug?.toBugApi()
		out.note = this.note
		out.id = this.id
		out.analysisState = this.analysisState?.toAnalysisStateApi()
		out.startedAt = this.startedAt
		out.finishedAt = this.finishedAt
		out.dateCreated = this.dateCreated
		out.lastUpdated = this.lastUpdated
		return out
	}


	Map toJSONmap(Boolean includeTestOutput = false) {
		def outcome = this
		final SimpleDateFormat dateFormatter = new SimpleDateFormat(Defaults.fullDateFormat)

		def myJson = [
			id: outcome.id,
			analysisState: [name: outcome.analysisState?.name, 'id': outcome.analysisState?.id],
			testCase: [testName: outcome.testCase.testName, packageName: outcome.testCase.packageName,
				parameters: outcome.testCase.parameters, description: testCase.description, id: outcome.testCase?.id],
			result: outcome.testResult?.name,
			owner: outcome.owner,
			note: outcome.note,
			duration: outcome.duration,
			testRun: outcome.testRun?.toJSONMap(),
			dateCreated: dateFormatter.format(dateCreated),
			lastUpdated: dateFormatter.format(lastUpdated)
		]

		if (includeTestOutput) {
			myJson.testOutput = outcome.testOutput
		}
		myJson.bug = outcome.bug == null ? null : [title: outcome.bug?.title, url: outcome.bug?.url, 'id': outcome.bug?.id]
		myJson.startedAt = outcome.startedAt == null ? null : dateFormatter.format(outcome.startedAt)
		myJson.finishedAt = outcome.finishedAt == null ? null : dateFormatter.format(outcome.finishedAt)
		return myJson
	}
}
