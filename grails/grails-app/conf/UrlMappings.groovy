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



class UrlMappings {
	static mappings = {
		"/$controller/$action?/$id?"
			{
				constraints {
					// apply constraints here
				}
			}
		"500"(view: '/error')
		"/"(controller: "project", action: "mason")
		"/project/new/$id?"(controller: "project", action: "newProject")
		"/group/$group"(controller: "project", action: "listGroup")
		"/project/groupHistory/$group"(controller: "project", action: "groupHistory")
		"/help"(view: '/help/index.gsp')
		"/testRun/analysis/$id"(controller: "testRun", action: "results")
		"/testRun/latest/$projectKey"(controller: "testRun", action: "results")
		"/show/$projectKey"(controller: "project", action: "history")
		"/testCase/rename/$project"(controller: "testCase", action: "rename")
	}
}
