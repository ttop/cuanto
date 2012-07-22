package cuanto
import cuanto.test.TestObjects
/**
 * Created by IntelliJ IDEA.
 * User: todd.wells
 * Date: May 18, 2008
 * Time: 10:23:11 AM
 *
 */
class ProjectTests extends GroovyTestCase {

	DataService dataService
	InitializationService initializationService
	ProjectService projectService

	TestObjects fakes = new TestObjects()
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

		assertEquals null, projectService.getProjectByFullName(null)
		assertEquals null, projectService.getProjectByFullName("")
		assertEquals null, projectService.getProjectByFullName("foo/bar/baz")
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
			assertNotNull foundProj
			assertEquals proj, foundProj
		}

		Project projectQueuedForDeletion = projects[0]
		projectQueuedForDeletion.deleted = true
		assertNull("Project queued for deletion shouldn't be returned",
			dataService.getProject(projectQueuedForDeletion.projectGroup?.name, projectQueuedForDeletion.name))

		def proj = new Project(name: fakes.wordGen.getSentence(3), projectKey: fakes.getProjectKey(),
			'testType': testType)
		dataService.saveDomainObject(proj)

		def nullGroupProject = dataService.getProject(null, proj.name)
		assertNotNull nullGroupProject
		assertEquals proj, nullGroupProject
		assertEquals null, dataService.getProject(null, null)
		assertEquals null, dataService.getProject("foobar", null)
		assertEquals null, dataService.getProject(groupNames[0], null)
		assertEquals null, dataService.getProject(groupNames[1], fakes.wordGen.getSentence(3))
	}


	void testDataServiceDeleteProjectWithoutGroup() {
        int originalProjectCount = Project.count()
        int originalTestRunCount = TestRun.count()
        int originalTestCaseCount = TestCase.count()
        int originalTestOutcomeCount = TestOutcome.count()
		def proj = fakes.project
		dataService.saveDomainObject(proj)

		assertEquals "Wrong number of projects", 1, Project.list().size()

		def testCases = []
		def numCases = 5
		1.upto(numCases) {
			def tc = fakes.getTestCase(proj)
			testCases << tc
			dataService.saveDomainObject tc
		}

		def testRuns = []
		1.upto(3) {
			def tr = fakes.getTestRun(proj)
			testRuns << tr
			dataService.saveDomainObject tr
			testCases.each { tc ->
				def to = fakes.getTestOutcome(tc, tr)
				dataService.saveDomainObject to 
			}
		}

		assertEquals "Wrong number of projects", originalProjectCount + 1, Project.list().size()
		assertEquals "Wrong number of test outcomes", originalTestOutcomeCount + testRuns.size() * numCases, TestOutcome.list().size()
		assertEquals "Wrong number of test cases", originalTestCaseCount + numCases, TestCase.list().size()
		assertEquals "Wrong number of test runs", originalTestRunCount + testRuns.size(), TestRun.list().size()

		projectService.deleteProject(proj)

		assertEquals "Wrong number of projects", originalProjectCount, Project.list().size()
		assertEquals "Wrong number of test outcomes", originalTestOutcomeCount, TestOutcome.list().size()
		assertEquals "Wrong number of test cases", originalTestCaseCount, TestCase.list().size()
		assertEquals "Wrong number of test runs", originalTestRunCount, TestRun.list().size()
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
		projectService.deleteProject(projects[0])
		assertEquals "Wrong number of projects", 1, Project.list().size()
		assertEquals "Wrong number of project groups", 1, ProjectGroup.list().size()

		projectService.deleteProject(projects[1])
		assertEquals "Wrong number of projects", 0, Project.list().size()
		assertEquals "Wrong number of project groups", 0, ProjectGroup.list().size()
	}


	void testDataServiceDeleteNullProject() {
		projectService.deleteProject(null)
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

	
	void testCreateTestCaseErrorConditions() {
        int originalTestCaseCount = TestCase.count()
		assertNull "No test case should've been returned", projectService.createTestCase(null)
		assertEquals "No test case should've been created", originalTestCaseCount, TestCase.list().size()

		assertNull "No test case should've been returned", projectService.createTestCase([:])
		assertEquals "No test case should've been created", originalTestCaseCount, TestCase.list().size()

		def msg = shouldFail(CuantoException) {
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

		assertEquals "No test cases should've been returned for null project", 0, projectService.getSortedTestCases(null).size()

		def proj = fakes.project
		dataService.saveDomainObject(proj)
		assertEquals "Wrong number of projects", 1, Project.list().size()

		assertEquals "No test cases should've been returned", 0, projectService.getSortedTestCases(proj).size()

		def testCaseNames = ["a", "b", "c", "d", "e"].reverse()
		def testCases = []
		testCaseNames.each { name ->
			def tc = new TestCase(testName: name, fullName: name, project: proj)
			testCases << tc
			dataService.saveDomainObject tc
		}

		def fetchedCases = projectService.getSortedTestCases(proj)
		assertEquals "Wrong number of test cases", testCases.size(), fetchedCases.size()
		testCaseNames.reverse().eachWithIndex { name, indx ->
			assertEquals "Wrong title/order", name, fetchedCases[indx].testName
		}
	}


	void testGetTestCasesWithPackage() {
		assertEquals "No test cases should've been returned", 0, projectService.getSortedTestCases(null).size()

		def proj = fakes.project
		dataService.saveDomainObject(proj)

		assertEquals "Wrong number of projects", 1, Project.list().size()

		def testPackageNames = ["a", "b", "c", "d", "e"].reverse()
		def testCases = []
		testPackageNames.each { pkgName ->
			def tName = fakes.wordGen.getCamelWords(2)
			def tc = projectService.createTestCase([testName: tName, packageName: pkgName,
				fullName: pkgName + "." + tName, project: proj.id])
			testCases << tc
			dataService.saveDomainObject tc
		}

		def fetchedCases = projectService.getSortedTestCases(proj)
		assertEquals "Wrong number of test cases", testCases.size(), fetchedCases.size()
		testPackageNames.reverse().eachWithIndex { name, indx ->
			assertEquals "Wrong title/order", name, fetchedCases[indx].packageName
		}
	}


	void testUpdateProjectAndChangeProjectGroup() {
		def projectGroups = []
		1.upto(2) {
			ProjectGroup group = dataService.createProjectGroup(fakes.wordGen.getCamelWords(3))
			dataService.saveDomainObject(group)
			projectGroups << group
		}
		assertEquals "Wrong number of project groups", 2, ProjectGroup.list().size()

		def projects = []
		1.upto(2) {
			def proj = fakes.project
			proj.projectGroup = projectGroups[0]
			dataService.saveDomainObject proj 
			projects << proj
		}

		def proj = fakes.project
		proj.projectGroup = projectGroups[1]
		dataService.saveDomainObject(proj)

		projectService.updateProject([group: "", project: projects[0].id])
		Project fetched = Project.get(projects[0].id)
		assertNull "Shouldn't have group", fetched.projectGroup
		assertEquals "Wrong number of project groups", 2, ProjectGroup.list().size()

		projectService.updateProject([group: projectGroups[1].name, project: projects[0].id])
		fetched = Project.get(projects[0].id)
		assertEquals "Wrong project group", projectGroups[1], fetched.projectGroup
		assertEquals "Wrong number of project groups", 2, ProjectGroup.list().size()

		projectService.updateProject([group: "", project: projects[0].id])
		fetched = Project.get(projects[0].id)
		assertNull "Shouldn't have group", fetched.projectGroup
		assertEquals "Wrong number of project groups", 2, ProjectGroup.list().size()

		projectService.updateProject([group: "", project: projects[1].id])
		fetched = Project.get(projects[1].id)
		assertNull "Shouldn't have group", fetched.projectGroup
		assertEquals "Wrong number of project groups", 1, ProjectGroup.list().size()

		projectService.updateProject([group: "", project: proj.id])
		fetched = Project.get(proj.id)
		assertNull "Shouldn't have group", fetched.projectGroup
		assertEquals "Wrong number of project groups", 0, ProjectGroup.list().size()
	}

	
	void testCreateProjectViaParams() {
		def params = [group: "Sample", bugUrlPattern: "http://bug{BUG}", name: fakes.wordGen.getCamelWords(3),
			projectKey: fakes.wordGen.getCamelWords(2), testType: "JUnit"]

		Project paramProj = projectService.createProject(params)

		try {
			Project fetched = projectService.getProject(params.projectKey)
			assertNotNull "Project not found", fetched
			assertEquals "Wrong project group", params.group, fetched.projectGroup?.name
			assertEquals "Wrong bug pattern", params.bugUrlPattern, fetched.bugUrlPattern
			assertEquals "Wrong name", params.name, fetched.name
			assertEquals "Wrong project key", params.projectKey, fetched.projectKey
			assertEquals "Wrong TestType", TestType.findByName("JUnit"), fetched.testType
		} finally {
			projectService.deleteProject(paramProj)
		}
	}
}
