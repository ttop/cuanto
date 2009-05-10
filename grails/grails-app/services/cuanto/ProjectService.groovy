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
	def testCaseFormatterRegistry


	def getProjectByFullName(String fullName) throws CuantoException {
		def groupName, projectName
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
			dataService.deleteTestRun(testRun)
		}
        dataService.deleteTestCasesForProject(project)
		dataService.deleteProject(project)
	}


	def saveProject(project) {
		if (project.validate()) {
			dataService.saveDomainObject(project)
		}
	}


	def createProject(params) {
		def project = new Project()
		Project.withTransaction {status ->
			if (params.bugUrlPattern) {
				project.bugUrlPattern = params.bugUrlPattern
			}

			if (params.group) {
				def grp = dataService.findProjectGroupByName(params.group)
				if (!grp) {
					grp = dataService.createProjectGroup(params.group)
				}
				project.projectGroup = grp
			}

			if (params.name) {
				project.name = params.name
			}

			if (params.projectKey) {
				project.projectKey = params.projectKey
			}			

			if (params.tcFormat) {
				project.testCaseFormatKey = testCaseFormatterRegistry.getFormatMap()[params.tcFormat].getKey()
			}

			if (params.testType) {
				project.testType = dataService.getTestType(params.testType)
			}

			if (project.validate()) {
				dataService.saveDomainObject(project)
			} else {
				status.setRollbackOnly() // rollback any potential group creation if anything failed validation
			}
		}
		return project
	}


	def getTestCases(project) {
		def testCases = project.testCases
		Collections.sort(testCases)
		return testCases  
	}


	def createTestCase(params){
		def project = dataService.getProject(params.project)
		def tc = new TestCase(testName: params.testName)

		if (params.packageName) {
			tc.packageName = params.packageName
			tc.fullName = tc.packageName + "." + tc.testName
		} else {
			tc.fullName = tc.testName
		}

		if (params.description) {
			tc.description = params.description
		}
		dataService.addTestCases(project, [tc])
	}

	def getAllGroupNames() {
		def groupNames = dataService.getAllGroups().collect { grp ->
			grp.name
		}
		return groupNames
	}
}
