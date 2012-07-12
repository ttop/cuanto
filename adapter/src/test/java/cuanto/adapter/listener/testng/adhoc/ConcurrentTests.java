package cuanto.adapter.listener.testng.adhoc;

import org.testng.annotations.Test;

/**
 * @author Suk-Hyun.Cho
 */
@Test(threadPoolSize = 10, invocationCount = 50)
public class ConcurrentTests extends BaseTestClass
{
    @Test(threadPoolSize = 10, invocationCount = 50)
    public void testA()
    {
    }

    @Test(threadPoolSize = 10, invocationCount = 50)
    public void testB()
    {
    }

    @Test(threadPoolSize = 10, invocationCount = 50)
    public void testC()
    {
    }

    @Test(threadPoolSize = 10, invocationCount = 50)
    public void testD()
    {
    }

    @Test(threadPoolSize = 10, invocationCount = 50)
    public void testE()
    {
    }
}
