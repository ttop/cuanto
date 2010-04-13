/*

 Copyright (c) 2010 Todd Wells

 This file is part of Cuanto, a test results repository and analysis program.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package cuanto.api;


import net.sf.json.JSONObject;
import net.sf.json.JSONNull;
import net.sf.json.JSONArray;

import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;


/**
 * A class that represents the TestOutcome of a particular TestCase. A TestOutcome is the outcome of executing a
 * TestCase and it's associated analysis.
 */
public class TestOutcome {
	TestCase testCase;
	TestResult testResult;
	TestRun testRun;
	String testOutput;
	Long duration;
	String owner;
	Bug bug;
	String note;
	Long id;
	AnalysisState analysisState;
	Date startedAt;
	Date finishedAt;
	Date dateCreated;
	Date lastUpdated;
	String projectKey;
	List<String> tags = new ArrayList<String>();


	TestOutcome() {
		// no public constructor
	}


	/**
	 * Creates a new TestOutcome for the named test case. This is not added to the Cuanto server until
	 * CuantoConnector.addTestOutcome() is called.
	 *
	 * @param testCasePackageName The packageName of the TestCase. A package is a namespace for a particular test. In java
	 *                            (for instance, JUnit and TestNG), it will most often correspond to the fully-qualified
	 *                            java class name of a particular test method. For example, org.myorganization.my.package.TestClassName.
	 * @param testCaseTestName    The name of the TestCase. This is usually the method name of the test.
	 * @param testResult          The result of executing the TestCase.
	 * @return The new TestOutcome object.
	 */
	public static TestOutcome newInstance(String testCasePackageName, String testCaseTestName, TestResult testResult) {
		return newInstance(testCasePackageName, testCaseTestName, null, testResult);
	}


	/**
	 * Creates a new TestOutcome for the named test case. This is not added to the Cuanto server until
	 * CuantoConnector.addTestOutcome() is called.
	 *
	 * @param testCasePackageName The packageName of the TestCase. A package is a namespace for a particular test. In java
	 *                            (for instance, JUnit and TestNG), it will most often correspond to the fully-qualified
	 *                            java class name of a particular test method. For example, org.myorganization.my.package.TestClassName.
	 * @param testCaseTestName    The name of the TestCase. This is usually the method name of the test.
	 * @param testCaseParameters  The parameters of this TestCase. Parameters is a string that should represent the actual
	 *                            parameters passed to the TestCase. In the case of JUnit and TestNG, this is the
	 *                            parameters joined by ", ", for instance "arg1, arg2, arg3".
	 * @param testResult          The result of executing the TestCase.
	 * @return The new TestOutcome object.
	 */
	public static TestOutcome newInstance(String testCasePackageName, String testCaseTestName,
		String testCaseParameters,
		TestResult testResult) {
		TestCase testCase = new TestCase();

		testCase = new TestCase();
		testCase.setPackageName(testCasePackageName);
		testCase.setTestName(testCaseTestName);
		testCase.setParameters(testCaseParameters);

		TestOutcome testOutcome = new TestOutcome();
		testOutcome.testCase = testCase;
		testOutcome.testResult = testResult;
		return testOutcome;
	}


	/**
	 * Gets the TestCase associated with this TestOutcome.
	 *
	 * @return The TestCase associated with this TestOutcome.
	 */
	public TestCase getTestCase() {
		return testCase;
	}


	/**
	 * Gets the TestResult associated with this TestOutcome.
	 *
	 * @return The TestResult associated with this TestOutcome.
	 */
	public TestResult getTestResult() {
		return testResult;
	}


	/**
	 * Gets the test output associated with this TestOutcome, if it is available. TestOutcomes retrieved from the Cuanto
	 * server do not return the test output by default. You can fetch the test output for a particular TestOutcome by
	 * calling CuantoConnector.getTestOutput(). Note that normally output is only recorded for test failures.
	 *
	 * @return The test output if available, null otherwise.
	 */
	public String getTestOutput() {
		return testOutput;
	}


	/**
	 * Gets the duration of this TestOutcome's execution in milliseconds.
	 *
	 * @return The duration of this TestOutcome in milliseconds.
	 */
	public Long getDuration() {
		return duration;
	}


	/**
	 * Gets the owner of this TestOutcome. Owner is just an arbitrary string, it can be set to anything.
	 *
	 * @return The owner of this TestOutcome.
	 */
	public String getOwner() {
		return owner;
	}


	/**
	 * Gets the bug associated with this TestOutcome, if any.
	 *
	 * @return The bug associated with this TestOutcome or null if there isn't one.
	 */
	public Bug getBug() {
		return bug;
	}


	/**
	 * Gets the note associated with this TestOutcome or null if there isn't one.
	 * @return The note associated with this TestOutcome or null if there isn't one.
	 */
	public String getNote() {
		return note;
	}


	/**
	 * Gets the server-assigned ID for this TestOutcome. This will only be populated if the TestOutcome was fetched from
	 * the Cuanto server.
	 * @return The server-assigned ID for this TestOutcome, or null if there isn't one.
	 */
	public Long getId() {
		return id;
	}


	/**
	 * Gets the analysis state associated with this TestOutcome, if any.
	 *
	 * @return The analysis state for this TestOutcome.
	 */
	public AnalysisState getAnalysisState() {
		return analysisState;
	}


	/**
	 * Gets the time this TestOutcome's execution was started, if available.
	 *
	 * @return The time this TestOutcome's execution was started, or null if unavailable.
	 */
	public Date getStartedAt() {
		return startedAt;
	}


	/**
	 * Gets the time this TestOutcome's execution was finished, if available.
	 *
	 * @return The time this TestOutcome's execution was finished, or null if unavailable.
	 */
	public Date getFinishedAt() {
		return finishedAt;
	}


	/**
	 * Gets the time this TestOutcome was created on the Cuanto server.
	 *
	 * @return The time this TestOutcome was created on the Cuanto server, or null if not available.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}


	/**
	 * Gets the time this TestOutcome was last updated on the Cuanto server.
	 *
	 * @return The time this TestOutcome was last updated on the Cuanto server, or null if not available.
	 */
	public Date getLastUpdated() {
		return lastUpdated;
	}


	void setTestCase(TestCase testCase) {
		this.testCase = testCase;
	}


	/**
	 * Sets the TestResult for this TestOutcome.
	 *
	 * @param testResult The TestResult.
	 */
	public void setTestResult(TestResult testResult) {
		this.testResult = testResult;
	}


	/**
	 * Sets the output for this TestOutcome. By convention, the output should only be provided if the test failed or
	 * otherwise did not successfully pass.
	 *
	 * @param testOutput The output of the test failure.
	 */
	public void setTestOutput(String testOutput) {
		this.testOutput = testOutput;
	}


	/**
	 * Sets the duration of the execution for this TestOutcome.
	 *
	 * @param duration The duration of the execution in milliseconds.
	 */
	public void setDuration(Long duration) {
		this.duration = duration;
	}


	/**
	 * Sets the owner of this TestOutcome. Owner is just an arbitrary string, it can be set to anything.
	 *
	 * @param owner The owner.
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}


	/**
	 * Sets the bug associated with this TestOutcome.
	 *
	 * @param bug The bug.
	 */
	public void setBug(Bug bug) {
		this.bug = bug;
	}


	/**
	 * Sets the note associated with this TestOutcome.
	 *
	 * @param note The note.
	 */
	public void setNote(String note) {
		this.note = note;
	}


	void setId(Long id) {
		this.id = id;
	}


	/**
	 * Sets the analysis state associated with this TestOutcome.
	 *
	 * @param analysisState The analysis state.
	 */
	public void setAnalysisState(AnalysisState analysisState) {
		this.analysisState = analysisState;
	}


	/**
	 * Sets the time this TestOutcome's execution started.
	 * @param startedAt The time this TestOutcome's execution started.
	 */
	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}


	/**
	 * Sets the time this TestOutcome's execution finished.
	 *
	 * @param finishedAt The time this TestOutcome's execution finished.
	 */
	public void setFinishedAt(Date finishedAt) {
		this.finishedAt = finishedAt;
	}


	void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}


	void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}


	/**
	 * Gets the TestRun associated with this TestOutcome, if any.
	 *
	 * @return The TestRun associated with this TestOutcome, or null if there is none.
	 */
	public TestRun getTestRun() {
		return testRun;
	}


	void setTestRun(TestRun testRun) {
		this.testRun = testRun;
	}


	void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}


	String getProjectKey() {
		return projectKey;
	}



	static TestOutcome fromJSON(String jsonString) throws ParseException {
		JSONObject jsonOutcome = JSONObject.fromObject(jsonString);
		return fromJSON(jsonOutcome);
	}


	static TestOutcome fromJSON(JSONObject jsonOutcome) throws ParseException {
		TestOutcome testOutcome = new TestOutcome();
		testOutcome.setId(jsonOutcome.getLong("id"));

		TestCase testCase = new TestCase();
		JSONObject jsonTestCase = jsonOutcome.getJSONObject("testCase");
		testCase = TestCase.fromJSON(jsonTestCase);
		testOutcome.setTestCase(testCase);

		testOutcome.setTestResult(TestResult.forResult(jsonOutcome.getString("result")));

		if (!jsonOutcome.getJSONObject("bug").isNullObject()) {
			Bug bug = new Bug();
			JSONObject jsonBug = jsonOutcome.getJSONObject("bug");
			if (!(jsonBug.get("title") instanceof JSONNull)) {
				bug.setTitle(jsonBug.getString("title"));
			}
			if (!(jsonBug.get("url") instanceof JSONNull)) {
				bug.setUrl(jsonBug.getString("url"));
			}
			if (!(jsonBug.get("id") instanceof JSONNull)) {
				bug.setId(jsonBug.getLong("id"));
			}
			testOutcome.setBug(bug);
		}

		if (!(jsonOutcome.get("duration") instanceof JSONNull)) {
			testOutcome.setDuration(jsonOutcome.getLong("duration"));
		}

		if (!(jsonOutcome.get("startedAt") instanceof JSONNull)) {
			testOutcome.setStartedAt(parseJsonDate(jsonOutcome.getString("startedAt")));
		}
		if (!(jsonOutcome.get("finishedAt") instanceof JSONNull)) {
			testOutcome.setFinishedAt(parseJsonDate(jsonOutcome.getString("finishedAt")));
		}
		if (!(jsonOutcome.get("dateCreated") instanceof JSONNull)) {
			testOutcome.setDateCreated(parseJsonDate(jsonOutcome.getString("dateCreated")));
		}
		if (!(jsonOutcome.get("lastUpdated") instanceof JSONNull)) {
			testOutcome.setLastUpdated(parseJsonDate(jsonOutcome.getString("lastUpdated")));
		}

		if (!jsonOutcome.getJSONObject("analysisState").isNullObject()) {
			JSONObject jsonAnalysis = jsonOutcome.getJSONObject("analysisState");
			testOutcome.setAnalysisState(AnalysisState.forState(jsonAnalysis.getString("name")));
		}

		if (!(jsonOutcome.get("owner") instanceof JSONNull)) {
			testOutcome.setOwner(jsonOutcome.getString("owner"));
		}

		if (!(jsonOutcome.get("note") instanceof JSONNull)) {
			testOutcome.setNote(jsonOutcome.getString("note"));
		}

		if (!jsonOutcome.getJSONObject("testRun").isNullObject()) {
			testOutcome.setTestRun(TestRun.fromJSON(jsonOutcome.getJSONObject("testRun").toString()));
		}

		if ((jsonOutcome.get("tags") != null) && !(jsonOutcome.get("tags") instanceof JSONNull)) {
			JSONArray tagArray = jsonOutcome.getJSONArray("tags");
			testOutcome.addTags(tagArray);
		}
		return testOutcome;
	}


	/**
	 * Creates a JSON representation of this TestOutcome.
	 * @return a JSON string that represents this TestOutcome.
	 */
	public String toJSON() {
		Map jsonMap = new HashMap();
		jsonMap.put("id", this.getId());
		jsonMap.put("projectKey", this.getProjectKey());
		if (this.bug != null) {
			Map bugMap = new HashMap();
			bugMap.put("id", this.bug.getId());
			bugMap.put("title", this.bug.getTitle());
			bugMap.put("url", this.bug.getUrl());
			jsonMap.put("bug", bugMap);
		}

		String analysisStr = null;
		if (this.analysisState != null) {
			analysisStr = this.analysisState.toString();
		}
		jsonMap.put("analysisState", analysisStr);

		Map testCaseMap = new HashMap();
		testCaseMap.put("id", this.testCase.getId());
		testCaseMap.put("testName", this.testCase.getTestName());
		testCaseMap.put("packageName", this.testCase.getPackageName());
		testCaseMap.put("parameters", this.testCase.getParameters());
		testCaseMap.put("description", this.testCase.getDescription());
		jsonMap.put("testCase", testCaseMap);

		jsonMap.put("result", this.testResult.toString());
		jsonMap.put("owner", this.owner);
		jsonMap.put("note", this.note);
		jsonMap.put("testOutput", this.testOutput);
		jsonMap.put("duration", this.duration);
		jsonMap.put("startedAt", toJsonDate(startedAt));
		jsonMap.put("finishedAt", toJsonDate(finishedAt));

		if (this.testRun != null) {
			jsonMap.put("testRun", this.testRun.toJsonMap());
		}

		if (tags.size() > 0) {
			jsonMap.put("tags", tags);
		}

		JSONObject jsonTestOutcome = JSONObject.fromObject(jsonMap);
		return jsonTestOutcome.toString();
	}


	/**
	 * Get all the Tags for this TestOutcome.
	 * @return an unmodifiable list of all the Tags.
	 */
	public List<String> getTags() {
		return Collections.unmodifiableList(tags);
	}


	/**
	 * Add a tag to this TestOutcome.
	 * @param tag The tag to add.
	 */
	public void addTag(String tag) {
		tags.add(tag);
	}


	/**
	 * Add tags to this TestOutcome.
	 * @param tags The tags to add.
	*/
	public void addTags(List<String> tags) {
		this.tags.addAll(tags);
	}


	private static Date parseJsonDate(String dateString) throws ParseException {
		if (dateString == null) {
			return null;
		} else {
			return new SimpleDateFormat(CuantoConnector.JSON_DATE_FORMAT).parse(dateString);
		}
	}


	private String toJsonDate(Date date) {
		if (date == null) {
			return null;
		} else {
			return new SimpleDateFormat(CuantoConnector.JSON_DATE_FORMAT).format(date);
		}
	}


	public enum Sort {
		ANALYSIS_STATE,
		BUG,
		DATE_CREATED,
		DURATION,
		FINISHED_AT,
		FULL_NAME,
		ID,
		LAST_UPDATED,
		NOTE,
		OWNER,
		STARTED_AT,
		TEST_OUTPUT,
		TEST_RESULT
	}
}
