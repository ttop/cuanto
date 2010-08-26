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



public class AntTaskTests extends ApiTestBase {


	void testSubmitSingleSuite() {
		TestRun testRun = new TestRun(new Date())
		client.addTestRun(testRun)
		File fileToSubmit = getFile("junitReport_single_suite.xml")
		client.importTestFile(fileToSubmit, testRun)
	}


	void testSubmitMultipleSuite() {
		TestRun testRun = new TestRun(new Date())
		client.addTestRun(testRun)
		File fileToSubmit = getFile("junitReport_multiple_suite.xml")
		client.importTestFile(fileToSubmit, testRun)
	}


	void testSubmitMultipleFiles() {
		TestRun testRun = new TestRun(new Date())
		def filesToSubmit = []
		filesToSubmit << getFile("junitReport_single_suite.xml")
		filesToSubmit << getFile("junitReport_single_suite_2.xml")
		client.importTestFiles(filesToSubmit, testRun)
	}


	File getFile(String filename) {
		def path = "grails/test/resources"
		File myFile = new File("${path}/${filename}")
		assertTrue "file not found: ${myFile.absoluteFile}", myFile.exists()
		return myFile
	}

}