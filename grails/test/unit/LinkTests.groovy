import grails.test.*
import cuanto.TestRunLink
import cuanto.TestRunLink


class LinkTests extends GrailsUnitTestCase {

	TestRunLink linkOne = new TestRunLink(description: "a b", url: "http://ab")
	TestRunLink linkTwo = new TestRunLink(description: "c d", url: "http://cd")
	TestRunLink linkThree = new TestRunLink(description: "a b", url: "http://cd")
	TestRunLink linkFour = new TestRunLink(description: "a b", url: "http://aa" )
	TestRunLink linkFive = new TestRunLink(description: "a b", url: "http://ab")

    void testCompareTo() {
	    assertEquals 0, linkOne.compareTo(linkFive)
	    assertTrue linkOne.compareTo(linkTwo) < 0
	    assertTrue linkTwo.compareTo(linkOne) > 0

	    assertTrue linkOne.compareTo(linkFour) > 0
	    assertTrue linkFour.compareTo(linkThree) < 0

	    assertTrue linkOne.compareTo(linkThree) < 0
	    assertTrue linkThree.compareTo(linkOne) > 0

    }

	void testEquals() {
		assertEquals linkOne, linkFive
		assertEquals linkFive, linkOne

		[linkTwo, linkThree, linkFour].each { TestRunLink it ->
			assertFalse linkOne.equals(it)
			assertFalse it.equals(linkOne)
		}
	}

	void testSort() {
		def links = [linkTwo, linkOne, linkFour, linkThree]
		Collections.sort(links)
		assertEquals linkFour, links[0]
		assertEquals linkOne, links[1]
		assertEquals linkThree, links[2]
		assertEquals linkTwo, links[3]
	}
}
