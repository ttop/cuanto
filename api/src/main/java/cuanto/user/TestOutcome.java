package cuanto.user;

import cuanto.api.Bug;
import cuanto.api.AnalysisState;

import java.util.Date;

/**
 * An immutable class that represents the server-side TestOutcome of a particular TestCase. 
 */
public class TestOutcome {
	TestCase testCase;
	String testResult;
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


	public TestCase getTestCase() {
		return testCase;
	}


	public String getTestResult() {
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


	/**
	 * Get a TestOutcomeDetails object with details that correspond to this TestOutcome's values.
	 * @return a TestOutcomeDetails object with details that correspond to this TestOutcome's values.
	 */
	public TestOutcomeDetails getTestOutcomeDetails() {
		if (this.testCase == null) {
			throw new RuntimeException("This test outcome has no associated TestCase");
		}
		TestOutcomeDetails details = TestOutcomeDetails.newInstance(this.testCase.getPackageName(), 
			this.testCase.getTestName(), this.testCase.getParameters());
		details.setTestOutput(this.testOutput);
		details.setDuration(this.duration);
		details.setStartedAt(this.startedAt);
		details.setFinishedAt(this.finishedAt);
		details.setTestResult(TestResult.forStatus(this.testResult));
		return details;
	}


	void setTestCase(TestCase testCase) {
		this.testCase = testCase;
	}


	void setTestResult(String testResult) {
		this.testResult = testResult;
	}


	void setTestOutput(String testOutput) {
		this.testOutput = testOutput;
	}


	void setDuration(Long duration) {
		this.duration = duration;
	}


	void setOwner(String owner) {
		this.owner = owner;
	}


	void setBug(Bug bug) {
		this.bug = bug;
	}


	void setNote(String note) {
		this.note = note;
	}


	void setId(Long id) {
		this.id = id;
	}


	void setAnalysisState(AnalysisState analysisState) {
		this.analysisState = analysisState;
	}


	void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}


	void setFinishedAt(Date finishedAt) {
		this.finishedAt = finishedAt;
	}


	void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}


	void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}
