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
def targetDir = "${cuantoBase}/dist/target"
def apiDir = "${cuantoBase}/api"

def propfileName = "${cuantoBase}/grails/application.properties"
ant.property(file: propfileName)
def cuantoVersion = ant.project.properties.'app.version'
println "Grails app version is $cuantoVersion"

def pomXml = new XmlSlurper().parse(new File("${apiDir}/pom.xml"))
println "API pom version is ${pomXml.version}"

if (cuantoVersion != pomXml.version) {
	println "\nWARNING: Grails application version and API pom version are mismatched!\n"
}


target(cuantoapi: "Build the Cuanto API") {
	println "Packaging the Cuanto API"

	"mvn -f ${apiDir}/pom.xml clean package".execute().text

	ant.delete(verbose: "true", failonerror: "true") {
		fileset(dir:"lib", includes: "${pomXml.artifactId}-*.jar")
	}
	
	def clientJar = "${apiDir}/target/${pomXml.artifactId}-${pomXml.version}.jar"
	ant.copy(file: clientJar, todir: "lib", verbose: "true")
	ant.copy(file: clientJar, todir: targetDir, verbose: "true")

	grailsSettings.compileDependencies << new File(clientJar)
}


target(cuantowar: "Build the Cuanto WAR") {
	println "Building the Cuanto WAR"
	includeTargets << grailsScript("_GrailsWar")

	ant.copy(file:"cuanto-db.groovy", todir: targetDir)
	depends(war)
	ant.copy(file:"cuanto-${cuantoVersion}.war", todir: targetDir)
}


target(cuantopackage: "Build the Cuanto distributable") {

	def licenseDir = "$targetDir/licenses"

	ant.delete(dir: targetDir)
	ant.delete(dir: releaseDir)

	ant.mkdir(dir: licenseDir)
	ant.mkdir(dir: releaseDir)

	depends(cuantoapi, cuantowar)

	println "Packaging the Cuanto distribution"

	def zipfile = "cuanto-${cuantoVersion}.zip"
	ant.copy(todir: licenseDir) {
		fileset(dir: "${cuantoBase}/licenses", includes: "**/*")
	}

	ant.copy(todir: targetDir) {
		fileset(dir: "${cuantoBase}") {
			include(name: "COPYING*")
		}
	}

	ant.zip(basedir:targetDir, destfile: "${releaseDir}/${zipfile}")
}

