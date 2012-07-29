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

class TestCase implements Comparable{

	static constraints = {
		project(nullable: false)
		testName(nullable: false, blank: false, maxSize: 1024)
		fullName(nullable:false, blank: false, index:"full_name_idx")
		description(nullable: true, blank: true, maxSize: 2048)
		packageName(nullable:true, maxSize: 1024)
		parameters(nullable:true, maxSize: 1024)
	}
	static mapping = {
		cache true
        version false
	}

	String testName
	String packageName
	String fullName
	String parameters
	String description
	Project project
	Integer analysisCount = 0

	public int compareTo(Object t) {
		TestCase tc = (TestCase)t
		if (this.fullName) {
			return this.fullName.compareTo(tc.fullName)
		} else {
			return this.testName.compareTo(tc.testName)
		}
	}


	Map toJSONmap() {
		return [
			testCase: [testName: this.testName, packageName: this.packageName,
				parameters: this.parameters, description: this.description, id: this.id, analysisCount: this.analysisCount]
		]
	}
}
