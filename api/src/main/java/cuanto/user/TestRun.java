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
	Map<String, String> links;
	Map<String, String> testProperties;
	Long id;


	TestRun() {
	}


	public TestRun(Date dateExecuted) {
		if (dateExecuted == null) {
			throw new NullPointerException("null is not a valid value for dateExecuted");
		}
		this.dateExecuted = dateExecuted;
		links = new HashMap<String, String>();
		testProperties = new HashMap<String, String>();
		valid = true;
	}


	TestRun(String projectKey) {
		this.projectKey = projectKey;
		links = new HashMap<String, String>();
		testProperties = new HashMap<String, String>();
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


		JSONObject links = jsonTestRun.getJSONObject("links");
		for (Object urlObj : links.keySet()) {
			String url = (String) urlObj;
			testRun.addLink(url, links.getString(url));
		}

		JSONObject props = jsonTestRun.getJSONObject("testProperties");
		for (Object nameObj : props.keySet()) {
			String name = (String) nameObj;
			testRun.addTestProperty(name, props.getString(name));
		}

		return testRun;
	}


	public String toJSON() {
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
		jsonMap.put("links", links);
		jsonMap.put("testProperties", testProperties);
		return jsonMap;
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
		testProperties.put(name, value);
		return this;
	}


	public TestRun addLink(String url, String description) {
		links.put(url, description);
		return this;
	}


	public void setTestProperty(String name, String value) {
		testProperties.put(name, value);
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


	public Map getLinks() {
		return Collections.unmodifiableMap(links);
	}


	public Map getTestProperties() {
		return Collections.unmodifiableMap(testProperties);
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
