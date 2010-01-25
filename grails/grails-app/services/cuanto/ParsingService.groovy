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
import cuanto.api.TestOutcome as TestOutcomeApi

class ParsingService {
	static transactional = false

	def dataService
	def testRunService
	def bugService
	def testParserRegistry
	def statisticService
	private XStream xstream = new XStream()


	TestRun parseFileFromStream(stream, testRunId, projectId = null) {
		def testRun = null
		def project
		if (testRunId) {
			testRun = getTestRun(testRunId)
			project = testRun.project
		} else if (projectId) {
			project = Project.get(projectId)
		} else {
			throw new ParsingException("No TestRun ID or Project ID was provided")
		}

		def parser = getParser(project.testType)
		def outcomes = parser.parseStream(stream)

		def testOutcomesToSave = []
		def numberOfOutcomes = 0

		for (TestOutcomeApi TestOutcomeApi in outcomes) {
			numberOfOutcomes++
			def testOutcome = processParsableOutcome(TestOutcomeApi, testRun, project)
			testOutcomesToSave.add(testOutcome)
		}

		dataService.saveTestOutcomes(testRun, testOutcomesToSave)
		log.info "${numberOfOutcomes} outcomes parsed from file for project ${testRun.project}"
		if (testRun) {
			statisticService.queueTestRunStats(testRun)
		}
		return testRun
	}


	TestOutcome parseTestOutcome(InputStream inputStream, Long testRunId, Long projectId = null) {
		TestOutcomeApi testOutcomeApi = (TestOutcomeApi) xstream.fromXML(inputStream)
		return parseTestOutcome(testOutcomeApi, testRunId, projectId)
	}


	TestOutcome parseTestOutcome(TestOutcomeApi testOutcomeApi, testRunId, projectId = null) {
		def testRun = null
		def project = null

		if (testRunId) {
			testRun = dataService.getTestRun(testRunId)
		}

		if (testRun) {
			project = testRun.project
		} else if (projectId) {
			project = Project.get(projectId)
		} else {
			throw new ParsingException("No TestRun ID or Project ID was provided")
		}

		def testOutcome = processParsableOutcome(testOutcomeApi, testRun, project)

		if (testOutcomeApi.bug) {
			throw new RuntimeException("bug parsing from TestOutcomeApi not yet implemented")
		}

		dataService.saveTestOutcomes(testRun, [testOutcome])
		if (testRun) {
			statisticService.queueTestRunStats(testRun)
		}
		return testOutcome
	}


	private TestOutcome processParsableOutcome(TestOutcomeApi testOutcomeApi, TestRun testRun, Project project = null) {
		if (!project) {
			project = testRun?.project
		}

		TestCase testCase = parseTestCase(testOutcomeApi, project)
		def matchingTestCase = dataService.findMatchingTestCaseForProject(project, testCase)
		
		if (matchingTestCase) {
			testCase = matchingTestCase
		} else {
			dataService.addTestCases(project, [testCase])
		}

		setTestCaseDescription(testOutcomeApi.testCase.description, testCase)
		TestOutcome testOutcome = new TestOutcome('testCase': testCase)

		testOutcome.testResult = dataService.result(testOutcomeApi.testResult.toLowerCase())
		testOutcome.duration = testOutcomeApi.duration
		testOutcome.testCase = testCase
		testOutcome.testOutput = processTestOutput(testOutcomeApi.testOutput)
		testOutcome.owner = testOutcomeApi.owner
		testOutcome.note = testOutcomeApi.note
		testOutcome.testRun = testRun
		testOutcome.startedAt = testOutcomeApi.startedAt
		testOutcome.finishedAt = testOutcomeApi.finishedAt
		processTestFailure(testOutcome, project)
		return testOutcome
	}


	private TestCase parseTestCase(TestOutcomeApi, project) {
		TestCase testCase = new TestCase(
			testName: TestOutcomeApi.testCase.testName,
			packageName: TestOutcomeApi.testCase.packageName,
			parameters: TestOutcomeApi.testCase.parameters,
			'project': project
		)

		if (testCase.packageName) {
			testCase.fullName = testCase.packageName + "." + testCase.testName
		} else {
			testCase.packageName = ""
			testCase.fullName = testCase.testName
		}

		return testCase
	}


	TestRun parseFileWithTestRun(file, testRunId) {
		parseFileFromStream(file.newInputStream(), testRunId)
	}

	TestRun parseFileWithoutTestRun(file,  projectId) {
		parseFileFromStream(file.newInputStream(), null, projectId)
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
			if (project?.bugUrlPattern) {
				def urls = parseUrls(testOutcome.testOutput)
				def firstUrl = urls.find {it -> project.getBugMap(it).url }
				def bugInfo = project.getBugMap(firstUrl)
				if (bugInfo && bugInfo.url) {
					testOutcome.bug = bugService.getBug(bugInfo.title, bugInfo.url) //todo: will this work? Test!
					testOutcome.analysisState = AnalysisState.findByIsBug(true)
					testOutcome.note = "Bug auto-populated based on the test output matching the project's bug pattern."
				}
			}
			testOutcome.analysisState = AnalysisState.findByIsDefault(true)
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