import cuanto.TestRun
import cuanto.Project

/**
 * User: Todd Wells
 * Date: May 21, 2009
 * Time: 10:47:16 PM
 * 
 */
class TestRunUnitTests extends GroovyTestCase {

	void testBeforeInsert() {
		TestRun run = new TestRun()
		run.dateCreated = new Date()
		run.beforeInsert()
		assertEquals "Wrong dateExecuted", run.dateCreated, run.dateExecuted
	}

	void testEquals() {
		TestRun one = new TestRun()
		TestRun two = new TestRun()
		assertEquals one, two
		assertFalse(one.equals(null))

		Project projOne = new Project(name: "smile")
		one.project = projOne
		assertFalse one.equals(two)
		assertFalse two.equals(one)

		two.project = new Project(name: "bar")
		assertFalse one.equals(two)
		assertFalse two.equals(one)
		projOne.name = two.project.name
		assertEquals one, two

		two.note = "foosicle"
		assertFalse one.equals(two)
		assertFalse two.equals(one)
		one.note = two.note
		assertEquals one, two

		two.valid = true
		one.valid = false
		assertFalse one.equals(two)
		assertFalse two.equals(one)
		one.valid = true
		assertEquals one, two
		one.valid = false
		two.valid = false
		assertEquals one, two

	}
}