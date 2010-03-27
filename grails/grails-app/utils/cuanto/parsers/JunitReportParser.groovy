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

package cuanto.parsers

import cuanto.ParsingException

class JunitReportParser implements CuantoTestParser {

	public JunitReportParser() {}


	public List<ParsableTestOutcome> parseFile(File file) {
		parseOutcomesFromNode(new XmlParser().parse(file))
	}


	public List<ParsableTestOutcome> parseStream(InputStream stream) {
		parseOutcomesFromNode(new XmlParser().parse(stream))
	}


	private List<ParsableTestOutcome> parseOutcomesFromNode(Node topNode) {
		List<ParsableTestOutcome> outcomes
		if (topNode.name() == "testsuite") {
			outcomes = parseSingleSuiteStyle(topNode)
		} else if (topNode.name() == "testsuites") {
			outcomes = parseMultipleSuiteStyle(topNode)
		} else {
			throw new ParsingException("error parsing XML file")
		}
		return outcomes
	}


	private List<ParsableTestOutcome> parseSingleSuiteStyle(Node testsuite) {
		def outcomes = []
		testsuite.testcase.each {tc ->
			ParsableTestOutcome outcome = new ParsableTestOutcome()

			if (tc.error) {
				outcome.testResult = "error"
				outcome.testOutput = tc.error.text().trim()
			} else if (tc.failure) {
				outcome.testResult = "fail"
				outcome.testOutput = tc.failure.text().trim()
			} else {
				outcome.testResult = "pass"
			}

			def time = tc.'@time'.replaceAll(",", "")
			def dec = new BigDecimal(time) * 1000
			outcome.duration = dec.toLong()
			outcome.testCase = new ParsableTestCase()
			outcome.testCase.packageName = testsuite.'@name'
			outcome.testCase.testName = getTestName(tc.'@name')
			outcome.testCase.parameters = getTestParameters(tc.'@name')
			outcome.testCase.fullName = outcome.testCase.packageName + "." + outcome.testCase.testName
			outcomes.add(outcome)
		}
		return outcomes
	}


	List<ParsableTestOutcome> parseMultipleSuiteStyle(Node testsuites) {
		def outcomes = []

		testsuites.testsuite.each {testsuite ->
			testsuite.testcase.each {testcase ->
				ParsableTestOutcome outcome = new ParsableTestOutcome()
				outcome.testCase = new ParsableTestCase()
				outcome.testCase.packageName = testcase.'@classname'
				if (!outcome.testCase.packageName) {
					outcome.testCase.packageName = testsuite.'@package'
				}
				outcome.testCase.testName = getTestName(testcase.'@name')
				outcome.testCase.parameters = getTestParameters(testcase.'@name')
				outcome.testCase.fullName = outcome.testCase.packageName + "." + outcome.testCase.testName

				def time = testcase.'@time'.replaceAll(",", "")
				def dec = new BigDecimal(time) * 1000 
				outcome.duration = dec.toLong()

				if (testcase.failure) {
					outcome.testResult = "fail"
					outcome.testOutput = testcase.failure.text().trim()
				} else if (testcase.error) {
					outcome.testResult = "error"
					outcome.testOutput = testcase.error.text().trim()
				} else {
					outcome.testResult = "pass"
				}
				outcomes.add(outcome)
			}
		}
		return outcomes
	}

	String getTestName(String baseName) {
		def matcher = baseName =~ /.*\[\d+\]/
		if (matcher.matches()) {
			return baseName.replaceAll("\\[\\d+\\]", "")
		} else {
			return baseName
		}
	}

	String getTestParameters(String baseName) {
		def matcher = baseName =~ /.*(\[\d+\])/
		if (matcher.matches()) {
			return matcher[0][1] as String
		} else {
			return ""
		}
	}

	public String getTestType() {
		return "JUnit"
	}
}