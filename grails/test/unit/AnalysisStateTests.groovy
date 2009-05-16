import cuanto.AnalysisState

/**
 * User: Todd Wells
 * Date: May 15, 2009
 * Time: 8:27:10 AM
 * 
 */
class AnalysisStateTests extends GroovyTestCase{

	void testToString() {
		def state = new AnalysisState(name: "Foo")
		assertEquals "Wrong name", "Foo", state.toString()
	}
}