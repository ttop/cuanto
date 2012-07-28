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

package cuanto

import grails.util.GrailsUtil

class InitializationService {

	def grailsApplication
	def dataService
	boolean transactional = false

	void initTestResults() {
		def resultList = [
			new TestResult(name: "Pass", includeInCalculations: true, isFailure: false, isSkip: false),
			new TestResult(name: "Fail", includeInCalculations: true, isFailure: true, isSkip: false),
			new TestResult(name: "Error", includeInCalculations: true, isFailure: true, isSkip: false),
			new TestResult(name: "Ignore", includeInCalculations: false, isFailure: false, isSkip: false),
			new TestResult(name: "Skip", includeInCalculations: true, isFailure: false, isSkip: true),
			new TestResult(name: "Unexecuted", includeInCalculations: false, isFailure: false, isSkip: false)
		]

		resultList.each {TestResult result ->
			def dirty = false
			def fetchedResult = TestResult.findByName(result.name)
			if (fetchedResult) {
				["includeInCalculations", "isFailure", "isSkip"].each {
					if (fetchedResult.getProperty(it) != result.getProperty(it)) {
						fetchedResult.setProperty(it, result.getProperty(it))
						dirty = true
					}
				}

				if (dirty) {
					log.debug "Updating definition of TestResult ${fetchedResult.name}"
					dataService.saveDomainObject fetchedResult, true
					log.debug "Updated definition of TestResult ${fetchedResult.name}"
				} else {
					log.debug "Definition of TestResult ${fetchedResult.name} is current."
				}
			} else {
				log.debug "Creating definition for TestResult ${result.name}"
				dataService.saveDomainObject result, true
				log.debug "Created definition for TestResult ${result.name}"
			}
		}
	}


	void initAnalysisStates() {
		def existingAnalysisStates = AnalysisState.list()

		def analysisList = []
		analysisList << new AnalysisState(name: "Unanalyzed", isAnalyzed: false, isDefault: true, isBug: false)
		analysisList << new AnalysisState(name: "Bug", isAnalyzed: true, isDefault: false, isBug: true)
		analysisList << new AnalysisState(name: "Environment", isAnalyzed: true, isDefault: false, isBug: false)
		analysisList << new AnalysisState(name: "Harness", isAnalyzed: true, isDefault: false, isBug: false)
		analysisList << new AnalysisState(name: "No Repro", isAnalyzed: true, isDefault: false, isBug: false)
		analysisList << new AnalysisState(name: "Other", isAnalyzed: true, isDefault: false, isBug: false)
		analysisList << new AnalysisState(name: "Test Bug", isAnalyzed: true, isDefault: false, isBug: false)
		analysisList << new AnalysisState(name: "Investigate", isAnalyzed: false, isDefault: false, isBug: false)
		analysisList << new AnalysisState(name: "Quarantined", isAnalyzed: true, isDefault: false, isBug: false)

		if (existingAnalysisStates.size() < analysisList.size()) {
			analysisList.each {analysis ->
                def doesAnalysisExist = existingAnalysisStates.find { it.name == analysis.name }
                if (!doesAnalysisExist && !analysis.save()) {
					analysis.errors.allErrors.each {
						log.warning it.toString()
					}
				}
			}
		}
	}


	void initTestTypes() {
		if (TestType.list().size() <= 0) {
			def typeList = []

			typeList += new TestType(name: "JUnit")
			typeList += new TestType(name: "TestNG")
			typeList += new TestType(name: "NUnit")
			typeList += new TestType(name: "Manual")

			typeList.each {tp ->
				if (!tp.save()) {
					tp.errors.allErrors.each {
						log.warning it.toString()
					}
				}
			}
		}

		if (!TestType.findByNameIlike("NUnit")) {
			dataService.saveDomainObject(new TestType(name: "NUnit"))
		}
	}

	void initProjects() {
		if (GrailsUtil.environment == "development") {
			if (!Project.findByName("CuantoProd")) {
				def grp = new ProjectGroup(name: "Sample").save()
				new Project(name: "CuantoProd", projectKey: "CUANTO", projectGroup: grp,
					bugUrlPattern: "http://tpjira/browse/{BUG}", testType: TestType.findByName("JUnit")).save()
			}
			if (!Project.findByName("CuantoNG")) {
				def grp = ProjectGroup.findByName("Sample")
				new Project(name: "CuantoNG", projectKey: "CNG", projectGroup: grp,
					bugUrlPattern: "http://tpjira/browse/{BUG}", testType: TestType.findByName("TestNG")).save()
			}
			if (!Project.findByName("ClientTest")) {
				def grp = ProjectGroup.findByName("Sample")
				new Project(name: "ClientTest", projectKey: "ClientTest", projectGroup: grp,
					bugUrlPattern: "http://tpjira/browse/{BUG}", testType: TestType.findByName("TestNG")).save()
			}
			if (grailsApplication.config.dataSource.lotsOfExtraProjects)
				createLotsOfExtraProjects()
		}

		def projectsWithoutDeleteInitialized = Project.findAllByDeletedIsNull()
		projectsWithoutDeleteInitialized.each {
			it.deleted = false
			dataService.saveDomainObject(it, true)
		}
	}

	void createLotsOfExtraProjects() {
		def rnd = new Random()
		30.times { grpIndex ->
			def grp = new ProjectGroup(name: "Sample$grpIndex").save()
			(rnd.nextInt(9) + 1).times { prjIndex ->
				if (!Project.findByName("CuantoProd$grpIndex-$prjIndex")) {
					new Project(name: "CuantoProd$grpIndex-$prjIndex", projectKey: "CUANTO$grpIndex-$prjIndex", projectGroup: grp,
						bugUrlPattern: "http://tpjira/browse/{BUG}", testType: TestType.findByName("JUnit")).save()
				}
			}
		}

		50.times {
			// create ungrouped projects
			if (!Project.findByName("Ungrouped-$it")) {
				new Project(name: "Ungrouped-$it", projectKey: "Ungrouped-$it",
					bugUrlPattern: "http://tpjira/browse/{BUG}", testType: TestType.findByName("JUnit")).save()
			}
		}
	}

	void initializeAll() {
		initTestResults()
		initAnalysisStates()
		initTestTypes()
		initProjects()
	}
}
