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
sql.execute("CREATE TABLE test_property (id bigint(20) NOT NULL AUTO_INCREMENT, version bigint(20) NOT NULL,  name varchar(255) NOT NULL,  value varchar(255) NOT NULL,  PRIMARY KEY (id))")
sql.execute("""CREATE TABLE test_run_test_property (
  test_run_test_properties_id bigint(20) DEFAULT NULL,
  test_property_id bigint(20) DEFAULT NULL,
  test_properties_idx int(11) DEFAULT NULL,
  KEY fk_test_property (test_property_id),
  CONSTRAINT fk_test_property FOREIGN KEY (test_property_id) REFERENCES test_property (id))""")


sql.eachRow("select id, milestone, build, target_env from test_run") {

	def index = 0
	// Build
	if (it.build) {
		sql.execute("insert into test_property (name, value, version) values ('Build', ${it.build}, 0)")
		def buildId = sql.firstRow("select LAST_INSERT_ID()")[0]
		sql.execute("insert into test_run_test_property (test_run_test_properties_id, test_property_id, test_properties_idx) values (${it.id}, ${buildId}, ${index++})")
	}

	// Milestone
	if (it.milestone) {
		sql.execute("insert into test_property (name, value, version) values ('Milestone', ${it.milestone}, 0)")
		def milestoneId = sql.firstRow("select LAST_INSERT_ID()")[0]
		sql.execute("insert into test_run_test_property (test_run_test_properties_id, test_property_id, test_properties_idx) values (${it.id}, ${milestoneId}, ${index++})")
	}

	// TargetEnv
	if (it.target_env) {
		sql.execute("insert into test_property (name, value, version) values ('Target Environment', ${it.target_env}, 0)")
		def targId = sql.firstRow("select LAST_INSERT_ID()")[0]
		sql.execute("insert into test_run_test_property (test_run_test_properties_id, test_property_id, test_properties_idx) values (${it.id}, ${targId}, ${index++})")
	}
}

sql.execute("alter table test_run drop column build")
sql.execute("alter table test_run drop column milestone")
sql.execute("alter table test_run drop column target_env")

