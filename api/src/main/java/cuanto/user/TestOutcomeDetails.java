package cuanto.user;

import java.util.Date;

/**
 * The mutable details of a TestOutcome which are used for TestOutcome creation or updating.
 */
public class TestOutcomeDetails {
	private String testCasePackageName;
	private String testCaseTestName;
	private String testCaseParameters;
	private String testOutput;
	private Long duration;
	private Date startedAt;
	private Date finishedAt;
	private TestResult testResult;


	TestOutcomeDetails() {
		// use static factory methods instead
	}

	public static TestOutcomeDetails newInstance(String testCasePackageName, String testCaseTestName) {
		return newInstance(testCasePackageName, testCaseTestName, null);
	}

	public static TestOutcomeDetails newInstance(String testCasePackageName, String testCaseTestName,
		String testCaseParameters) {
		TestOutcomeDetails details = new TestOutcomeDetails();
		details.setTestCasePackageName(testCasePackageName);
		details.setTestCaseTestName(testCaseTestName);
		details.setTestCaseParameters(testCaseParameters);
		return details;
	}


	public String getTestCasePackageName() {
		return testCasePackageName;
	}


	void setTestCasePackageName(String testCasePackageName) {
		this.testCasePackageName = testCasePackageName;
	}


	public String getTestCaseTestName() {
		return testCaseTestName;
	}


	void setTestCaseTestName(String testCaseTestName) {
		this.testCaseTestName = testCaseTestName;
	}


	public String getTestCaseParameters() {
		return testCaseParameters;
	}


	void setTestCaseParameters(String testCaseParameters) {
		this.testCaseParameters = testCaseParameters;
	}


	public String getTestOutput() {
		return testOutput;
	}


	public void setTestOutput(String testOutput) {
		this.testOutput = testOutput;
	}


	public Long getDuration() {
		return duration;
	}


	public void setDuration(Long duration) {
		this.duration = duration;
	}


	public Date getStartedAt() {
		return startedAt;
	}


	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}


	public Date getFinishedAt() {
		return finishedAt;
	}


	public void setFinishedAt(Date finishedAt) {
		this.finishedAt = finishedAt;
	}


	public TestResult getTestResult() {
		return testResult;
	}


	public void setTestResult(TestResult testResult) {
		this.testResult = testResult;
	}
}
