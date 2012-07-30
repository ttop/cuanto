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

import java.text.SimpleDateFormat

class TestRun {

	static constraints = {
		project(nullable: false)
		note(nullable: true, blank: true)
		valid(nullable: true)
        tags(nullable:true)
	}

	static hasMany = [links:TestRunLink, testProperties:TestRunProperty, tags: Tag]

	static mapping = {
		cache true
		testRunLink fetch: "join"
		testRunProperty fetch: "join"
        tag fetch: "join"
        version false
	}
	
	Date dateCreated // date the record was added to the database
	Date dateExecuted // date the test run was executed
	String note
	Boolean valid = true
	Date lastUpdated  // for calculations
	Project project
	List<TestRunLink> links
	List<TestRunProperty> testProperties
	Boolean allowPurge = true

	def beforeInsert = {

		if (!dateCreated){
			dateCreated = new Date()
		}
		if (!dateExecuted){
			dateExecuted = dateCreated
		}
	}

	boolean equals(TestRun testRun) {
		if (testRun == null) {
			return false
		} else if (this.project != testRun.project) {
			return false
		} else if (this.note != testRun.note) {
 			return false

		} else if (this.valid != testRun.valid) {
			return false
		}
		//todo: compare links and properties?
		return true
	}

	Map toJSONWithDateFormat(String dateFormat) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat)
		def jsonMap = [:]
		jsonMap.id = this.id
		jsonMap.dateCreated = dateFormatter.format(dateCreated)
		jsonMap.dateExecuted = dateFormatter.format(dateExecuted)
		jsonMap.lastUpdated = dateFormatter.format(lastUpdated)
		jsonMap.valid = this.valid
		jsonMap.project = this.project.toJSONMap()
		jsonMap.note = this.note

		def jsonLinks = [:]
		this.links.each {
			jsonLinks[it.url] = it.description
		}
		jsonMap.links = jsonLinks
		jsonMap.testProperties = jsonTestProperties()

        if (tags) {
            jsonMap.tags = tags.collect{ it.name }
        }
		return jsonMap
	}


	List jsonTestProperties() {
		def jsonProps = []
		this.testProperties.each {
			jsonProps << [name: it.name, value: it.value]
		}
		return jsonProps
	}


	Map toJSONMap() {
		return toJSONWithDateFormat(Defaults.fullDateFormat)
	}

}