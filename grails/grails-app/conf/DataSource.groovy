dataSource {
	pooled = false
	driverClassName = "org.hsqldb.jdbcDriver"
	username = "sa"
	password = ""
}
hibernate {
    cache.use_second_level_cache=true
    cache.use_query_cache=true
    cache.provider_class='org.hibernate.cache.EhCacheProvider'
}
// environment specific settings
environments {
	development {
		dataSource {
			dbCreate = "update" // one of 'create', 'create-drop','update'
			driverClassName = application.config.dataSource.driverClassName
			url = application.config.dataSource.url
			username = application.config.dataSource.username
			password = application.config.dataSource.password
		}
	}
	test {
		dataSource {
			dbCreate = "update"
			url = "jdbc:hsqldb:mem:testDb"
		}
	}
	production {
		dataSource {
			dbCreate = "update"
			boolean pooling = true
			driverClassName = application.config.dataSource.driverClassName
			url = application.config.dataSource.url
			username = application.config.dataSource.username
			password = application.config.dataSource.password
		}
	}
}