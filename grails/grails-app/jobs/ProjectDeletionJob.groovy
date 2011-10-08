import cuanto.Project
/*
 Copyright (c) 2011 Todd Wells

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

class ProjectDeletionJob {

	def projectService
	static triggers = {
		simple name: 'ProjectDeletionJob', startDelay: 20000, repeatInterval: 60000
	}

	def concurrent = false

	def execute() {
		log.debug "executing ${this.class.simpleName}"
		def toDelete = Project.findAllByDeleted(true)
		toDelete.each {
			def pg = it.projectGroup?.name
			def startTime = new Date().time
			log.info("Deleting project ${it.name}")
			projectService.deleteProject(it)
			def elapsed = new Date().time - startTime
			log.info("Project ${it.name} deleted, ${elapsed} ms elapsed.")
		}
	}
}
