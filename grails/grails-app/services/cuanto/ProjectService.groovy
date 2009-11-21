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

import cuanto.CuantoException
import cuanto.Project
import cuanto.TestCase

class ProjectService {

	boolean transactional = false

	def dataService


	def getProjectByFullName(String fullName) throws CuantoException {
		def groupName = null
		def projectName = null
		if (fullName) {
			def parts = fullName.tokenize("/")
			if (parts.size() == 2) {
				groupName = parts[0]
				projectName = parts[1]
			} else if (parts.size() == 1) {
				groupName = null
				projectName = parts[0]
			}
		}
		dataService.getProject(groupName, projectName)
	}


	void deleteProject(Project project) {
		def testRuns = dataService.getTestRunsByProject(project)

		testRuns.each { testRun ->
			dataService.deleteOutcomesForTestRun(testRun)
		}
		dataService.deleteTestRunsForProject(project)
        dataService.deleteTestCasesForProject(project)
		dataService.deleteProject(project)
	}


	def getProjectGroupByName(groupName) {
		def groupToReturn = null
		if (groupName && groupName.trim() != "") {
			groupToReturn = dataService.findProjectGroupByName(groupName)
			if (!groupToReturn) {
				groupToReturn = dataService.createProjectGroup(groupName)
			}
		}
		return groupToReturn
	}
	

	def createProject(params) {
		def project = new Project()
		Project.withTransaction {status ->
			project.bugUrlPattern = params?.bugUrlPattern
			project.projectGroup = getProjectGroupByName(params?.group)
			project.name = params?.name
			project.projectKey = params?.projectKey
			project.testType = dataService.getTestType(params?.testType)

			if (project.validate()) {
				dataService.saveDomainObject(project)
			} else {
				status.setRollbackOnly() // rollback any potential group creation if anything failed validation
				def err = ""
				project.errors.allErrors.each {
					err += "${it}\n"
				}
				throw new CuantoException(err)
			}
		}
		return project
	}


	def getTestCases(project) {
		def cases = []
		if (project && project.testCases) {
			cases = project.testCases
			Collections.sort(cases)
		}
		return cases  
	}


	def createTestCase(params){
		def tc = null
		if (params) {
			def project = dataService.getProject(params.project)
			if (project) {

				if (params.testName) {
					tc = new TestCase(testName: params.testName)

					if (params.packageName?.trim()) {
						tc.packageName = params.packageName.trim()
						tc.fullName = tc.packageName + "." + tc.testName
					} else {
						tc.fullName = tc.testName
					}

					tc.description = params.description
					dataService.addTestCases(project, [tc])
				} else {
					throw new CuantoException("No test case name was provided") 
				}
			} else {
				throw new CuantoException("Valid project not provided")
			}
		}
		return tc
	}

	def getAllGroupNames() {
		def groupNames = dataService.getAllGroups().collect { grp ->
			grp.name
		}
		return groupNames
	}
}
