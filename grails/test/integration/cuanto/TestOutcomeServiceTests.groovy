package cuanto

import cuanto.test.TestObjects
import cuanto.test.WordGenerator
import cuanto.*
import cuanto.testapi.TestOutcome as TestOutcomeApi
import cuanto.testapi.TestCase as TestCaseApi

public class TestOutcomeServiceTests extends GroovyTestCase {

	DataService dataService
	TestOutcomeService testOutcomeService

	def initializationService
	def testRunService
	def bugService
	StatisticService statisticService
	ParsingService parsingService

	TestObjects to
	WordGenerator wordGen

	@Override
	void setUp() {
		initializationService.initializeAll()
		to = new TestObjects()
		to.dataService = this.dataService
		testOutcomeService.dataService = this.dataService
		wordGen = new WordGenerator()
	}

	void testUpdateTestOutcomeWithoutScriptTag() {
		// create a project
		Project proj = to.getProject()
		proj.save()

		// create a test case
		TestCase tc = to.getTestCase(proj)
		dataService.saveDomainObject tc

		// create a test run with an outcome
		TestRun testRun = to.getTestRun(proj)
		testRun.save()

		TestOutcome outcome = to.getTestOutcome(tc, testRun)
		outcome.save()

		def params = [
			id: outcome.id,
			testResult: 'fail',
			note: textWithoutScriptTags,
			owner: textWithoutScriptTags,
			bug: textWithoutScriptTags
		]
		testOutcomeService.updateTestOutcome(params)

		// the notes are the same
		def persistedOutcome = dataService.getTestOutcomes(new TestOutcomeQueryFilter(testRun: testRun, testResultIncludedInCalculations: true))[0]
		assertEquals textWithoutScriptTags, persistedOutcome.note
		assertEquals textWithoutScriptTags, persistedOutcome.owner
		assertEquals textWithoutScriptTags, persistedOutcome.bug.title
	}


	void testUpdateTestOutcomeNoOutcome() {
		testOutcomeService.updateTestOutcome([:])
	}



	void testCreateTestOutcomeWithScriptTags() {
		// create a project
		Project proj = to.getProject()
		proj.save()

		// create a test case
		TestCase tc = to.getTestCase(proj)
		dataService.saveDomainObject tc

		// create a test run with an outcome
		TestRun testRun = to.getTestRun(proj)
		testRun.save()

		TestOutcome outcome = to.getTestOutcome(tc, testRun)
		outcome.save()

		def params = [
			id: outcome.id,
			testResult: 'fail',
			note: textWithScriptTags,
			owner: textWithScriptTags,
			bug: textWithScriptTags
		]
		testOutcomeService.updateTestOutcome(params)

		// the notes are the same
		def persistedOutcome = dataService.getTestOutcomes(new TestOutcomeQueryFilter(testRun: testRun, testResultIncludedInCalculations: true))[0]
		assertEquals textWithSanitizedScriptTags, persistedOutcome.note
		assertEquals textWithSanitizedScriptTags, persistedOutcome.owner
		assertEquals textWithSanitizedScriptTags, persistedOutcome.bug.title
	}

	void testApplyAnalysisStateToTestOutcome() {
		// create a project
		Project proj = to.getProject()
		proj.save()

		// create a test case
		TestCase tc = to.getTestCase(proj)
		dataService.saveDomainObject tc 

		// create a test run with an outcome
		TestRun testRun = to.getTestRun(proj)
		dataService.saveDomainObject testRun 

		TestOutcome outcome = to.getTestOutcome(tc, testRun)
		dataService.saveDomainObject outcome
		statisticService.calculateTestRunStats(testRun.id)

		def stateToApply = dataService.getAnalysisStateByName("Bug")

		testOutcomeService.applyAnalysisStateToTestOutcome(outcome, [analysisState: stateToApply.id.toString()])
		assertEquals "Wrong analysis state when applying by ID", stateToApply, outcome.analysisState

		outcome.refresh()
		testOutcomeService.applyAnalysisStateToTestOutcome(outcome, [analysisStateName: stateToApply.name])
		assertEquals "Wrong analysis state when applying by name", stateToApply, outcome.analysisState

		outcome.refresh()
		testOutcomeService.applyAnalysisStateToTestOutcome(outcome, [:])
		assertNull "Wrong analysis state when applying to passing TestOutcome without parameters", outcome.analysisState

		outcome.refresh()
		testOutcomeService.applyAnalysisStateToTestOutcome(outcome, [testResult:"Pass"])
		assertNull "Wrong analysis state when applying to passing TestOutcome without parameters", outcome.analysisState

		outcome.testResult = TestResult.findByName("Fail")
		dataService.saveDomainObject outcome
		outcome.refresh()
		testOutcomeService.applyAnalysisStateToTestOutcome(outcome, [:])
		assertNull "Wrong analysis state when applying to passing TestOutcome without parameters", outcome.analysisState


	}


	void testGetCsvForTestOutcomes() {
		//todo: submit results
		// retrieve CSV
		// verify
		// create a project
		Project proj = to.getProject()
		proj.save()

		def testRun = to.getTestRun(proj)
		dataService.saveDomainObject testRun

		def outcomes = []
		1.upto(3) {
			def tc = to.getTestCase(proj)
			dataService.saveDomainObject tc
			def outcome = to.getTestOutcome(tc, testRun)
			dataService.saveDomainObject outcome
			outcomes << outcome
		}

		def csv = testOutcomeService.getDelimitedTextForTestOutcomes(outcomes, ",")
		def csvLines = csv.readLines()
		assertEquals "Wrong number of lines for CSV output", outcomes.size() + 1, csvLines.size() 

		// do 0 results, 1 result, 3 results, 10 results

	}

	def textWithoutScriptTags = '''
			Lorem ipsum dolor sit amet, consectetur adipiscing elit.
			Duis luctus erat ultrices ipsum mollis nec scelerisque nibh eleifend.
			Vivamus hendrerit cursus viverra. Pellentesque dui sem, convallis non eleifend ut, commodo id elit.
			Aenean imperdiet lectus eu diam molestie sollicitudin. Curabitur id libero neque, non vulputate leo.'''
	def textWithScriptTags = '''
			Lorem ipsum dolor sit amet, consectetur adipiscing elit.
			<script type="text/javascript"><![CDATA[
				Duis luctus erat ultrices ipsum mollis nec scelerisque nibh eleifend.
			// ]]></script>
			Vivamus hendrerit cursus viverra. Pellentesque dui sem, convallis non eleifend ut, commodo id elit.
			Aenean imperdiet lectus eu diam molestie sollicitudin. Curabitur <script type="text/javascript">id</script>
			libero neque, non vulputate leo.'''
	def textWithSanitizedScriptTags = '''
			Lorem ipsum dolor sit amet, consectetur adipiscing elit.
			&lt;script type="text/javascript"><![CDATA[
				Duis luctus erat ultrices ipsum mollis nec scelerisque nibh eleifend.
			// ]]>&lt;/script&gt;
			Vivamus hendrerit cursus viverra. Pellentesque dui sem, convallis non eleifend ut, commodo id elit.
			Aenean imperdiet lectus eu diam molestie sollicitudin. Curabitur &lt;script type="text/javascript">id&lt;/script&gt;
			libero neque, non vulputate leo.'''
}