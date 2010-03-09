package cuanto.user;

/**
 * User: Todd Wells Date: Mar 9, 2010 Time: 8:41:46 AM
 */
public class TestResult {
	private final String status;

	public static final TestResult Pass = new TestResult("Pass");
	public static final TestResult Fail = new TestResult("Fail");
	public static final TestResult Error = new TestResult("Error");
	public static final TestResult Ignore = new TestResult("Ignore");
	public static final TestResult Skip = new TestResult("Skip");
	public static final TestResult Unexecuted = new TestResult("Unexecuted");

	TestResult(String status) {
		this.status = status;
	}


	public String getStatus() {
		return status;
	}


	/**
	 * Create a TestResult for a custom status. This probably isn't what you want -- you should favor the static
	 * TestResult members on this class. This method is here for the rare case when a Cuanto server has non-default
	 * TestResult statuses that aren't named the same as the static members on this class. If you specify a TestResult
	 * status that doesn't exist, you will get an error when you attempt to create a TestOutcome with that status.
	 * Consider yourself warned.
	 * @param status The custom status -- should match a TestResult status on the server.
	 * @return A TestResult corresponding to the specified status.
	 */
	public static TestResult forStatus(String status) {
		return new TestResult(status);
	}
}
