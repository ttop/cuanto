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

import cuanto.Project

class TestCase implements Comparable{

	static constraints = {
		project(nullable: false)
		testName(nullable: false, blank: false)
		fullName(nullable:false, blank: false)
		description(nullable: true, blank: true, maxSize:2048)
		packageName(nullable:true)
	}
	static mapping = {
		cache true
	}

	String testName
	String packageName
	String fullName
	String description
	Project project

	public int compareTo(Object t) {
		TestCase tc = (TestCase)t
		return this.fullName.compareTo(tc.fullName)
	}

	ParsableTestCase toParsableTestCase() {
		ParsableTestCase tc = new ParsableTestCase()
		tc.testName = this.testName
		tc.packageName = this.packageName
		tc.project = this.project.toString()
		tc.description = this.description
		tc.fullName = this.fullName
		return tc
	}
}
