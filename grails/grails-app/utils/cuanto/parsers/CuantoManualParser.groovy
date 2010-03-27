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

/**
 * User: Todd Wells
 * Date: Oct 16, 2008
 * Time: 5:48:16 PM
 * 
 */
class CuantoManualParser implements CuantoTestParser {


	public List<ParsableTestOutcome> parseFile(File file) {
		parseOutcomesFromNode(new XmlParser().parse(file))
	}


	public String getTestType() {
		"Manual"
	}


	public List<ParsableTestOutcome> parseStream(InputStream stream) {
		parseOutcomesFromNode(new XmlParser().parse(stream))
	}

	List parseOutcomesFromNode(Node topNode) {
		def outcomes = []
		if (topNode.name() == "testCases") {
			topNode.testCase.each { tc ->
				ParsableTestOutcome outcome = new ParsableTestOutcome()
				outcome.testCase = new ParsableTestCase()
				outcome.testCase.testName = tc.name.text()
				outcome.testCase.packageName = tc.'package'.text()
				outcome.testCase.fullName = outcome.testCase.packageName + "." + outcome.testCase.testName
				outcome.testCase.description = tc.description.text()
				outcome.testResult = "unexecuted"
				outcomes << outcome
			}
		} else {
			throw new ParsingException("Unrecognized XML file format")
		}
		return outcomes
	}

}