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
import cuanto.api.TestOutcome as ParsableTestOutcome
import cuanto.api.TestCase as ParsableTestCase

class TestOutcome {
	static constraints = {
	    testCase(nullable:false)
	    testResult(nullable:false)
		testRun(nullable:true)
	    analysisState(nullable:true)
	    duration(nullable:true)
	    bug(nullable:true)
	    note(blank:true, nullable:true)
	    owner(blank:true, nullable:true)
		testOutput(nullable:true, blank:true, maxSize:10000)
    }
	static mapping = {
		cache true
		analysisState lazy: false
	}

	TestCase testCase
	TestRun testRun
    TestResult testResult
    AnalysisState analysisState
    String testOutput
    BigDecimal duration
    String owner
    Bug bug
    String note
	

	ParsableTestOutcome toParsableTestOutcome() {
		ParsableTestOutcome out = new ParsableTestOutcome()
		out.testCase = this.testCase.toParsableTestCase()
		out.testResult = this.testResult.toString()
		out.testOutput = this.testOutput
		out.duration = this.duration
		out.owner = this.owner
		out.bug = this.bug?.url ? this.bug?.url : this.bug?.title
		out.note = this.note
		return out
	}
}
