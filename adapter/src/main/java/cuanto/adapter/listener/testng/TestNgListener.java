package cuanto.adapter.listener.testng;

import cuanto.adapter.CuantoAdapterException;
import cuanto.adapter.util.ArgumentParser;
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * ITestListener implementation to be used with TestNG to report test outcomes to Cuanto on-the-fly.
 *
 * @author Suk-Hyun Cho
 */
public class TestNgListener implements ITestListener
{
	private static final Logger logger = LoggerFactory.getLogger(TestNgListener.class);

	private static TestNgListenerArguments failoverTestNgListenerArguments;

	private static final ThreadLocal<TestNgListenerArguments> testNgListenerArguments =
		new ThreadLocal<TestNgListenerArguments>()
		{
			@Override
			protected TestNgListenerArguments initialValue()
			{
				return new TestNgListenerArguments();
			}
		};

	// in order to update either the cuantoUrl or the testNgListenerArguments,
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
	public TestNgListener() throws CuantoAdapterException, URISyntaxException
	{
		failoverTestNgListenerArguments = getFailoverTestNgListenerArguments();
	}

	/**
	 * {@inheritDoc}
	 */
	public void onTestStart(ITestResult iTestResult)
	{
	}

	/**
	 * {@inheritDoc}
	 */
	public void onTestSuccess(ITestResult iTestResult)
	{
		createTestOutcome(iTestResult, TestResult.Pass);
	}

	/**
	 * {@inheritDoc}
	 */
	public void onTestFailure(ITestResult iTestResult)
	{
		createTestOutcome(iTestResult, TestResult.Fail);
	}

	/**
	 * {@inheritDoc}
	 */
	public void onTestSkipped(ITestResult iTestResult)
	{
		createTestOutcome(iTestResult, TestResult.Skip);
	}

	/**
	 * {@inheritDoc}
	 */
	public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult)
	{
		createTestOutcome(iTestResult, TestResult.Pass);
	}

	/**
	 * {@inheritDoc}
	 */
	public void onStart(ITestContext iTestContext)
	{
	}

	/**
	 * {@inheritDoc}
	 */
	public void onFinish(ITestContext iTestContext)
	{
	}

	/**
	 * @return cuantoUrl for the current thread
	 */
	public static URI getCuantoUrl()
	{
		synchronized (adapterModificationLock)
		{
			URI currentThreadCuantoUrl = testNgListenerArguments.get().getCuantoUrl();
			return (currentThreadCuantoUrl == null)
				? failoverTestNgListenerArguments.getCuantoUrl()
				: currentThreadCuantoUrl;
		}
	}

	/**
	 * @return testRunId for the current thread
	 */
	public static Long getTestRunId()
	{
		synchronized (adapterModificationLock)
		{
			Long currentThreadTestRunId = testNgListenerArguments.get().getTestRunId();
			return (currentThreadTestRunId == null)
				? failoverTestNgListenerArguments.getTestRunId()
				: currentThreadTestRunId;
		}
	}

	/**
	 * @return projectKey for the current thread
	 */
	public static String getProjectKey()
	{
		synchronized (adapterModificationLock)
		{
			String currentThreadProjectKey = testNgListenerArguments.get().getProjectKey();
			return (currentThreadProjectKey == null)
				? failoverTestNgListenerArguments.getProjectKey()
				: currentThreadProjectKey;
		}
	}

	/**
	 * @return links for the current thread
	 */
	public static Map<String, String> getLinks()
	{
		synchronized (adapterModificationLock)
		{
			Map<String, String> currentThreadLinks = testNgListenerArguments.get().getLinks();
			return (currentThreadLinks == null)
				? failoverTestNgListenerArguments.getLinks()
				: currentThreadLinks;
		}
	}

	/**
	 * @return testProperties for the current thread
	 */
	public static Map<String, String> getTestProperties()
	{
		synchronized (adapterModificationLock)
		{
			Map<String, String> currentThreadTestProperties = testNgListenerArguments.get().getTestProperties();
			return (currentThreadTestProperties == null)
				? failoverTestNgListenerArguments.getTestProperties()
				: currentThreadTestProperties;
		}
	}

	/**
	 * @return isCreateTestRun for the current thread
	 */
	public static Boolean isCreateTestRun()
	{
		synchronized (adapterModificationLock)
		{
			Boolean currentThreadCreateTestRun = testNgListenerArguments.get().isCreateTestRun();
			return (currentThreadCreateTestRun == null)
				? failoverTestNgListenerArguments.isCreateTestRun()
				: currentThreadCreateTestRun;
		}
	}

	/**
	 * @return the testNgListenerArguments for the current thread
	 */
	public static TestNgListenerArguments getTestNgListenerArguments()
	{
		return new TestNgListenerArguments(testNgListenerArguments.get());
	}

	/**
	 * @param testNgListenerArguments to set for the current thread
	 */
	public static void setTestNgListenerArguments(TestNgListenerArguments testNgListenerArguments)
	{
		TestNgListener.testNgListenerArguments.set(testNgListenerArguments);
	}

	/**
	 * Prepare the TestRun to be used to post test outcomes.
	 * <p/>
	 * If cuanto.testrun is provided, attempt to parse that to Long. If not, create a new TestRun and use its id.
	 *
	 * @param cuanto    cuanto connector
	 * @param arguments to use to determine the current test run
	 * @return the determined TestRun
	 */
	private TestRun determineTestRunId(CuantoConnector cuanto, TestNgListenerArguments arguments)
	{

		Long testRunId = getTestRunId();

		if (testRunId == null && isCreateTestRun())
		{
			logger.info("TestRun id was not provided. Creating a new TestRun...");
			testRunId = createTestRun(cuanto, getProjectKey());
			logger.info("Created TestRun #" + testRunId);
			arguments.setTestRunId(testRunId);
			if (failoverTestNgListenerArguments.getTestRunId() == null)
				failoverTestNgListenerArguments.setTestRunId(testRunId);
			setTestNgListenerArguments(arguments);
		}

		if (testRunId == null)
			return null;

		TestRun testRun = cuanto.getTestRun(testRunId);

		Map<String, String> testProperties = getTestProperties();
		if (testProperties != null)
			for (Map.Entry<String, String> testPropertyEntry : testProperties.entrySet())
				testRun.addTestProperty(testPropertyEntry.getKey(), testPropertyEntry.getValue());

		Map<String, String> links = getLinks();
		if (links != null)
			for (Map.Entry<String, String> linkEntry : links.entrySet())
				testRun.addLink(linkEntry.getValue(), linkEntry.getKey());

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
	private Long createTestRun(CuantoConnector cuanto, String cuantoProjectKey)
	{
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
	private void createTestOutcome(ITestResult testCaseResult, TestResult cuantoTestResult)
	{
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
		synchronized (adapterModificationLock)
		{
			String projectKey = getProjectKey();
			String cuantoUrl = getCuantoUrl().toString();
			cuanto = CuantoConnector.newInstance(cuantoUrl, projectKey);
			testRun = determineTestRunId(cuanto, TestNgListener.testNgListenerArguments.get());
		}

		cuanto.addTestOutcome(testOutcome, testRun);
	}

	/**
	 * Get the test output to store in the test outcome.
	 *
	 * @param testCaseResult result of the current test case
	 * @return the stacktrace of the resulting exception
	 */
	@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
	private String getTestOutput(ITestResult testCaseResult)
	{
		return getStackTrace(testCaseResult.getThrowable());
	}

	/**
	 * Get the stack trace as String.
	 *
	 * @param throwable from which to get the stack trace
	 * @return the stack trace of throwable
	 */
	private static String getStackTrace(Throwable throwable)
	{
		if (throwable == null)
			return null;

		Writer stacktrace = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stacktrace);
		throwable.printStackTrace(printWriter);
		return stacktrace.toString();
	}

	/**
	 * Parse the system environment properties to construct a TestNgListenerArguments to which to fail over.
	 *
	 * @return failover TestNgListenerArguments
	 * @throws URISyntaxException if cuanto.url is a malformed URI
	 */
	private static TestNgListenerArguments getFailoverTestNgListenerArguments() throws URISyntaxException
	{
		TestNgListenerArguments arguments = new TestNgListenerArguments();

		// parse environment variables
		String cuantoUrl = System.getenv("cuanto.url");
		String cuantoProjectKey = System.getenv("cuanto.projectkey");
		String cuantoTestRun = System.getenv("cuanto.testrun");
		String testRunPropertiesString = System.getenv("cuanto.testrun.properties");
		String testRunLinksString = System.getenv("cuanto.testrun.links");
		String cuantoCreateTestRun = System.getenv("cuanto.testrun.create");

		Map<String, String> testRunProperties = ArgumentParser.parseMap(testRunPropertiesString);
		Map<String, String> testRunLinks = ArgumentParser.parseMap(testRunLinksString);

		if (cuantoUrl != null)
			arguments.setCuantoUrl(new URI(cuantoUrl));
		if (cuantoTestRun != null)
			arguments.setTestRunId(Long.valueOf(cuantoTestRun));

		arguments.setProjectKey(cuantoProjectKey);
		arguments.setTestProperties(testRunProperties);
		arguments.setLinks(testRunLinks);
		arguments.setCreateTestRun(false);
		arguments.setCreateTestRun(Boolean.valueOf(cuantoCreateTestRun));
		return arguments;
	}
}
