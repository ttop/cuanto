package cuanto.adapter.listener.testng.adhoc;

import cuanto.adapter.listener.testng.TestNgListener;
import cuanto.adapter.listener.testng.TestNgListenerArguments;
import org.testng.annotations.BeforeMethod;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Suk-Hyun.Cho
 */
public class BaseTestClass
{
	@BeforeMethod
	public void setUpTestNgListener() throws URISyntaxException, InterruptedException {
		TestNgListenerArguments arguments = new TestNgListenerArguments();
		arguments.setCuantoUrl(new URI("http://localhost:8080/cuanto"));
		arguments.setProjectKey("CNG");
		arguments.setCreateTestRun(true);
		arguments.setIncludeConfigDuration(true);
		TestNgListener.setTestNgListenerArguments(arguments);
	}
}
