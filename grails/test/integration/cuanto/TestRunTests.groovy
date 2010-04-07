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
		TestRun tr = to.getTestRun(proj)

		def links = [
			new TestRunLink(description: "a b c", url: "http://linkOne"),
			new TestRunLink(description: "d e f", url: "http://linkTwo"),
		]

		links.each {
			tr.addToLinks(it)
		}
		dataService.saveDomainObject tr

		def fetchedRun = TestRun.get(tr.id)
		assertEquals "Wrong number of links", 2, fetchedRun.links.size()
		
		fetchedRun.links.eachWithIndex { TestRunLink item, index ->
			assertEquals item.description, links[index].description
			assertEquals item.url, links[index].url
		}

		tr.removeFromLinks(links[1])
		dataService.saveDomainObject tr

		fetchedRun = TestRun.get(tr.id)
		assertEquals "Wrong number of links", 1, fetchedRun.links.size()
	}
}