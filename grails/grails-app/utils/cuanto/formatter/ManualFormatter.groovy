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

package cuanto.formatter

import cuanto.TestCase

/**
 * User: Todd Wells
 * Date: Apr 4, 2009
 * Time: 9:05:45 AM
 * 
 */
class ManualFormatter implements TestNameFormatter {

	Boolean showParams = false

	public String getTestName(TestCase testCase) {
		def packageName = testCase.packageName
		def testName = testCase.testName

		def params = ""
		if (showParams) {
			params = testCase.parameters ? ": ${testCase.parameters}" : ""
		}

		if (packageName) {
			return "${packageName} ${testName + params}"
		} else {
			return testName + params
		}
	}

	public String getDescription() {
		if (showParams) {
			return "full.package.path testName: params"
		} else {
			return "full.package.path testName"
		}
	}


	public String getKey() {
		"manual"
	}

}