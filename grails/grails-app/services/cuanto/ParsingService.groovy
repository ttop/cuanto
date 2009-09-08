/*
 Copyright (c) 2008 thePlatform, Inc.

This file is part of Cuanto, a test results repository and analysis program.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package cuanto

import com.thoughtworks.xstream.XStream


/**
 * User: Todd Wells
 * Date: May 7, 2008
 * Time: 7:44:29 AM
 */
class ParsingService {
	static transactional = false

	def dataService
	def testRunService
	def bugService
	def testParserRegistry


	TestRun parseFileFromStream(stream, testRunId) {
		def testRun = getTestRun(testRunId)
		def parser = getParser(testRun.project.testType)
		def outcomes = parser.parseStream(stream)

		def testOutcomesToSave = []
		def numberOfOutcomes = 0

		for (ParsableTestOutcome parsableTestOutcome in outcomes) {
			numberOfOutcomes++
			def testOutcome = processParsableOutcome(testRun, parsableTestOutcome)
			testOutcomesToSave.add(testOutcome)
		}

		dataService.saveTestOutcomes(testRun, testOutcomesToSave)
		log.info "${numberOfOutcomes} outcomes parsed from file for project ${testRun.project}"
		return testRun
	}


	TestOutcome parseTestOutcome(inputStream, testRunId) {
		def testRun = dataService.getTestRun(testRunId)

		XStream xstream = new XStream()
		ParsableTestOutcome parsableTestOutcome = (ParsableTestOutcome) xstream.fromXML(inputStream)
		def testOutcome = processParsableOutcome(testRun, parsableTestOutcome)

		if (parsableTestOutcome.bug) {
			throw new RuntimeException("bug parsing from ParsableTestOutcome not yet implemented")
		}

		dataService.saveTestOutcomes(testRun, [testOutcome])
		return testOutcome
	}


	private TestOutcome processParsableOutcome(testRun, ParsableTestOutcome parsableTestOutcome) {
		TestCase testCase = parseTestCase(parsableTestOutcome)

		def matchingTestCase = dataService.findMatchingTestCaseForProject(testRun.project, testCase)
		if (matchingTestCase) {
			testCase = matchingTestCase
		} else {
			dataService.addTestCases(testRun.project, [testCase])
		}

		setTestCaseDescription(parsableTestOutcome.testCase.description, testCase)

		TestOutcome testOutcome = null
		if (matchingTestCase) {
			testOutcome = dataService.findOutcomeForTestCase(testCase, testRun)
		}

		if (!testOutcome) {
			testOutcome = new TestOutcome('testCase': testCase)
		}

		testOutcome.testResult = dataService.result(parsableTestOutcome.testResult.toLowerCase())
		testOutcome.duration = parsableTestOutcome.duration
		testOutcome.testCase = testCase
		testOutcome.testOutput = processTestOutput(parsableTestOutcome.testOutput)
		testOutcome.owner = parsableTestOutcome.owner
		testOutcome.note = parsableTestOutcome.note
		processTestFailure(testOutcome, testRun.project)
		return testOutcome
	}


	private TestCase parseTestCase(parsableTestOutcome) {
		TestCase testCase = new TestCase(
			testName: parsableTestOutcome.testCase.testName,
			packageName: parsableTestOutcome.testCase.packageName
		)

		if (testCase.packageName) {
			testCase.fullName = testCase.packageName + "." + testCase.testName
		} else {
			testCase.packageName = ""
			testCase.fullName = testCase.testName
		}
		return testCase
	}


	TestRun parseFile(file, testRunId) {
		parseFileFromStream(file.newInputStream(), testRunId)
	}


	def getParser(testType) {
		def parserToReturn = null
		testParserRegistry.parsers.each { parser ->
			if (testType && parser.testType.equalsIgnoreCase(testType.name)) {
				parserToReturn = parser
			}
		}

		if (parserToReturn) {
			return parserToReturn
		} else {
			throw new ParsingException("Unsupported test type: ${testType}")
		}
	}


	def setTestCaseDescription(desiredDescription, testCase) {
		if (desiredDescription != testCase.description && desiredDescription != "") {
			testCase.description = desiredDescription
		}
	}


	def processTestOutput(testOutput) {
		int MAX_LENGTH = 10000
		if (testOutput?.length() > MAX_LENGTH) {
			return testOutput.substring(0, MAX_LENGTH - 2)
		} else {
			return testOutput
		}
	}


	def processTestFailure(testOutcome, project) {
		if (testOutcome.testResult?.isFailure) {
			def unanalyzed = true
			if (project.bugUrlPattern) {
				def urls = parseUrls(testOutcome.testOutput)
				def firstUrl = urls.find {it -> project.getBugMap(it).url }
				def bugInfo = project.getBugMap(firstUrl)
				if (bugInfo && bugInfo.url) {
					testOutcome.bug = bugService.getBug(bugInfo.title, bugInfo.url) //todo: will this work? Test!
					testOutcome.analysisState = AnalysisState.findByIsBug(true)
					testOutcome.note = "Bug auto-populated based on the test output matching the project's bug pattern."
					unanalyzed = false
				}
			}
			if (unanalyzed) {
				testOutcome.analysisState = AnalysisState.findByIsDefault(true)
			}
		}
	}


	def parseUrls(strInput) {
		// return a list of all URLs found in strInput
		def urls = []
		def urlRegEx = '([A-Za-z][A-Za-z0-9+.-]{1,120}:[A-Za-z0-9/](([A-Za-z0-9$_.+!*,;/?:@&~=-])|%[A-Fa-f0-9]{2}){1,333}(#([a-zA-Z0-9][a-zA-Z0-9$_.+!*,;/?:@&~=%-]{0,1000}))?)'
		// regex from http://www.manamplified.org/archives/2006/10/url-regex-pattern.html

		def matcher = strInput =~ urlRegEx
		while (matcher.find()) {
			urls += matcher.group()
		}
		return urls
	}


	def getTestRun(testRunId) {
		def errMsg = "Unable to locate test run ID ${testRunId}"
		def testRun
		try {
			testRun = TestRun.get(testRunId)
		} catch (Exception e) {
			throw new ParsingException(errMsg)
		}
		if (!testRun) {
			throw new ParsingException(errMsg)
		}
		return testRun
	}
}