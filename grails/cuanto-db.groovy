/*
   This file needs to be placed either in the classpath or in ~/.grails/
   (valid locations specified in conf/Config.groovy/grails.config.locations)
*/

environments {
	production {
		dataSource {
			pooled = true
			username = "my_sql_user"
			password = "my_sql_password"
			driverClassName = "com.mysql.jdbc.Driver"
			url = "jdbc:mysql://my_sql_server:3306/cuanto?autoreconnect=true"
		}

	}
	development {
		dataSource {
			pooled = true
			username = "root"
			password = ""
			driverClassName = "com.mysql.jdbc.Driver"
			url = "jdbc:mysql://localhost:3306/cuanto?autoreconnect=true"
		}
	}
}

