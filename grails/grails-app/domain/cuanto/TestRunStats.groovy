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

	static constraints = {
		passed(nullable: true)
		failed(nullable: true)
		skipped(nullable: true)
		analyzed(nullable: true)
		newFailures(nullable: true)
        quarantined(nullable: true)
		totalDuration(nullable: true)
		averageDuration(nullable: true)
		successRate(nullable: true)
        successRateChange(nullable: true)
		tests(nullable: true)
		analysisStatistics(nullable: true)
        testRun(nullable: false)
	}

	Integer tests
	Integer passed
	Integer failed
	Integer skipped
	Integer analyzed
	Integer newFailures
    Integer quarantined
	Long totalDuration
	Long averageDuration
	BigDecimal successRate
	BigDecimal successRateChange
	Date lastUpdated
	List analysisStatistics
    List tagStatistics
    TestRun testRun

	static hasMany = [analysisStatistics: AnalysisStatistic, tagStatistics:TagStatistic]

	static mapping = {
		tagStatistics(cascade: 'all-delete-orphan')
	}

	Map toJsonMap() {
		def json = [:]
		json['id'] = this.id
		json['passed'] = passed ?: 0
		json['skipped'] = skipped ?: 0
		json['failed'] = failed ?: 0
        json['quarantined'] = quarantined ?: 0
		json['tests'] = tests ?: 0
		json['totalDuration'] = totalDuration ?: 0
		json['averageDuration'] = averageDuration ?: 0
		json['successRate'] = successRate ?: 0
		json['successRateChange'] = successRateChange
		return json
	}

}
