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

import grails.util.GrailsUtil

class BootStrap {

	def dataService
	def initializationService
	def statisticService
	def grailsApplication

	def init = {servletContext ->
		def ds = grailsApplication.config.dataSource
		if (GrailsUtil.environment != "test" && !ds.driverClassName) {
			log.fatal "No database driver found! Is cuanto-db.groovy in the classpath?"
			System.exit(1)
		}

		switch (GrailsUtil.environment) {
			case "development":
				break
			case "test":
				break
			case "production":
				break
		}

		initializationService.initializeAll()
		statisticService.updateTestRunsWithoutAnalysisStats()
	}

	def destroy = {
	}


}