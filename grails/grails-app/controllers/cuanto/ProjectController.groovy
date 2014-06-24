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

import grails.converters.JSON
import java.text.SimpleDateFormat

class ProjectController {
	def projectService
	def testRunService
	def testCaseFormatterRegistry
	def dataService

	// the delete, save and update actions only accept POST requests
	static def allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

	SimpleDateFormat dateFormat = new SimpleDateFormat(Defaults.dateFormat)
	SimpleDateFormat chartDateFormat = new SimpleDateFormat(Defaults.chartDateFormat)

	def index = { redirect(action: mason, params: params) }

	def delete = {
		def project = Project.get(params.id)
		String projectName = project.name
		projectService.queueForDeletion(project)
		render "Project ${projectName} queued for deletion."
	}


	def save = {
		def project
		if (params.project) {
			project = projectService.updateProject(params)
		} else {
			project = projectService.createProject(params)
		}

		if (project.validate()) {
			render project.toJSONMap() as JSON
		} else {
			def errMsg
			if (project.hasErrors()) {
				def errors = ""
				project.errors.allErrors.each { err ->
					errors += "${err.field} rejected value \"${err.rejectedValue}\". "
					errors += "Error: "
					errors += err.codes[err.codes.size() - 1]
				}
				errMsg = errors
			} else {
				errMsg = "Unknown validation error saving Project"
			}
			response.status = response.SC_BAD_REQUEST
			def myJSON = ['error': errMsg]
			render myJSON as JSON
		}
	}

	def select = {
		[projects: Project.listOrderByName()]
	}


	def history = {
		def proj
		if (params.id) {
			proj = dataService.getProject(params.id)
		} else if (params.projectKey) {
			proj = projectService.getProject(params.projectKey)
		}

		if (!proj) {
			flash.message = "Please select a project."
			redirect(controller:"project", action:"mason")
		} else {
			withFormat {
				html {
					if (!proj) {
						redirect(controller: 'project', view: 'mason')
					} else {
						def chartUrl = testRunService.getGoogleChartUrlForProject(proj)
						def propNames = testRunService.getTestRunPropertiesByProject(proj)
						def propString = "[" + propNames.collect{'"' + it + '"'}.join(",") + "]"
						render(view: "history", model: [project: proj, 'chartUrl': chartUrl, 'propNames': propString])
					}
				}
				json {

					def testRuns = testRunService.getTestRunsForProject(params)
					def totalCount = dataService.countTestRunsByProject(proj)
					def jsonRuns = []
					testRuns.each { testRun ->
						Map run = getJsonForTestRun(testRun, false)
						if (run) {
							jsonRuns += run
						}
					}

					def myJson = [count: jsonRuns.size(), 'testRuns': jsonRuns, 'totalCount': totalCount]
					if (params.containsKey("offset")) {
						myJson.offset = Integer.valueOf(params.offset)
					}
					render myJson as JSON
				}
			}
		}
	}


	def projectHeader = {
		def proj
		if (params.id) {
			proj = dataService.getProject(params.id)
		}
		if (proj) {
			render(template: "phHeader", model:[project: proj])
		} else {
			response.status = response.SC_NOT_FOUND
			render "Project ${params.id} not found"
		}
	}


	def groupHistory = {
		if (params.group) {
			def testRuns = testRunService.getTestRunsForGroupName(params.group)
			def jsonRuns = []
			testRuns.each { testRun ->
				Map run = getJsonForTestRun(testRun, false)
				if (run) {
					jsonRuns += run
				}
			}
			def myJson = [count: jsonRuns.size(), 'testRuns': jsonRuns]
			render myJson as JSON
		}
	}


	def get = {
		def proj = Project.get(params.id)
		if (proj) {
			withFormat {
				text {
				  ['project':proj]
				}
				json {
					render proj.toJSONMap() as JSON
				}
				xml {
					render xstream.toXML(proj?.toProjectApi())
				}
			}
		} else {
			response.status = response.SC_NOT_FOUND
			render "Project ${params.id} not found"
		}
	}

	
	def getByKey = {
		def proj = projectService.getProject(params.id)
		if (proj) {
			withFormat {
				xml {
					render xstream.toXML(proj?.toProjectApi())
				}
			}
		} else {
			response.status = response.SC_NOT_FOUND
			render "Project ${params.id} not found"
		}
	}


	def newProject = {
		[formatters: testCaseFormatterRegistry.formatterList]
	}


	def feed = {
		if (params.id) {
			Project proj = Project.get(params.id)
            if (proj) {
                render(feedType: "rss", feedVersion: "2.0") {
                    title = "${proj.toString()} Recent Results"
                    link = "http://your.test.server/yourController/feed"
                    description = "Recent Test Results for ${proj.toString()}"

                    List<TestRun> testRuns = testRunService.getTestRunsForProject([id: proj.id, offset: 0, max: Defaults.ItemsPerFeed,
                        sort: "dateExecuted", order: "desc"])

                    testRuns.each {testRun ->
                        def stats = TestRunStats.findByTestRun(testRun)
                        if (stats){
                            entry(testRun.dateExecuted) {
                                link = createLink(controller: "testRun", action: "summary", id: testRun.id)
                                def feedTxt = testRunService.getFeedText(testRun, stats)
                                return feedTxt
                            }
                        }
                    }
                }
            } else {
                response.status = response.SC_NOT_FOUND
                render "Project ${params.id} not found"
            }

        }
	}


	def list = {
		def project = null
		if (params.id) {
			project = Project.get(params.id)
		}
		[groups: projectService.getAllGroupNames(), projects: dataService.getAllProjects(), 'project': project,
			formatters: testCaseFormatterRegistry.formatterList]
	}


	def mason = {
		def project = null
		if (params.id) {
			project = Project.get(params.id)
		}
		if (params.refresh) {
			render(template: "mason", model: ['project': project, groupsToProjectsMap: projectService.getProjectMap()])
		} else {
			render(view: "mason", model: ['project': project, groupsToProjectsMap: projectService.getProjectMap()])
		}
	}


	def masonProj = {
		def group = null
		if (params.id) {
			group = ProjectGroup.get(params.id)
		}
		render(template:"masonProj", model:[projects: group.projects.sort()])
	}


	def listOnly = {
		def project = null
		if (params.id) {
			project = Project.get(params.id)
		}
		render(template: "projectList", model:[groups: projectService.getAllGroupNames(),
			projects: dataService.getAllProjects(), 'project': project])
	}


	def groups = {
		[groups: projectService.getAllGroupNames()]
	}

	def listGroup = {
		if (params.group) {
			def group = params.group.replaceAll("%20", " ").replaceAll("\\+", " ")
			return [projects: dataService.getProjectsByGroupName(group), 'group': group,
				formatters: testCaseFormatterRegistry.formatterList]
		} else {
			redirect(controller: 'project', view: 'mason')
		}
	}


	def listGroupTable = {
		def group = ProjectGroup.get(params.id)
		def groupProjects = null
		if (group) {
			groupProjects = dataService.getProjectsByGroup(group)
		}
		render (template: "listGroupTable", model:[projects: groupProjects])
	}


	def groupNames = {
		def groups = dataService.getProjectGroupsWithPrefix(params.query)
		def names = groups.collect {
			['name': it.name]
		}
		def groupNameJson = ['groups': names]
		render groupNameJson as JSON
	}


	def projectMap = {
		def groups = projectService.getProjectMap()
		render "TBD"
	}

	synchronized def getJsonForTestRun(testRun, graph) {
		def stats = TestRunStats.findByTestRun(testRun)
		if (stats) {
			def friendlyDate
			if (graph) {
				friendlyDate = chartDateFormat.format(testRun.dateExecuted)
			} else {
				friendlyDate = dateFormat.format(testRun.dateExecuted)
			}

			def numAnalyzed;
			if (stats && stats.failed >= 0) {
				numAnalyzed = "${stats?.analyzed} of ${stats?.failed}"
			} else {
				numAnalyzed = ""
			}

			return [
                    projectName: testRun?.project?.name,
                    projectKey: testRun?.project?.projectKey,
                    id: testRun?.id,
                    dateExecuted: friendlyDate,
                    testProperties: testRun?.jsonTestProperties(),
                    dateCreated: testRun?.dateCreated,
                    note: testRun?.note,
                    valid: testRun?.valid,
                    successRate: stats?.successRate ?: 0,
				    successRateChange: stats?.successRateChange,
                    tests: stats?.tests,
                    passed: stats?.passed,
                    failed: stats?.failed,
                    skipped: stats?.skipped,
                    quarantined: stats?.quarantined ?: 0,
                    totalDuration: stats?.totalDuration,
                    averageDuration: stats?.averageDuration,
                    numAnalyzed: numAnalyzed,
                    tags: testRun?.tags?.collect{it.name}?.sort()
            ]
		} else {
			return null
		}
	}
}

