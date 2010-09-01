/*
	Copyright (c) 2010 Todd E. Wells

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

class TestRunLink implements Comparable {

	static belongsTo = [testRun: TestRun]

	String description
	String url

	public TestRunLink(){}
	
	public TestRunLink(String url, String description) {
		this.description = description
		this.url = url
	}


	public int compareTo(Object t) {
		TestRunLink other = (TestRunLink) t
		int descriptionCompare = this.description.compareTo(other.description)
		if (descriptionCompare == 0) {
			return this.url.compareTo(other.url)
		} else {
			return descriptionCompare
		}
	}


	public boolean equals(Object t) {
		TestRunLink other = (TestRunLink) t
		return this.description == other.description && this.url == other.url
	}

}
