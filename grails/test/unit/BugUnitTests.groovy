import cuanto.Bug

/**
 * User: Todd Wells
 * Date: Apr 29, 2009
 * Time: 5:51:50 PM
 * 
 */
class BugUnitTests extends GroovyTestCase {

	void testBugEquals() {
		Bug bugOne = new Bug()
		assertTrue bugOne.equals(bugOne)

		Bug bugTwo = new Bug()
		assertTrue bugOne.equals(bugTwo)

		bugOne.title = "foo"
		assertFalse bugOne.equals(bugTwo)
		bugTwo.title = "foo"
		assertTrue bugOne.equals(bugTwo)

		bugOne.url = "http://foo"
		assertFalse bugOne.equals(bugTwo)
		bugTwo.url = "http://foo"
		assertTrue bugOne.equals(bugTwo)
		bugTwo.url = "http://foobar"
		assertFalse bugOne.equals(bugTwo)
	}
}