
package cuanto.user;


import net.sf.json.JSONObject;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collections;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 *
 */
public class TestRun {
	String projectKey;
	String note;
	Date dateCreated;
	Date dateExecuted;
	Boolean valid;
	SortedSet<Link> links;
	SortedSet<TestProperty> testProperties;
	Long id;

	//private final static String JSON_DATE = "yyyy-MM-dd HH:mm:ss.zzz";
	//private final static String jsonDateFormat = "yyyy-MM-dd HH:mm:ss";
	//private final static String jsonDateFormat = "yyyy-MM-DD'T'HH:mm:ssZ";

	//private final static SimpleDateFormat jsonDateFormatter = new SimpleDateFormat(jsonDateFormat);

	TestRun() {}


	public TestRun(Date dateExecuted) {
		if (dateExecuted == null) {
			throw new NullPointerException("null is not a valid value for dateExecuted");
		}
		this.dateExecuted = dateExecuted;
		links = new TreeSet<Link>();
		testProperties = new TreeSet<TestProperty>();
	}


	TestRun(String projectKey) {
		this.projectKey = projectKey;
		links = new TreeSet<Link>();
		testProperties = new TreeSet<TestProperty>();
	}

	
	static TestRun fromJSON(String json) throws ParseException {
		JSONObject jsonTestRun = JSONObject.fromObject(json);
		TestRun testRun = new TestRun(jsonTestRun.getJSONObject("project").getString("projectKey"));
		testRun.setId(jsonTestRun.getLong("id"));
		testRun.setDateExecuted(parseJsonDate(jsonTestRun.getString("dateExecuted")));
		testRun.setDateCreated(parseJsonDate(jsonTestRun.getString("dateCreated")));
		testRun.setNote(jsonTestRun.getString("note"));
		//todo: process links
		//for (Link link : jsonTestRun.getJSONArray("links").)
		return testRun;
	}


	public String toJSON() {
		return "";
	}


	private static Date parseJsonDate(String dateString) throws ParseException {
		return new SimpleDateFormat(CuantoConnector.jsonDateFormat).parse(dateString);
	}


	public TestRun addTestProperty(String name, String value) {
		testProperties.add(new TestProperty(name, value));
		return this;
	}

	public TestRun addLink(String url, String description) {
		Link link = new Link(url, description);
		links.add(link);
		return this;
	}

	public String getProjectKey() {
		return projectKey;
	}


	public String getNote() {
		return note;
	}


	public Date getDateCreated() {
		return dateCreated;
	}


	public Date getDateExecuted() {
		return dateExecuted;
	}


	public Boolean isValid() {
		return valid;
	}


	public SortedSet<Link> getLinks() {
		return Collections.unmodifiableSortedSet(links);
	}


	public SortedSet<TestProperty> getTestProperties() {
		return Collections.unmodifiableSortedSet(testProperties);
	}


	public Long getId() {
		return Long.valueOf(id);
	}


	void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}


	public void setNote(String note) {
		this.note = note;
	}


	void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}


	public void setDateExecuted(Date dateExecuted) {
		this.dateExecuted = dateExecuted;
	}


	public void setValid(Boolean valid) {
		this.valid = valid;
	}


	void setLinks(SortedSet<Link> links) {
		this.links = links;
	}


	void setTestProperties(SortedSet<TestProperty> testProperties) {
		this.testProperties = testProperties;
	}


	void setId(Long id) {
		this.id = id;
	}
}
