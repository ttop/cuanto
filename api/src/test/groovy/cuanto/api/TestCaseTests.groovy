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


public class TestCaseTests extends ApiTestBase {


	void testGetTestCase() {
		TestOutcome outcome = TestOutcome.newInstance("org.codehaus.cuanto", "testAddTestOutcome", "my parameters",
			TestResult.valueOf("Fail"))

		TestRun run = new TestRun(new Date())
		client.addTestRun(run)
		try {
			client.addTestOutcome(outcome, run)
			final TestCase expectedTestCase = outcome.testCase
			TestCase fetchedTestCase = client.getTestCase(expectedTestCase.packageName, outcome.testCase.testName,
				outcome.testCase.parameters)
			assertNotNull "TestCase", fetchedTestCase
			assertEquals expectedTestCase.packageName, fetchedTestCase.packageName
			assertEquals expectedTestCase.testName, fetchedTestCase.testName
			assertEquals expectedTestCase.parameters,  fetchedTestCase.parameters
		} finally {
			client.deleteTestRun run
		}
	}


	void testGetTestCaseParams() {
		TestOutcome outcome = TestOutcome.newInstance("org.codehaus.cuanto", "testAddTestOutcome", "(my, parameters)",
			TestResult.valueOf("Fail"))
		client.addTestOutcome(outcome)

		final TestCase expectedTestCase = outcome.testCase
		TestCase fetchedTestCase = client.getTestCase(expectedTestCase.packageName, outcome.testCase.testName,
			outcome.testCase.parameters);
		assertNotNull "TestCase", fetchedTestCase
		assertEquals expectedTestCase.packageName, fetchedTestCase.packageName
		assertEquals expectedTestCase.testName, fetchedTestCase.testName
		assertEquals expectedTestCase.parameters,  fetchedTestCase.parameters
	}


	void testGetTestCaseNoParams() {
		TestOutcome outcome = TestOutcome.newInstance("org.codehaus.cuanto", "testAddTestOutcome",
			TestResult.valueOf("Fail"))
		client.addTestOutcome(outcome)
		TestOutcome outcomeTwo = TestOutcome.newInstance("org.codehaus.cuanto", "testAddTestOutcome", "params",
			TestResult.valueOf("Fail"))
		client.addTestOutcome(outcomeTwo)

		final TestCase expectedTestCase = outcome.testCase
		TestCase fetchedTestCase = client.getTestCase(expectedTestCase.packageName, outcome.testCase.testName);
		assertNotNull "TestCase", fetchedTestCase
		assertEquals expectedTestCase.packageName, fetchedTestCase.packageName
		assertEquals expectedTestCase.testName, fetchedTestCase.testName
		assertEquals expectedTestCase.parameters,  fetchedTestCase.parameters
	}
}