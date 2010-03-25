package cuanto.user;

import net.sf.json.JSONObject;
import net.sf.json.JSONNull;

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


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TestCase testCase = (TestCase) o;

		if (description != null ? !description.equals(testCase.description) : testCase.description != null)
			return false;
		if (fullName != null ? !fullName.equals(testCase.fullName) : testCase.fullName != null) return false;
		if (id != null ? !id.equals(testCase.id) : testCase.id != null) return false;
		if (packageName != null ? !packageName.equals(testCase.packageName) : testCase.packageName != null)
			return false;
		if (parameters != null ? !parameters.equals(testCase.parameters) : testCase.parameters != null) return false;
		if (projectKey != null ? !projectKey.equals(testCase.projectKey) : testCase.projectKey != null) return false;
		if (testName != null ? !testName.equals(testCase.testName) : testCase.testName != null) return false;

		return true;
	}


	@Override
	public int hashCode() {
		int result = projectKey != null ? projectKey.hashCode() : 0;
		result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
		result = 31 * result + (testName != null ? testName.hashCode() : 0);
		result = 31 * result + (fullName != null ? fullName.hashCode() : 0);
		result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
		result = 31 * result + (description != null ? description.hashCode() : 0);
		result = 31 * result + (id != null ? id.hashCode() : 0);
		return result;
	}


	public static TestCase fromJSON(String jsonString) {
		JSONObject jsonTestCase = JSONObject.fromObject(jsonString);
		return fromJSON(jsonTestCase);
	}


	static TestCase fromJSON(JSONObject jsonTestCase) {
		TestCase testCase = new TestCase();
		if (!(jsonTestCase.get("packageName") instanceof JSONNull)) {
			testCase.setPackageName(jsonTestCase.getString("packageName"));
		}
		testCase.setTestName(jsonTestCase.getString("testName"));

		if (!(jsonTestCase.get("parameters") instanceof JSONNull)) {
			testCase.setParameters(jsonTestCase.getString("parameters"));
		}
		if (!(jsonTestCase.get("description") instanceof JSONNull)) {
			testCase.setDescription(jsonTestCase.getString("description"));
		}
		if (!(jsonTestCase.get("id") instanceof JSONNull)) {
			testCase.setId(jsonTestCase.getLong("id"));
		}
		if (!(jsonTestCase.get("fullName") instanceof JSONNull)) {
			testCase.setFullName(jsonTestCase.getString("fullName"));
		}
		return testCase;
	}
}
