/*
	Copyright (c) 2010 Todd E. Wells

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

import groovy.sql.Sql

username = "root"
password = ""
driverClassName = "com.mysql.jdbc.Driver"
url = "jdbc:mysql://localhost:3306/cuanto?autoreconnect=true"


sql = Sql.newInstance(url, username, password, driverClassName)
sql.execute("""CREATE TABLE test_run_property (
id bigint(20) NOT NULL AUTO_INCREMENT,
version bigint(20) NOT NULL,
name varchar(255) NOT NULL,
value varchar(255) NOT NULL,
test_run_id bigint(20) NOT NULL,
test_properties_idx int(11) DEFAULT NULL,
PRIMARY KEY (id))""")

sql.eachRow("select id, milestone, build, target_env from test_run") {

	def index = 0
	// Build
	if (it.build) {
		sql.execute("insert into test_run_property (name, value, version, test_run_id, test_properties_idx) values ('Build', ${it.build}, 0, ${it.id}, ${index++})")
	}

	// Milestone
	if (it.milestone) {
		sql.execute("insert into test_run_property (name, value, version, test_run_id, test_properties_idx) values ('Milestone', ${it.milestone}, 0, ${it.id}, ${index++})")
	}

	// TargetEnv
	if (it.target_env) {
		sql.execute("insert into test_run_property (name, value, version, test_run_id, test_properties_idx) values ('Target Environment', ${it.target_env}, 0, ${it.id}, ${index++})")
	}
}

sql.execute("alter table test_run drop column build")
sql.execute("alter table test_run drop column milestone")
sql.execute("alter table test_run drop column target_env")


