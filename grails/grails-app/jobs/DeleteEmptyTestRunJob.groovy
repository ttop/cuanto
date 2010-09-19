import cuanto.TestRun
import cuanto.TestRunStats
import cuanto.TestOutcome

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

class DeleteEmptyTestRunJob {

	def statisticService

	static triggers = {
		simple name: 'DeleteEmptyTestRuns', startDelay: 10000, repeatInterval: 60000
	}

    def execute() {
        // execute task
	    log.debug "executing ${this.class.simpleName}"
	    statisticService.deleteEmptyTestRuns()
    }

}
