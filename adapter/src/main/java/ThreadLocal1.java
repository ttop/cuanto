import java.util.Random;

public class ThreadLocal1
{

    // Define/create thread local variable
    static ThreadLocal<Integer> threadLocal = new ThreadLocal<Integer>();

    // For random number generation
    static Random random = new Random();

    // Displays thread local variable and thread name
    private static void displayValues()
    {
        System.out.println(threadLocal.get() + "\t" + Thread.currentThread().getName());
    }

    public static void main(String args[])
    {

        // Each thread increments counter
        // Displays variable info
        // And sleeps for the random amount of time
        // Before displaying info again
        Runnable runner = new Runnable()
        {
            public void run()
            {
                threadLocal.set(new Integer(random.nextInt(1000)));
                displayValues();
                try
                {
                    Thread.sleep(((Integer) threadLocal.get()).intValue());
                    displayValues();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        };

        threadLocal.set(new Integer(random.nextInt(1000)));
        displayValues();

        // Here's where the other threads
        // are actually created
        for (int i = 0; i < 5; i++)
        {
            Thread t = new Thread(runner);
            t.start();
        }
    }
}
