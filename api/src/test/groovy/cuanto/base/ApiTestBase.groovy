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

package cuanto.base

import cuanto.api.CuantoConnector
import cuanto.api.TestRun
import cuanto.api.WordGenerator

class ApiTestBase extends GroovyTestCase {
	CuantoConnector client
	List<TestRun> testRunsToCleanUp
	static WordGenerator wordGen = new WordGenerator()

	@Override
	void setUp() {
		super.setUp()
		//client = CuantoConnector.newInstance("http://localhost:8080/cuanto", "ClientTest")
		client = CuantoConnector.newInstance("http://172.16.150.1:8080/cuanto",
			"ClientTest", "192.168.125.128", 8888)

		testRunsToCleanUp = []
	}

	@Override
	void tearDown() {
		testRunsToCleanUp.each {
			client.deleteTestRun it
		}
		super.tearDown()
	}

}
