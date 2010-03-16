package cuanto.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The mutable TestRunDetails that an API user uses to create or update a TestRun.
 */
public class TestRunDetails {
	Date dateExecuted;
	List<Link> links;
	List<TestProperty> testProperties;

	public TestRunDetails(Date dateExecuted) {
		if (dateExecuted == null) {
			throw new NullPointerException("null is not a valid value for dateExecuted");
		}
		this.dateExecuted = dateExecuted;
		links = new ArrayList<Link>();
		testProperties = new ArrayList<TestProperty>();
	}

	public TestRunDetails addLink(String url, String description) {
		Link link = new Link(url, description);
		links.add(link);
		return this;
	}

	public TestRunDetails addLink(String url) {
		return addLink(url, null);
	}

	public TestRunDetails addTestProperty(String name, String value) {
		testProperties.add(new TestProperty(name, value));
		return this;
	}


	public Date getDateExecuted() {
		return dateExecuted;
	}


	public List<Link> getLinks() {
		return links;
	}


	public List<TestProperty> getTestProperties() {
		return testProperties;
	}


	public void setDateExecuted(Date dateExecuted) {

		this.dateExecuted = dateExecuted;
	}


	void setLinks(List<Link> links) {
		this.links = links;
	}


	void setTestProperties(List<TestProperty> testProperties) {
		this.testProperties = testProperties;
	}

	public void validate() throws IllegalStateException{
		if (dateExecuted == null) {
			throw new IllegalStateException("null is not a valid value for dateExecuted");
		}
		if (testProperties == null) {
			throw new IllegalStateException("null is not a valid value for testProperties. Use an empty list instead.");
		}
		if (dateExecuted == null) {
			throw new IllegalStateException("null is not a valid value for links. Use an empty list instead");
		}
	}

}
