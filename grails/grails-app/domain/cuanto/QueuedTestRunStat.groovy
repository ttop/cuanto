package cuanto
/**
 * User: Todd Wells
 * Date: Jan 5, 2010
 * Time: 12:53:22 PM
 * 
 */

public class QueuedTestRunStat {
	// This class represents a test run that is queued for stat calculation -- we'll just use the ID rather than
	// a reference to the actual TestRun object. If the TestRun is not present at the time the calculation is
	// attempted, it will just be ignored. This way we don't have to manage the case where TestRuns are deleted
	// while in the queue
	Long testRunId
	Date dateCreated
}