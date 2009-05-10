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

	static hasMany = [outcomes:TestOutcome]

	static constraints = {
		project(nullable: false)
		milestone(nullable: true, blank: true)
		build(nullable: true, blank: true)
		targetEnv(nullable: true, blank: true)
		note(nullable: true, blank: true)
		valid(nullable: true)
		outcomes(nullable:true)
		testRunStatistics(nullable:true)
	}
	
	static mapping = {
		cache true
	}
	
	String build
	String targetEnv
	Date dateCreated // date the record was added to the database
	Date dateExecuted // date the test run was executed
	String milestone
	String note
	Boolean valid = true
	Date lastUpdated  // for calculations
	TestRunStats testRunStatistics
	Project project


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
		} else if (this.build != testRun.build) {
			return false
		} else if (this.targetEnv != testRun.targetEnv) {
			return false
		} else if (this.milestone != testRun.milestone) {
			return false
		} else if (this.note != testRun.note) {
 			return false

		} else if (this.valid != testRun.valid) {
			return false
		}

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
		jsonMap.build = this.build
		jsonMap.dateExecuted = this.dateExecuted.toString()
		jsonMap.valid = this.valid
		jsonMap.targetEnv = this.targetEnv
		jsonMap.milestone = this.milestone
		jsonMap.project = this.project.toString()
		jsonMap.note = this.note
		return jsonMap
	}
}