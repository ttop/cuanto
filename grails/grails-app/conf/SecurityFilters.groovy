class SecurityFilters {
	def userService
	def authenticateService

	def filters = {
		changePassword(controller: '*', action: '*') {
			before = {
				if (authenticateService.securityConfig.security.active) {
					if (controllerName != "login" && controllerName != "logout" && controllerName != "profile") {
						def currentUser = userService.getUserForUsername(authenticateService.principal().username)
						if (currentUser.changePassword) {
							flash.message = " Please change your password"
							redirect(controller: "profile", action: "edit")
						}
					}
				}
			}
			after = {

			}
			afterView = {

			}
		}
	}

}
