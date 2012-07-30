import cuanto.Project
import cuanto.TestRun

/*
	Copyright (c) 2012 Todd E. Wells

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

class PurgeTestRunsJob {
	def testRunService
	def concurrent = false

	static Integer MINUTES = 1000 * 60

	static triggers = {
		simple name: 'PurgeTestRuns', startDelay: 5 * MINUTES, repeatInterval: 30 * MINUTES
	}

    def execute() {
	    List<Project> projects = Project.findAllByPurgeDaysGreaterThan(0)
	    log.info "Found ${projects.size()} projects to purge"

	    projects.each { Project project ->
		    Calendar cal = Calendar.getInstance()
		    cal.add(Calendar.DATE, -1 * project.purgeDays)
		    Date purgeDate = cal.getTime()

		    def query = "FROM TestRun tr WHERE tr.project = ? AND tr.dateExecuted < ? AND tr.allowPurge IS TRUE " +
			    "ORDER BY tr.dateExecuted asc"
		    List<TestRun> testRuns = TestRun.executeQuery(query, [project, purgeDate])

		    if (testRuns.size() > 0) {
			    log.info "Purging ${testRuns.size()} test runs for ${project.name} before ${purgeDate.toString()}"
			    testRuns.each { TestRun testRun ->
				    log.info "Purging ${project.name}: ${testRun.dateExecuted}"
				    testRunService.deleteTestRun(testRun, false)
			    }
			    log.info "Purged ${testRuns.size()} from ${project.name}"
		    }
	    }
    }


}
