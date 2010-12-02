import cuanto.TestRunController
/**
 * User: Todd Wells
 * Date: Dec 2, 2010
 * Time: 5:31:07 AM
 */
class TestRunControllerUnitTests extends GroovyTestCase {

	void testGetTestRunIdFromFilename() {
		def trc = new TestRunController()
		["TestRun_2842.csv": 2842, "TestRun_2.csv": 2, "TestRun_28.csv":28].each { name, val->
			assertEquals("Wrong value parsed from filename", val, trc.getTestRunIdFromFilename(name))
		}
	}
}
