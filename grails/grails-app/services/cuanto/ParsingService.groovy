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

import org.codehaus.groovy.grails.web.json.JSONObject
import java.text.SimpleDateFormat
import cuanto.parsers.ParsableTestOutcome
import org.codehaus.groovy.grails.web.json.JSONArray
import org.hibernate.StaleObjectStateException
import java.text.ParseException

class ParsingService {
	static transactional = false

	def dataService
	def testRunService
	def bugService
	def testParserRegistry
	def statisticService
	def projectService
	def testOutcomeService


	TestRun parseFileFromStream(stream, testRunId, projectId = null) {
		def testRun = null
		def project = null

		if (testRunId) {
			testRun = getTestRun(testRunId)
			project = testRun.project
		} else if (projectId) {
			project = Project.get(projectId)
		} else {
			throw new ParsingException("No TestRun ID or Project ID was provided")
		}

		def parser = getParser(project?.testType)
		def outcomes = parser.parseStream(stream)

		def attempts = 0
		def testRunToReturn = null

		while (attempts < 4) {
			attempts++
			try {
				testRunToReturn = saveParsedOutcomes(outcomes, testRun, project)
				break;
			} catch (StaleObjectStateException e) {
				log.info "StaleObjectStateException during saveParsedOutcomes()"
				if (attempts < 4) {
					sleep 2000
				} else {
					throw e
				}
			}
		}

		return testRunToReturn
	}

	TestRun saveParsedOutcomes(outcomes, testRun, project) {
		def localTestRun = null
		if (testRun) {
			localTestRun = TestRun.lock(testRun.id)
		}

		def testOutcomesToSave = []
		def numberOfOutcomes = 0

		for (ParsableTestOutcome parsableTestOutcome in outcomes) {
			numberOfOutcomes++
			def testOutcome = processParsableOutcome(parsableTestOutcome, localTestRun, project)
            def lastTestOutcome = dataService.findLastOutcomeForTestCase(testOutcome.testCase)
            if (lastTestOutcome?.analysisState == dataService.getAnalysisStateByName('Quarantined')) {
                testOutcome.applyAnalysisFrom(lastTestOutcome)
            }
			testOutcomesToSave.add(testOutcome)
		}

		dataService.saveTestOutcomes(testOutcomesToSave)
		log.info "${numberOfOutcomes} outcomes parsed from file for project ${localTestRun.project}"

		if (localTestRun) {
			localTestRun = TestRun.lock(localTestRun.id)
			testOutcomesToSave.each { outcome ->
				outcome.tags?.each { tag ->
					localTestRun.addToTags(tag)
				}
			}
			dataService.saveTestRun(localTestRun)
			statisticService.queueTestRunStats(localTestRun)
		}
		return localTestRun
	}


	TestOutcome parseTestOutcome(JSONObject jsonTestOutcome) {
		Project project = getProjectFromJsonObject(jsonTestOutcome)
		TestOutcome testOutcome = null

		if (!jsonTestOutcome.isNull("id")) {
            testOutcome = TestOutcome.lock(jsonTestOutcome.getLong("id"))
		}

		if (!testOutcome) {
			TestCase testCase = parseTestCase(jsonTestOutcome.getJSONObject("testCase"), project)
			def matchingTestCase = dataService.findMatchingTestCaseForProject(project, testCase)
			if (matchingTestCase) {
				testCase = matchingTestCase
			} else {
				dataService.addTestCases(project, [testCase])
			}

			testOutcome = new TestOutcome('testCase': testCase)
		}

		if (jsonTestOutcome.has("testRun")) {
			JSONObject jsonTestRun = jsonTestOutcome.getJSONObject("testRun")
			if (jsonTestRun != null) {
				testOutcome.testRun = TestRun.lock(jsonTestRun.getLong("id"))
			}
		}
		testOutcome.testResult = dataService.result(jsonTestOutcome.getString("result").toLowerCase())

		def startedAt = getDateFromJson(jsonTestOutcome, "startedAt")
		if (startedAt) {
			testOutcome.startedAt = startedAt
		}

		def finishedAt = getDateFromJson(jsonTestOutcome, "finishedAt")
		if (finishedAt) {
			testOutcome.finishedAt = getDateFromJson(jsonTestOutcome, "finishedAt")
		}

		if (!jsonTestOutcome.isNull("duration")) {
			testOutcome.duration = jsonTestOutcome.getLong("duration")
		} else if (testOutcome.startedAt != null && testOutcome.finishedAt != null) {
			testOutcome.duration = testOutcome.finishedAt.time - testOutcome.startedAt.time
		}

		def testOutput = parseJsonForString(jsonTestOutcome, "testOutput")
		if (testOutput) {
			testOutcome.testOutput = processTestOutput(testOutput)
			setTestOutputSummary(testOutcome);
		}

		def note = parseJsonForString(jsonTestOutcome, "note")
		if (note) {
			testOutcome.note = note
		}

		def owner = parseJsonForString(jsonTestOutcome, "owner")
		if (owner) {
			testOutcome.owner = owner
		}

		testOutcome.isFailureStatusChanged = testOutcomeService.isFailureStatusChanged(testOutcome)

		if (!jsonTestOutcome.isNull("bug")) {
			def jsonBug = jsonTestOutcome.getJSONObject("bug")
			String bugTitle = jsonBug.getString("title")
			String bugUrl = jsonBug.getString("url")
			testOutcome.bug = bugService.getBug(bugTitle, bugUrl)
		} else {
			processTestFailure(testOutcome, project)
		}

		if (!jsonTestOutcome.isNull("analysisState")) {
			testOutcome.analysisState = dataService.getAnalysisStateByName(jsonTestOutcome.getString("analysisState"))
		}

		if (!jsonTestOutcome.isNull("tags")) {
			JSONArray jsonTags = jsonTestOutcome.getJSONArray("tags")
			jsonTags.each {
				testOutcome.addToTags(procureTag(it))
			}
		}

		if (!jsonTestOutcome.isNull("testProperties")) {
			def testProps = jsonTestOutcome.getJSONObject("testProperties")
			testProps?.each {key, value ->
				testOutcome.addToTestProperties(new TestOutcomeProperty(key, value))
			}
		}

		if (!jsonTestOutcome.isNull("links")) {
			def links = jsonTestOutcome.getJSONObject("links")
			links?.each { key, value ->
				testOutcome.addToLinks(new TestOutcomeLink(key, value))
			}
		}
		return testOutcome;
	}

	/**
	 * Parse a TestRun from the JSONObject.
	 */
	TestRun parseTestRun(JSONObject jsonObj) {
		if (!jsonObj.containsKey("projectKey")) {
			throw new CuantoException("No projectKey parameter was specified.")
		}

		def projectKey = jsonObj.getString("projectKey")
		def project = projectService.getProject(projectKey)

		if (!project) {
			throw new CuantoException("Unable to locate project with the project key or full title of '${projectKey}'")
		}

		def testRun = new TestRun('project': project)
		if (!jsonObj.isNull("id")) {
			testRun.id = jsonObj.getLong("id")
		}

		if (!jsonObj.isNull("note")) {
			testRun.note = jsonObj.getString("note")
		}
		if (!jsonObj.isNull("valid")) {
			testRun.valid = jsonObj.getBoolean("valid")
		}
		testRun.dateExecuted = getDateFromString(jsonObj.getString("dateExecuted"))

		if (!jsonObj.isNull("links")) {
			def links = jsonObj.getJSONObject("links")
			links?.each { key, value ->
				testRun.addToLinks(new TestRunLink(key, value))
			}
		}

		if (!jsonObj.isNull("testProperties")) {
			def testProps = jsonObj.getJSONObject("testProperties")
			testProps?.each {key, value ->
				testRun.addToTestProperties(new TestRunProperty(key, value))
			}
		}
		return testRun
	}


	Date getDateFromString(String dateString) {
		Date parsedDate = null
		ParseException pe = null
		for (String dateFormat : [Defaults.fullDateFormat, Defaults.jsonDateFormat, Defaults.dateFormat])
		{
			def sdf = new SimpleDateFormat(dateFormat)
			try {
				parsedDate = sdf.parse(dateString)
				break
			} catch (ParseException e) {
				pe = e
			}
		}

		if (parsedDate) {
			return parsedDate
		} else {
			Long dateLong
			try {
				return new Date(Long.valueOf(dateString))
			} catch (NumberFormatException e) {
				throw pe
			}
		}
	}


	Project getProjectFromJsonObject(JSONObject jsonTestOutcome) {
		String projectKey = jsonTestOutcome.getString("projectKey")
		Project project = projectService.getProject(projectKey)
		if (!project) {
			throw new CuantoException("Unable to locate project with the project key or full title of '${projectKey}'")
		}
		return project
	}


	String parseJsonForString(JSONObject jsonObj, String field) {
		if (jsonObj.isNull(field)) {
			return null
		} else {
			return jsonObj.getString(field)
		}
	}


	Date getDateFromJson(JSONObject jsonTestOutcome, String fieldName) {
		if (jsonTestOutcome.isNull(fieldName)) {
			return null
		} else {
			return getDateFromString(jsonTestOutcome.getString(fieldName))
		}
	}


	private TestOutcome processParsableOutcome(ParsableTestOutcome parsableTestOutcome, TestRun testRun, Project project = null) {
		if (!project) {
			project = testRun?.project
		}

		TestCase testCase = parseTestCase(parsableTestOutcome, project)
		def matchingTestCase = dataService.findMatchingTestCaseForProject(project, testCase)

		if (matchingTestCase) {
			testCase = matchingTestCase
		} else {
			dataService.addTestCases(project, [testCase])
		}

		setTestCaseDescription(parsableTestOutcome.testCase.description, testCase)
		TestOutcome testOutcome = new TestOutcome('testCase': testCase)

		testOutcome.testResult = dataService.result(parsableTestOutcome.testResult.toLowerCase())
		testOutcome.duration = parsableTestOutcome.duration
		testOutcome.testCase = testCase
		testOutcome.testOutput = processTestOutput(parsableTestOutcome.testOutput)

        setTestOutputSummary(testOutcome)

		testOutcome.testRun = testRun
		testOutcome.startedAt = parsableTestOutcome.startedAt
		testOutcome.finishedAt = parsableTestOutcome.finishedAt
		testOutcome.isFailureStatusChanged = testOutcomeService.isFailureStatusChanged(testOutcome)
        testOutcome.owner = parsableTestOutcome.owner
        testOutcome.note = parsableTestOutcome.note

		processTestFailure(testOutcome, project)
		List tags = processTags(parsableTestOutcome)
		tags.each {
			testOutcome.addToTags(it)
		}
		return testOutcome
	}


    void setTestOutputSummary(TestOutcome testOutcome) {
        if (testOutcome.testOutput) {
            def matcher = testOutcome.testOutput =~ /(?m).+$/
            String outsummary = matcher[0] as String
            int MAX_LENGTH = 255
            if (outsummary?.length() > MAX_LENGTH) {
                testOutcome.testOutputSummary = outsummary.substring(0, MAX_LENGTH - 2)
            } else {
                testOutcome.testOutputSummary = outsummary
            }
        }
    }


	private TestCase parseTestCase(JSONObject jsonTestCase, Project project) {
		TestCase testCase
		if (!jsonTestCase.isNull("id")) {
			testCase = TestCase.get(jsonTestCase.getLong("id"))
		} else {
			testCase = new TestCase(testName: jsonTestCase.getString("testName"), 'project': project)

			if (!jsonTestCase.isNull("packageName")) {
				testCase.packageName = jsonTestCase.getString("packageName")
				testCase.fullName = testCase.packageName + "." + testCase.testName
			} else {
				testCase.packageName = ""
				testCase.fullName = testCase.testName
			}

			if (!jsonTestCase.isNull("parameters")) {
				testCase.parameters = jsonTestCase.getString("parameters")
			}

			if (!jsonTestCase.isNull("description")) {
				testCase.description = jsonTestCase.getString("description")
			}
		}
		return testCase
	}


	private TestCase parseTestCase(ParsableTestOutcome parsableTestOutcome, Project project) {
		TestCase testCase = new TestCase(
			testName: parsableTestOutcome.testCase.testName,
			packageName: parsableTestOutcome.testCase.packageName,
			parameters: parsableTestOutcome.testCase.parameters,
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

	TestRun parseFileWithoutTestRun(file, projectId) {
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


	def processTestFailure(TestOutcome testOutcome, Project project) {
		if (testOutcome.testResult?.isFailure) {
			if (project?.bugUrlPattern) {
				def urls = parseUrls(testOutcome.testOutput)
				def firstUrl = urls.find {it -> project.getBugMap(it).url }
				def bugInfo = project.getBugMap(firstUrl)
				if (bugInfo && bugInfo.url) {
					testOutcome.bug = bugService.getBug(bugInfo.title, bugInfo.url)
					testOutcome.analysisState = AnalysisState.findByIsBug(true)
					testOutcome.note = "Bug auto-populated based on the test output matching the project's bug pattern."
				}
			}
			testOutcome.analysisState = AnalysisState.findByIsDefault(true)
		}
	}

	List<Tag> processTags(ParsableTestOutcome testOutcome) {
		List tags = []
		testOutcome.tags.each { tagName ->
			def tag = procureTag(tagName)
			tags << tag
		}
		return tags
	}

	/**
	 * Find an existing tag or create a new tag
	 * @param tagName - the tag name
	 * @return The tag
	 */
	Tag procureTag(String tagName) {
		def existingTag = Tag.findByNameIlike(tagName)
		if (existingTag) {
			return existingTag
		} else {
			Tag newTag = new Tag(name: tagName)
			dataService.saveDomainObject newTag
			return newTag
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
			testRun = TestRun.lock(testRunId)
		} catch (Exception e) {
			throw new ParsingException(errMsg)
		}
		if (!testRun) {
			throw new ParsingException(errMsg)
		}
		return testRun
	}


	Project parseProject(JSONObject jsonObj) {
		Map paramsMap = [:]

		if (jsonObj.containsKey("projectKey")) {
			paramsMap.projectKey = jsonObj.getString("projectKey")
		}

		if (jsonObj.containsKey("name")) {
			paramsMap.name = jsonObj.getString("name")
		}

		if (jsonObj.containsKey("projectGroup")) {
			paramsMap.group = jsonObj.getString("projectGroup")
		}

		if (jsonObj.containsKey("bugUrlPattern")) {
			paramsMap.bugUrlPattern = jsonObj.getString("bugUrlPattern")
		}

		if (jsonObj.containsKey("testType")) {
			paramsMap.testType = jsonObj.getString("testType")
		}

		return projectService.createProject(paramsMap)
	}
}