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


	void addTestRunAndGetTestRun() {
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
		List expectedLinks = new ArrayList(testRun.links)
		List actualLinks = new ArrayList(createdTestRun.links)
		expectedLinks.eachWithIndex { it, idx ->
			assertEquals "Wrong link description", it.description, actualLinks[idx].description
			assertEquals "Wrong link url", it.url, actualLinks[idx].url
		}

		assertEquals "Wrong number of test properties", testRun.testProperties.size(), createdTestRun.testProperties.size()
		List expectedProps = new ArrayList(testRun.testProperties)
		List actualProps = new ArrayList(createdTestRun.testProperties)
		expectedProps.eachWithIndex { it, idx ->
			assertEquals "Wrong property name", it.name, actualProps[idx].name
			assertEquals "Wrong property value", it.value, actualProps[idx].value
		}
		
		assertEquals "Wrong project key", testRun.projectKey, createdTestRun.projectKey
	}


	void assertEquals(String message, Date expected, Date actual) {
		// Date should be within one second
		assertTrue message + ". Expected time ${expected.time}, actual time ${actual.time}",
			Math.abs(expected.time - actual.time) < 1000
	}
}