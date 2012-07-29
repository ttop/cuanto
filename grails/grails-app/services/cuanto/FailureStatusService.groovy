/*
	Copyright (c) 2010 Suk-Hyun Cho

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

import org.hibernate.StaleObjectStateException

class FailureStatusService
{

	def dataService
	boolean transactional = false

	def queueFailureStatusUpdateForOutcomes(affectedOutcomes) {
		if (affectedOutcomes) {
			def notNullOutcomes = affectedOutcomes.findAll { it != null && it.id != null }

			notNullOutcomes.each { affectedOutcome ->
				def existingTask = FailureStatusUpdateTask.findByTargetIdAndType(
					affectedOutcome.id, TestOutcome.class.name)
				if (existingTask)
					return

				log.info "adding affected test outcomes ${notNullOutcomes*.id} to failure status update queue"
				dataService.saveDomainObject(new FailureStatusUpdateTask(affectedOutcome))
			}
		}
	}

	def queueFailureStatusUpdateForOutcome(affectedOutcome) {
		queueFailureStatusUpdateForOutcomes([affectedOutcome])
	}


	def queueFailureStatusUpdateForRun(affectedTestRun) {
		if (affectedTestRun && affectedTestRun.id) {
			def existingTask = FailureStatusUpdateTask.findByTargetIdAndType(affectedTestRun.id, TestRun.class.name)
			if (existingTask)
				return

			try {
				log.info "adding test run ${affectedTestRun.id} to failure status update queue"
				dataService.saveDomainObject(new FailureStatusUpdateTask(affectedTestRun))
			} catch (StaleObjectStateException e) {
				log.info "StaleObjectStateException for test run ${affectedTestRun.testRunId}"
			}
		}
	}
}
