package cuanto.adapter.listener.testng;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

/**
 * Ad-hoc tests intended for use with TestNgListener to exercise all TestOutcome possibilities.
 */
public class TestNgListenerAdHocTests {

	@Test
	public void testHappy1() {
		System.out.println("testHappy1");
	}

	@Test
	public void testHappy2() {
		System.out.println("testHappy2");
	}

	@Test
	public void testSad1() {
		System.out.println("testSad1");
		Assert.fail("sad1");
	}

	@Test
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
}
