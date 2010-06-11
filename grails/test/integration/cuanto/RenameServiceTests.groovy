package cuanto

import cuanto.test.TestObjects
import org.apache.commons.lang.RandomStringUtils

class RenameServiceTests extends GroovyTestCase
{
	def renameService
	def dataService
	def testRunService

	TestObjects testObjectGenerator

	@Override
	void setUp()
	{
		super.setUp()
		testObjectGenerator = new TestObjects(dataService: dataService, testRunService: testRunService)
	}

	void testRenameToExistingPackage()
	{
		// create a project with a few test cases with a common package
		def project = testObjectGenerator.getProject().save()
		def testCasesToMove = (0..3).collect { testObjectGenerator.getTestCase(project).save() } as List<TestCase>

		// create a test case that lives in a package to which all the other test cases will be moved
		def commonPackageName = testCasesToMove[0].packageName
		def testCaseWithTargetPackage = testObjectGenerator.getTestCase(project)
		testCaseWithTargetPackage.packageName = commonPackageName + '.foo'
		testCaseWithTargetPackage.fullName = testCaseWithTargetPackage.packageName + '.' +
			testCaseWithTargetPackage.testName
		testCaseWithTargetPackage.save()

		// create a test case that will be untouched
		def untouchableTestCase = testObjectGenerator.getTestCase(project)
		untouchableTestCase.packageName = commonPackageName + '.bar'
		untouchableTestCase.fullName = untouchableTestCase.packageName + '.' +
			untouchableTestCase.testName
		untouchableTestCase.save()

		// create some test outcomes
		def testRun = testObjectGenerator.getTestRun(project).save()
		(testCasesToMove + [testCaseWithTargetPackage, untouchableTestCase]).each { TestCase testCase ->
			testObjectGenerator.getTestOutcome(testCase, testRun).save()
		}

		// replace the common package with the existing package
		renameService.renameAllPackages(project, commonPackageName, testCaseWithTargetPackage.packageName)

		// verify package/fullName
		def retrievedTestCaseWithTargetPackage = TestCase.get(testCaseWithTargetPackage.id)
		def untouchedTestCase = TestCase.get(untouchableTestCase.id)
		def movedTestCases = testCasesToMove.collect { TestCase.get(it.id) } as List<TestCase>

		assertEquals testCaseWithTargetPackage.packageName, retrievedTestCaseWithTargetPackage.packageName
		assertEquals testCaseWithTargetPackage.fullName, retrievedTestCaseWithTargetPackage.fullName
		assertEquals untouchableTestCase.packageName, untouchedTestCase.packageName
		assertEquals untouchableTestCase.fullName, untouchedTestCase.fullName

		movedTestCases.each { TestCase movedTestCase ->
			assertEquals(testCaseWithTargetPackage.packageName, movedTestCase.packageName)
			assertEquals(testCaseWithTargetPackage.packageName + "." + movedTestCase.testName, movedTestCase.fullName)
		}
	}

	void testRenameToNewPackage()
	{
		// create a project with a few test cases with a common package
		def project = testObjectGenerator.getProject().save()
		def testCasesToMove = (0..3).collect { testObjectGenerator.getTestCase(project).save() } as List<TestCase>

		// create a test case that lives in a package to which all the other test cases will be moved
		def commonPackageName = testCasesToMove[0].packageName

		// create a test case that will be untouched
		def untouchableTestCase = testObjectGenerator.getTestCase(project)
		untouchableTestCase.packageName = commonPackageName + '.bar'
		untouchableTestCase.fullName = untouchableTestCase.packageName + '.' +
			untouchableTestCase.testName
		untouchableTestCase.save()

		// create some test outcomes
		def testRun = testObjectGenerator.getTestRun(project).save()
		(testCasesToMove + [untouchableTestCase]).each { TestCase testCase ->
			testObjectGenerator.getTestOutcome(testCase, testRun).save()
		}

		// replace the common package with a new package
		def newPackage = "new.package.name." + RandomStringUtils.randomAlphabetic(5)
		renameService.renameAllPackages(project, commonPackageName, newPackage)

		// verify package/fullName
		def untouchedTestCase = TestCase.get(untouchableTestCase.id)
		def movedTestCases = testCasesToMove.collect { TestCase.get(it.id) } as List<TestCase>

		assertEquals untouchableTestCase.packageName, untouchedTestCase.packageName
		assertEquals untouchableTestCase.fullName, untouchedTestCase.fullName

		movedTestCases.each { TestCase movedTestCase ->
			assertEquals(newPackage, movedTestCase.packageName)
			assertEquals(newPackage + "." + movedTestCase.testName, movedTestCase.fullName)
		}
	}
}
