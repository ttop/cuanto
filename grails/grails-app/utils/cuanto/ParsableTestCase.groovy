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

/**
 * User: Todd Wells
 * Date: May 13, 2008
 * Time: 9:49:54 PM
 *
 */
class ParsableTestCase implements Comparable {
	String project
	String testPackage // todo: change to packageName
	String testName
	String fullName
	String description


	public int compareTo(Object t) {
		def other = (ParsableTestCase) t
		if (this.project != other.project)
			return this.project.compareTo(other.project)
		if (this.testPackage != other.testPackage)
			return this.testPackage.compareTo(other.testPackage)
		if (this.testName != other.testName)
			return this.testName.compareTo(other.testName)
		if (this.fullName != other.fullName)
			return this.fullName.compareTo(other.fullName)
		if (this.description != other.description)
			return this.description.compareTo(other.description)
		return 0
	}

}