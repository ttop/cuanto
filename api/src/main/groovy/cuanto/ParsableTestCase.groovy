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
	String packageName 
	String testName
	String fullName
	String parameters
	String description


	boolean equals(o) {
		if (this.is(o)) return true;

		if (!(o instanceof ParsableTestCase)) return false;

		ParsableTestCase that = (ParsableTestCase) o;

		if (description ? !description.equals(that.description) : that.description != null) return false;
		if (fullName ? !fullName.equals(that.fullName) : that.fullName != null) return false;
		if (packageName ? !packageName.equals(that.packageName) : that.packageName != null) return false;
		if (parameters ? !parameters.equals(that.parameters) : that.parameters != null) return false;
		if (project ? !project.equals(that.project) : that.project != null) return false;
		if (testName ? !testName.equals(that.testName) : that.testName != null) return false;

		return true;
	}

	int hashCode() {
		int result;

		result = (project ? project.hashCode() : 0);
		result = 31 * result + (packageName ? packageName.hashCode() : 0);
		result = 31 * result + (testName ? testName.hashCode() : 0);
		result = 31 * result + (fullName ? fullName.hashCode() : 0);
		result = 31 * result + (parameters ? parameters.hashCode() : 0);
		result = 31 * result + (description ? description.hashCode() : 0);
		return result;
	}


	public int compareTo(Object t) {
		def other = (ParsableTestCase) t
		if (this.project != other.project)
			return this.project.compareTo(other.project)
		if (this.packageName != other.packageName)
			return this.packageName.compareTo(other.packageName)
		if (this.testName != other.testName)
			return this.testName.compareTo(other.testName)
		if (this.fullName != other.fullName)
			return this.fullName.compareTo(other.fullName)
		if (this.description != other.description)
			return this.description.compareTo(other.description)
		if (this.parameters != other.parameters)
		    return this.parameters.compareTo(other.parameters)
		return 0
	}

}