package cuanto.user;

import java.util.List;
import java.util.Date;
import java.util.Collections;
import java.util.ArrayList;

/**
 * The immutable TestRun as retrieved from the Cuanto server.
 */
public class TestRun {
	String projectKey;
	String note;
	String dateCreated;
	Date dateExecuted;
	Boolean valid;
	List<Link> links;
	List<TestProperty> testProperties;
	Long id;


	TestRun() {}


	TestRun(String projectKey) {
		this.projectKey = projectKey;
		links = new ArrayList<Link>();
		testProperties = new ArrayList<TestProperty>();
	}


	public String getProjectKey() {
		return projectKey;
	}


	public String getNote() {
		return note;
	}


	public String getDateCreated() {
		return dateCreated;
	}


	public Date getDateExecuted() {
		return dateExecuted;
	}


	public Boolean isValid() {
		return valid;
	}


	public List<Link> getLinks() {
		return Collections.unmodifiableList(links);
	}


	public List<TestProperty> getTestProperties() {
		return Collections.unmodifiableList(testProperties);
	}


	public Long getId() {
		return Long.valueOf(id);
	}


	/**
	 * Get a new TestRunDetails object with details that correspond to this TestRun's values
	 * @return A new TestRunDetails object with details that correspond to this TestRun's values 
	 */
	public TestRunDetails getTestRunDetails() {
		TestRunDetails details = new TestRunDetails(this.dateExecuted);
		details.setLinks(new ArrayList<Link>(links));
		details.setTestProperties(new ArrayList<TestProperty>(testProperties));
		return details;
	}


	void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}


	void setNote(String note) {
		this.note = note;
	}


	void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}


	void setDateExecuted(Date dateExecuted) {
		this.dateExecuted = dateExecuted;
	}


	void setValid(Boolean valid) {
		this.valid = valid;
	}


	void setLinks(List<Link> links) {
		this.links = links;
	}


	void setTestProperties(List<TestProperty> testProperties) {
		this.testProperties = testProperties;
	}


	void setId(Long id) {
		this.id = id;
	}
}
