import grails.test.*
import cuanto.Link


class LinkTests extends GrailsUnitTestCase {

	Link linkOne = new Link(description: "a b", url: "http://ab")
	Link linkTwo = new Link(description: "c d", url: "http://cd")
	Link linkThree = new Link(description: "a b", url: "http://cd")
	Link linkFour = new Link(description: "a b", url: "http://aa" )
	Link linkFive = new Link(description: "a b", url: "http://ab")

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

		[linkTwo, linkThree, linkFour].each { Link it ->
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
