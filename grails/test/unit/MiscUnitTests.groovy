import cuanto.TestResult

/**
 * User: Todd Wells
 * Date: May 21, 2009
 * Time: 10:45:41 PM
 * 
 */
class MiscUnitTests extends GroovyTestCase{

	void testTestResult() {
		TestResult result = new TestResult(name: "foobar")
		assertEquals "Wrong name", "foobar", result.name
	}

	
}