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
	WordGenerator wordGen = new WordGenerator()

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


	void testGetTestOutcomesForTestRun() {
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
			List<TestOutcome> outcomes = client.getTestOutcomesForTestRun(run, 0, 100, TestOutcome.Sort.FULL_NAME, "asc")
			assertEquals "Wrong number of TestOutcomes", 2, outcomes.size()
			assertEquals "Wrong outcome first", outcome.id, outcomes[0].id
			assertEquals "Wrong outcome second", outcomeTwo.id, outcomes[1].id
		} finally {
			client.deleteTestRun run
		}
	}

	void testGetTestOutcomesForTestRunMany() {
		TestRun run = new TestRun(new Date())
		client.addTestRun(run)
		try {
			1.upto(202) {
				TestOutcome outcome = createTestOutcome(TestResult.Pass)
				client.addTestOutcome(outcome, run)
				sleep 10
			}
			List<TestOutcome> outcomes = client.getTestOutcomesForTestRun(run, 0, 100, TestOutcome.Sort.FULL_NAME, "asc")
			assertEquals "Wrong number of TestOutcomes", 100, outcomes.size()
			outcomes = client.getTestOutcomesForTestRun(run, 100, 100, TestOutcome.Sort.FULL_NAME, "asc")
			assertEquals "Wrong number of TestOutcomes", 100, outcomes.size()
			outcomes = client.getTestOutcomesForTestRun(run, 200, 100, TestOutcome.Sort.FULL_NAME, "asc")
			assertEquals "Wrong number of TestOutcomes", 2, outcomes.size()
		} finally {
			client.deleteTestRun run
		}
	}


	void testCountTestOutcomesForTestRun() {
		TestRun run = new TestRun(new Date())
		client.addTestRun(run)
		def count = client.countTestOutcomesForTestRun(run)
		assertEquals "Wrong count", 0, count
		try {
			client.addTestOutcome(createTestOutcome(TestResult.Pass), run)
			count = client.countTestOutcomesForTestRun(run)
			assertEquals "Wrong count", 1, count

			1.upto(201) {
				TestOutcome outcome = createTestOutcome(TestResult.Pass)
				client.addTestOutcome(outcome, run)
			}

			count = client.countTestOutcomesForTestRun(run)
			assertEquals "Wrong count", 202, count
		} finally {
			client.deleteTestRun run
		}
	}

	
	void testGetTestOutcomesForTestRunSorts() {
		TestRun run = new TestRun(new Date())
		client.addTestRun(run)

		def outcomes = new ArrayList<TestOutcome>()
		
		outcomes << TestOutcome.newInstance("org.codehaus.cuanto", "testA", "aa", TestResult.Error)
		outcomes[0].startedAt = new Date()
		outcomes[0].finishedAt = new Date() + 1
		outcomes[0].duration = 10
		outcomes[0].owner = "aa"
		outcomes[0].note = "abc"
		outcomes[0].testOutput = "abc"
		outcomes[0].analysisState = AnalysisState.Bug
		
		outcomes << TestOutcome.newInstance("org.codehaus.cuanto", "testA", "ab", TestResult.Fail)
		outcomes[1].startedAt = new Date() + 1
		outcomes[1].finishedAt = new Date() + 2
		outcomes[1].duration = 20
		outcomes[1].owner = "bb"
		outcomes[1].note = "def"
		outcomes[1].testOutput = "def"
		outcomes[1].analysisState = AnalysisState.Harness

		outcomes << TestOutcome.newInstance("org.codehaus.cuanto", "testB", "aa", TestResult.Pass)
		outcomes[2].startedAt = new Date() + 2
		outcomes[2].finishedAt = new Date() + 3
		outcomes[2].duration = 30
		outcomes[2].owner = "cc"
		outcomes[2].note = "ghi"
		outcomes[2].testOutput = "ghi"
		outcomes[2].analysisState = AnalysisState.TestBug

		outcomes.each {
			client.addTestOutcome(it, run)
			sleep 1000
		}

		def sortOptions = [TestOutcome.Sort.FULL_NAME, TestOutcome.Sort.STARTED_AT, TestOutcome.Sort.FINISHED_AT,
			TestOutcome.Sort.DURATION, TestOutcome.Sort.OWNER, TestOutcome.Sort.NOTE, TestOutcome.Sort.TEST_OUTPUT,
			TestOutcome.Sort.TEST_RESULT, TestOutcome.Sort.ANALYSIS_STATE, TestOutcome.Sort.DATE_CREATED,
			TestOutcome.Sort.ID]

		sortOptions.each { TestOutcome.Sort sort ->
			List<TestOutcome> fetched = client.getTestOutcomesForTestRun(run, 0, 100, sort, "asc")
			assertEquals "Wrong number of outcomes returned for ${sort.toString()}", 3, fetched.size()
			[0, 1, 2].eachWithIndex {it, idx ->
				assertEquals "Wrong outcome for ${sort.toString()}", outcomes[it].id, fetched[idx].id
			}

			fetched = client.getTestOutcomesForTestRun(run, 0, 100, sort, "desc")
			[2, 1, 0].eachWithIndex {it, idx ->
				assertEquals "Wrong outcome for ${sort.toString()}", outcomes[it].id, fetched[idx].id
			}
		}
	}


    void testAddTestOutcomeForTestRunWithTags() {
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
        outcome.addTag(wordGen.getWord())
        outcome.addTag(wordGen.getWord())
        assertEquals "tags", 2, outcome.tags?.size()

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

            assertEquals "tags", outcome.tags.size(), fetchedOutcome.tags?.size()

            TestRun fetchedRun = client.getTestRun(run.id)
            outcome.tags.each { tag ->
                assertNotNull "Couldn't find tag ${tag} on TestOutcome", fetchedOutcome.tags.find { it == tag }
                assertNotNull "Couldn't find tag ${tag} on TestRun", fetchedRun.tags.find { it == tag }
            }
        } finally {
            client.deleteTestRun run
        }
    }
    

    void testTagsAndManyOutcomes() {
        def tags = []
        tags << wordGen.getCamelWords(2)
        tags << wordGen.getCamelWords(2)

        def outcomes = []
        1.upto(2000) {
            TestOutcome outcome = createTestOutcome(TestResult.Pass)
            outcome.addTags(tags)
            outcomes << outcome
        }

        TestRun run = new TestRun(new Date())
        client.addTestRun(run)
        try {
            outcomes.each {
                client.addTestOutcome(it, run)
            }
            println "finished adding tests"
        } finally {
            println "deleting test run"
            def start = new Date()
            client.deleteTestRun run
            println "finished deleting"
            def totalTime = (new Date().time - start.time) / 1000
            println "took ${totalTime} seconds"
        }
    }



	TestOutcome createTestOutcome(TestResult result) {
		TestOutcome outcome = TestOutcome.newInstance("org.codehaus.cuanto", "test${wordGen.getCamelWords(3)}",
			wordGen.getSentence(2),	result)
		outcome.startedAt = new Date()
		outcome.finishedAt = new Date() + 1
		outcome.duration = outcome.finishedAt.time - outcome.startedAt.time
		outcome.owner = wordGen.getSentence(2)
		outcome.note = wordGen.getSentence(3)
		outcome.testOutput = wordGen.getSentence(10)
		return outcome
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