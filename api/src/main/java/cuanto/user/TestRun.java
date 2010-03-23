package cuanto.user;


import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

import java.util.*;
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
	Date lastUpdated;
	Boolean valid;
	SortedSet<Link> links;
	SortedSet<TestProperty> testProperties;
	Long id;


	TestRun() {
	}


	public TestRun(Date dateExecuted) {
		if (dateExecuted == null) {
			throw new NullPointerException("null is not a valid value for dateExecuted");
		}
		this.dateExecuted = dateExecuted;
		links = new TreeSet<Link>();
		testProperties = new TreeSet<TestProperty>();
		valid = true;
	}


	TestRun(String projectKey) {
		this.projectKey = projectKey;
		links = new TreeSet<Link>();
		testProperties = new TreeSet<TestProperty>();
		valid = true;
	}


	static TestRun fromJSON(String json) throws ParseException {
		JSONObject jsonTestRun = JSONObject.fromObject(json);
		TestRun testRun = new TestRun(jsonTestRun.getJSONObject("project").getString("projectKey"));
		testRun.setId(jsonTestRun.getLong("id"));
		testRun.setDateExecuted(parseJsonDate(jsonTestRun.getString("dateExecuted")));
		testRun.setDateCreated(parseJsonDate(jsonTestRun.getString("dateCreated")));
		testRun.setLastUpdated(parseJsonDate(jsonTestRun.getString("lastUpdated")));
		testRun.setNote(jsonTestRun.getString("note"));

		for (Object jsonLink : jsonTestRun.getJSONArray("links")) {
			JSONObject jlink = (JSONObject) jsonLink;
			testRun.addLink(jlink.getString("url"), jlink.getString("description"));
		}

		for (Object jsonProp : jsonTestRun.getJSONArray("testProperties")) {
			JSONObject testProp = (JSONObject) jsonProp;
			testRun.addTestProperty(testProp.getString("name"), testProp.getString("value"));
		}

		return testRun;
	}


	public String toJSON() {
		/* {"id":2622,"dateCreated":"2010-03-18T08:17:03-0700","dateExecuted":"2010-03-18T08:17:03-0700","valid":true,
		"project":{"id":113,"name":"CuantoClientTest","projectGroup":{"name":"Sample","id":9},"projectKey":"ClientTest",
		"bugUrlPattern":"http://url/{BUG}","testType":{"name":"JUnit","id":1}},
		"note":null,"links":[{"description":"Test Link 1","url":"http://testlink1"},{"description":"Test Link 2","url":"http://testlink2"}],"testProperties":[{"name":"build","value":"233093"},{"name":"Custom Property 1","value":"Custom value 1"},{"name":"Custom Property 2","value":"Custom value 2"},{"name":"milestone","value":"1.0"},{"name":"targetEnv","value":"test lab"}]} */

		JSONObject jsonTestRun = JSONObject.fromObject(toJsonMap());
		return jsonTestRun.toString();
	}


	Map toJsonMap() {
		Map jsonMap = new HashMap();
		jsonMap.put("id", this.id);
		jsonMap.put("dateExecuted", toJsonDate(this.dateExecuted));
		jsonMap.put("valid", this.valid);
		jsonMap.put("projectKey", this.projectKey);
		jsonMap.put("note", this.note);
		jsonMap.put("links", JSONArray.fromObject(getJsonLinks()));
		jsonMap.put("testProperties", JSONArray.fromObject(getJsonTestProperties()));
		return jsonMap;
	}


	private List getJsonTestProperties() {
		if (this.testProperties == null) {
			return null;
		} else {
			List propList = new ArrayList();
			for (TestProperty testProp : this.testProperties) {
				Map propMap = new HashMap();
				propMap.put("name", testProp.getName());
				propMap.put("value", testProp.getValue());
				propList.add(propMap);
			}
			return propList;
		}
	}


	private List getJsonLinks() {
		if (this.links == null) {
			return null;
		} else {
			List linkList = new ArrayList();
			for (Link link : this.links) {
				Map linkMap = new HashMap();
				linkMap.put("description", link.getDescription());
				linkMap.put("url", link.getUrl());
				linkList.add(linkMap);
			}
			return linkList;
		}
	}


	private static Date parseJsonDate(String dateString) throws ParseException {
		return new SimpleDateFormat(CuantoConnector.jsonDateFormat).parse(dateString);
	}


	private String toJsonDate(Date date) {
		if (date == null) {
			return null;
		} else {
			return new SimpleDateFormat(CuantoConnector.jsonDateFormat).format(date);
		}
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
		return id;
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


	public Date getLastUpdated() {
		return lastUpdated;
	}


	void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

}
