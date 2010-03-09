package cuanto.user;

import java.net.URL;
import java.net.MalformedURLException;

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
	 * @param cuantoServerUrl The URL of the Cuanto server instance.
	 * @param projectKey The key for the project that this client will be utilizing.
	 * @return The CuantoConnector instance.
	 */
	public static CuantoConnector newInstance(String cuantoServerUrl, String projectKey) {
		return newInstance(cuantoServerUrl, getProjectId(projectKey));
	}


	/**
	 * Create a new instance of CuantoConnector.
	 * @param cuantoServerUrl The URL of the Cuanto server instance.
	 * @param projectId The id for the project that this client will be utilizing.
	 * @return The CuantoConnector instance.
	 */
	public static CuantoConnector newInstance(String cuantoServerUrl, Long projectId) {
		CuantoConnector connector = new CuantoConnector();
		connector.setCuantoUrl(cuantoServerUrl);
		connector.setProjectId(projectId);
		return connector;
	}


	public TestRun createTestRun(TestRunDetails testRunDetails) {
		validateProject();
		try {
			testRunDetails.validate();
		} catch (CuantoException e) {
			throw new RuntimeException(e);
		}
		//todo: implement createTestRun
		return new TestRun();
	}

	public TestRun updateTestRun(TestRunDetails updatedDetails, Long testRunId) {
		validateProject();
		if (testRunId == null) {
			throw new NullPointerException("null is not a valid testRunId");
		}
		// todo: implement updateTestRun
		return new TestRun();
	}


	/**
	 * Get the project ID for the provided project key.
	 * @param projectKey The projectKey associated with the project on the Cuanto server.
	 * @return the server ID value of the project.
	 */
	static Long getProjectId(String projectKey)
	{
		throw new RuntimeException("not implemented");
	}



	private void validateProject() {
		if (projectId == null) {
			throw new RuntimeException("No project ID was specified");
		}
	}


	/**
	 * Get the project ID associated with this client.
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
