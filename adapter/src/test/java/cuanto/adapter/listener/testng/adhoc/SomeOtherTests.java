package cuanto.adapter.listener.testng.adhoc;

import org.apache.commons.lang.math.RandomUtils;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Ad-hoc tests intended for use with TestNgListener to exercise all TestOutcome possibilities.
 * <p/>
 * Failures are expected and do not indicate a bug.
 */
public class SomeOtherTests extends BaseTestClass
{
    @BeforeMethod
    void delay() throws InterruptedException {
        Thread.sleep(1000 + RandomUtils.nextLong() % 1000);
    }
    
	@Test(groups = "1.0.0")
	public void testSomething1() throws InterruptedException {
        Thread.sleep(1000 + RandomUtils.nextLong() % 1000);
    }

	@Test(groups = "reaperagent")
	public void testSomething2() throws InterruptedException {
        Thread.sleep(1000 + RandomUtils.nextLong() % 1000);
    }

	@Test(groups = { "Happy", "Second" })
	public void testSomething3() {}
	{
		System.out.println("testHappy2: " + Thread.currentThread().getName());
	}

	@Test
	public void testSkip1()
	{
		System.out.println("testSkip1: " + Thread.currentThread().getName());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; ++i)
        {
            sb.append(i % 10);
        }
		throw new SkipException("skip1: " + sb.toString());
	}

	@Test
	public void testSkip2()
	{
		System.out.println("testSkip2: " + Thread.currentThread().getName());
		throw new SkipException("skip2");
	}

	@Test(
		groups = { "Happy", "Sad" },
		dataProvider = "data-provider")
	public void testDataProvider(String p1, Integer p2)
	{
		System.out.println(p1 + ":" + p2 + " - " + Thread.currentThread().getName());
		if (p1 == null || p2 == null)
			throw new IllegalArgumentException("The test method parameter may not be null.");
	}

	@DataProvider(name = "data-provider")
	private Object[][] dataProvider()
	{
		return new Object[][] {
			new Object[] { "1-param1", 12 },
			new Object[] { "2-param1", null },
			new Object[] { null, 22 },
		};
	}
}
