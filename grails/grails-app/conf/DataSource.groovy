dataSource {
	pooled = false
	driverClassName = "hack.dataSourceConfigFileCouldNotBeRead.NonExistentDriver"
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
//			lotsOfExtraProjects = true
		}
	}
	test {
		dataSource {
			dbCreate = "update"
			driverClassName = "org.hsqldb.jdbcDriver"
			url = "jdbc:hsqldb:mem:testDb"
			username = "sa"
			password = ""
		}
	}
	production {
		dataSource {
			dbCreate = "update"
			pooled = true
			driverClassName = application.config.dataSource.driverClassName
			url = application.config.dataSource.url
			username = application.config.dataSource.username
			password = application.config.dataSource.password
			properties {
				maxActive = 10
				maxIdle = 8
				minIdle = 5
				initialSize = 5
				minEvictableIdleTimeMillis = 1800000
				timeBetweenEvictionRunsMillis = 1800000
				maxWait = 10000
			}
		}
	}
}