package cuanto.adapter.listener.testng;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encapsulate necessary information to interact with CuantoConnector.
 */
public class TestNgListenerArguments
{

	// Cuanto url
	private URI cuantoUrl;

	// TestRun.id
	private Long testRunId;

	// TestRun.projectKey
	private String projectKey;

	// TestRun.links
	private Map<String, String> links;

	// TestRun.properties
	private Map<String, String> testProperties;

	// whether to create a new TestRun if testRunId is null
	private Boolean createTestRun;

	public TestNgListenerArguments()
	{
	}

	public TestNgListenerArguments(TestNgListenerArguments arguments)
	{
		cuantoUrl = arguments.getCuantoUrl();
		testRunId = arguments.getTestRunId();
		projectKey = arguments.getProjectKey();
		links = new LinkedHashMap<String, String>(arguments.getLinks());
		testProperties = new LinkedHashMap<String, String>(arguments.getTestProperties());
		createTestRun = arguments.isCreateTestRun();
	}

	public URI getCuantoUrl()
	{
		return cuantoUrl;
	}

	public void setCuantoUrl(URI cuantoUrl)
	{
		this.cuantoUrl = cuantoUrl;
	}

	public Long getTestRunId()
	{
		return testRunId;
	}

	public void setTestRunId(Long testRunId)
	{
		this.testRunId = testRunId;
	}

	public String getProjectKey()
	{
		return projectKey;
	}

	public void setProjectKey(String projectKey)
	{
		this.projectKey = projectKey;
	}

	public Map<String, String> getLinks()
	{
		return links;
	}

	public void setLinks(Map<String, String> links)
	{
		this.links = links;
	}

	public Map<String, String> getTestProperties()
	{
		return testProperties;
	}

	public void setTestProperties(Map<String, String> testProperties)
	{
		this.testProperties = testProperties;
	}

	public Boolean isCreateTestRun()
	{
		return createTestRun;
	}

	public void setCreateTestRun(Boolean createTestRun)
	{
		this.createTestRun = createTestRun;
	}
}
