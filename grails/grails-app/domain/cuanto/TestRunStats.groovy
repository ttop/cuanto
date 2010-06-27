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


class TestRunStats {  

	static belongsTo = [testRun: TestRun]

	static constraints = {
		passed(nullable: true)
		failed(nullable: true)
		analyzed(nullable: true)
		newFailures(nullable: true)
		totalDuration(nullable: true)
		averageDuration(nullable: true)
		successRate(nullable: true)
		tests(nullable: true)
		analysisStatistics(nullable: true)
	}

	static mapping = {
		cache true
	}

	Integer tests
	Integer passed
	Integer failed
	Integer analyzed
	Integer newFailures
	Long totalDuration
	Long averageDuration
	BigDecimal successRate
	Date lastUpdated
	List analysisStatistics
    List tagStatistics

	static hasMany = [analysisStatistics: AnalysisStatistic, tagStatistics:TagStatistic]

	Map toJsonMap() {
		def json = [:]
		json['id'] = this.id
		json['passed'] = passed
		json['failed'] = failed
		json['tests'] = tests
		json['totalDuration'] = totalDuration
		json['averageDuration'] = averageDuration
		json['successRate'] = successRate ? successRate : 0
		return json
	}

}
