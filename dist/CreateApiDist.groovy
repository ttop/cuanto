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

// Create the distribution file for the client

def ant = new AntBuilder()
def targetDir = 'target'
def licenseDir = "$targetDir/licenses"
def apiPath = "../api"
def releaseDir = "release"

ant.property(file: "../grails/application.properties")
def version = ant.project.properties.'app.version'
def zipfile = "cuanto-api-client-${version}.zip"

ant.delete(dir: targetDir)
ant.delete(dir: releaseDir)

ant.mkdir(dir: licenseDir)
ant.mkdir(dir: releaseDir)
println "Packaging the Cuanto API"

// attempt to mvn package the api
"mvn -f ${apiPath}/pom.xml clean package".execute().text

def pomXml = new XmlSlurper().parse(new File("${apiPath}/pom.xml"))
def clientJar = "${apiPath}/target/${pomXml.artifactId}-${pomXml.version}.jar"

ant.copy(file: clientJar, todir: "target", verbose: "true")

// copy dependency licenses to dir
ant.copy(todir: licenseDir) {
	fileset(dir: "../licenses", includes: "**/*")
}

ant.copy(todir: targetDir) {
	fileset(dir: "..") {
		include(name: "COPYING*")
	}
}

ant.zip(basedir:targetDir, destfile: "${releaseDir}/${zipfile}")

