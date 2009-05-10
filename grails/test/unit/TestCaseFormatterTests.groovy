import cuanto.formatter.*

/**
 * User: Todd Wells
 * Date: Apr 3, 2009
 * Time: 6:15:13 PM
 * 
 */
class TestCaseFormatterTests extends GroovyTestCase {

	final static String BAD_DESCRIPTION = "Wrong format description"
	final static String BAD_FORMAT = "Wrong format"
	final static String BAD_KEY = "Wrong key"
	
	void testFullPackageFormatter() {
		def formatter = new FullPackageFormatter()
		assertEquals BAD_DESCRIPTION, "full.package.Class.testMethod()", formatter.getDescription()
		assertEquals BAD_KEY, "fullpackage", formatter.getKey()

		assertEquals BAD_FORMAT, "Spinal.tap()", formatter.getTestName('Spinal', 'tap')
		assertEquals BAD_FORMAT, "goes.to.eleven()", formatter.getTestName('goes.to', 'eleven')
		assertEquals BAD_FORMAT, "stonehenge()", formatter.getTestName('', 'stonehenge')
	}

	void testClassnameFormatter() {
		def formatter = new ClassnameFormatter()
		assertEquals BAD_DESCRIPTION, "Class.testMethod()", formatter.getDescription()
		assertEquals BAD_KEY, "classname", formatter.getKey()

		assertEquals BAD_FORMAT, "Dwight.Schrute()", formatter.getTestName('office.Dwight', 'Schrute')
		assertEquals BAD_FORMAT, "liz.Lemon()", formatter.getTestName('30.rock.liz', 'Lemon')
		assertEquals BAD_FORMAT, "joelMcHale()", formatter.getTestName('', "joelMcHale")
		assertEquals BAD_FORMAT, "you.up()", formatter.getTestName('never.gonna.give.you', 'up')
		assertEquals BAD_FORMAT, "boring()", formatter.getTestName('', 'boring')
	}

	void testParentPackageFormatter() {
		def formatter = new ParentPackageFormatter()
		assertEquals BAD_DESCRIPTION, "parentPackage.Class.testMethod()", formatter.getDescription()
		assertEquals BAD_KEY, "parentpackage", formatter.getKey()

		assertEquals BAD_FORMAT, "not.A.contract()", formatter.getTestName("a.kiss.is.not.A", "contract")
		assertEquals BAD_FORMAT, "business.time()", formatter.getTestName("business", "time")
		assertEquals BAD_FORMAT, "inner.City.pressure()", formatter.getTestName("inner.City", "pressure")
	}

	void testMethodOnlyFormatter() {
		def formatter = new MethodOnlyFormatter()
		assertEquals BAD_DESCRIPTION, "testMethod()", formatter.getDescription()
		assertEquals BAD_KEY, "methodonly", formatter.getKey()

		assertEquals BAD_FORMAT, "testFoo()", formatter.getTestName("blah.blah.blah", "testFoo")
		assertEquals BAD_FORMAT, "testFoo()", formatter.getTestName("blah", "testFoo")
		assertEquals BAD_FORMAT, "testFoo()", formatter.getTestName("", "testFoo")
	}

	void testManualFormatter() {
		def formatter = new ManualFormatter()
		assertEquals BAD_DESCRIPTION, "full.package.path testName", formatter.getDescription()
		assertEquals BAD_KEY, "manual", formatter.getKey()

		assertEquals BAD_FORMAT, "My Manual Test", formatter.getTestName("My Manual", "Test")
		assertEquals BAD_FORMAT, "Test Name", formatter.getTestName("", "Test Name")
	}
}
