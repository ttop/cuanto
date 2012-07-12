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

class BugService {

    boolean transactional = false
	def dataService

	Bug getBug(final String title, final String url) {
		if (!title && !url) {
			return null
		}

		def localTitle = title
		def localUrl = url
		[localTitle, localUrl].each {
			if (it == "null") {it = null}
			it = it?.trim()
		}

		def bug = null

		if (localTitle || localUrl) {
			bug = Bug.findByTitleAndUrl(localTitle, localUrl)
		}
		if (bug) {
			return bug
		} else {
			bug = new Bug('title': localTitle, 'url': localUrl)
			dataService.saveDomainObject(bug)
		}
		return bug
	}

	
}
