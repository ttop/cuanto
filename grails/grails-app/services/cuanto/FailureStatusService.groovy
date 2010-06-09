package cuanto

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
class FailureStatusService {

	def dataService
	boolean transactional = false

	def queueFailureStatusUpdateForOutcomes(affectedOutcomes) {
		if (affectedOutcomes) {
			log.info "adding test outcomes ${affectedOutcomes*.id} to failure status update queue"
             def notNullOutcomes = affectedOutcomes.findAll {
                it != null
            }
			def updateTasksForAffectedOutcomes = notNullOutcomes.collect { affectedOutcome ->
				new FailureStatusUpdateTask(affectedOutcome)
			}
			dataService.saveTestOutcomes(updateTasksForAffectedOutcomes)
		}
	}

	def queueFailureStatusUpdateForOutcome(affectedOutcome) {
		queueFailureStatusUpdateForOutcomes([affectedOutcome])
	}

	def queueFailureStatusUpdateForRun(affectedTestRun) {
		if (affectedTestRun) {
			log.info "adding test run ${affectedTestRun.id} to failure status update queue"
			dataService.saveDomainObject(new FailureStatusUpdateTask(affectedTestRun))
		}
	}
}
