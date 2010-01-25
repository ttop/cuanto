/*
 Copyright (c) 2008 thePlatform, Inc.

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

package cuanto.api

import org.apache.tools.ant.BuildException
import org.apache.tools.ant.types.FileSet
import cuanto.api.Link
import java.text.SimpleDateFormat
import org.apache.tools.ant.taskdefs.Property
import cuanto.api.TestProperty
import cuanto.api.TestRun
import cuanto.api.TestRun
import cuanto.api.CuantoClient

class CuantoAntTask extends org.apache.tools.ant.Task {
	URL url
	String userId
	String password
	String proxyHost
	String proxyPort
	File resultFile
	String testType // here just for backward compatibility
	String testProject
	String milestone // deprecated
	String targetEnv // deprecated
	String build // deprecated
	String date
	String dateFormat
	String action = "submit"
	protected static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

	List<FileSet> filesets = []
	List<Link> links = []
	List<Property> properties = []

	public void execute() {
		if (!testProject) {
			throw new BuildException("The project attribute needs to be specified")
		}

		action = action.toLowerCase().trim()
		
		if ( action == "submit") {
			submit()
		}
	}

	
	private def getCuantoClient() {
		def cuantoClient = new CuantoClient(url.toString())
		cuantoClient.userId = userId
		cuantoClient.password = password
		
		if (dateFormat) {
			cuantoClient.dateFormat = dateFormat
		}

		if (proxyHost && proxyPort) {
			cuantoClient.proxyHost = proxyHost
			cuantoClient.proxyPort = Integer.valueOf(proxyPort)
		}
		return cuantoClient
	}


	private void submit() {
		def cuantoClient = getCuantoClient()

		TestRun testRun = new TestRun()
		testRun.projectKey = testProject

		if (date) {
			def dateFormatToUse = dateFormat ? dateFormat : DEFAULT_DATE_FORMAT
			testRun.dateExecuted = new SimpleDateFormat(dateFormatToUse).parse(date)
		}

		testRun.links = links

		// process deprecated attributes
		["build", "milestone", "targetEnv"].each { String propName ->
			def propVal = getProperty(propName) as String
			if (propVal) {
				testRun.testProperties << new TestProperty(propName, propVal)
				log "cuanto task attribute '${propName}' is deprecated, use a nested property node instead",
					this.project.MSG_WARN
			}
		}

		properties.each {Property prop ->
			testRun.testProperties << new TestProperty(prop.name, prop.value)
		}

		def testRunId = cuantoClient.createTestRun(testRun)
		log "Submitting results to ${url}"

		def startTime = new Date()
		cuantoClient.submitFiles(getFilesToSubmit(), testRunId)
		def endTime = new Date()
		def duration = (endTime.time - startTime.time) / 1000
		log "Submitting results took ${duration} second(s)"
	}


	private def getFilesToSubmit() {
		def files = []

		if (filesets) {
			for (theFileSet in filesets) {
				def ds = theFileSet.getDirectoryScanner(getProject())
				def basedir = ds.getBasedir()
				ds.getIncludedFiles().each {filename ->
					files += new File(basedir, filename)
				}
			}
		} else {
			if (!resultFile) {
				throw new BuildException("No files found!")
			}
			files += resultFile
		}
		return files
	}


	public FileSet createFileset() {
		FileSet fset = new FileSet()
		filesets.add fset
		return fset
	}


	public Link createLink() {
		def link = new Link()
		links << link
		return link
	}

	
	public Property createProperty(){
		def prop = new Property()
		properties << prop
		return prop
	}
}