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

package cuanto.user


public class TestOutcomeTests extends GroovyTestCase {

	CuantoConnector client

	void setUp() {
		client = CuantoConnector.newInstance("http://localhost:8080/cuanto", "ClientTest")
	}

	
	void testAddTestOutcomeAndGetTestOutcome() {
		TestOutcome outcome = TestOutcome.newInstance("org.codehaus.cuanto", "testAddTestOutcome", "my parameters",
			TestResult.valueOf("Fail"))
		outcome.bug = new Bug("MyBug", "http://jira.codehaus.org/CUANTO-1")
		outcome.analysisState = "Bug"
		outcome.startedAt = new Date()
		outcome.finishedAt = new Date() + 1
		outcome.duration = outcome.finishedAt.time - outcome.startedAt.time
		outcome.owner = "Cuanto"
		outcome.note = "Cuanto note"
		outcome.testOutput = "Fantastic test output"

		TestRun run = new TestRun(new Date())
		client.addTestRun(run)
		Long outcomeId = client.addTestOutcome(outcome, run)

		TestOutcome fetchedOutcome = client.getTestOutcome(outcomeId)

		assertNotNull "No outcome fetched", fetchedOutcome
		assertEquals "Wrong testCase", outcome.testCase, fetchedOutcome.testCase
		assertNotNull "Bug", fetchedOutcome.bug
		assertEquals "Bug title", outcome.bug.title, fetchedOutcome.bug.title
		assertEquals "Bug url", outcome.bug.url, fetchedOutcome.bug.url
		assertEquals "Analysis state", outcome.analysisState, fetchedOutcome.analysisState
		assertEquals "startedAt", outcome.startedAt, fetchedOutcome.startedAt
		assertEquals "finishedAt", outcome.finishedAt, fetchedOutcome.finishedAt
		assertEquals "duration", outcome.duration, fetchedOutcome.duration
		assertEquals "owner", outcome.owner, fetchedOutcome.owner
		assertEquals "note", outcome.note, fetchedOutcome.note
		assertEquals "testOutput", outcome.testOutput, client.getTestOutput(fetchedOutcome)
		assertEquals "testRun", run.id, fetchedOutcome.testRun.id
	}


	void assertEquals(String message, Date expected, Date actual) {
		// Date should be within one second
		assertTrue message + ". Expected time ${expected.time}, actual time ${actual.time}",
			Math.abs(expected.time - actual.time) < 1000
	}
}