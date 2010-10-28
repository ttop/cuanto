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

class ProjectGroup implements Comparable {

    static constraints = {
	    name(nullable: false, unique: true)
    }

	static mapping = {
		cache true
	}

	static hasMany = [projects: Project]
	
	String name

	String toString() {
		return name
	}

	Map toJSONMap() {
		def json = [:]
		json.name = this.name
		json.id = this.id
		return json
	}


	public int compareTo(Object t) {
		return this.id.compareTo(t.id)
	}
}
