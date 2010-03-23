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


public class TestRunTests extends GroovyTestCase {

	CuantoConnector client

	void setUp() {
		client = CuantoConnector.newInstance("http://localhost:8080/cuanto", "ClientTest")
	}


	void testAddTestRunAndGetTestRun() {
		TestRun testRun = new TestRun(new Date())
		testRun.note = "My note"
		testRun.addLink("http://foo", "FOO")
		testRun.addLink("http://bar", "BAR")
		testRun.addTestProperty("Radio", "KEXP");
		testRun.addTestProperty("Computer", "Apple");
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
	}


	void assertEquals(String message, Date expected, Date actual) {
		// Date should be within one second
		assertTrue message + ". Expected time ${expected.time}, actual time ${actual.time}",
			Math.abs(expected.time - actual.time) < 1000
	}
}