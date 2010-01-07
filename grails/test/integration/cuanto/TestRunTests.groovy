package cuanto

import cuanto.test.TestObjects

/**
 * User: Todd Wells
 * Date: Jan 7, 2010
 * Time: 8:28:46 AM
 * 
 */

public class TestRunTests extends GroovyTestCase {

	DataService dataService

	TestObjects to = new TestObjects()
	Project proj


	void setUp() throws Exception {
		proj = to.project
		dataService.saveDomainObject proj
	}


	void testLinks() {
		TestRun tr = to.getTestRun(proj, "1.0")

		def links = [
			new Link(description: to.wordGen.getWord(), url: "http://linkOne"),
			new Link(description: to.wordGen.getWord(), url: "http://linkTwo"),
		]

		links.each {
			tr.addToLinks(it)
		}
		dataService.saveDomainObject tr

		def fetchedRun = TestRun.get(tr.id)
		assertEquals "Wrong number of links", 2, fetchedRun.links.size()
		assertEquals "Wrong link 0", links[0], fetchedRun.links[0]
		assertEquals "Wrong link 1", links[1], fetchedRun.links[1]

		tr.removeFromLinks(fetchedRun.links[1])
		dataService.saveDomainObject tr

		fetchedRun = TestRun.get(tr.id)
		assertEquals "Wrong number of links", 1, fetchedRun.links.size()
		assertEquals "Wrong link 0", links[0], fetchedRun.links[0]
	}
}