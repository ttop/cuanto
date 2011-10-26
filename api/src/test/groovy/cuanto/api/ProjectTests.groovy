/*

Copyright (c) 2011 Todd Wells

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

import cuanto.base.TestBase

class ProjectTests extends TestBase {

	CuantoConnector client
	List<TestRun> testRunsToCleanUp
	static WordGenerator wordGen = new WordGenerator()
	public static CUANTO_URL = "http://localhost:8080/cuanto"


	@Override
	void setUp() {
		client = CuantoConnector.newInstance(CUANTO_URL)
	}


	void testAddAndDeleteProject() {

		// add a project
		final projectName = wordGen.getCamelWords(3)
		final projectGroup = "CuantoTest"
		final String projectKey = wordGen.getCamelWords(3)
		final bugUrlPattern = "http://foo/{BUG}"
		final testType = "JUnit"
		Project project = new Project(projectName, projectGroup, projectKey, bugUrlPattern, testType)
		
		Long projectId = client.addProject(project)

		// get project by ID
		Project fetchedProjectById = client.getProject(projectId)

		// get project by key
		Project fetchedProjectByKey = client.getProject(projectKey)
		
		assertEquals("Projects fetched by ID and projectKey are not equal", fetchedProjectById, fetchedProjectByKey)
		
		assertEquals("Wrong project name", projectName, fetchedProjectById.name)
		assertEquals("Wrong project group", projectGroup, fetchedProjectById.projectGroup)
		assertEquals("Wrong projectKey", projectKey, fetchedProjectById.projectKey)
		assertEquals("Wrong bugUrlPattern", bugUrlPattern, fetchedProjectById.bugUrlPattern)
		assertEquals("Wrong testType", testType, fetchedProjectById.testType)
		assertEquals("Wrong ID", projectId, fetchedProjectById.id)
		
		assertEquals("Wrong project name", projectName, fetchedProjectByKey.name)
		assertEquals("Wrong project group", projectGroup, fetchedProjectByKey.projectGroup)
		assertEquals("Wrong projectKey", projectKey, fetchedProjectByKey.projectKey)
		assertEquals("Wrong bugUrlPattern", bugUrlPattern, fetchedProjectByKey.bugUrlPattern)
		assertEquals("Wrong testType", testType, fetchedProjectByKey.testType)
		assertEquals("Wrong ID", projectId, fetchedProjectByKey.id)

		// delete the project
		client.deleteProject(projectId)

		// try fetching the deleted project
		try {
			client.getProject(projectId)
			fail "Expected exception not thrown when fetching deleted project via ID"
		} catch (RuntimeException e) {
			assertNotNull e.message
			assertTrue("Wrong exception message: ${e.message}",
				e.message?.contains("A Project matching the projectkey or id was not found."))
		}
		
		try {
			client.getProject(projectKey)
			fail "Expected exception not thrown when fetching deleted project via projectKey"
		} catch (RuntimeException e) {
			assertNotNull e.message
			assertTrue("Wrong exception message: ${e.message}",
				e.message?.contains("A Project matching the projectkey or id was not found."))
		}

		// rinse, repeat

		client.addProject(project)
		client.deleteProject(projectKey)

		try {
			client.getProject(projectId)
			fail "Expected exception not thrown when fetching deleted project via ID"
		} catch (RuntimeException e) {
			assertNotNull(e.message)
			assertTrue("Wrong exception message: ${e.message}",
				e.message?.contains("A Project matching the projectkey or id was not found."))
		}

		try {
			client.getProject(projectKey)
			fail "Expected exception not thrown when fetching deleted project via projectKey"
		} catch (RuntimeException e) {
			assertNotNull(e.message)
			assertTrue("Wrong exception message: ${e.message}",
				e.message?.contains("A Project matching the projectkey or id was not found."))
		}
	}


	void testDeleteProjectByNonExistentId() {
		try {
			client.deleteProject(9298379372L)
			fail "Exception not thrown for non-existent ID"
		} catch (RuntimeException e) {
			assertNotNull e.message
			assertTrue("Wrong exception message: ${e.message}",
				e.message?.contains("A Project matching the projectkey or id was not found."))
		}

	}


	void testGetAllProjects() {
		def allProjects = client.getAllProjects();
		allProjects.each { Project origProject ->
			assertEquals("Projects aren't equal", origProject, client.getProject(origProject.id))
		}
	}


	void testGetProjectsByGroup() {
		Map<String, List<Project>> projects = new HashMap<String, List<Project>>()

		// get all the projects and put them in a map by projectGroup name
		def allProjects = client.getAllProjects();
		allProjects.each { Project project ->
			if (project.projectGroup) {
				if (!projects.containsKey(project.projectGroup)) {
					projects[project.projectGroup] = []
				}
				projects[project.projectGroup] << project
			}
		}

		// fetch each projectGroup and verify that when calling client.getProjectsForGroup the correct number of projects
		// are returned with the correct names
		projects.keySet().each { groupName ->
			def groupProjects = client.getProjectsForGroup(groupName)
			assertEquals("Wrong number of projects returned for ${groupName}",
				projects[groupName].size(), groupProjects.size())

			projects[groupName].each { expectedProject ->
				assertTrue("Project ${expectedProject.name} wasn't found",
					groupProjects.contains(expectedProject))
			}
		}
	}


	void testGetProjectsByNullGroup() {
		try {
			client.getProjectsForGroup(null)
			fail "Exception not thrown"
		} catch (RuntimeException e) {
			assertTrue "Wrong exception message for null ProjectGroup: '${e.message}'",
				e.message?.contains("No projectGroup argument was provided.")
		}
	}


	void testGetProjectsByNonExistentGroup() {
		try {
			client.getProjectsForGroup("NonExistentGroup")
			fail "Exception not thrown"
		} catch (RuntimeException e) {
			assertTrue "Wrong exception message for non-existent Project Group: '${e.message}'",
				e.message.contains("No group by the name NonExistentGroup was found.")
		}
	}


	void testAddDeficientProjects() {
		Project project = new Project()
		try {
			client.addProject(project)
			fail "Exception should've been thrown for empty project"
		} catch (RuntimeException e) {
			assertTrue("Wrong exception message: ${e.message}",
				e.message.contains("Field error in object 'cuanto.Project' on field 'name': rejected value [null]"))
		}

		project.name = wordGen.getCamelWords(3)

		try {
			client.addProject(project)
			fail "Exception should've been thrown for empty project"
		} catch (RuntimeException e) {
			assertTrue("Wrong exception message: ${e.message}",
				e.message.contains("Field error in object 'cuanto.Project' on field 'projectKey': rejected value [null]"))
		}

		project.projectKey = wordGen.getCamelWords(3)

		try {
			client.addProject(project)
			fail "Exception should've been thrown for empty project"
		} catch (RuntimeException e) {
			assertTrue("Wrong exception message: ${e.message}",
				e.message.contains("Field error in object 'cuanto.Project' on field 'testType': rejected value [null]"))
		}

		project.testType = "Foobar"

		try {
			client.addProject(project)
			fail "Exception should've been thrown for empty project"
		} catch (RuntimeException e) {
			assertTrue("Wrong exception message: ${e.message}",
				e.message.contains("Field error in object 'cuanto.Project' on field 'testType': rejected value [null]"))
		}

		project.testType = "TestNG"

		client.addProject(project)
		client.deleteProject(project.projectKey)
	}
}
