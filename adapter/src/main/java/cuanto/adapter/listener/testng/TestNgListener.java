package cuanto.adapter.listener.testng;

import cuanto.adapter.CuantoAdapterException;
import cuanto.adapter.util.DualOutputStream;
import cuanto.adapter.util.StringOutputStream;
import cuanto.api.CuantoConnector;
import cuanto.api.TestOutcome;
import cuanto.api.TestResult;
import cuanto.api.TestRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IClass;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;

/**
 * ITestListener implementation to be used with TestNG to report test outcomes to Cuanto on-the-fly.
 *
 * @author Suk-Hyun Cho
 */
public class TestNgListener implements ITestListener {
	private static final Logger logger = LoggerFactory.getLogger(TestNgListener.class);

	private CuantoConnector cuanto;
	private TestRun testRun;
	private PrintStream stdout;
	private StringOutputStream cuantoOutputStream;
	private DualOutputStream dualOutputStream;

	/**
	 * Construct a TestNgListener.
	 * <p/>
	 * The following environment variables are used to connect to Cuanto:
	 * - cuanto.url: the url at which Cuanto is running; required
	 * - cuanto.projectkey: the project key for which the tests run; required
	 * - cuanto.testrun: the id of
	 *
	 * @throws CuantoAdapterException if unable to connect to Cuanto
	 */
	public TestNgListener() throws CuantoAdapterException {
		String cuantoUrl = System.getenv("cuanto.url");
		String cuantoProjectKey = System.getenv("cuanto.projectkey");
		String cuantoTestRun = System.getenv("cuanto.testrun");

		if (cuantoUrl == null || cuantoProjectKey == null) {
			throw new CuantoAdapterException("Please provide cuanto.url and cuanto.projectkey");
		}

		cuanto = CuantoConnector.newInstance(cuantoUrl, cuantoProjectKey);
		Long testRunId = determineTestRunId(cuantoProjectKey, cuantoTestRun);
		testRun = cuanto.getTestRun(testRunId);
		stdout = System.out;
		cuantoOutputStream = new StringOutputStream();
		dualOutputStream = new DualOutputStream(stdout, cuantoOutputStream);
		System.setOut(new PrintStream(dualOutputStream));
	}

	/**
	 * {@inheritDoc}
	 */
	public void onTestStart(ITestResult iTestResult) {
		cuantoOutputStream = new StringOutputStream();
		dualOutputStream.setStream2(cuantoOutputStream);
	}

	/**
	 * {@inheritDoc}
	 */
	public void onTestSuccess(ITestResult iTestResult) {
		createTestOutcome(iTestResult, TestResult.Pass);
	}

	/**
	 * {@inheritDoc}
	 */
	public void onTestFailure(ITestResult iTestResult) {
		createTestOutcome(iTestResult, TestResult.Fail);
	}

	/**
	 * {@inheritDoc}
	 */
	public void onTestSkipped(ITestResult iTestResult) {
		createTestOutcome(iTestResult, TestResult.Skip);
	}

	/**
	 * {@inheritDoc}
	 */
	public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
		createTestOutcome(iTestResult, TestResult.Pass);
	}

	/**
	 * {@inheritDoc}
	 */
	public void onStart(ITestContext iTestContext) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void onFinish(ITestContext iTestContext) {
	}

	/**
	 * Determine the TestRun id.
	 * <p/>
	 * If cuanto.testrun is provided, attempt to parse that to Long. If not, create a new TestRun and use its id.
	 *
	 * @param cuantoProjectKey Cuanto project key
	 * @param cuantoTestRun    TestRun id for which to submit test results
	 * @return Long id of the provided or newly created TestRun
	 * @throws CuantoAdapterException if cuanto.testrun is provided but cannot be parsed as Long
	 */
	private Long determineTestRunId(String cuantoProjectKey, String cuantoTestRun) throws CuantoAdapterException {
		Long testRunId = null;
		if (cuantoTestRun == null) {
			logger.info("cuanto.testrun not provided. Creating a new TestRun...");
			testRunId = createTestRun(cuantoProjectKey);
			logger.info("Created TestRun #" + testRunId);
		} else {
			try {
				testRunId = Long.parseLong(cuantoTestRun);
			}
			catch (NumberFormatException nfe) {
				throw new CuantoAdapterException("Unable to parse cuanto.testrun.", nfe);
			}
		}
		return testRunId;
	}

	/**
	 * Create a new TestRun for the given project key.
	 *
	 * @param cuantoProjectKey for which to create a new TestRun
	 * @return the id of the created TestRun
	 */
	private Long createTestRun(String cuantoProjectKey) {
		Long testRunId;
		TestRun testRun = new TestRun(cuantoProjectKey);
		testRun.setDateExecuted(new Date());
		testRun.setNote("Created by " + this.getClass().getSimpleName());
		testRunId = cuanto.addTestRun(testRun);
		return testRunId;
	}

	/**
	 * Create a TestOutcome appropriate for the current test.
	 *
	 * @param testCaseResult   ITestResult
	 * @param cuantoTestResult TestResult
	 */
	private void createTestOutcome(ITestResult testCaseResult, TestResult cuantoTestResult) {
		IClass testClass = testCaseResult.getTestClass();

		// todo: TestNG bug?
		// if the test class overrode getTestName(), the customized test name should be used (testClass.getTestName()).
		// however, even if ITest is not implemented, a test returns non-null value.
		// so, just use the testCaseResult.getName() for now, which is just the test method name.
		String testCaseName = testCaseResult.getName();

		// package name in Cuanto is the fully-qualified test class name (e.g., cuanto.foo.MyTest)
		String packageName = testClass.getRealClass().getName();
		Object[] testCaseParameters = testCaseResult.getParameters();

		TestOutcome testOutcome = TestOutcome.newInstance(
			packageName, testCaseName, testCaseParameters, cuantoTestResult);
		String[] tags = testCaseResult.getMethod().getGroups();
		long duration = testCaseResult.getEndMillis() - testCaseResult.getStartMillis();
		testOutcome.setTestOutput(getTestOutput(testCaseResult));
		testOutcome.addTags(Arrays.asList(tags));
		testOutcome.setDuration(duration);
		cuanto.addTestOutcome(testOutcome, testRun);
	}

	@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
	private String getTestOutput(ITestResult testCaseResult) {
		StringBuffer sb = new StringBuffer();
		sb.append("[stdout]\r\n");
		sb.append(cuantoOutputStream.toString());
		sb.append("\r\n---\r\n\r\n");
		sb.append("[stderr]\r\n");
		sb.append(getStackTrace(testCaseResult.getThrowable()));
		return sb.toString();
	}

	/**
	 * Get the stack trace as String.
	 *
	 * @param throwable from which to get the stack trace
	 * @return the stack trace of throwable
	 */
	private static String getStackTrace(Throwable throwable) {
		if (throwable == null)
			return null;

		Writer stacktrace = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stacktrace);
		throwable.printStackTrace(printWriter);
		return stacktrace.toString();
	}
}
