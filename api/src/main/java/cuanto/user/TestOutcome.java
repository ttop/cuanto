package cuanto.user;



import net.sf.json.JSONObject;
import net.sf.json.JSONNull;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * A class that represents the TestOutcome of a particular TestCase.
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
	String analysisState;
	Date startedAt;
	Date finishedAt;
	Date dateCreated;
	Date lastUpdated;
	String projectKey;

	TestOutcome() {
		// no public constructor
	}

	public static TestOutcome newInstance(String testCasePackageName, String testCaseTestName, TestResult testResult) {
		return newInstance(testCasePackageName, testCaseTestName, null, testResult);
	}


	public static TestOutcome newInstance(String testCasePackageName, String testCaseTestName, String testCaseParameters,
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


	public TestCase getTestCase() {
		return testCase;
	}


	public TestResult getTestResult() {
		return testResult;
	}


	public String getTestOutput() {
		return testOutput;
	}


	public Long getDuration() {
		return duration;
	}


	public String getOwner() {
		return owner;
	}


	public Bug getBug() {
		return bug;
	}


	public String getNote() {
		return note;
	}


	public Long getId() {
		return id;
	}


	public String getAnalysisState() {
		return analysisState;
	}


	public Date getStartedAt() {
		return startedAt;
	}


	public Date getFinishedAt() {
		return finishedAt;
	}


	public Date getDateCreated() {
		return dateCreated;
	}


	public Date getLastUpdated() {
		return lastUpdated;
	}


	void setTestCase(TestCase testCase) {
		this.testCase = testCase;
	}


	public void setTestResult(TestResult testResult) {
		this.testResult = testResult;
	}


	public void setTestOutput(String testOutput) {
		this.testOutput = testOutput;
	}


	public void setDuration(Long duration) {
		this.duration = duration;
	}


	public void setOwner(String owner) {
		this.owner = owner;
	}


	public void setBug(Bug bug) {
		this.bug = bug;
	}


	public void setNote(String note) {
		this.note = note;
	}


	void setId(Long id) {
		this.id = id;
	}


	public void setAnalysisState(String analysisState) {
		this.analysisState = analysisState;
	}


	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}


	public void setFinishedAt(Date finishedAt) {
		this.finishedAt = finishedAt;
	}


	void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}


	void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}


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


	public static TestOutcome fromJSON(String jsonString) throws ParseException {
		JSONObject jsonOutcome = JSONObject.fromObject(jsonString);
		TestOutcome testOutcome = new TestOutcome();
		testOutcome.setId(jsonOutcome.getLong("id"));

		TestCase testCase = new TestCase();
		JSONObject jsonTestCase = jsonOutcome.getJSONObject("testCase");
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

		testOutcome.setTestCase(testCase);

		testOutcome.setTestResult(TestResult.valueOf(jsonOutcome.getString("result")));

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

		if (!jsonOutcome.getJSONObject("analysisState").isNullObject()) {
			JSONObject jsonAnalysis = jsonOutcome.getJSONObject("analysisState");
			testOutcome.setAnalysisState(jsonAnalysis.getString("name"));
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
		return testOutcome;
	}


	public String toJSON() {
		/*
{"id":667300,"bug":{"title":"MPS-1077","url":"http://tpjira/MPS-1077","id":387},"analysisState":{"name":"Bug","id":2},
"testCase":{"testName":"testCreateListResponse","packageName":"com.theplatform.test.assettype.compliance.AtomFormatComplianceTest",
"parameters":null,"description":null,"id":32892},"result":"Fail","owner":null,"note":null,"
testOutput":"junit.framework.AssertionFailedError: Number of \"startIndex\" nodes found. expected:<0> but was:<1>\n\tat junit.framework.Assert.fail(Assert.java:47)\n\tat junit.framework.Assert.failNotEquals(Assert.java:277)\n\tat junit.framework.Assert.assertEquals(Assert.java:64)\n\tat junit.framework.Assert.assertEquals(Assert.java:195)\n\tat com.theplatform.test.modules.assertioncontext.AssertionContextAssert.assertEquals(AssertionContextAssert.java:238)\n\tat sun.reflect.GeneratedMethodAccessor184.invoke(Unknown Source)\n\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n\tat java.lang.reflect.Method.invoke(Method.java:597)\n\tat org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:86)\n\tat groovy.lang.MetaMethod.doMethodInvoke(MetaMethod.java:230)\n\tat groovy.lang.MetaClassImpl.invokeStaticMethod(MetaClassImpl.java:1126)\n\tat org.codehaus.groovy.runtime.InvokerHelper.invokeStaticMethod(InvokerHelper.java:804)\n\tat org.codehaus.groovy.runtime.ScriptBytecodeAdapter.invokeStaticMethodN(ScriptBytecodeAdapter.java:215)\n\tat com.theplatform.test.data.framework.util.XmlFormatUtil.verifyNoNode(XmlFormatUtil.groovy:41)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n\tat java.lang.reflect.Method.invoke(Method.java:597)\n\tat org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:86)\n\tat groovy.lang.MetaMethod.doMethodInvoke(MetaMethod.java:230)\n\tat groovy.lang.MetaClassImpl.invokeStaticMethod(MetaClassImpl.java:1105)\n\tat org.codehaus.groovy.runtime.InvokerHelper.invokeMethod(InvokerHelper.java:749)\n\tat org.codehaus.groovy.runtime.ScriptBytecodeAdapter.invokeMethodN(ScriptBytecodeAdapter.java:170)\n\tat com.theplatform.test.data.framework.compliance.AbstractXmlFormatComplianceTester.verifyNodeExistence(AbstractXmlFormatComplianceTester.groovy:88)\n\tat com.theplatform.test.data.framework.compliance.AbstractXmlFormatComplianceTester.this$6$verifyNodeExistence(AbstractXmlFormatComplianceTester.groovy)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n\tat java.lang.reflect.Method.invoke(Method.java:597)\n\tat org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:86)\n\tat groovy.lang.MetaMethod.doMethodInvoke(MetaMethod.java:230)\n\tat groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:912)\n\tat org.codehaus.groovy.runtime.ScriptBytecodeAdapter.invokeMethodOnCurrentN(ScriptBytecodeAdapter.java:78)\n\tat com.theplatform.test.data.framework.compliance.AbstractXmlFormatComplianceTester.verifyCountNodes(AbstractXmlFormatComplianceTester.groovy:65)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n\tat java.lang.reflect.Method.invoke(Method.java:597)\n\tat org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:86)\n\tat groovy.lang.MetaMethod.doMethodInvoke(MetaMethod.java:230)\n\tat groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:912)\n\tat org.codehaus.groovy.runtime.ScriptBytecodeAdapter.invokeMethodOnCurrentN(ScriptBytecodeAdapter.java:78)\n\tat com.theplatform.test.data.framework.compliance.AtomFormatComplianceTester.verifyCreateListResponse(AtomFormatComplianceTester.groovy:180)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n\tat java.lang.reflect.Method.invoke(Method.java:597)\n\tat org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:86)\n\tat groovy.lang.MetaMethod.doMethodInvoke(MetaMethod.java:230)\n\tat groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:912)\n\tat groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:756)\n\tat org.codehaus.groovy.runtime.InvokerHelper.invokePogoMethod(InvokerHelper.java:778)\n\tat org.codehaus.groovy.runtime.InvokerHelper.invokeMethod(InvokerHelper.java:758)\n\tat org.codehaus.groovy.runtime.ScriptBytecodeAdapter.invokeMethodN(ScriptBytecodeAdapter.java:170)\n\tat com.theplatform.test.assettype.compliance.AtomFormatComplianceTest.testCreateListResponse(AtomFormatComplianceTest.groovy:63)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n\tat java.lang.reflect.Method.invoke(Method.java:597)\n\tat junit.framework.TestCase.runTest(TestCase.java:168)\n\tat junit.framework.TestCase.runBare(TestCase.java:134)\n\tat junit.framework.TestResult$1.protect(TestResult.java:110)\n\tat junit.framework.TestResult.runProtected(TestResult.java:128)\n\tat junit.framework.TestResult.run(TestResult.java:113)\n\tat junit.framework.TestCase.run(TestCase.java:124)\n\tat junit.framework.TestSuite.runTest(TestSuite.java:232)\n\tat junit.framework.TestSuite.run(TestSuite.java:227)\n\tat org.junit.internal.runners.JUnit38ClassRunner.run(JUnit38ClassRunner.java:81)\n\tat junit.framework.JUnit4TestAdapter.run(JUnit4TestAdapter.java:36)\n\tat org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner.run(JUnitTestRunner.java:420)\n\tat org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner.launch(JUnitTestRunner.java:911)\n\tat org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner.main(JUnitTestRunner.java:768)","
duration":1420,"startedAt":null,"finishedAt":null}

		*/

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
		jsonMap.put("analysisState", this.analysisState);

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
		JSONObject jsonTestOutcome = JSONObject.fromObject(jsonMap);
		return jsonTestOutcome.toString();
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
	
}
