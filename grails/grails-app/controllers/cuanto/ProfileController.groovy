/*
 Copyright (c) 2009 Todd Wells

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

class ProfileController {
	def authenticateService
	def userService

	def edit = {
		def currentUser = userService.getUserForUsername(authenticateService.principal().username)
		[user: currentUser]
	}


	def save = {
		def username = authenticateService.principal().username
		def currentUser = userService.getUserForUsername(username)
		if (!params.userRealName) {
			flash.message = "Real Name cannot be blank"
		} else {
			bindData(currentUser, params, [exclude: ["pass", "passwd", "username", "enabled"]])
			if (!currentUser.save()) {
				flash.message = currentUser.errors.allErrors[0].toString()
			} else {
				flash.message = "Updated ${username}"
			}
		}
		redirect (action: 'edit')
	}


	def changePassword = {
		def username = authenticateService.principal().username
		def currentUser = userService.getUserForUsername(username)
		if (params.passwd != params.confpassword) {
			flash.message = "Passwords do not match"
		} else {
			def validationMessage = userService.validatePassword(params.passwd)
			if (validationMessage) {
				flash.message = validationMessage
			} else {
				def newpwd = authenticateService.encodePassword(params.passwd)
				if (newpwd == currentUser.passwd) {
					flash.message = "New password is the same as the old password, please select a new password."
				} else {
					currentUser.passwd = newpwd
					currentUser.changePassword = false
					if (currentUser.save()) {
						flash.message = "Changed password"
					} else {
						flash.message = "There was a problem changing your password"
					}
				}
			}
		}
		redirect (action: 'edit')
	}

}