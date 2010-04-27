/*
Copyright (c) 2010 Todd Wells

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


public class NUnitParser implements CuantoTestParser {


	public List<ParsableTestOutcome> parseFile(File file) {
		return parseStream(file.newInputStream())
	}


	public String getTestType() {
		return "NUnit"
	}


	public List<ParsableTestOutcome> parseStream(InputStream stream) {
		def parsableOutcomes = []

		def xml = new XmlParser().parse(stream)
		xml.'test-suite'.each {
			parsableOutcomes.addAll(parseTestSuite(it))
		}
		return parsableOutcomes
	}


	List<ParsableTestOutcome> parseTestSuite(Node testSuite) {
		def outcomes = []
		testSuite.results.each {
			it.'test-case'.each {tc ->
				outcomes << parseTestCase(tc)
			}
			it.'test-suite'.each {ts ->
				outcomes.addAll(parseTestSuite(ts))
			}
		}
		return outcomes
	}


	ParsableTestOutcome parseTestCase(Node tcNode) {
		ParsableTestOutcome pto = new ParsableTestOutcome()
		ParsableTestCase ptc = new ParsableTestCase()
		ptc.fullName = tcNode.'@name'

		def lastDot = ptc.fullName.lastIndexOf(".")
		ptc.packageName = ptc.fullName.substring(0, lastDot)
		ptc.testName = ptc.fullName.substring(lastDot + 1)

		pto.testCase = ptc

		def executed = tcNode.'@executed'
		def success = tcNode.'@success'

		if (executed == "False") {
			pto.testResult = "Skip"
			pto.testOutput = tcNode.reason?.message?.text()
		} else if (success == "True") {
			pto.testResult = "Pass"
		} else if (success == "False") {
			pto.testResult = "Fail"

			def msgNode = tcNode.failure?.message
			if (msgNode) {
				pto.testOutput = msgNode.text()
				def stackTrace = tcNode.failure?.'stack-trace'
				if (stackTrace) {
					pto.testOutput += "\n" + stackTrace.text()
				}
			}
		}

		def timeNode = tcNode.'@time'
		if (timeNode) {
			def time = new BigDecimal(timeNode.replaceAll(",", "")) * 1000
			pto.duration = time.toLong()
		}

        if (tcNode.categories) {
            def categories = []
            tcNode.categories.category.each { category ->
                categories << category.'@name'
            }
            pto.tags = categories
        }
		return pto
	}
}