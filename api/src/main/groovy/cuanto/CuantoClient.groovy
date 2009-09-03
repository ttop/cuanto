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


package cuanto

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.methods.multipart.FilePart
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity
import org.apache.commons.httpclient.methods.multipart.Part
import com.thoughtworks.xstream.XStream
import org.apache.commons.httpclient.methods.StringRequestEntity
import org.apache.commons.httpclient.HttpStatus

/**
 * User: Todd Wells
 * Date: Sep 29, 2008
 * Time: 5:58:07 PM
 *
 */
class CuantoClient {

	String cuantoUrl
	String dateFormat
	String proxyHost
	Integer proxyPort


	public CuantoClient() {
		dateFormat = "yyyy-MM-dd HH:mm:ss"
	}


	public CuantoClient(String cuantoUrl) {
		this()
		this.cuantoUrl = cuantoUrl
	}


	public Long createProject(String fullName, String projectKey, String testType) {
		def post = new PostMethod("${cuantoUrl}/project/create")
		post.addParameter "name", fullName
		post.addParameter "projectKey", projectKey
		post.addParameter "testType", testType

		def responseCode
		def responseText
		try {
			responseCode = httpClient.executeMethod(post)
			responseText = post.getResponseBodyAsStream().text
		} finally {
			post.releaseConnection()
		}

		def projectId
		if (responseCode == 200) {
			try {
				projectId = Long.valueOf(responseText)
			} catch (NumberFormatException e) {
			    throw new CuantoClientException("Couldn't parse project ID response: ${responseText}")
			}
		} else if (responseCode == 500) {
			throw new CuantoClientException(responseText)
		}
		else {
			throw new CuantoClientException("HTTP Response code ${responseCode}: ${responseText}")
		}
		return projectId
	}


	void deleteProject(Long id) {
		def post = new PostMethod("${cuantoUrl}/project/delete")
		post.addParameter "id", id.toString()
		post.addParameter "client", ""

		def responseCode
		def responseText
		try {
			responseCode = httpClient.executeMethod(post)
			responseText = post.getResponseBodyAsStream().text
		} finally {
			post.releaseConnection()
		}

		if (responseCode != 200) {
			throw new CuantoClientException("HTTP Response code ${responseCode}: ${responseText}")
		}
	}


	Map getProject(Long projectId) {
		return getValueMap("${cuantoUrl}/project/get/${projectId.toString()}")
	}


	Map getTestRunInfo(Long testRunId) {
		return getValueMap("${cuantoUrl}/testRun/get/${testRunId.toString()}")
	}


	public Long getTestRunId(String project, String dateExecuted, String milestone, String build, String targetEnv) {
		if (!project) {
			throw new IllegalArgumentException("Project argument must be a valid cuanto project")
		}

		def post = new PostMethod("${cuantoUrl}/testRun/create")
		post.addParameter "project", project

		if (dateExecuted) {
			post.addParameter "dateExecuted", dateExecuted
			post.addParameter "dateFormat", dateFormat
		}
		if (milestone) {
			post.addParameter "milestone", milestone
		}
		if (build) {
			post.addParameter "build", build
		}
		if (targetEnv) {
			post.addParameter "targetEnv", targetEnv
		}

		def testRunId = null
		try {
			def responseCode = httpClient.executeMethod(post)
			def responseText = post.getResponseBodyAsStream().text.trim()
			if (responseCode == 200) {
				testRunId = Long.valueOf(responseText)
			} else if (responseCode == 403) {
				throw new IllegalArgumentException(responseText)
			} else {
				throw new CuantoClientException("HTTP Response code ${responseCode}: ${responseText}")
			}
		} finally {
			post.releaseConnection()
		}
		return testRunId

	}

	public ParsableTestOutcome getTestOutcome(Long testOutcomeId) {
		def url = "${cuantoUrl}/testOutcome/getXml/${testOutcomeId.toString()}"
		def get = new GetMethod(url)
		get.addRequestHeader "Accept", "text/xml"

		def responseCode
		def responseText
		try {
			responseCode = httpClient.executeMethod(get)
			responseText = get.getResponseBodyAsStream().text
			if (responseCode == HttpStatus.SC_NOT_FOUND) {
				return null
			} else {
				XStream xstream = new XStream()
				def outcome = (ParsableTestOutcome)xstream.fromXML(responseText)
				return outcome
			}
		} finally {
			get.releaseConnection()
		}

	}

	public void submit(File file, Long testRunId) {
		submit([file], testRunId)
	}
	

	public void submit(List<File> files, Long testRunId) {
		def fullUri = "${cuantoUrl}/testRun/submitFile"
		PostMethod post = new PostMethod(fullUri)
		post.addRequestHeader "Cuanto-TestRun-Id", testRunId.toString()

		def parts = []
		files.each { file ->
			parts += new FilePart(file.getName(), file)
		}
		post.requestEntity = new MultipartRequestEntity(parts as Part[], post.params)

		def responseCode
		def responseText
		try {
			HttpClient hclient = getHttpClient()
			responseCode = hclient.executeMethod(post)
			responseText = post.getResponseBodyAsStream().text
		} finally {
			post.releaseConnection()
		}
		if (responseCode != 200) {
			throw new CuantoClientException("HTTP Response code ${responseCode}: ${responseText}")
		}
	}


	public Long submit(ParsableTestOutcome testOutcome, Long testRunId) {
		def fullUri = "${cuantoUrl}/testRun/submitSingleTest"
		PostMethod post = new PostMethod(fullUri)
		post.addRequestHeader "Cuanto-TestRun-Id", testRunId.toString()

		XStream xstream = new XStream()
		def outcomeXml = xstream.toXML(testOutcome)
		post.requestEntity = new StringRequestEntity(outcomeXml, "text/xml", null)

		def responseCode
		def responseText
		def testOutcomeId
		try {
			HttpClient hclient = getHttpClient()
			responseCode = hclient.executeMethod(post)
			responseText = post.getResponseBodyAsStream().text
			testOutcomeId = Long.valueOf(responseText)
		} finally {
			post.releaseConnection()
		}
		if (responseCode != 200) {
			throw new CuantoClientException("HTTP Response code ${responseCode}: ${responseText}")
		}
		return testOutcomeId 
	}

	Map getTestRunStats(Long testRunId){
		return getValueMap("${cuantoUrl}/testRun/statistics/${testRunId.toString()}?calculate=t")
	}


	private Map getValueMap(url) {
		def get = new GetMethod(url)
		get.addRequestHeader "Accept", "text/plain"

		def responseCode
		def responseText
		try {
			responseCode = httpClient.executeMethod(get)
			responseText = get.getResponseBodyAsStream().text
		} finally {
			get.releaseConnection()
		}

		def valueMap = [:]

		if (responseCode == 200) {
			responseText.eachLine {String line ->
				def rawValues = line.split("=")
				if (rawValues.size() > 1) {
					valueMap[rawValues[0].trim()] = rawValues[1].trim()
				}
			}
		} else {
			throw new CuantoClientException("HTTP Response code ${responseCode}: ${responseText}")
		}
		return valueMap
	}


	private HttpClient getHttpClient() {
		HttpClient httpClient = new HttpClient()
		if (proxyHost && proxyPort) {
			httpClient.hostConfiguration.setProxy proxyHost, proxyPort
		}
		return httpClient
	}
  }
