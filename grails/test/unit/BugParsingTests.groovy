import cuanto.Project
import cuanto.TestOutcomeService

/**
 * User: Todd Wells
 * Date: Dec 3, 2008
 * Time: 3:59:26 AM
 * 
 */
class BugParsingTests extends GroovyTestCase {

	def tos

	@Override
	void setUp() {
		tos = new TestOutcomeService()
	}

	void testGetBugRegEx() {
		def project = new Project()

		project.bugUrlPattern = "http://{BUG}"
		assertEquals "http://(\\S+)", project.getBugRegEx()

		project.bugUrlPattern = "http://{BUG}/"
		assertEquals "http://(\\S+)/", project.getBugRegEx()

		project.bugUrlPattern = "http://{BUG}/blah"
		assertEquals "http://(\\S+)/blah", project.getBugRegEx()

		project.bugUrlPattern = "http://{BUG}/blah/"
		assertEquals "http://(\\S+)/blah/", project.getBugRegEx()

		project.bugUrlPattern = "http://foo/bar{BUG}/blah"
		assertEquals "http://foo/bar(\\S+)/blah", project.getBugRegEx()

		project.bugUrlPattern = "http://foo/bar?bug={BUG}"
		assertEquals "http://foo/bar\\?bug=(\\S+)", project.getBugRegEx()

		project.bugUrlPattern = "http://foo/bar?bug={BUG}gah"
		assertEquals "http://foo/bar\\?bug=(\\S+)gah", project.getBugRegEx()


	}

	void testExtractBugInfo() {
		def bugInfo
		def project = new Project()

		project.bugUrlPattern = "http://{BUG}"
		bugInfo = project.extractBugInfo("foo")
		assertEquals "wrong title", "foo", bugInfo.title
		assertEquals "wrong url", "http://foo", bugInfo.url
		bugInfo = project.extractBugInfo("http://bar")
		assertEquals "wrong title", "bar", bugInfo.title
		assertEquals "wrong url", "http://bar", bugInfo.url
		
		project.bugUrlPattern = "http://{BUG}/"
		bugInfo = project.extractBugInfo("foo")
		assertEquals "wrong title", "foo", bugInfo.title
		assertEquals "wrong url", "http://foo/", bugInfo.url
		bugInfo = project.extractBugInfo("http://bar/")
		assertEquals "wrong title", "bar", bugInfo.title
		assertEquals "wrong url", "http://bar/", bugInfo.url


		project.bugUrlPattern = "http://{BUG}/blah"
		bugInfo = project.extractBugInfo("foo")
		assertEquals "wrong title", "foo", bugInfo.title
		assertEquals "wrong url", "http://foo/blah", bugInfo.url
		bugInfo = project.extractBugInfo("http://bar/blah")
		assertEquals "wrong title", "bar", bugInfo.title
		assertEquals "wrong url", "http://bar/blah", bugInfo.url


		project.bugUrlPattern = "http://{BUG}/blah/"
		bugInfo = project.extractBugInfo("foo")
		assertEquals "wrong title", "foo", bugInfo.title
		assertEquals "wrong url", "http://foo/blah/", bugInfo.url
		bugInfo = project.extractBugInfo("http://bar/blah/")
		assertEquals "wrong title", "bar", bugInfo.title
		assertEquals "wrong url", "http://bar/blah/", bugInfo.url

		project.bugUrlPattern = "http://foo/bar{BUG}/blah"
		bugInfo = project.extractBugInfo("foo")
		assertEquals "wrong title", "foo", bugInfo.title
		assertEquals "wrong url", "http://foo/barfoo/blah", bugInfo.url
		bugInfo = project.extractBugInfo("http://foo/barnone/blah")
		assertEquals "wrong title", "none", bugInfo.title
		assertEquals "wrong url", "http://foo/barnone/blah", bugInfo.url

		project.bugUrlPattern = "http://foo/bar?bug={BUG}"
		bugInfo = project.extractBugInfo("foo")
		assertEquals "wrong title", "foo", bugInfo.title
		assertEquals "wrong url", "http://foo/bar?bug=foo", bugInfo.url
		bugInfo = project.extractBugInfo("http://foo/bar?bug=run")
		assertEquals "wrong title", "run", bugInfo.title
		assertEquals "wrong url", "http://foo/bar?bug=run", bugInfo.url

		project.bugUrlPattern = "http://foo/bar?bug={BUG}gah"
		bugInfo = project.extractBugInfo("foo")
		assertEquals "wrong title", "foo", bugInfo.title
		assertEquals "wrong url", "http://foo/bar?bug=foogah", bugInfo.url
		bugInfo = project.extractBugInfo("http://foo/bar?bug=fuggah")
		assertEquals "wrong title", "fug", bugInfo.title
		assertEquals "wrong url", "http://foo/bar?bug=fuggah", bugInfo.url
	}

}