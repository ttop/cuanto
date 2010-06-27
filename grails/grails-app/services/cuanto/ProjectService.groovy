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

import cuanto.parsers.ParsableProject

class ProjectService {

	boolean transactional = false

	def dataService
	def testRunService

	def getProject(projectString) {
		def project = Project.findByProjectKey(projectString)
		if (!project) {
			project = getProjectByFullName(projectString)
		}
		//todo: fall back to ID?
		return project
	}


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
		if (project) {
			Project.withTransaction {
				def origGroup = project.projectGroup
				def testRuns = dataService.getTestRunsByProject(project)
				dataService.deleteTestCasesForProject(project)
				testRuns.each { testRun ->
					testRunService.deleteTestRun(testRun)
				}
				project.delete(flush: true)
				deleteProjectGroupIfUnused(origGroup)
			}
		}
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


	def createProject(Map params) {
		def parsedProject = new ParsableProject()
		parsedProject.bugUrlPattern = params?.bugUrlPattern
		parsedProject.projectGroup = getProjectGroupByName(params?.group)
		parsedProject.name = params?.name
		parsedProject.projectKey = params?.projectKey
		parsedProject.testType = params?.testType
		return createProject(parsedProject)
	}


	def createProject(ParsableProject project) {
		def newProj = new Project()
		Project.withTransaction { status ->
			newProj.bugUrlPattern = project?.bugUrlPattern
			newProj.projectGroup = getProjectGroupByName(project?.projectGroup)
			newProj.name = project?.name
			newProj.projectKey = project?.projectKey
			newProj.testType = dataService.getTestType(project?.testType)

			if (newProj.validate()) {
				dataService.saveDomainObject(newProj)
			} else {
				status.setRollbackOnly() // rollback any potential group creation if anything failed validation
				def err = ""
				newProj.errors.allErrors.each {
					err += "${it}\n"
				}
				throw new CuantoException(err)
			}
		}
		return newProj
	}


	def updateProject(params) {
		def project = Project.get(params.project)     // todo: refactor with new grails 1.1 validation?
		if (project) {
			def origGroup = project.projectGroup
			if (params.group != null) {
				project.projectGroup = getProjectGroupByName(params.group)
			}

			if (params.bugUrlPattern) {
				project.bugUrlPattern = params.bugUrlPattern
			}

			if (params.name) {
				project.name = params.name
			}

			if (params.projectKey) {
				project.projectKey = params.projectKey
			}

			if (params.testType) {
				project.testType = dataService.getTestType(params.testType)
			}

			if (project.projectGroup != origGroup) {
				deleteProjectGroupIfUnused(origGroup)
			}

			dataService.saveDomainObject(project)
			return project
		} else {
			throw new CuantoException("Project ${params.project} not found")
		}
	}


	def deleteProjectGroupIfUnused(group) {
		Project.withTransaction {
			if (group && Project.countByProjectGroup(group) == 0) {
				group.delete(flush: true)
			}
		}
	}


	def getSortedTestCases(project) {
		TestCase.findAllByProject(project, [sort: "fullName"])
	}


	def createTestCase(params) {
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
