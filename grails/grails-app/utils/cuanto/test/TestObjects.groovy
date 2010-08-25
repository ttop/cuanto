/*
 Copyright (c) 2008 thePlatform, Inc.

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

package cuanto.test
import cuanto.*

/**
 * User: Todd Wells
 * Date: May 5, 2008
 * Time: 6:08:28 PM
 * 
 */
class TestObjects {

	static cuanto.test.WordGenerator wordGen = new cuanto.test.WordGenerator()
	static Random rand = new Random()
	def dataService
	def testRunService

	static Set projKeys = new HashSet()

	TestCase getTestCase(Project proj) {
		def tc = new TestCase()
		tc.setTestName "test${wordGen.getCamelWords(3)}"
		tc.project = proj
		tc.packageName = "cuanto.test"
		tc.fullName = tc.packageName + "." + tc.testName
		tc.setDescription wordGen.getSentence(10)
		return tc
	}


	TestOutcome getTestOutcome(TestCase testCase, TestRun testRun) {
		def to = new TestOutcome()
		to.testCase = testCase
		to.setNote wordGen.getSentence(6)
		to.testRun = testRun
		to.testOutput = wordGen.getSentence(5)
		to.testResult = dataService.result("pass")
		to.analysisState = AnalysisState.findByName("Unanalyzed")
		return to
	}


	TestRun getTestRun(Project project){
		TestRun tr = new TestRun(project:project)
		tr.note = wordGen.getSentence(10)
		tr.valid = true
		tr.dateExecuted = new Date()
		return tr
	}

	
	TestRun getTestRun(String project){
		getTestRun(new Project(name:project, projectKey:wordGen.getCamelWords(1)))
	}


	Project getProject() {
		return new Project(name:wordGen.getWord(), projectKey: getProjectKey(), testType: TestType.findByName("JUnit"))
	}


	String getProjectKey(){
		String ky = wordGen.getCamelWords(2)
		if (ky.size() > 25) {
			ky = ky.substring(24)
		}

		if (projKeys.contains(ky)) {  // make sure it's unique
			ky = getProjectKey()
			projKeys += ky
		}
		return ky
	}

	
	ProjectGroup getProjectGroup(groupName) {
		return new ProjectGroup(name: groupName)
	}

	
	TestRunProperty getTestProperty() {
		return new TestRunProperty(wordGen.getCamelWords(3), wordGen.getSentence(3))
	}

	TestOutcomeProperty getTestOutcomeProperty() {
		return new TestOutcomeProperty(wordGen.getCamelWords(3), wordGen.getSentence(3))
	}

	TestOutcomeLink getTestOutcomeLink() {
		return new TestOutcomeLink("http://${wordGen.getCamelWords(3)}", wordGen.getSentence(3))
	}
}