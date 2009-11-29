package cuanto

import cuanto.test.TestObjects
import org.springframework.orm.hibernate3.HibernateSystemException


/**
 * Created by IntelliJ IDEA.
 * User: todd.wells
 * Date: May 18, 2008
 * Time: 10:23:11 AM
 *
 */
class ProjectTests extends GroovyTestCase {

	def dataService
	def initializationService
	def projectService
	def testResultService

	def fakes = new TestObjects()
	def testType;

	@Override
	void setUp() {
		initializationService.initializeAll()
		testType = TestType.findByName("JUnit")
		fakes.dataService = dataService
	}


	void testFindProjectByFullName() {
		def groupNames = ["aa", "bb", "cc"]
		def projectsPerGroup = 3
		def projects = []

		groupNames.each { groupName ->
			def group = fakes.getProjectGroup(groupName)
			dataService.saveDomainObject(group)
			1.upto(projectsPerGroup) {
				def proj = new Project(name: fakes.wordGen.getSentence(3), projectKey: fakes.getProjectKey(),
					projectGroup: group, 'testType': testType)
				dataService.saveDomainObject(proj)
				projects << proj
			}
		}

		projects.each { proj ->
			def foundProj = projectService.getProjectByFullName("${proj.projectGroup.name}/${proj.name}")
			assertNotNull foundProj
			assertEquals proj, foundProj
		}

		def proj = new Project(name: fakes.wordGen.getSentence(3), projectKey: fakes.getProjectKey(),
			'testType': testType)
		dataService.saveDomainObject(proj)

		def nullGroupProject = projectService.getProjectByFullName(proj.name)
		assertNotNull nullGroupProject
		assertEquals proj, nullGroupProject

		shouldFail(CuantoException) {
			projectService.getProjectByFullName(null)
		}

		shouldFail(CuantoException) {
			projectService.getProjectByFullName("")
		}

		shouldFail(CuantoException) {
			projectService.getProjectByFullName("foo/bar/baz")
		}
	}


	void testProjectGroup() {
		Project proj = new Project(name: fakes.wordGen.getSentence(3), projectKey: fakes.getProjectKey(),
			'testType': testType)
		def groupName = "Test Group"
		def group = fakes.getProjectGroup(groupName)
		dataService.saveDomainObject(group)
		proj.projectGroup = group
		dataService.saveDomainObject(proj)

		Project found = Project.findByProjectGroup(group)
		assertNotNull "Didn't find project", found
		assertEquals "Wrong group name", groupName, found.projectGroup.name

		def projs = dataService.getAllProjects()
		assertEquals 1, projs.size()
	}


	void testGetAllProjects() {
		def groupNames = ["b", "aa", "a", "e", "c", "d"]
		Collections.shuffle(groupNames)

		def projectsPerGroup = 3
		groupNames.each { group ->
			def projGroup = fakes.getProjectGroup(group)
			dataService.saveDomainObject(projGroup)
			1.upto(projectsPerGroup) {
				Project proj = new Project(name: fakes.wordGen.getSentence(3), projectKey: fakes.getProjectKey(),
					projectGroup: projGroup, 'testType': testType)
				dataService.saveDomainObject(proj)
			}
		}
		
		def allProjs = dataService.getAllProjects()
		def numProjs = groupNames.size() * projectsPerGroup
		assertEquals "Wrong number of groups", numProjs, allProjs.size()

		groupNames.sort()
		def groupIdx = 0
		def rev = 0

		0.upto(numProjs - 1) { idx ->
			if (rev == projectsPerGroup) {
				rev = 0
				groupIdx++
			}
			rev++
			assertEquals "Wrong group", groupNames[groupIdx], allProjs[idx].projectGroup.name
		}
	}

	void testGetAllGroups() {
		def groupNames = ["b", "aa", "a", "e", "c", "d"]
		Collections.shuffle(groupNames)

		def projectsPerGroup = 3
		groupNames.each { groupName ->
			def group = fakes.getProjectGroup(groupName)
			dataService.saveDomainObject(group)
			1.upto(projectsPerGroup) {
				Project proj = new Project(name: fakes.wordGen.getSentence(3), projectKey: fakes.getProjectKey(),
					projectGroup: group, 'testType': testType)
				dataService.saveDomainObject(proj)
			}
		}

		groupNames.sort()

		def fetchedGroups = dataService.getAllGroups()
		assertEquals groupNames.size(), fetchedGroups.size()

		0.upto(fetchedGroups.size() - 1) {
			assertEquals groupNames[it], fetchedGroups[it].name
		}

	}


	void testGetProjectByGroupAndName() {
		def groupNames = ["a", "b", "c"]
		def projectsPerGroup = 3
		def projects = []

		groupNames.each { groupName ->
			def group = fakes.getProjectGroup(groupName)
			dataService.saveDomainObject(group)
			1.upto(projectsPerGroup) {
				def proj = new Project(name: fakes.wordGen.getSentence(3), projectKey: fakes.getProjectKey(),
					projectGroup: group, 'testType': testType)
				dataService.saveDomainObject(proj)
				projects << proj
			}   
		}

		projects.each { proj ->
			def foundProj = dataService.getProject(proj.projectGroup.name, proj.name)
			assertNotNull proj
			assertNotNull foundProj
			assertEquals proj, foundProj
		}

		def proj = new Project(name: fakes.wordGen.getSentence(3), projectKey: fakes.getProjectKey(),
			'testType': testType)
		dataService.saveDomainObject(proj)

		def nullGroupProject = dataService.getProject(null, proj.name)
		assertNotNull nullGroupProject
		assertEquals proj, nullGroupProject

		shouldFail(CuantoException) {
			dataService.getProject(null, null)
		}

		shouldFail(CuantoException) {
			dataService.getProject("foobar", null)
		}

		shouldFail(CuantoException) {
			dataService.getProject(groupNames[0], null)
		}

		shouldFail(CuantoException) {
			dataService.getProject(groupNames[1], fakes.wordGen.getSentence(3))
		}

	}


	void testDataServiceDeleteProjectWithoutGroup() {
		def proj = fakes.project
		dataService.saveDomainObject(proj)

		assertEquals "Wrong number of projects", 1, Project.list().size()

		def testCases = []
		def numCases = 5
		1.upto(numCases) {
			def tc = fakes.getTestCase(proj)
			testCases << tc
			proj.addToTestCases(tc)
		}
		dataService.saveDomainObject proj

		def testRuns = []
		1.upto(3) {
			def tr = fakes.getTestRun(proj, "foobar")
			testRuns << tr
			proj.addToTestRuns(tr)
			dataService.saveDomainObject proj
			testCases.each { tc ->
				def to = fakes.getTestOutcome(tc, tr)
				tr.addToOutcomes(to)
			}
			//dataService.saveDomainObject tr
		}
		dataService.saveDomainObject proj, true

		assertEquals "Wrong number of projects", 1, Project.list().size()
		assertEquals "Wrong number of test outcomes", testRuns.size() * numCases, TestOutcome.list().size()
		assertEquals "Wrong number of test cases", numCases, TestCase.list().size()
		assertEquals "Wrong number of test runs", testRuns.size(), TestRun.list().size()

		projectService.deleteProject(proj)

		assertEquals "Wrong number of projects", 0, Project.list().size()
		assertEquals "Wrong number of test outcomes", 0, TestOutcome.list().size()
		assertEquals "Wrong number of test cases", 0, TestCase.list().size()
		assertEquals "Wrong number of test runs", 0, TestRun.list().size()
	}



	void testDataServiceDeleteProjectWithGroup() {
		ProjectGroup group = dataService.createProjectGroup(fakes.wordGen.getCamelWords(3))
		dataService.saveDomainObject(group)
		assertEquals "Wrong number of project groups", 1, ProjectGroup.list().size()

		def projects = []
		1.upto(2) {
			def proj = fakes.project
			proj.projectGroup = group
			dataService.saveDomainObject(proj)
			projects << proj
		}

		assertEquals "Wrong number of projects", 2, Project.list().size()
		dataService.deleteProject(projects[0])
		assertEquals "Wrong number of projects", 1, Project.list().size()
		assertEquals "Wrong number of project groups", 1, ProjectGroup.list().size()

		dataService.deleteProject(projects[1])
		assertEquals "Wrong number of projects", 0, Project.list().size()
		assertEquals "Wrong number of project groups", 0, ProjectGroup.list().size()
	}


	void testDataServiceDeleteNullProject() {
		dataService.deleteProject(null)
	}


	void testGetProjectsByGroup() {
		assertEquals "Wrong number of projects", 0, dataService.getProjectsByGroup(null).size()
		assertEquals "Wrong number of projects", 0, dataService.getProjectsByGroupName(null).size()
		assertEquals "Wrong number of projects", 0, dataService.getProjectsByGroupName("").size()

		def groupToFind = dataService.createProjectGroup(fakes.wordGen.getCamelWords(3))
		def otherGroup = dataService.createProjectGroup(fakes.wordGen.getCamelWords(3))
		def projectOne = fakes.project
		projectOne.projectGroup = otherGroup
		dataService.saveDomainObject(projectOne)

		assertEquals "Wrong number of projects", 0, dataService.getProjectsByGroup(groupToFind).size()
		assertEquals "Wrong number of projects", 0, dataService.getProjectsByGroupName(groupToFind.name).size()

		def projectTwo = fakes.project
		projectTwo.projectGroup = groupToFind
		dataService.saveDomainObject(projectTwo)

		assertEquals "Wrong number of projects", 1, dataService.getProjectsByGroup(groupToFind).size()
		assertEquals "Wrong number of projects", 1, dataService.getProjectsByGroupName(groupToFind.name).size()

		def projectThree = fakes.project
		projectThree.projectGroup = groupToFind
		dataService.saveDomainObject(projectThree)

		def foundProjects = dataService.getProjectsByGroup(groupToFind)
		assertEquals "Wrong number of projects", 2, foundProjects.size()
		[projectTwo, projectThree].each { proj ->
		    assertTrue "${proj.name} not found", foundProjects.contains(proj)
		}

		foundProjects = dataService.getProjectsByGroupName(groupToFind.name)
		assertEquals "Wrong number of projects", 2, foundProjects.size()
		[projectTwo, projectThree].each { proj ->
		    assertTrue "${proj.name} not found", foundProjects.contains(proj)
		}
	}


	void testGetProjectGroupsWithPrefix() {
		def expectedGroups = ["dog", "dogged", "doggy", "dowel", "snoop"]
		expectedGroups.each {
			dataService.createProjectGroup(it)
		}

		def groups = dataService.getProjectGroupsWithPrefix("d")
		assertEquals "Wrong groups", expectedGroups[0..3] as String[], groups as String[]

		groups = dataService.getProjectGroupsWithPrefix("dog")
		assertEquals "Wrong groups", expectedGroups[0..2] as String[], groups as String[]

		groups = dataService.getProjectGroupsWithPrefix("dogg")
		assertEquals "Wrong groups", expectedGroups[1..2] as String[], groups as String[]

		groups = dataService.getProjectGroupsWithPrefix("sn")
		assertEquals "Wrong groups", expectedGroups[4..4] as String[], groups as String[]
	}


	void testGetProjectGroupByName() {
		def groupNames = ["foo", "fie", "bar"]
		def groups = []
		groupNames.each {
			groups << dataService.createProjectGroup(it)
		}

		assertNull "Wrong project group for null", projectService.getProjectGroupByName(null)
		assertNull "Wrong project group for empty string", projectService.getProjectGroupByName("")
		assertEquals "Wrong project group", groups[0].id, projectService.getProjectGroupByName("foo").id
		assertEquals "Wrong project group", "blah", projectService.getProjectGroupByName("blah").name
		assertEquals "Wrong total number of groups", groupNames.size() + 1, ProjectGroup.list().size()

		groupNames << "blah"
		def fetchedGroupNames = projectService.getAllGroupNames()

		assertEquals "Wrong number of group names", groupNames.size(), fetchedGroupNames.size()
		groupNames.each {
			assertTrue "Couldn't find group $it", fetchedGroupNames.contains(it)
		}
	}


	void testCreateProject() {
		def msg = shouldFail(CuantoException) {
			projectService.createProject(null)
		}
		assertTrue "Wrong message: msg", msg.contains("Field error in object 'cuanto.Project'")

		shouldFail(CuantoException) {
			projectService.createProject([name: " "])
		}
		assertTrue "Wrong message: msg", msg.contains("Field error in object 'cuanto.Project'")

		def projParams = [ name: fakes.wordGen.getSentence(2),
			group: fakes.wordGen.getSentence(2),
			bugUrlPattern: "http://foo/{BUG}",
			projectKey: fakes.projectKey,
			testType: "JUnit"]

		def proj = projectService.createProject(projParams)
		assertNotNull "Project not created", proj
		assertEquals "Wrong name", projParams.name, proj.name
		assertEquals "Wrong group", projParams.group, proj.projectGroup.name
		assertEquals "Wrong bug pattern", projParams.bugUrlPattern, proj.bugUrlPattern
		assertEquals "Wrong test type", dataService.getTestType("JUnit"), proj.testType
	}


	void testCreateTestCaseErrorConditions() {
		assertNull "No test case should've been returned", projectService.createTestCase(null)
		assertEquals "No test case should've been created", 0, TestCase.list().size()

		assertNull "No test case should've been returned", projectService.createTestCase([:])
		assertEquals "No test case should've been created", 0, TestCase.list().size()

		def msg = shouldFail(HibernateSystemException) {
			def params = [project: "foo"]
			projectService.createTestCase(params)
		}
		assertTrue "Wrong error message", msg.contains("Provided id of the wrong type")

		msg = shouldFail(CuantoException) {
			def params = [project: 12345]
			projectService.createTestCase(params)
		}
		assertEquals "Wrong exception message", "Valid project not provided", msg

		def project = fakes.project
		dataService.saveDomainObject project

		def params =[:]
		params.project = project.id

		msg = shouldFail(CuantoException) {
			projectService.createTestCase(params)
		}
		assertEquals "Wrong message", "No test case name was provided", msg
	}


	void testCreateTestCase() {
		def project = fakes.project
		dataService.saveDomainObject project

		def params =[:]
		params.project = project.id
		params.testName = fakes.wordGen.getCamelWords(2)
		params.packageName = fakes.wordGen.getCamelWords(3)
		params.description = fakes.wordGen.getSentence(4)

		def testCase = projectService.createTestCase(params)
		assertNotNull "No test case created", testCase
		assertEquals "Wrong project", project, testCase.project
		assertEquals "Wrong test name", params.testName, testCase.testName
		assertEquals "Wrong package name", params.packageName, testCase.packageName
		assertEquals "Wrong description", params.description, testCase.description

		params.packageName = null
		testCase = projectService.createTestCase(params)
		assertNotNull "No test case created", testCase
		assertEquals "Wrong project", project, testCase.project
		assertEquals "Wrong test name", params.testName, testCase.testName
		assertNull "Wrong package name", testCase.packageName
		assertEquals "Wrong description", params.description, testCase.description

		params.packageName = "   "
		testCase = projectService.createTestCase(params)
		assertNotNull "No test case created", testCase
		assertEquals "Wrong project", project, testCase.project
		assertEquals "Wrong test name", params.testName, testCase.testName
		assertNull "Wrong package name", testCase.packageName
		assertEquals "Wrong description", params.description, testCase.description
	}


	void testGetTestCasesOnlyName() {

		assertEquals "No test cases should've been returned for null project", 0, projectService.getTestCases(null).size()

		def proj = fakes.project
		dataService.saveDomainObject(proj)
		assertEquals "Wrong number of projects", 1, Project.list().size()

		assertEquals "No test cases should've been returned", 0, projectService.getTestCases(proj).size()

		def testCaseNames = ["a", "b", "c", "d", "e"].reverse()
		def testCases = []
		testCaseNames.each { name ->
			def tc = new TestCase(testName: name)
			testCases << tc
			proj.addToTestCases(tc)
		}
		dataService.saveDomainObject proj

		def fetchedCases = projectService.getTestCases(proj)
		assertEquals "Wrong number of test cases", testCases.size(), fetchedCases.size()
		testCaseNames.reverse().eachWithIndex { name, indx ->
			assertEquals "Wrong title/order", name, fetchedCases[indx].testName
		}
	}


	void testGetTestCasesWithPackage() {

		assertEquals "No test cases should've been returned", 0, projectService.getTestCases(null).size()

		def proj = fakes.project
		dataService.saveDomainObject(proj)

		assertEquals "Wrong number of projects", 1, Project.list().size()

		def testPackageNames = ["a", "b", "c", "d", "e"].reverse()
		def testCases = []
		testPackageNames.each { name ->
			def tc = projectService.createTestCase([testName: fakes.wordGen.getCamelWords(2), packageName: name,
				project: proj.id])
			testCases << tc
		}
		dataService.saveDomainObject proj

		def fetchedCases = projectService.getTestCases(proj)
		assertEquals "Wrong number of test cases", testCases.size(), fetchedCases.size()
		testPackageNames.reverse().eachWithIndex { name, indx ->
			assertEquals "Wrong title/order", name, fetchedCases[indx].packageName
		}
	}
	



}
