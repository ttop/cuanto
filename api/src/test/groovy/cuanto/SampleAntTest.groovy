package cuanto
/**
 * User: Todd Wells
 * Date: May 14, 2009
 * Time: 5:51:55 AM
 * 
 */
class SampleAntTest extends GroovyTestCase {

	void testSampleAntHasCorrectVersion() {
		def apiPath = "api"
		def pom = new File("${apiPath}/pom.xml")
		println "Looking for pom at ${pom.absolutePath}"
		def pomXml = new XmlSlurper().parse(pom)
		def expectedJarName = "target/${pomXml.artifactId}-${pomXml.version}.jar"

		def sampleAnt = new File("${apiPath}/sample_ant.xml")
		def sampleAntXml = new XmlSlurper().parse(sampleAnt)
		def antValue = sampleAntXml.target.find {it.@name == "init"}.taskdef.@classpath

		assertEquals "${sampleAnt.toString()} taskdef jar name doesn't match the pom version", expectedJarName, antValue
	}
}