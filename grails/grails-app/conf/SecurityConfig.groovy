security {

	// see DefaultSecurityConfig.groovy for all settable/overridable properties

	active = true
	basicProcessingFilter = true
	cacheUsers = true

	loginUserDomainClass = "cuanto.User"
	authorityDomainClass = "cuanto.Role"
	requestMapClass = "cuanto.Requestmap"

	cookieName = 'cuanto_remember_me'
	rememberMeKey = 'cuantoWebApp'
}
