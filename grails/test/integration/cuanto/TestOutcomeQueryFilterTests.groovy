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

    }

}