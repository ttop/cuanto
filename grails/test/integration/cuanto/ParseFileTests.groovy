package cuanto
import cuanto.test.TestObjects

/**
 * User: Todd Wells
 * Date: Sep 24, 2008
 * Time: 6:37:42 AM
 * 
 */
class ParseFileTests extends GroovyTestCase {
	ParsingService parsingService
	def initializationService

	def fakes = new TestObjects()
	File tempFile

	@Override
	void setUp() {
		super.setUp()
		initializationService.initializeAll()
		tempFile = new File("temp_delete_me")
		tempFile.write "it's ok to delete this file, in fact, please do."
	}

	@Override
	void tearDown() {
		tempFile.deleteOnExit()
		super.tearDown()
	}


	void testBadTestRunId() {
		[98783, "foobar", "23398","foo bar"].each { runId ->
			def msg = shouldFail(ParsingException) {
				parsingService.parseFileWithTestRun(tempFile, runId)
			}
			assertEquals "Wrong error message", ("Unable to locate test run ID ${runId}"), msg
		}

		def msg = shouldFail(ParsingException) {
			parsingService.parseFileWithTestRun(tempFile, null)
		}
		assertEquals "Wrong error message", ("No TestRun ID or Project ID was provided"), msg

	}
}