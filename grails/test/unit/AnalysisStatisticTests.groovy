import cuanto.AnalysisStatistic
import cuanto.AnalysisState

/**
 * User: Todd Wells
 * Date: May 21, 2009
 * Time: 10:36:55 PM
 * 
 */
class AnalysisStatisticTests extends GroovyTestCase {

	void testToString() {
		def anState = new AnalysisState(name: "Investigate", isAnalyzed: true)
		def stat = new AnalysisStatistic(state: anState, qty: 5)
		assertEquals "Wrong toString()", "Investigate: 5", stat.toString()
	}
}