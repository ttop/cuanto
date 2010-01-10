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
import cuanto.api.Link as ApiLink
import cuanto.api.TestProperty as ApiProperty

class TestRun {

	static constraints = {
		project(nullable: false)
		note(nullable: true, blank: true)
		valid(nullable: true)
		testRunStatistics(nullable:true)
	}

	static hasMany = [links:Link, testProperties:TestProperty]

	static mapping = {
		cache true
		link fetch: "join"
		testProperty fetch: "join"
	}
	
	Date dateCreated // date the record was added to the database
	Date dateExecuted // date the test run was executed
	String note
	Boolean valid = true
	Date lastUpdated  // for calculations
	TestRunStats testRunStatistics
	Project project
	List<Link> links
	List<TestProperty> testProperties

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

	Map toJSONWithDateFormat(SimpleDateFormat dateFormat) {
		def jsonMap = this.toJSONMap()
		jsonMap.dateExecuted = dateFormat.format(this.dateExecuted)
		return jsonMap
	}
	
	Map toJSONMap() {
		def jsonMap = [:]
		jsonMap.id = this.id
		jsonMap.dateExecuted = this.dateExecuted.toString()
		jsonMap.valid = this.valid
		jsonMap.project = this.project.toString()
		jsonMap.note = this.note

		def jsonLinks = []
		this.links.each {
			jsonLinks << [description: it.description, url: it.url]
		}
		jsonMap.links = jsonLinks

		List jsonProps = getJsonTestProperties()
		jsonMap.properties = jsonProps

		return jsonMap
	}


	List getJsonTestProperties() {
		def jsonProps = []
		this.testProperties.each {
			jsonProps << [name: it.name, value: it.value]
		}
		return jsonProps
	}


	ParsableTestRun toParsableTestRun(dateFormat = Defaults.dateFormat) {
		ParsableTestRun pTestRun = new ParsableTestRun()
		pTestRun.dateCreated =  new SimpleDateFormat(dateFormat).format(this.dateCreated)
		pTestRun.dateExecuted = this.dateExecuted
		pTestRun.note = this.note
		pTestRun.valid = this.valid
		pTestRun.projectKey = this.project.projectKey

		pTestRun.links = new ArrayList<ApiLink>()
		this.links?.each {
			pTestRun.links << new ApiLink(it.description, it.url)
		}

		pTestRun.testProperties = new ArrayList<TestProperty>()
		this.testProperties?.each {
			pTestRun.testProperties << new ApiProperty(it.name, it.value)
		}
		return pTestRun
	}
}