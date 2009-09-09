/**
 * User: Todd Wells
 * Date: Sep 9, 2009
 * Time: 12:54:36 PM
 * 
 */

grails.war.resources = { stagingDir, args ->
	delete { fileset dir: "$stagingDir/WEB-INF/lib", includes: "hsqldb*.jar" }
}

