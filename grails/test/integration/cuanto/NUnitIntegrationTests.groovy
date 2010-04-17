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

package cuanto

import cuanto.test.TestObjects


public class NUnitIntegrationTests extends GroovyTestCase {

	ParsingService parsingService
	DataService dataService
	InitializationService initializationService
	def testOutcomeService
	def testRunService
	def testResultService

	TestObjects fakes = new TestObjects()


	@Override
	void setUp() {
		initializationService.initializeAll()
		fakes.dataService = dataService
	}


	void testSubmitNunit() {
		def proj = fakes.project
		proj.testType = TestType.findByName("NUnit")
		dataService.saveDomainObject proj

		def testRun = fakes.getTestRun(proj)
		dataService.saveTestRun testRun

		getFile("NUnit-Compagny.Argos-Test-Result-French.xml").withInputStream {
			parsingService.parseFileFromStream(it, testRun.id)
		}

		assertEquals "Wrong number of TestOutcomes", 14, TestOutcome.count()
		assertEquals "Wrong number of failures", 1, TestOutcome.findAllByTestResult(dataService.result("Fail")).size()
	}


	File getFile(filename) {
		File file = new File("test/resources/${filename}")
		assertTrue("Couldn't find file: ${file.toString()}", file.exists())
		return file
	}

}