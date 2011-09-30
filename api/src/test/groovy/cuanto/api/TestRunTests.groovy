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


public class TestRunTests extends ApiTestBase {


	void testAddTestRunAndGetTestRun() {
		TestRun testRun = new TestRun(new Date())
		try {
			testRun.note = "My note"
			testRun.addLink("http://foo", "FOO")
			testRun.addLink("http://bar", "BAR")
			testRun.addTestProperty("Radio", "KEXP")
			testRun.addTestProperty("Computer", "Apple")
			Long testRunId = client.addTestRun(testRun)
			TestRun createdTestRun = client.getTestRun(testRunId)
			assertNotNull "Wrong TestRun id", createdTestRun.id
			assertEquals "Wrong TestRun note", testRun.note, createdTestRun.note
			assertEquals "Wrong TestRun dateExecuted", testRun.dateExecuted, createdTestRun.dateExecuted
			assertNotNull "Wrong TestRun dateCreated", createdTestRun.dateCreated
			assertNotNull "Wrong TestRun lastUpdated", createdTestRun.lastUpdated
			assertEquals "Wrong number of links", testRun.links.size(), createdTestRun.links.size()
			testRun.links.keySet.each {url, descr ->
				assertTrue "Link not found: ${url}", createdTestRun.links.containsKey(url)
				assertEquals "Wrong link description", descr, createdTestRun.links[url]
			}
			testRun.testProperties.each {name, value ->
				assertTrue "TestProperty not found: ${name}", createdTestRun.testProperties.containsKey(name)
				assertEquals "Wrong value for TestProperty ${name}", value, createdTestRun.testProperties[name]

			}
			assertEquals "Wrong project key", testRun.projectKey, createdTestRun.projectKey
		} finally {
			client.deleteTestRun testRun
		}
	}


	void testUpdateTestRun() {
		TestRun testRun = new TestRun(new Date())
		try {
			testRun.note = "My note"
			testRun.addLink("http://foo", "FOO")
			testRun.addLink("http://bar", "BAR")
			testRun.addTestProperty("Radio", "KEXP")
			testRun.addTestProperty("Computer", "Apple")
			Long testRunId = client.addTestRun(testRun)
			testRun.note = "new note"
			testRun.addLink("http://foo", "UPDATED foo")
			testRun.addLink("http://blahblah", "Blah")
			testRun.deleteLink("http://bar")
			testRun.addTestProperty("Twin", "Peaks")
			testRun.addTestProperty("Computer", "Mac")
			testRun.deleteTestProperty("Radio")
			client.updateTestRun(testRun)
			TestRun updatedTestRun = client.getTestRun(testRunId)
			assertNotNull "Wrong TestRun id", updatedTestRun.id
			assertEquals "Wrong TestRun note", testRun.note, updatedTestRun.note
			assertEquals "Wrong TestRun dateExecuted", testRun.dateExecuted, updatedTestRun.dateExecuted
			assertNotNull "Wrong TestRun dateCreated", updatedTestRun.dateCreated
			assertNotNull "Wrong TestRun lastUpdated", updatedTestRun.lastUpdated
			assertEquals "Wrong number of links", testRun.links.size(), updatedTestRun.links.size()
			testRun.links.keySet.each {url, descr ->
				assertTrue "Link not found: ${url}", updatedTestRun.links.containsKey(url)
				assertEquals "Wrong link description", descr, updatedTestRun.links[url]
			}
			testRun.testProperties.each {name, value ->
				assertTrue "TestProperty not found: ${name}", updatedTestRun.testProperties.containsKey(name)
				assertEquals "Wrong value for TestProperty ${name}", value, updatedTestRun.testProperties[name]

			}
			assertEquals "Wrong project key", testRun.projectKey, updatedTestRun.projectKey
		} finally {
			client.deleteTestRun testRun
		}

	}


	void testGetTestRunsWithProperties() {
		WordGenerator wordGen = new WordGenerator()

		def testPropertyNames = []

		1.upto(4) {
			def word = wordGen.word
			while (testPropertyNames.contains(word)) {
				word = wordGen.word
			}
			testPropertyNames << word
		}

		def testRuns = []

		try {
			1.upto(5) {
				TestRun testRun = new TestRun(new Date() + it)
				testPropertyNames.each {propName ->
					testRun.addTestProperty(propName, wordGen.getSentence(2))
				}
				client.addTestRun(testRun)
				testRuns << testRun
			}
			String propOneName = testPropertyNames[0] as String
			String propOneValue = testRuns[0].testProperties[propOneName]
			testRuns[1].addTestProperty(propOneName, propOneValue)
			client.updateTestRun(testRuns[1] as TestRun)

			def props = [:]
			props[propOneName] = propOneValue
			List<TestRun> fetchedTestRuns = client.getTestRunsWithProperties(props)
			assertEquals "Wrong number of test runs returned", 2, fetchedTestRuns.size()
		} finally {
			testRuns.each {
				client.deleteTestRun it
			}
		}
	}


	void testGetAllTestRuns() {
		def testRuns = []

		def notes = ["first run", "second run", "third run"]
		def links = [["http://build/1": "build artifacts"], ["http://build/2": "build artifacts"], ["http://build/3": "build artifacts"]]
		def testProps = [["greeting": "Hello"], ["greeting": "Bonjour"], ["greeting": "Hola"]]

		try {
			1.upto(3) {
				TestRun testRun = new TestRun(new Date() + it)
				testRun.note = notes[it - 1]
				testRun.links = links[it - 1]
				testRun.testProperties = testProps[it - 1]
				sleep 500
				client.addTestRun(testRun)
				testRuns << testRun
			}

			List<TestRun> fetchedRuns = client.getAllTestRuns()
			assertTrue "Not enough TestRuns returned", fetchedRuns.size() >= testRuns.size()

			fetchedRuns.eachWithIndex { TestRun run, indx ->
				if (indx != testRuns.size() - 1) {
					assertTrue "TestRun was not in descending order", run.dateExecuted.time >= fetchedRuns[indx].dateExecuted.time
				}
			}
		} finally {
			testRuns.each {
				client.deleteTestRun it
			}
		}
	}


	void testCreateTestRunWithoutProject() {
		CuantoConnector projectLessClient = CuantoConnector.newInstance(CUANTO_URL)

		TestRun testRun = new TestRun(new Date())
		testRun.note = "My note"
		testRun.addLink("http://foo", "FOO")
		testRun.addLink("http://bar", "BAR")
		testRun.addTestProperty("Radio", "KEXP")
		testRun.addTestProperty("Computer", "Apple")

		try {
			projectLessClient.addTestRun(testRun)
			fail "Expected exception not thrown"
		} catch (RuntimeException e) {
			assertTrue("Wrong error message returned by client: ${e.message}",
				e.message.contains("No projectKey parameter was specified"))
		}
	}


	void testUpdateTestRunWithoutProject() {
		CuantoConnector projectLessClient = CuantoConnector.newInstance(CUANTO_URL)

		TestRun testRun = new TestRun(new Date())
		testRun.note = "My note"
		testRun.addLink("http://foo", "FOO")
		testRun.addLink("http://bar", "BAR")
		testRun.addTestProperty("Radio", "KEXP")
		testRun.addTestProperty("Computer", "Apple")

		try {
			projectLessClient.updateTestRun(testRun)
			fail "Expected exception not thrown"
		} catch (RuntimeException e) {
			assertTrue("Wrong error message returned by client: ${e.message}",
				e.message.contains("No projectKey parameter was specified"))
		}
	}


	void testGetInvalidTestRun() {
		def INVALID_TEST_RUN_ID = 999993425
		try {
			client.getTestRun(INVALID_TEST_RUN_ID)
			fail("Expected exception not thrown")
		} catch (RuntimeException e) {
			assertTrue("Wrong error message returned by client: ${e.message}",
				e.message.contains("TestRun ${INVALID_TEST_RUN_ID} not found."))
		}
	}


	void testGetAllTestRunsWithoutProject() {
		CuantoConnector projectLessClient = CuantoConnector.newInstance(CUANTO_URL)

		try {
			projectLessClient.getAllTestRuns()
			fail("Expected exception not thrown")
		} catch (RuntimeException e) {
			assertTrue("Wrong error message returned by client: ${e.message}",
				e.message.contains("No projectKey parameter was specified"))
		}
	}


	void testGetTestRunsWithPropertiesWithNoProject() {
		CuantoConnector projectLessClient = CuantoConnector.newInstance(CUANTO_URL)

		try {
			projectLessClient.getTestRunsWithProperties(["foo": "bar"])
			fail("Expected Exception not thrown")
		} catch (RuntimeException e) {
			assertTrue("Wrong error message returned by client: ${e.message}",
				e.message.contains("No projectKey parameter was specified"))
		}
	}


	void assertEquals(String message, Date expected, Date actual) {
		// Date should be within one second
		assertTrue message + ". Expected time ${expected.time}, actual time ${actual.time}",
			Math.abs(expected.time - actual.time) < 1000
	}
}