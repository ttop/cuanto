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

import cuanto.ProjectGroup
import cuanto.TestType

class Project implements Comparable {

	String name
	ProjectGroup projectGroup
	String projectKey
	String bugUrlPattern
	TestType testType
	Boolean deleted = false
	Integer purgeDays

	static constraints = {
		name(nullable: false, blank: false)
		projectGroup(nullable:true)
		projectKey(unique: true, blank: false, nullable: false, maxSize:25,
			validator: {
				if (it =~/\s/) {
					return "Whitespace is not allowed in a project key."
				} else {
					return null
				}
			}
		)
		bugUrlPattern(nullable: true)
		testType(nullable: false)
		deleted(nullable: true)
		purgeDays(nullable: true)
	}

	Project(){}

	
	Project(String name) {
		this.name = name
	}


	boolean equals(Project other) {
		return this.toString().equals(other?.toString())
	}


	String toString() {
		projectGroup ? "${this.projectGroup?.name}: ${this.name}" : this.name
	}


	public int compareTo(Object t) {
		Project otherProject = (Project) t
		if (t == null) {
			return 1
		}
		if (this.name != otherProject.name) {
			if (this.name == null) {
				return -1
			} else if (otherProject.name == null) {
				return 1
			} else {
				return this.name.compareTo(otherProject.name)
			}
		}
		
		if (this.projectGroup != otherProject.projectGroup) {
			if (this.projectGroup == null) {
				return -1
			} else if (otherProject.projectGroup == null) {
				return 1
			} else {
				return this.projectGroup.compareTo(otherProject.projectGroup)
			}
		}
		return 0
	}


	def getBugRegEx() {
		// turn a bug pattern like http://url/{BUG}/foo into a regex that will match that pattern
		if (this.bugUrlPattern) {
			def bugRegEx = this.bugUrlPattern.replaceAll('\\{BUG\\}', '(\\\\S+)')
			bugRegEx = bugRegEx.replaceAll('\\?', '\\\\?')
			return bugRegEx
		} else {
			return null
		}
	}


	def extractBugInfo(final bugStr) {
		// return a map with title and url based on the provided string
		def bugMap
		if (this.bugUrlPattern) {
			bugMap = getBugMap(bugStr)
			if (!bugMap) {
				bugMap.title = bugStr
				bugMap.url = this.bugUrlPattern.replaceAll('\\{BUG\\}', bugStr)
			}
		} else {
			bugMap = [title: bugStr]
		}
		return bugMap
	}


	def getBugMap(final bugStr) {
		def regex = getBugRegEx()
		if (regex) {
			def pattern = ~regex
			def matcher = bugStr =~ pattern
			if (matcher.matches()) {
				def title = matcher[0][1]
				def url = matcher[0][0]
				return ['title': title, 'url': url]
			}
		} 
		return [:]
	}

	
	Map toJSONMap() {
		def json = [:]
		json.id = this.id
		json.name = this.name
		json.projectGroup = this.projectGroup?.toJSONMap()
		json.projectKey = this.projectKey
		json.bugUrlPattern = this.bugUrlPattern
		json.testType = this.testType?.toJSONMap()
		json.purgeDays = this.purgeDays
		return json
	}


}