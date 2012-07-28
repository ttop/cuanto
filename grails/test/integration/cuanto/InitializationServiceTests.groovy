package cuanto

import cuanto.test.TestObjects

/**
 * User: Todd Wells
 * Date: Apr 22, 2009
 * Time: 5:44:16 PM
 * 
 */
class InitializationServiceTests extends GroovyTestCase {

	def dataService
	def testOutcomeService
	def initializationService
	def to = new TestObjects()

	@Override
	void setUp()
	{
		super.setUp()
		to.dataService = dataService
	}

	void testDefaultAnalysisState() {
		initializationService.initializeAll()

		assertEquals "Wrong number of analysis states", 9, AnalysisState.list().size()
		def analyzed = AnalysisState.findAllByIsAnalyzed(true)
		assertEquals "Wrong number are analyzed", 7, analyzed.size()
		def stateNotDefault = AnalysisState.findAllByIsDefault(false)
		assertEquals "Wrong number are not default", 8, stateNotDefault.size()

		assertNull "Found Unanalyzed state in analyzed list", analyzed.find { it -> it.name == "Unanalyzed" }
		assertNull "Found Investigate state in analyzed list", analyzed.find { it -> it.name == "Investigate" }

		def unanalyzed = AnalysisState.findAllByIsAnalyzed(false)
		assertEquals "Wrong number of unanalyzed states", 2, unanalyzed.size()
		assertNotNull "Couldn't find Unanalyzed state in unanalyzed list", unanalyzed.find { it -> it.name == "Unanalyzed" }
		assertNotNull "Couldn't find Investigate state in unanalyzed list", unanalyzed.find { it -> it.name == "Investigate" }


		def stateDefault = AnalysisState.findAllByIsDefault(true)
		assertEquals "Should only be one default analysis state", 1, stateDefault.size()
		assertEquals "Wrong default analysis state", "Unanalyzed", stateDefault[0].name
	}
}