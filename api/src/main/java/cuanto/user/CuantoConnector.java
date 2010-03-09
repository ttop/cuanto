package cuanto.user;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;

/**
 *
 */
public class CuantoConnector {

	private Long projectId;
	private String cuantoUrl;


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
		return newInstance(cuantoServerUrl, getProjectId(projectKey));
	}


	/**
	 * Create a new instance of CuantoConnector.
	 *
	 * @param cuantoServerUrl The URL of the Cuanto server instance.
	 * @param projectId       The id for the project that this client will be utilizing.
	 * @return The CuantoConnector instance.
	 */
	public static CuantoConnector newInstance(String cuantoServerUrl, Long projectId) {
		CuantoConnector connector = new CuantoConnector();
		connector.setCuantoUrl(cuantoServerUrl);
		connector.setProjectId(projectId);
		return connector;
	}


	/**
	 * Create a new TestRun on the Cuanto server using the details provided.
	 *
	 * @param testRunDetails The TestRun details to use when creating the new TestRun.
	 * @return The created TestRun.
	 */
	public TestRun createTestRun(TestRunDetails testRunDetails) {
		validateProject();
		testRunDetails.validate();
		//todo: implement createTestRun
		return new TestRun();
	}


	/**
	 * Update a TestRun on the Cuanto server using the details provided.
	 *
	 * @param updatedDetails The TestRun details to use when updating the existing TestRun.
	 * @param testRunId      The ID of the TestRun to update.
	 * @return The updated TestRun.
	 */
	public TestRun updateTestRun(TestRunDetails updatedDetails, Long testRunId) {
		validateProject();
		if (testRunId == null) {
			throw new NullPointerException("null is not a valid testRunId");
		}
		// todo: implement updateTestRun
		return new TestRun();
	}


	/**
	 * Create a new TestOutcome for the specified TestRun on the server using the details provided.
	 *
	 * @param testOutcomeDetails The details that should be assigned to the new TestOutcome.
	 * @param testRunId          The TestRun to which the TestOutcome should be added.
	 * @return The server representation of the TestOutcome.
	 */
	public TestOutcome createTestOutcome(TestOutcomeDetails testOutcomeDetails, Long testRunId) {
		//todo: implement createTestOutcome
		return new TestOutcome();
	}


	/**
	 * Create a new TestOutcome that is not associated with any TestRun. This is probably not what you want, use
	 * createTestOutcome(TestOutcomeDetails testOutcomeDetails, Long testRunId) instead.
	 *
	 * @param testOutcomeDetails The details that should be assigned to the new TestOutcome.
	 * @return The server representation of the TestOutcome.
	 */
	public TestOutcome createTestOutcome(TestOutcomeDetails testOutcomeDetails) {
		//todo: implement createTestOutcome
		return new TestOutcome();
	}


	/**
	 * Update a TestOutcome on the Cuanto server with the details provided.
	 *
	 * @param testOutcomeDetails The new details that will replace the corresponding values of the existing TestOutcome
	 * @param testOutcomeId      The ID of the existing TestOutcome to update.
	 * @return The TestOutcome with the updated values.
	 */
	public TestOutcome updateTestOutcome(TestOutcomeDetails testOutcomeDetails, Long testOutcomeId) {
		// todo: implement updateTestOutcome
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
	 * Get the TestRun from the Cuanto server.
	 *
	 * @param testRunId The TestRun to retrieve.
	 * @return The retrieved TestRun
	 */
	public TestRun getTestRun(Long testRunId) {
		// todo: implement getTestRun
		return new TestRun();
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
		if (projectId == null) {
			throw new IllegalStateException("No project ID was specified");
		}
	}


	/**
	 * Get the project ID associated with this client.
	 *
	 * @return the Project ID associate with this client
	 */
	public Long getProjectId() {
		return Long.valueOf(projectId);
	}


	void setProjectId(Long projectId) {
		this.projectId = projectId;
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
		this.cuantoUrl = cuantoUrl;
	}
}
