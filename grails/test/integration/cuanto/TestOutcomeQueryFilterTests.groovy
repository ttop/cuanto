package cuanto

import cuanto.test.TestObjects

/**
 * User: Todd Wells
 * Date: Mar 9, 2010
 * Time: 4:03:02 PM
 * 
 */

public class TestOutcomeQueryFilterTests extends GroovyTestCase {

	DataService dataService
	Project project

	TestObjects testObjects = new TestObjects();


	void setUp() {
		project = testObjects.project;
		dataService.saveDomainObject project
		testObjects.dataService = dataService
	}

	void testFilterByTestRun() {
		def testCases = []
		1.upto(3) {
			def tc = testObjects.getTestCase(project)
			dataService.saveDomainObject tc
			testCases << tc
		}

		// create outcomes without TestRun, one for a testcase used by the test run, one that isn't
		[0, 2].each {
			def outcomeWithoutTestRun = testObjects.getTestOutcome(testCases[it], null)
			dataService.saveDomainObject outcomeWithoutTestRun
		}

		// create two outcomes for TestRun A, one for a testcase used by the test run, one that isn't
		def testRunA = testObjects.getTestRun(project)
		dataService.saveDomainObject testRunA

		def outcomesA = []
		0.upto(1) {
			def outcome = testObjects.getTestOutcome(testCases[it], testRunA)
			dataService.saveDomainObject outcome
			outcomesA << outcome
		}

		// create two outcomes for TestRun B
		def testRunB = testObjects.getTestRun(project)
		dataService.saveDomainObject testRunB

		[0,2].each {
			def outcome = testObjects.getTestOutcome(testCases[it], testRunB)
			dataService.saveDomainObject outcome
		}

		// create QueryFilter for TestRun A
		TestOutcomeQueryFilter queryFilterA = new TestOutcomeQueryFilter()
		queryFilterA.testRun = testRunA

		def fetchedOutcomes = dataService.getTestOutcomes(queryFilterA)
		assertEquals "Wrong number of outcomes", outcomesA.size(), fetchedOutcomes.size()
		assertNotNull "Outcome 1 not found", fetchedOutcomes.find { it.id == outcomesA[0].id }
		assertNotNull "Outcome 2 not found", fetchedOutcomes.find { it.id == outcomesA[1].id }
		assertEquals "Outcome 1", outcomesA[0], fetchedOutcomes[0]
		assertEquals "Outcome 2", outcomesA[1], fetchedOutcomes[1]

		queryFilterA.sorts = [new SortParameters(sort:"testCase.fullName", sortOrder: "asc")]
		fetchedOutcomes = dataService.getTestOutcomes(queryFilterA)
		assertTrue "Wrong sort sortOrder", fetchedOutcomes[0].testCase.fullName < fetchedOutcomes[1].testCase.fullName

		queryFilterA.sorts = [new SortParameters(sort:"testCase.fullName", sortOrder: "desc")]
		fetchedOutcomes = dataService.getTestOutcomes(queryFilterA)
		assertTrue "Wrong sort sortOrder", fetchedOutcomes[0].testCase.fullName > fetchedOutcomes[1].testCase.fullName


		// todo: verify QueryFilter returns only outcomes for A
		// todo: create QueryFilter for TestRun B
		// todo: verify QueryFilter returns only outcomes for B
	}


    void testFilterByTagsAndTestRun() {
        def testCases = []
        def numOutcomes = 10
        0.upto(numOutcomes - 1) {
            def tc = testObjects.getTestCase(project)
            dataService.saveDomainObject tc
            testCases << tc
        }

        def outcomes = []
        def testRun = testObjects.getTestRun(project)
        dataService.saveTestRun testRun
        
        testCases.each {
            outcomes << testObjects.getTestOutcome(it, testRun)
        }

        def tags = []
        ["john", "paul", "george", "ringo"].each {
            tags << new Tag(name: it)
        }
        tags.each {
            dataService.saveDomainObject it
        }
        
        outcomes[0].addToTags(tags[0])
        outcomes[1].addToTags(tags[1])
        outcomes[2].addToTags(tags[2])
        outcomes[3].addToTags(tags[3])

        outcomes[4].addToTags(tags[0])
        outcomes[4].addToTags(tags[1])

        outcomes[5].addToTags(tags[2])
        outcomes[5].addToTags(tags[3])

        outcomes[6].addToTags(tags[0])
        outcomes[6].addToTags(tags[1])
        outcomes[6].addToTags(tags[2])
        outcomes[6].addToTags(tags[3])

        outcomes.each {
            dataService.saveDomainObject it, true
        }

        TestOutcomeQueryFilter queryFilterA = new TestOutcomeQueryFilter()
        queryFilterA.testRun = testRun
        queryFilterA.tags = tags.collect{it.name}

        def fetchedOutcomes = dataService.getTestOutcomes(queryFilterA)
        assertEquals "Wrong number of TestOutcomes returned", 7, fetchedOutcomes.size()

        TestOutcomeQueryFilter queryFilterB = new TestOutcomeQueryFilter()
        queryFilterB.testRun = testRun
        queryFilterB.tags = tags[0..1].collect{ it.name }
        fetchedOutcomes = dataService.getTestOutcomes(queryFilterB)
        assertEquals "Wrong number of TestOutcomes returned", 4, fetchedOutcomes.size()

        fetchedOutcomes.each { outcome ->
            assertNotNull "Couldnt find a matching tag", outcome.tags.find {it.name == tags[0].name} || outcome.tags.find {it.name == tags[1].name}
        }

        // now do all uppercase, tags should still be found
        queryFilterB.tags = tags[0..1].collect{ it.name.toUpperCase() }
        fetchedOutcomes = dataService.getTestOutcomes(queryFilterB)
        assertEquals "Wrong number of TestOutcomes returned", 4, fetchedOutcomes.size()

        fetchedOutcomes.each { outcome ->
            assertNotNull "Couldnt find a matching tag", outcome.tags.find {it.name == tags[0].name} || outcome.tags.find {it.name == tags[1].name}
        }

        // now search for TestOutcomes without any tags
        TestOutcomeQueryFilter queryFilterC = new TestOutcomeQueryFilter()
        queryFilterC.testRun = testRun
        queryFilterC.hasTags = false
        fetchedOutcomes = dataService.getTestOutcomes(queryFilterC)
        assertEquals "Wrong number of TestOutcomes returned", 3, fetchedOutcomes.size()

        queryFilterC.hasTags = true
        fetchedOutcomes = dataService.getTestOutcomes(queryFilterC)
        assertEquals "Wrong number of TestOutcomes returned", 7, fetchedOutcomes.size()
    }


	void testFilterByTestOutcomeProperties() {
		def testCases = []
		def numOutcomes = 10
		0.upto(numOutcomes - 1) {
		    def tc = testObjects.getTestCase(project)
		    dataService.saveDomainObject tc
		    testCases << tc
		}

		def outcomes = []
		def testRun = testObjects.getTestRun(project)
		dataService.saveTestRun testRun

		testCases.each {
		    outcomes << testObjects.getTestOutcome(it, testRun)
		}
		
		Tag coolTag = new Tag(name: "cool")
		dataService.saveDomainObject(coolTag)
	
		outcomes[0].addToTestProperties(new TestOutcomeProperty("john", "lennon"))
		outcomes[0].addToTags(coolTag)

		outcomes[1].addToTestProperties(new TestOutcomeProperty("john", "lennon"))
		outcomes[1].addToTestProperties(new TestOutcomeProperty("paul", "mccartney"))
		outcomes[1].addToTags(coolTag)

		outcomes[2].addToTestProperties(new TestOutcomeProperty("john", "lennon"))
		outcomes[2].addToTestProperties(new TestOutcomeProperty("paul", "mccartney"))
		outcomes[2].addToTestProperties(new TestOutcomeProperty("george", "harrison"))

		outcomes[3].addToTestProperties(new TestOutcomeProperty("john", "lennon"))
		outcomes[3].addToTestProperties(new TestOutcomeProperty("george", "harrison"))
		outcomes[3].addToTags(coolTag)

		outcomes.each {
			dataService.saveDomainObject it, true
		}

		def totalProps = TestOutcomeProperty.count()
		println "*********** ${totalProps} total TestProperties" 

		TestOutcomeQueryFilter queryFilterA = new TestOutcomeQueryFilter()
		queryFilterA.testRun = testRun
		queryFilterA.hasAllTestOutcomeProperties = [new TestOutcomeProperty("john", "lennon")]

		def fetchedOutcomes = dataService.getTestOutcomes(queryFilterA)
		assertEquals "Wrong number of TestOutcomes returned", 4, fetchedOutcomes.size()


		
		TestOutcomeQueryFilter queryfilterB = new TestOutcomeQueryFilter()
		queryfilterB.testRun = testRun
		queryfilterB.hasAllTestOutcomeProperties = [new TestOutcomeProperty("john", "lennon"), new TestOutcomeProperty("paul", "mccartney")]

		fetchedOutcomes = dataService.getTestOutcomes(queryfilterB)
		assertEquals "Wrong number of TestOutcomes returned", 2, fetchedOutcomes.size()

		TestOutcomeQueryFilter queryfilterC = new TestOutcomeQueryFilter()
		queryfilterC.testRun = testRun
		queryfilterC.hasAllTestOutcomeProperties = [new TestOutcomeProperty("george", "harrison")]

		fetchedOutcomes = dataService.getTestOutcomes(queryfilterC)
		assertEquals "Wrong number of TestOutcomes returned", 2, fetchedOutcomes.size()
		
		TestOutcomeQueryFilter queryfilterD = new TestOutcomeQueryFilter()
		queryfilterD.testRun = testRun
		queryfilterD.hasAllTestOutcomeProperties = [new TestOutcomeProperty("ringo", "starr")]

		fetchedOutcomes = dataService.getTestOutcomes(queryfilterD)
		assertEquals "Wrong number of TestOutcomes returned", 0, fetchedOutcomes.size()

		println "****** ${Tag.count()} tags"

		TestOutcomeQueryFilter queryFilterWithTagsB = new TestOutcomeQueryFilter()
		queryFilterWithTagsB.testRun = testRun
		//queryFilterWithTagsB.hasAllTestOutcomeProperties = [new TestOutcomeProperty("john", "lennon")]
		queryFilterWithTagsB.tags = ["cool"]

		fetchedOutcomes = dataService.getTestOutcomes(queryFilterWithTagsB)
		assertEquals "Wrong number of TestOutcomes returned", 3, fetchedOutcomes.size()


		TestOutcomeQueryFilter queryFilterWithTags = new TestOutcomeQueryFilter()
		queryFilterWithTags.testRun = testRun
		queryFilterWithTags.hasAllTestOutcomeProperties = [new TestOutcomeProperty("john", "lennon")]
		queryFilterWithTags.tags = ["cool"]

		fetchedOutcomes = dataService.getTestOutcomes(queryFilterWithTags)
		assertEquals "Wrong number of TestOutcomes returned", 3, fetchedOutcomes.size()

		TestOutcomeQueryFilter queryFilterWithTagsC = new TestOutcomeQueryFilter()
		queryFilterWithTagsC.testRun = testRun
		queryFilterWithTagsC.hasAllTestOutcomeProperties = [new TestOutcomeProperty("george", "harrison")]
		queryFilterWithTagsC.tags = ["cool"]

		fetchedOutcomes = dataService.getTestOutcomes(queryFilterWithTagsC)
		assertEquals "Wrong number of TestOutcomes returned", 1, fetchedOutcomes.size()

	}
}