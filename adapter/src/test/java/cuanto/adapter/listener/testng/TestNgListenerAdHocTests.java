package cuanto.adapter.listener.testng;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Ad-hoc tests intended for use with TestNgListener to exercise all TestOutcome possibilities.
 */
public class TestNgListenerAdHocTests {

	@Test(groups = "Happy")
	public void testHappy1() {
		System.out.println("testHappy1");
	}

	@Test(groups = { "Happy", "Second" })
	public void testHappy2() {
		System.out.println("testHappy2");
	}

	@Test(groups = "Sad")
	public void testSad1() {
		System.out.println("testSad1");
		Assert.fail("sad1");
	}

	@Test(groups = { "Sad", "Second" })
	public void testSad2() {
		System.out.println("testSad2");
		Assert.fail("sad2");
	}

	@Test
	public void testSkip1() {
		System.out.println("testSkip1");
		throw new SkipException("skip1");
	}

	@Test
	public void testSkip2() {
		System.out.println("testSkip2");
		throw new SkipException("skip2");
	}

	@Test(
		groups = { "Happy", "Sad" },
		dataProvider = "data-provider")
	public void testDataProvider(String p1, Integer p2) {
		System.out.println(p1 + ":" + p2);
		if (p1 == null || p2 == null)
			throw new IllegalArgumentException("The test method parameter may not be null.");
	}

	@DataProvider(name = "data-provider")
	private Object[][] dataProvider() {
		return new Object[][] {
			new Object[] { "1-param1", 12 },
			new Object[] { "2-param1", null },
			new Object[] { null, 22 },
		};
	}
}
