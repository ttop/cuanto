package cuanto.user;

/**
 * An immutable class that represents a Cuanto server-side TestCase.
 */
public class TestCase {
	String projectKey;
	String packageName;
	String testName;
	String fullName;
	String parameters;
	String description;
	Long id;
	
	TestCase() {
		// no public constructor
	}


	public String getProjectKey() {
		return projectKey;
	}


	public String getPackageName() {
		return packageName;
	}


	public String getTestName() {
		return testName;
	}


	public String getFullName() {
		return fullName;
	}


	public String getParameters() {
		return parameters;
	}


	public String getDescription() {
		return description;
	}


	public Long getId() {
		return id;
	}


	void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}


	void setPackageName(String packageName) {
		this.packageName = packageName;
	}


	void setTestName(String testName) {
		this.testName = testName;
	}


	void setFullName(String fullName) {
		this.fullName = fullName;
	}


	void setParameters(String parameters) {
		this.parameters = parameters;
	}


	void setDescription(String description) {
		this.description = description;
	}


	void setId(Long id) {
		this.id = id;
	}
}
