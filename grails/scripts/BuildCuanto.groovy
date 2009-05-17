includeTargets << grailsScript("Init")

target(main: "Build the CuantoClient and the Cuanto web app") {

	println "Packaging the Cuanto API"
	def apiPath = "../api"

	// attempt to mvn package the api
	"mvn -f ${apiPath}/pom.xml clean package".execute().text

	def pomXml = new XmlSlurper().parse(new File("${apiPath}/pom.xml"))
	def clientJar = "${apiPath}/target/${pomXml.artifactId}-${pomXml.version}.jar"
	ant.copy(file: clientJar, todir: "lib", verbose: "true")
}

setDefaultTarget(main)
