/*
	Copyright (c) 2009 Todd E. Wells

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


def cuantoBase = grailsSettings.baseDir.toString() + "/.."
def releaseDir = "${cuantoBase}/dist/release"
def apiDir = "${cuantoBase}/api"
def sqlDir = "${cuantoBase}/grails/sql"
def javadocDir = "${apiDir}/target/site/apidocs"

def propfileName = "${cuantoBase}/grails/application.properties"
ant.property(file: propfileName)
def cuantoVersion = ant.project.properties.'app.version'
println "Grails app version is $cuantoVersion"

def targetDir = "${cuantoBase}/dist/target"
def zipDir = "${targetDir}/cuanto-${cuantoVersion}"
def targetApiDir = "${zipDir}/api"
def targetSqlDir = "${zipDir}/sql"

def sqlFileCount;

def pomXml = new XmlSlurper().parse(new File("${apiDir}/pom.xml"))
println "API pom version is ${pomXml.version}"

if (cuantoVersion.toString() != pomXml.version.toString()) {
	println "\nWARNING: Grails application version and API pom version are mismatched!\n"
}


target(cuantoapi: "Build the Cuanto test API") {
	println "Packaging the Cuanto test API"

	// Update the Cuanto Java Client version to match the grails application version.
	String userAgent = "final static String HTTP_USER_AGENT = \"Java CuantoConnector ${cuantoVersion.toString()}; Jakarta Commons-HttpClient/3.1\";"
	ant.replaceregexp(file: "${apiDir}/src/main/java/cuanto/api/CuantoConnector.java",
		match: '(.+)final static String HTTP_USER_AGENT.+', replace: "\\1${userAgent}")

	def packageProcess = "mvn -f ${apiDir}/pom.xml clean package".execute()
	packageProcess.waitFor()
	if (packageProcess.exitValue() != 0) {
		ant.fail(message: "Packaging API failed:\n " + packageProcess.text)
	}
	"mvn -f ${apiDir}/pom.xml dependency:copy-dependencies -DexcludeTransitive=true -DexcludeScope=provided -DexcludeArtifactIds=junit".execute().text

	def javadocProcess = "mvn -f ${apiDir}/pom.xml javadoc:javadoc".execute()
	javadocProcess.waitFor()
	if (javadocProcess.exitValue() != 0) {
		ant.fail(message: "Creating JavaDocs failed:\n" + javadocProcess.text)
	}

	ant.delete(verbose: "true", failonerror: "true") {
		fileset(dir:"lib", includes: "${pomXml.artifactId}-*.jar")
	}
	
	def distClientJar = "${apiDir}/target/${pomXml.artifactId}-${pomXml.version}.jar"
	ant.copy(file: distClientJar, todir: targetApiDir, verbose: "true")
	ant.copy(todir: targetApiDir, verbose: "true") {
		fileset(dir:"${apiDir}/target/dependency", includes: "*.jar")
	}

	grailsSettings.compileDependencies << new File(distClientJar)
}


target(cuantowar: "Build the Cuanto WAR") {
	println "Building the Cuanto WAR"
	includeTargets << grailsScript("_GrailsWar")

	ant.copy(file:"cuanto-db.groovy", todir: zipDir)
	depends(war)
	ant.copy(file:"target/cuanto-${cuantoVersion}.war", todir: zipDir)
}

target(cuantosql: "Copy the Cuanto SQL files") {
	ant.resourcecount(property: "sql.count") {
		fileset(dir: cuantoBase, includes:"grails/sql/*.sql")
	}
	sqlFileCount = Integer.valueOf(ant.project.properties["sql.count"])
	
	if (sqlFileCount > 0) {
		println "Copying the Cuanto SQL files"
		ant.mkdir(dir: targetSqlDir)
		ant.copy(todir: targetSqlDir, verbose: "true") {
			fileset(dir:"sql", includes: "*")
		}
	} else {
		println "No SQL files were found for including in the distribution"
	}
}


target(cuantopackage: "Build the Cuanto distributable") {

	def licenseDir = "$zipDir/licenses"

	ant.delete(dir: targetDir)
	ant.delete(dir: releaseDir)

	ant.mkdir(dir: licenseDir)
	ant.mkdir(dir: releaseDir)
	ant.mkdir(dir: targetApiDir)

	depends(cuantoapi, cuantowar, cuantosql)

	println "Packaging the Cuanto distribution"

	def zipfile = "cuanto-${cuantoVersion}.zip"
	ant.copy(todir: licenseDir) {
		fileset(dir: "${cuantoBase}/licenses", includes: "**/*")
	}

	ant.copy(todir: zipDir) {
		fileset(dir: "${cuantoBase}") {
			include(name: "COPYING*")
		}
	}

	ant.copy(todir: "${targetApiDir}/javadoc") {
		fileset(dir:javadocDir, includes: "**/*")
	}
	
	ant.copy(todir: zipDir, file: "${cuantoBase}/dist/INSTALL")
	ant.zip(basedir:targetDir, destfile: "${releaseDir}/${zipfile}")
}

target(name: "sqlCount") {
	ant.resourcecount(property: "sql.count") {
		fileset(dir: cuantoBase, includes:"grails/sql/*.sql")
	}
	sqlFileCount = Integer.valueOf(ant.project.properties["sql.count"])
}