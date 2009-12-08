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
 * Time: 8:26:13 AM
 *
 */
class ParentPackageFormatter implements TestNameFormatter {

	Boolean showParams = false

	public String getTestName(TestCase testCase) {
		def packageName = testCase.packageName
		def testName = testCase.testName
		def parentPattern = ~/.+\.(.+)\.(.+$)/
		def classPattern =  ~/(.+\.)?(.+$)/

		def params = ""
		if (showParams) {
			params = testCase.parameters ? "(${testCase.parameters})" : "()"
		}

		def parentMatcher = parentPattern.matcher(packageName)
		if (parentMatcher.matches()) {
			def parentPackage = parentMatcher[0][1]
			def className = parentMatcher[0][2]
			return "${parentPackage}.${className}.${testName + params}"
		}

		def classMatcher = classPattern.matcher(packageName)
		if (classMatcher.matches()) {
			def parentName = ""
			if (classMatcher[0][1]) {
				parentName += classMatcher[0][1]
			}
			parentName += classMatcher[0][2]
			
			return "${parentName}.${testName + params}"
		} else {
			return "${testName + params}"
		}
	}

	public String getDescription() {
		if (showParams) {
			return "parentPackage.Class.testMethod(params)"
		} else {
			return "parentPackage.Class.testMethod"
		}
	}


	public String getKey() {
		"parentpackage"
	}
}

