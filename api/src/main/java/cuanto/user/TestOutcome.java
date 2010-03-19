package cuanto.user;



import java.util.Date;

/**
 * An immutable class that represents the server-side TestOutcome of a particular TestCase. 
 */
public class TestOutcome {
	TestCase testCase;
	TestResult testResult;
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


	public AnalysisState getAnalysisState() {
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


	public void setAnalysisState(AnalysisState analysisState) {
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
}
