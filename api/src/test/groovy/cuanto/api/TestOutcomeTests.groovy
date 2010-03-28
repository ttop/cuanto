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

package cuanto.api


public class TestOutcomeTests extends GroovyTestCase {

	CuantoConnector client


	void setUp() {
		client = CuantoConnector.newInstance("http://localhost:8080/cuanto", "ClientTest")
	}


	void testAddTestOutcomeAndGetTestOutcomeWithTestRun() {
		TestOutcome outcome = TestOutcome.newInstance("org.codehaus.cuanto", "testAddTestOutcome", "my parameters",
			TestResult.valueOf("Fail"))
		outcome.bug = new Bug("MyBug", "http://jira.codehaus.org/CUANTO-1")
		outcome.analysisState = AnalysisState.Bug
		outcome.startedAt = new Date()
		outcome.finishedAt = new Date() + 1
		outcome.duration = outcome.finishedAt.time - outcome.startedAt.time
		outcome.owner = "Cuanto"
		outcome.note = "Cuanto note"
		outcome.testOutput = "Fantastic test output"

		TestRun run = new TestRun(new Date())
		client.addTestRun(run)
		try {
			Long outcomeId = client.addTestOutcome(outcome, run)
			TestOutcome fetchedOutcome = client.getTestOutcome(outcomeId)
			assertNotNull "No outcome fetched", fetchedOutcome
			assertEquals outcome.testCase, fetchedOutcome.testCase
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
		} finally {
			client.deleteTestRun run
		}


	}


	void testAddTestOutcomeAndGetTestOutcomeWithoutTestRun() {
		TestOutcome outcome = TestOutcome.newInstance("org.codehaus.cuanto", "testAddTestOutcome", "my parameters",
			TestResult.valueOf("Fail"))
		outcome.bug = new Bug("MyBug", "http://jira.codehaus.org/CUANTO-1")
		outcome.analysisState = AnalysisState.Bug
		outcome.startedAt = new Date()
		outcome.finishedAt = new Date() + 1
		outcome.duration = outcome.finishedAt.time - outcome.startedAt.time
		outcome.owner = "Cuanto"
		outcome.note = "Cuanto note"
		outcome.testOutput = "Fantastic test output"

		Long outcomeId = client.addTestOutcome(outcome)

		TestOutcome fetchedOutcome = client.getTestOutcome(outcomeId)

		assertNotNull "No outcome fetched", fetchedOutcome
		assertEquals outcome.testCase, fetchedOutcome.testCase
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
		assertEquals "testRun", null, fetchedOutcome.testRun
	}


	void testUpdateTestOutcomeWithTestRun() {
		TestOutcome outcome = TestOutcome.newInstance("org.codehaus.cuanto", "testAddTestOutcome", "my parameters",
			TestResult.valueOf("Fail"))
		outcome.bug = new Bug("MyBug", "http://jira.codehaus.org/CUANTO-1")
		outcome.analysisState = AnalysisState.Bug
		outcome.startedAt = new Date()
		outcome.finishedAt = new Date() + 1
		outcome.duration = outcome.finishedAt.time - outcome.startedAt.time
		outcome.owner = "Cuanto"
		outcome.note = "Cuanto note"
		outcome.testOutput = "Fantastic test output"

		TestRun run = new TestRun(new Date())
		client.addTestRun(run)
		try {
			Long outcomeId = client.addTestOutcome(outcome, run)
			outcome.bug = new Bug("MyOtherBug", "http://jira.codehaus.org/CUANTO-2")
			outcome.analysisState = AnalysisState.Other
			outcome.startedAt = new Date()
			outcome.finishedAt = new Date() + 2
			outcome.duration = outcome.finishedAt.time - outcome.startedAt.time
			outcome.owner = "New Cuanto"
			outcome.note = "New Cuanto note"
			outcome.testOutput = "Stupendous test output"
			client.updateTestOutcome(outcome)



			TestOutcome fetchedOutcome = client.getTestOutcome(outcomeId)

			assertNotNull "No outcome fetched", fetchedOutcome
			assertEquals outcome.testCase, fetchedOutcome.testCase
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
		} finally {
			client.deleteTestRun run
		}

	}


	void testGetTestOutcomes() {
		TestOutcome outcome = TestOutcome.newInstance("org.codehaus.cuanto", "testAddTestOutcome", "my parameters",
			TestResult.valueOf("Fail"))
		outcome.bug = new Bug("MyBug", "http://jira.codehaus.org/CUANTO-1")
		outcome.analysisState = AnalysisState.Bug
		outcome.startedAt = new Date()
		outcome.finishedAt = new Date() + 1
		outcome.duration = outcome.finishedAt.time - outcome.startedAt.time
		outcome.owner = "Cuanto"
		outcome.note = "Cuanto note"
		outcome.testOutput = "Fantastic test output"

		TestOutcome outcomeTwo = TestOutcome.newInstance("org.codehaus.cuanto", "testAddTestOutcome", "my parameters",
			TestResult.valueOf("Fail"))
		outcomeTwo.bug = new Bug("MyBugTwo", "http://jira.codehaus.org/CUANTO-2")
		outcomeTwo.analysisState = AnalysisState.Bug
		outcomeTwo.startedAt = new Date() + 1
		outcomeTwo.finishedAt = new Date() + 2
		outcomeTwo.duration = outcomeTwo.finishedAt.time - outcomeTwo.startedAt.time
		outcomeTwo.owner = "Cuanto Two"
		outcomeTwo.note = "Cuanto note two"
		outcomeTwo.testOutput = "Fantastic test output two"

		TestRun run = new TestRun(new Date())
		client.addTestRun(run)
		try {
			Long outcomeId = client.addTestOutcome(outcome, run)
			client.addTestOutcome(outcomeTwo, run)
			TestOutcome fetchedOutcome = client.getTestOutcome(outcomeId)
			List<TestOutcome> outcomes = client.getTestCaseOutcomesForTestRun(fetchedOutcome.testCase, run)
			assertEquals "Wrong number of TestOutcomes", 2, outcomes.size()
			assertEquals "Wrong outcome first", outcomeTwo.id, outcomes[0].id
			assertEquals "Wrong outcome second", outcome.id, outcomes[1].id
		} finally {
			client.deleteTestRun run
		}

	}


	void testGetTestOutcomesForTestCase() {
		TestOutcome outcome = TestOutcome.newInstance("org.codehaus.cuanto", "testAddTestOutcome", "my parameters",
			TestResult.valueOf("Fail"))
		outcome.bug = new Bug("MyBug", "http://jira.codehaus.org/CUANTO-1")
		outcome.analysisState = AnalysisState.Bug
		outcome.startedAt = new Date()
		outcome.finishedAt = new Date() + 1
		outcome.duration = outcome.finishedAt.time - outcome.startedAt.time
		outcome.owner = "Cuanto"
		outcome.note = "Cuanto note"
		outcome.testOutput = "Fantastic test output"

		TestOutcome outcomeTwo = TestOutcome.newInstance("org.codehaus.cuanto", "testAddTestOutcome", "my parameters",
			TestResult.valueOf("Fail"))
		outcomeTwo.bug = new Bug("MyBugTwo", "http://jira.codehaus.org/CUANTO-2")
		outcomeTwo.analysisState = AnalysisState.Bug
		outcomeTwo.startedAt = new Date() + 1
		outcomeTwo.finishedAt = new Date() + 2
		outcomeTwo.duration = outcomeTwo.finishedAt.time - outcomeTwo.startedAt.time
		outcomeTwo.owner = "Cuanto Two"
		outcomeTwo.note = "Cuanto note two"
		outcomeTwo.testOutput = "Fantastic test output two"

		Long outcomeId = client.addTestOutcome(outcome)
		sleep 2000
		client.addTestOutcome(outcomeTwo)

		TestOutcome fetchedOutcome = client.getTestOutcome(outcomeId)
		List<TestOutcome> outcomes = client.getAllTestOutcomesForTestCase(fetchedOutcome.testCase)
		assertTrue "Wrong number of TestOutcomes", outcomes.size() >= 2

		int indexOfOutcomeOne = null;
		int indexOfOutcomeTwo = null;

		outcomes.eachWithIndex {it, indx ->
			if (it.id == outcome.id) {
				indexOfOutcomeOne = indx
			}
			if (it.id == outcomeTwo.id) {
				indexOfOutcomeTwo = indx
			}
		}
		if (indexOfOutcomeOne == null) {
			fail "Outcome one not found"
		}
		if (indexOfOutcomeTwo == null) {
			fail "Outcome two not found"
		}
		assertTrue "Outcomes not in correct order", indexOfOutcomeTwo < indexOfOutcomeOne
	}


	void testGetAllTestOutcomesForTestRun() {
		TestOutcome outcome = TestOutcome.newInstance("org.codehaus.cuanto", "testAddTestOutcome", "my parameters",
			TestResult.valueOf("Fail"))
		outcome.bug = new Bug("MyBug", "http://jira.codehaus.org/CUANTO-1")
		outcome.analysisState = AnalysisState.Bug
		outcome.startedAt = new Date()
		outcome.finishedAt = new Date() + 1
		outcome.duration = outcome.finishedAt.time - outcome.startedAt.time
		outcome.owner = "Cuanto"
		outcome.note = "Cuanto note"
		outcome.testOutput = "Fantastic test output"

		TestOutcome outcomeTwo = TestOutcome.newInstance("org.codehaus.cuanto", "testAnother", "my parameters",
			TestResult.valueOf("Fail"))
		outcomeTwo.bug = new Bug("MyBugTwo", "http://jira.codehaus.org/CUANTO-2")
		outcomeTwo.analysisState = AnalysisState.Bug
		outcomeTwo.startedAt = new Date() + 1
		outcomeTwo.finishedAt = new Date() + 2
		outcomeTwo.duration = outcomeTwo.finishedAt.time - outcomeTwo.startedAt.time
		outcomeTwo.owner = "Cuanto Two"
		outcomeTwo.note = "Cuanto note two"
		outcomeTwo.testOutput = "Fantastic test output two"

		TestRun run = new TestRun(new Date())
		client.addTestRun(run)
		try {
			client.addTestOutcome(outcome, run)
			sleep 1000
			client.addTestOutcome(outcomeTwo, run)
			List<TestOutcome> outcomes = client.getAllTestOutcomesForTestRun(run)
			assertEquals "Wrong number of TestOutcomes", 2, outcomes.size()
			assertEquals "Wrong outcome first", outcome.id, outcomes[0].id
			assertEquals "Wrong outcome second", outcomeTwo.id, outcomes[1].id
		} finally {
			client.deleteTestRun run
		}

	}


	void assertEquals(String message, Date expected, Date actual) {
		// Date should be within one second
		assertTrue message + ". Expected time ${expected.time}, actual time ${actual.time}",
			Math.abs(expected.time - actual.time) < 1000
	}


	void assertEquals(TestCase expected, TestCase actual) {
		assertEquals "TestCase packageName", expected.packageName, actual.packageName
		assertEquals "TestCase testName", expected.testName, actual.testName
		assertEquals "TestCase parameters", expected.parameters, actual.parameters
	}
}