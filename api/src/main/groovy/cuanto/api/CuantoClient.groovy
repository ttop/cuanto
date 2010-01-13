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

import com.thoughtworks.xstream.XStream
import cuanto.api.CuantoClientException
import cuanto.api.TestOutcome
import cuanto.api.TestRun
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.methods.StringRequestEntity
import org.apache.commons.httpclient.methods.multipart.FilePart
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity
import org.apache.commons.httpclient.methods.multipart.Part
import sun.misc.BASE64Encoder

/**
 * User: Todd Wells
 * Date: Sep 29, 2008
 * Time: 5:58:07 PM
 *
 */
class CuantoClient {

	String cuantoUrl
	String proxyHost
	Integer proxyPort
	String userId
	String password


	public CuantoClient() {}


	public CuantoClient(String cuantoUrl) {
		this()
		this.cuantoUrl = cuantoUrl
	}


	public Long createProject(String fullName, String projectKey, String testType) {
		def post = getMethod("post", "${cuantoUrl}/project/create")
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


	public void deleteProject(Long id) {
		def post = getMethod("post", "${cuantoUrl}/project/delete")
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


	public Map getProject(Long projectId) {
		return getValueMap("${cuantoUrl}/project/get/${projectId.toString()}")
	}


	public Map getTestRunInfo(Long testRunId) {
		return getValueMap("${cuantoUrl}/testRun/get/${testRunId.toString()}")
	}


	public TestRun getTestRun(Long testRunId) {
		def get = getMethod("get", "${cuantoUrl}/testRun/get/${testRunId.toString()}")
		get.addRequestHeader "Accept", "application/xml"

		def responseCode
		def responseText
		TestRun testRun
		try {
			responseCode = httpClient.executeMethod(get)
			responseText = get.getResponseBodyAsStream().text
			XStream xstream = new XStream()
			testRun = (TestRun) xstream.fromXML(responseText)
		} finally {
			get.releaseConnection()
		}
		return testRun
	}

	
	public Long createTestRun(TestRun testRun) {
		if (!testRun.projectKey) {
			throw new IllegalArgumentException("Project argument must be a valid cuanto project key")
		}

		def post = getMethod("post", "${cuantoUrl}/testRun/createXml")
		XStream xstream = new XStream();
		def request = new StringRequestEntity(xstream.toXML(testRun), "text/xml", null)
		post.requestEntity = request
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


	public TestOutcome getTestOutcome(Long testOutcomeId) {
		def url = "${cuantoUrl}/testOutcome/getXml/${testOutcomeId.toString()}"
		def get = getMethod("get", url)
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
				def outcome = (TestOutcome)xstream.fromXML(responseText)
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
		PostMethod post = getMethod("post", fullUri)
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


	public Long submit(TestOutcome testOutcome, Long testRunId) {
		def fullUri = "${cuantoUrl}/testRun/submitSingleTest"
		PostMethod post = getMethod("post", fullUri)
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
		return getValueMap("${cuantoUrl}/testRun/statistics/${testRunId.toString()}")
	}


	private Map getValueMap(url) {
		def get = getMethod("get", url)
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

	private getAuthHeader(){
		def authHeader = ""
		if (userId && password) {
			 authHeader = "Basic " + new BASE64Encoder().encode((userId + ":" + password).getBytes()) 
		}
		return authHeader
	}


	private getMethod(methodType, url) {
		def method
		if (methodType.toLowerCase() == "get") {
			method = new GetMethod(url)
		} else if (methodType.toLowerCase() == "post") {
			method = new PostMethod(url)
		} else {
			throw new CuantoClientException("Unknown HTTP method: ${methodType}")
		}
		def authHeader = getAuthHeader()
		if (authHeader) {
			method.addRequestHeader("Authorization", authHeader)
		}
		return method

	}
}
