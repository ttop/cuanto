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

import cuanto.base.ApiTestBase


public class TestOutcomeTests extends ApiTestBase {

	static testCaseCounter = 1

    void testToggleAnalysisStateQuickly() {     // CUANTO-5
        TestRun run1 = new TestRun(new Date())
        client.addTestRun(run1)
        testRunsToCleanUp << run1

        TestOutcome outcome = TestOutcome.newInstance("org.codehaus.cuanto", "testAddTestOutcome", "my parameters",
                TestResult.valueOf("Fail"))
        outcome.analysisState = AnalysisState.Bug
        client.addTestOutcome(outcome, run1)

        List<Thread> threads = []
        Exception exception = null
        for (def i = 0; i < 10; i++) {
            [AnalysisState.Environment, AnalysisState.Other, AnalysisState.Bug].each {
                outcome.analysisState = it
                threads << Thread.start {
                    try {
                        client.updateTestOutcome(outcome)
                    } catch (Exception e) {
                        exception = e
                    }
                }
            }
        }
        threads*.join()
        assert exception == null
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
		testRunsToCleanUp << run

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
		testRunsToCleanUp << run

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
		testRunsToCleanUp << run

		Long outcomeId = client.addTestOutcome(outcome, run)
		client.addTestOutcome(outcomeTwo, run)
		TestOutcome fetchedOutcome = client.getTestOutcome(outcomeId)
		List<TestOutcome> outcomes = client.getTestCaseOutcomesForTestRun(fetchedOutcome.testCase, run)
		assertEquals "Wrong number of TestOutcomes", 2, outcomes.size()
		assertEquals "Wrong outcome first", outcomeTwo.id, outcomes[0].id
		assertEquals "Wrong outcome second", outcome.id, outcomes[1].id
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
		testRunsToCleanUp << run

		client.addTestOutcome(outcome, run)
		sleep 1000
		client.addTestOutcome(outcomeTwo, run)
		List<TestOutcome> outcomes = client.getTestOutcomesForTestRun(run, 0, 100, TestOutcome.Sort.FULL_NAME, "asc")
		assertEquals "Wrong number of TestOutcomes", 2, outcomes.size()
		assertEquals "Wrong outcome first", outcome.id, outcomes[0].id
		assertEquals "Wrong outcome second", outcomeTwo.id, outcomes[1].id
	}

	void testGetTestOutcomesForTestRunMany() {
		TestRun run = new TestRun(new Date())
		client.addTestRun(run)
		testRunsToCleanUp << run

		1.upto(202) {
			TestOutcome outcome = createTestOutcome(TestResult.Pass)
			client.addTestOutcome(outcome, run)
			sleep 100
		}
		List<TestOutcome> outcomes = client.getTestOutcomesForTestRun(run, 0, 100, TestOutcome.Sort.FULL_NAME, "asc")
		assertEquals "Wrong number of TestOutcomes", 100, outcomes.size()
		outcomes = client.getTestOutcomesForTestRun(run, 100, 100, TestOutcome.Sort.FULL_NAME, "asc")
		assertEquals "Wrong number of TestOutcomes", 100, outcomes.size()
		outcomes = client.getTestOutcomesForTestRun(run, 200, 100, TestOutcome.Sort.FULL_NAME, "asc")
		assertEquals "Wrong number of TestOutcomes", 2, outcomes.size()
	}


	void testCountTestOutcomesForTestRun() {
		TestRun run = new TestRun(new Date())
		client.addTestRun(run)
		testRunsToCleanUp << run

		def count = client.countTestOutcomesForTestRun(run)
		assertEquals "Wrong count", 0, count

		client.addTestOutcome(createTestOutcome(TestResult.Pass), run)
		count = client.countTestOutcomesForTestRun(run)
		assertEquals "Wrong count", 1, count

		1.upto(201) {
			TestOutcome outcome = createTestOutcome(TestResult.Pass)
			client.addTestOutcome(outcome, run)
			sleep(100)
		}

		count = client.countTestOutcomesForTestRun(run)
		assertEquals "Wrong count", 202, count
	}

	void testGetTestOutcomesForTestRunSorts() {
		TestRun run = new TestRun(new Date())
		client.addTestRun(run)
		testRunsToCleanUp << run

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


    void testTagsAndManyOutcomes() {
        def tags = []
        tags << wordGen.getCamelWords(2)
        tags << wordGen.getCamelWords(2)

        def outcomes = []
        1.upto(1000) {
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
		final tagOne = wordGen.getWord()
		outcome.addTag(tagOne)
		def tagTwo = wordGen.getWord()
		while (tagTwo == tagOne) {
			tagTwo = wordGen.getWord()
		}
		outcome.addTag(wordGen.getWord())
		assertEquals "tags", 2, outcome.tags?.size()

		TestRun run = new TestRun(new Date())
		client.addTestRun(run)
		testRunsToCleanUp << run

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
	}


	void testAddPassingTestOutcomeForFirstTime() {
		TestOutcome outcome = createTestOutcome(TestResult.Pass)

		TestRun run = new TestRun(new Date())
		client.addTestRun(run)
		testRunsToCleanUp << run

		Long outcomeId = client.addTestOutcome(outcome, run)
		TestOutcome fetchedOutcome = client.getTestOutcome(outcomeId)
		assertFalse "For the first pass, the failure status is not considered changed.",
			fetchedOutcome.isFailureStatusChanged
	}


	void testAddFailingTestOutcomeForFirstTime() {
		TestOutcome outcome = createTestOutcome(TestResult.Fail)

		TestRun run = new TestRun(new Date())
		client.addTestRun(run)
		testRunsToCleanUp << run

		Long outcomeId = client.addTestOutcome(outcome, run)
		TestOutcome fetchedOutcome = client.getTestOutcome(outcomeId)
		assertTrue "For the first fail, the failure status is considered changed.",
			fetchedOutcome.isFailureStatusChanged
	}


	void testAddConsecutivelyPassedTestOutcome() {
		TestOutcome outcome1 = createTestOutcome(TestResult.Pass)
		TestOutcome outcome2 = createTestOutcome(TestResult.Pass)

		TestRun run = new TestRun(new Date())
		client.addTestRun(run)
		testRunsToCleanUp << run

		Long firstOutcomeId = client.addTestOutcome(outcome1, run)
		Long outcomeId = client.addTestOutcome(outcome2, run)
		TestOutcome fetchedOutcome = client.getTestOutcome(outcomeId)
		assertFalse "For the consecutive pass, the failure status is not considered changed.",
			fetchedOutcome.isFailureStatusChanged
	}

	void testAddConsecutivelyFailedTestOutcome() {
		String testName = "test${wordGen.getCamelWords(3)}"

		TestOutcome outcome1 = createTestOutcome(TestResult.Fail, testName)
		TestOutcome outcome2 = createTestOutcome(TestResult.Fail, testName)
		outcome2.testCase = outcome1.testCase

		TestRun run1 = new TestRun(new Date())
		TestRun run2 = new TestRun(new Date())
		client.addTestRun(run1)
		client.addTestRun(run2)
		testRunsToCleanUp << run1
		testRunsToCleanUp << run2

		Long firstOutcomeId = client.addTestOutcome(outcome1, run1)
		Long outcomeId = client.addTestOutcome(outcome2, run2)
		TestOutcome fetchedOutcome = client.getTestOutcome(outcomeId)
		assertFalse "For the consecutive fail, the failure status is not considered changed.",
			fetchedOutcome.isFailureStatusChanged
	}

	void testAddNewlyFailedTestOutcome() {
		String testName = "test${wordGen.getCamelWords(3)}"

		TestOutcome outcome1 = createTestOutcome(TestResult.Pass, testName)
		TestOutcome outcome2 = createTestOutcome(TestResult.Fail, testName)
		outcome2.testCase = outcome1.testCase

		TestRun run1 = new TestRun(new Date())
		TestRun run2 = new TestRun(new Date())
		client.addTestRun(run1)
		client.addTestRun(run2)
		testRunsToCleanUp << run1
		testRunsToCleanUp << run2

		Long firstOutcomeId = client.addTestOutcome(outcome1, run1)
		Long secondOutcomeId = client.addTestOutcome(outcome2, run2)
		TestOutcome fetchedOutcome = client.getTestOutcome(secondOutcomeId)
		assertTrue "For the newly failed test outcome, the failure status is considered changed.",
			fetchedOutcome.isFailureStatusChanged
	}

	void testAddNewlyPassedTestOutcome() {
		String testName = "test${wordGen.getCamelWords(3)}"

		TestOutcome outcome1 = createTestOutcome(TestResult.Fail, testName)
		TestOutcome outcome2 = createTestOutcome(TestResult.Pass, testName)
		outcome2.testCase = outcome1.testCase

		TestRun run1 = new TestRun(new Date())
		TestRun run2 = new TestRun(new Date())
		client.addTestRun(run1)
		client.addTestRun(run2)
		testRunsToCleanUp << run1
		testRunsToCleanUp << run2

		Long firstOutcomeId = client.addTestOutcome(outcome1, run1)
		Long outcomeId = client.addTestOutcome(outcome2, run2)
		TestOutcome fetchedOutcome = client.getTestOutcome(outcomeId)
		assertTrue "For the newly passed test outcome, the failure status is considered changed.",
			fetchedOutcome.isFailureStatusChanged
	}


	void testOutcomeProperties() {
		String testName = "test${wordGen.getCamelWords(3)}"
		TestOutcome outcome1 = createTestOutcome(TestResult.Fail, testName)
		outcome1.addTestProperty("foo", "bar")

		TestRun run1 = new TestRun(new Date())

		client.addTestRun(run1)
		testRunsToCleanUp << run1

		Long firstOutcomeId = client.addTestOutcome(outcome1, run1)
		TestOutcome fetchedOutcome = client.getTestOutcome(firstOutcomeId)
		assertEquals "Wrong number of properties", 1, fetchedOutcome?.testProperties?.size()
		assertTrue "Property not found", fetchedOutcome.testProperties.keySet().contains("foo")
		assertEquals "Wrong property value", "bar", fetchedOutcome.testProperties["foo"]

		outcome1.addTestProperty("double", "rainbow")
		client.updateTestOutcome(outcome1)

		firstOutcomeId = client.addTestOutcome(outcome1, run1)
		fetchedOutcome = client.getTestOutcome(firstOutcomeId)
		assertEquals "Wrong number of properties", 2, fetchedOutcome?.testProperties?.size()
		assertTrue "Property not found", fetchedOutcome.testProperties.keySet().contains("foo")
		assertEquals "Wrong property value", "bar", fetchedOutcome.testProperties["foo"]
		assertTrue "Property not found", fetchedOutcome.testProperties.keySet().contains("double")
		assertEquals "Wrong property value", "rainbow", fetchedOutcome.testProperties["double"]
	}


	void testOutcomeLinks() {
		String testName = "test${wordGen.getCamelWords(3)}"
		TestOutcome outcome1 = createTestOutcome(TestResult.Fail, testName)
		outcome1.addLink("http://foobar", "FOOBAR")

		TestRun run1 = new TestRun(new Date())

		client.addTestRun(run1)
		testRunsToCleanUp << run1

		Long firstOutcomeId = client.addTestOutcome(outcome1, run1)
		TestOutcome fetchedOutcome = client.getTestOutcome(firstOutcomeId)
		assertEquals "Wrong number of links", 1, fetchedOutcome?.links?.size()
		assertTrue "Link not found", fetchedOutcome.links.keySet().contains("http://foobar")
		assertEquals "Link description", "FOOBAR", fetchedOutcome.links.get("http://foobar")

		outcome1.addLink("http://double/rainbow", "Almost a triple!")
		client.updateTestOutcome(outcome1)
		fetchedOutcome = client.getTestOutcome(firstOutcomeId)
		assertEquals "Wrong number of links", 2, fetchedOutcome?.links?.size()
		assertTrue "Link not found", fetchedOutcome.links.keySet().contains("http://foobar")
		assertEquals "Link description", "FOOBAR", fetchedOutcome.links.get("http://foobar")
		assertTrue "Link not found", fetchedOutcome.links.keySet().contains("http://double/rainbow")
		assertEquals "Link description", "Almost a triple!", fetchedOutcome.links.get("http://double/rainbow")
	}


	void testCreateTestOutcomeWithoutProject() {
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

		CuantoConnector projectLessClient = CuantoConnector.newInstance(CUANTO_URL)

		try {
			projectLessClient.addTestOutcome(outcome)
			fail "Expected exception not thrown"
		} catch (RuntimeException e) {
			assertTrue("Wrong error message returned by client: ${e.message}",
				e.message.contains("No projectKey parameter was specified"))
		}
	}



	TestOutcome createTestOutcome(TestResult result, String testName = "test${wordGen.getCamelWords(3)}") {
		TestOutcome outcome = TestOutcome.newInstance("org.codehaus.cuanto", testName, wordGen.getSentence(2), result)
		outcome.startedAt = new Date(System.currentTimeMillis() + 1000 * testCaseCounter)
		outcome.finishedAt = new Date(System.currentTimeMillis() + 2000 * testCaseCounter)
		outcome.duration = outcome.finishedAt.time - outcome.startedAt.time
		outcome.owner = wordGen.getSentence(2)
		outcome.note = wordGen.getSentence(3)
		outcome.testOutput = wordGen.getSentence(10)

		testCaseCounter++
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