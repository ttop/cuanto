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

package cuanto

class TestOutcomeProperty implements Comparable {

	static belongsTo = [testOutcome: TestOutcome]

	String name
	String value

	TestOutcomeProperty() {

	}

	TestOutcomeProperty(String name, String value) {
		this.name = name
		this.value = value
	}

	String toString() {
		return "${this.name}: ${this.value}"
	}

	int compareTo(Object o) {
		TestOutcomeProperty other = (TestOutcomeProperty) o
		int result = this.name.compareTo(other.name)
		if (result == 0) {
			return this.value.compareTo(other.value)
		} else {
			return result
		}
	}
}
