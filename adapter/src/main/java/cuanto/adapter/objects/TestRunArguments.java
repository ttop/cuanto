package cuanto.adapter.objects;

import java.util.Map;

/**
 * Encapsulate necessary information to interact with CuantoConnector.
 */
public class TestRunArguments {
	// TestRun.id
	private Long testRunId;

	// TestRun.projectKey
	private String projectKey;

	// TestRun.links
	private Map<String, String> links;

	// TestRun.properties
	private Map<String, String> testProperties;

	public Long getTestRunId() {
		return testRunId;
	}

	public void setTestRunId(Long testRunId) {
		this.testRunId = testRunId;
	}

	public String getProjectKey() {
		return projectKey;
	}

	public void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}

	public Map<String, String> getLinks() {
		return links;
	}

	public void setLinks(Map<String, String> links) {
		this.links = links;
	}

	public Map<String, String> getTestProperties() {
		return testProperties;
	}

	public void setTestProperties(Map<String, String> testProperties) {
		this.testProperties = testProperties;
	}
}
