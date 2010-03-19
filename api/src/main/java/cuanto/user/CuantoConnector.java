package cuanto.user;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.io.IOException;

/**
 *
 */
public class CuantoConnector {

	private final static String HTTP_USER_AGENT = "Cuanto Java Client 2.4.0; Jakarta Commons-HttpClient/3.1";

	private String projectKey;
	private String cuantoUrl;
	private String proxyHost;
	private Integer proxyPort;
	private static final String HTTP_GET = "get";
	private static final String HTTP_POST = "post";
	public final static String jsonDateFormat = "yyyy-MM-DD'T'HH:mm:ssZ";


	private CuantoConnector() {
		// must use factory method to instantiate
	}


	/**
	 * Create a new instance of CuantoConnector.
	 *
	 * @param cuantoServerUrl The URL of the Cuanto server instance.
	 * @param projectKey      The key for the project that this client will be utilizing.
	 * @return The CuantoConnector instance.
	 */
	public static CuantoConnector newInstance(String cuantoServerUrl, String projectKey) {
		return newInstance(cuantoServerUrl, projectKey, null, null);
	}


	public static CuantoConnector newInstance(String cuantoServerUrl, String projectKey, String proxyHost,
		Integer proxyPort) {
		CuantoConnector connector = new CuantoConnector();
		connector.setCuantoUrl(cuantoServerUrl);
		connector.setProjectKey(projectKey);
		connector.setProxyHost(proxyHost);
		connector.setProxyPort(proxyPort);
		return connector;
	}


	/**
	 * Get the TestRun from the Cuanto server.
	 *
	 * @param testRunId The TestRun to retrieve.
	 * @return The retrieved TestRun
	 */
	public TestRun getTestRun(Long testRunId) {
		GetMethod get = (GetMethod) getHttpMethod(HTTP_GET, getCuantoUrl() + "/api/getTestRun/" + testRunId.toString());

		TestRun testRun;
		try {
			getHttpClient().executeMethod(get);
			String out = get.getResponseBodyAsString();
			testRun = TestRun.fromJSON(out);
			return testRun;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ParseException e) {
			throw new RuntimeException("Unable to parse JSON response", e);
		}
	}


	/**
	 * A TestRun represents tests that were executed together. Create a new TestRun on the Cuanto server using the values provided. The projectKey will be assigned the same
	 * projectKey as this CuantoConnector. The testRun passed in will have it's id value assigned. 
	 *
	 * @param testRun The test run to create
	 * @return The created TestRun.
	 */
	public TestRun createTestRun(TestRun testRun) {
		validateProject();
		//todo: implement createTestRun

		PostMethod post = (PostMethod) getHttpMethod(HTTP_POST, getCuantoUrl() + "/api/createTestRun");

		HttpClient httpClient = getHttpClient();

		return new TestRun();
	}


	private HttpClient getHttpClient() {
		HttpClient client = new HttpClient();
		if (getProxyHost() != null && getProxyPort() != null) {
			client.getHostConfiguration().setProxy(getProxyHost(), getProxyPort());
		}
		return client;
	}


	private HttpMethod getHttpMethod(String methodType, String url) {
		HttpMethod method;
		if (methodType.toLowerCase().equals(HTTP_GET)) {
			method = new GetMethod(url);
		} else if (methodType.toLowerCase() == HTTP_POST) {
			method = new PostMethod(url);
		} else {
			throw new RuntimeException("Unknown HTTP method: ${methodType}");
		}
		method.setRequestHeader("User-Agent", HTTP_USER_AGENT);
		return method;
	}


	/**
	 * Update the TestRun with this id on the Cuanto Server to have all the properties specified in this
	 * TestRun. If the TestRun argument does not already have an id, it needs to be retrieved from the server as you
	 * can't set the ID on a TestRun directly. You can either retrieve the TestRun from the server by querying by ID or
	 * other values. If the TestRun does not already exist, then use createTestRun instead.
	 *
	 * @param testRun a TestRun with the updated values
	 *
	 * @return The updated TestRun.
	 */
	public TestRun updateTestRun(TestRun testRun) {
		validateProject();
		if (testRun == null) {
			throw new NullPointerException("null is not a valid testRunId");
		}
		// todo: implement updateTestRun
		return new TestRun();
	}


	/**
	 * Create a new TestOutcome for the specified TestRun on the Cuanto server using the details provided.  The ID value
	 * on the testOutcome argument will be set upon successful creation.
	 *
	 * @param testOutcome The TestOutcome to be created on the Cuanto server.
	 * @param testRunId   The TestRun to which the TestOutcome should be added.
	 * @return The server representation of the TestOutcome.
	 */
	public TestOutcome createTestOutcome(TestOutcome testOutcome, Long testRunId) {
		//todo: implement createTestOutcome

		//todo: populate TestOutcome with the projectKey
		return new TestOutcome();
	}


	/**
	 * Create a new TestOutcome that is not associated with any TestRun. This is probably not what you want, use
	 * createTestOutcome(TestOutcomeDetails testOutcomeDetails, Long testRunId) instead.
	 *
	 * @param testOutcome The details that should be assigned to the new TestOutcome.
	 * @return The server representation of the TestOutcome.
	 */
	public TestOutcome createTestOutcome(TestOutcome testOutcome) {
		//todo: implement createTestOutcome
		//todo: populate TestOutcome with the projectKey

		return new TestOutcome();
	}


	/**
	 * Update a TestOutcome on the Cuanto server with the details provided.
	 *
	 * @param testOutcome The new details that will replace the corresponding values of the existing TestOutcome
	 * @return The TestOutcome with the updated values.
	 */
	public TestOutcome updateTestOutcome(TestOutcome testOutcome) {
		// todo: implement updateTestOutcome
		// todo: throw exception if testOutcome doesn't have an ID. It should be retrieved from the server first.
		return new TestOutcome();
	}


	/**
	 * Get the specified TestOutcome from the server.
	 *
	 * @param testOutcomeId The ID of the TestOutcome to retrieve.
	 * @return The retrieved TestOutcome.
	 */
	public TestOutcome getTestOutcome(Long testOutcomeId) {
		// todo: implement getTestOutcome
		return new TestOutcome();
	}


	/**
	 * Get all TestOutcomes for the specified TestCase in the specified TestRun. In most normal Cuanto usages, a TestRun
	 * will only have a single TestOutcome per TestCase.
	 *
	 * @param testRunId  The ID of the TestRun to search.
	 * @param testCaseId The ID of the TestCase for which to retrieve TestOutcomes.
	 * @return A list of all the TestOutcomes for the specified TestCase and TestRun
	 */
	public List<TestOutcome> getTestOutcomes(Long testRunId, Long testCaseId) {
		//todo: implement getTestOutcomes
		return new ArrayList<TestOutcome>();
	}


	/**
	 * Get all TestOutcomes for the specified TestRun. TODO: specify order?
	 *
	 * @param testRunId The ID of the TestRun.
	 * @return The TestOutcomes for the specified TestRun.
	 */
	public List<TestOutcome> getAllTestOutcomes(Long testRunId) {
		//todo: implement getAllTestOutcomes
		return new ArrayList<TestOutcome>();
	}




	/**
	 * Get all TestRuns that include the specified TestProperties. The properties can be a subset of a TestRun's
	 * properties, but all of the specified properties must match for a TestRun to be returned.
	 *
	 * @param testProperties The properties for which to search.
	 * @return All TestRuns that contain the specified properties. A zero-length array is returned if no matching TestRuns
	 *         are found.
	 */
	public List<TestRun> getTestRunsWithProperties(List<TestProperty> testProperties) {
		//todo: implement getTestRunsWithProperties
		return new ArrayList<TestRun>();
	}


	/**
	 * Get a test case on the server that corresponds to the specified values.
	 *
	 * @param testPackage A test package is the namespace for a particular test. In the case of JUnit or TestNG, it would
	 *                    be the fully qualified class name, e.g. org.myorg.MyTestClass
	 * @param testName    The name of the test, in JUnit or TestNG this would be the method name.
	 * @param parameters  A string representing the parameters for this test, if it is a parameterized test. Otherwise this
	 *                    should be null. The server will attempt to locate the TestCase that has these parameters. If the
	 *                    parameters don't match, a TestCase will not be returned.
	 * @return The found TestCase or null if no match is found.
	 */
	public TestCase getTestCase(String testPackage, String testName, String parameters) {
		//todo: implement getTestCase
		return new TestCase();
	}


	/**
	 * Get a test case on the server that corresponds to the specified values.
	 *
	 * @param testPackage A test package is the namespace for a particular test. In the case of JUnit or TestNG, it would
	 *                    be the fully qualified class name, e.g. org.myorg.MyTestClass
	 * @param testName    The name of the test, in JUnit or TestNG this would be the method name.
	 * @return The found TestCase or null if no match is found.
	 */
	public TestCase getTestCase(String testPackage, String testName) {
		return getTestCase(testPackage, testName, null);
	}


	/**
	 * Get the project ID for the provided project key.
	 *
	 * @param projectKey The projectKey associated with the project on the Cuanto server.
	 * @return the server ID value of the project.
	 */
	static Long getProjectId(String projectKey) {
		throw new IllegalArgumentException("Couldn't find project with Project Key" + projectKey);
		//throw new RuntimeException("not implemented");
	}


	private void validateProject() {
		//if (projectId == null) {
		//	throw new IllegalStateException("No project ID was specified");
		//}
	}


	public String getCuantoUrl() {
		return cuantoUrl;
	}


	void setCuantoUrl(String cuantoUrl) {
		try {
			new URL(cuantoUrl);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		if (cuantoUrl.endsWith("/")) {
			cuantoUrl = cuantoUrl.substring(0, cuantoUrl.lastIndexOf('/')); // todo: does this include the slash?
		}
		this.cuantoUrl = cuantoUrl;
	}


	private String getProxyHost() {
		return proxyHost;
	}


	private void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}


	private Integer getProxyPort() {
		return proxyPort;
	}


	private void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}


	public String getProjectKey() {
		return projectKey;
	}


	void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}
}
