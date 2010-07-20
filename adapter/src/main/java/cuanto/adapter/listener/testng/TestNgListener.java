package cuanto.adapter.listener.testng;

import cuanto.adapter.CuantoAdapterException;
import cuanto.adapter.objects.TestRunArguments;
import cuanto.adapter.util.ArgumentParser;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ITestListener implementation to be used with TestNG to report test outcomes to Cuanto on-the-fly.
 *
 * @author Suk-Hyun Cho
 */
public class TestNgListener implements ITestListener {
	private static final Logger logger = LoggerFactory.getLogger(TestNgListener.class);

	private StringOutputStream cuantoOutputStream;
	private DualOutputStream dualOutputStream;

	private static URI failoverCuantoUrl;
	private static TestRunArguments failoverTestRunArguments;

	private static final ThreadLocal<URI> cuantoUrl = new ThreadLocal<URI>();
	private static final ThreadLocal<TestRunArguments> testRunArguments = new ThreadLocal<TestRunArguments>();

	// in order to update either the cuantoUrl or the testRunArguments,
	// the thread must synchronize on adapterModificationLock
	private static final Object adapterModificationLock = new Object();

	/**
	 * Construct a TestNgListener.
	 * <p/>
	 * The following environment variables are used to connect to Cuanto:
	 * - cuanto.url: the url at which Cuanto is running; required
	 * - cuanto.projectkey: the project key for which the tests run; required
	 * - cuanto.testrun: the id of an existing TestRun to use; if null, a new TestRun will be created
	 * - cuanto.testrun.properties: testProperties map in the form of key1:val1,key2:val2,...
	 * - cuanto.testrun.links: links map in the form of cuanto:http://cuanto.codehaus.org,google:http://www.google.com
	 *
	 * @throws CuantoAdapterException      if the cuanto.url or cuanto.projectkey are not specified
	 * @throws java.net.URISyntaxException if cuantoUrl is not a valid URI
	 */
	public TestNgListener() throws CuantoAdapterException, URISyntaxException {
		// set System.out to be a DualOutputStream that redirects the stdout to both System.out and cuantoOutputStream
		cuantoOutputStream = new StringOutputStream();
		dualOutputStream = new DualOutputStream(System.out, cuantoOutputStream);
		System.setOut(new PrintStream(dualOutputStream));

		// parse environment variables
		String cuantoUrl = System.getenv("cuanto.url");
		String cuantoProjectKey = System.getenv("cuanto.projectkey");
		String cuantoTestRun = System.getenv("cuanto.testrun");
		String testRunPropertiesString = System.getenv("cuanto.testrun.properties");
		String testRunLinksString = System.getenv("cuanto.testrun.links");

		Map<String, String> testRunProperties = ArgumentParser.parseMap(testRunPropertiesString);
		Map<String, String> testRunLinks = ArgumentParser.parseMap(testRunLinksString);

		// if cuantoUrl or cuantoProjectKey are not provided, then do not set the TestRunArguments.
		// this is because setting it will set the failoverTestRunArguments to the incomplete TestRunArguments.
		if (cuantoUrl == null || cuantoProjectKey == null)
			return;

		TestRunArguments testRunArguments = new TestRunArguments();
		testRunArguments.setProjectKey(cuantoProjectKey);
		testRunArguments.setTestProperties(testRunProperties);
		testRunArguments.setLinks(testRunLinks);
		if (cuantoTestRun != null)
			testRunArguments.setTestRunId(Long.valueOf(cuantoTestRun));
		if (cuantoUrl != null)
			setCuantoUrl(new URI(cuantoUrl));

		setTestRunArguments(testRunArguments);
	}

	/**
	 * {@inheritDoc}
	 * <p/>
	 * Re-initialize the cuantoOutputStream (stream2 on dualOutputStream),
	 * so that the stdout written by the following test will be isolated and usable for the test output.
	 */
	public void onTestStart(ITestResult iTestResult) {
		cuantoOutputStream = new StringOutputStream();
		dualOutputStream.setSecondStream(cuantoOutputStream);
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
	 * Get the Cuanto url for this thread.
	 *
	 * @return the cuanto url for this thread if available; if not, return failoverCuantoUrl
	 */
	public static URI getCuantoUrl() {
		synchronized (adapterModificationLock) {
			URI currentThreadCuantoUrl = cuantoUrl.get();
			return (currentThreadCuantoUrl == null)
				? failoverCuantoUrl
				: currentThreadCuantoUrl;
		}
	}

	/**
	 * Set the Cuanto url for this this thread.
	 * <p/>
	 * If failoverCuantoUrl is not set, then use the specified url for it.
	 *
	 * @param cuantoUrl the new Cuanto url for this thread
	 */
	public static void setCuantoUrl(URI cuantoUrl) {
		synchronized (adapterModificationLock) {
			TestNgListener.cuantoUrl.set(cuantoUrl);
			if (failoverCuantoUrl == null)
				failoverCuantoUrl = cuantoUrl;
		}
	}

	/**
	 * Get the TestRunArguments for this thread.
	 *
	 * @return the TestRunArguments for this thread if available; if not, return failoverTestRunArguments.
	 */
	public static TestRunArguments getTestRunArguments() {
		synchronized (adapterModificationLock) {
			TestRunArguments currentThreadTestRunArguments = testRunArguments.get();
			return (currentThreadTestRunArguments == null)
				? failoverTestRunArguments
				: currentThreadTestRunArguments;
		}
	}

	/**
	 * Set the TestRunArguments for this thread.
	 * <p/>
	 * If failoverTestRunArguments is not already set, then use the specified testRunArguments for it.
	 *
	 * @param testRunArguments the new TestRunArguments for this thread
	 */
	public static void setTestRunArguments(TestRunArguments testRunArguments) {
		synchronized (adapterModificationLock) {
			TestNgListener.testRunArguments.set(testRunArguments);
			if (failoverTestRunArguments == null)
				failoverTestRunArguments = testRunArguments;
		}
	}

	/**
	 * Prepare the TestRun to be used to post test outcomes.
	 * <p/>
	 * If cuanto.testrun is provided, attempt to parse that to Long. If not, create a new TestRun and use its id.
	 *
	 * @param cuanto           cuanto connector
	 * @param testRunArguments to use to determine the current test run
	 * @return the determined TestRun
	 */
	private TestRun determineTestRun(CuantoConnector cuanto, TestRunArguments testRunArguments) {

		Long testRunId = testRunArguments.getTestRunId();

		if (testRunId == null) {
			logger.info("TestRun id was not provided. Creating a new TestRun...");
			testRunId = createTestRun(cuanto, testRunArguments.getProjectKey());
			logger.info("Created TestRun #" + testRunId);
			testRunArguments.setTestRunId(testRunId);
			setTestRunArguments(testRunArguments);
		}

		TestRun testRun = cuanto.getTestRun(testRunId);
		Map<String, String> testProperties = testRunArguments.getTestProperties();

		// todo: TestRun.toJSON() sets "testProperties": null if testProperties is null. Bug?
		if (testProperties == null)
			testRun.setTestProperties(new LinkedHashMap<String, String>());
		else
			testRun.setTestProperties(testProperties);

		Map<String, String> links = testRunArguments.getLinks();
		if (links != null) {
			for (Map.Entry<String, String> entry : links.entrySet()) {
				testRun.addLink(entry.getValue(), entry.getKey());
			}
		}

		cuanto.updateTestRun(testRun);

		return testRun;
	}

	/**
	 * Create a new TestRun for the given project key.
	 *
	 * @param cuanto           the cuanto connector to use to either retrieve the test run, if applicable, or create a new one
	 * @param cuantoProjectKey for which to create a new TestRun
	 * @return the id of the created TestRun
	 */
	private Long createTestRun(CuantoConnector cuanto, String cuantoProjectKey) {
		TestRun testRun = new TestRun(cuantoProjectKey);
		testRun.setDateExecuted(new Date());
		testRun.setNote("Created by " + this.getClass().getSimpleName());
		return cuanto.addTestRun(testRun);
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
		if (cuantoTestResult != TestResult.Pass)
			testOutcome.setTestOutput(getTestOutput(testCaseResult));
		testOutcome.addTags(Arrays.asList(tags));
		testOutcome.setDuration(duration);

		// because the user may have modified the cuanto url or the test run arguments,
		// lazily create the cuanto connector and determine the test run to which to submit this test outcome
		TestRun testRun = null;
		CuantoConnector cuanto = null;
		synchronized (adapterModificationLock) {
			String projectKey = getTestRunArguments().getProjectKey();
			String cuantoUrl = getCuantoUrl().toString();
			cuanto = CuantoConnector.newInstance(cuantoUrl, projectKey);
			testRun = determineTestRun(cuanto, getTestRunArguments());
		}

		cuanto.addTestOutcome(testOutcome, testRun);
	}

	/**
	 * Get the test output to store in the test outcome.
	 * <p/>
	 * The stderr must show first in order for the grouped output feature to work as expected.
	 *
	 * @param testCaseResult result of the current test case
	 * @return the stderr appended with the stdout
	 */
	@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
	private String getTestOutput(ITestResult testCaseResult) {
		StringBuffer sb = new StringBuffer();
		sb.append(getStackTrace(testCaseResult.getThrowable()));
		sb.append("\r\n---\r\n\r\n");
		sb.append(cuantoOutputStream.toString());
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
