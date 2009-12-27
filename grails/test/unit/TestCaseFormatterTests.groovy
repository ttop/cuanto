import cuanto.formatter.*
import cuanto.TestCase

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
	
	void testFullPackageFormatterWithoutParams() {
		def formatter = new FullPackageFormatter()
		assertEquals BAD_KEY, "fullpackage", formatter.key
		assertEquals BAD_DESCRIPTION, "full.package.Class.testMethod", formatter.description
		assertEquals BAD_FORMAT, "Spinal.tap", formatter.getTestName(new TestCase(packageName: 'Spinal', testName: 'tap'))
		assertEquals BAD_FORMAT, "goes.to.eleven", formatter.getTestName(new TestCase(packageName: 'goes.to', testName: 'eleven'))
		assertEquals BAD_FORMAT, "stonehenge", formatter.getTestName(new TestCase(packageName: '', testName: 'stonehenge'))
	}

	void testFullPackageFormatterWithParams() {
		def formatter = new FullPackageFormatter(showParams: true)
		assertEquals BAD_DESCRIPTION, "full.package.Class.testMethod(params)", formatter.description
		assertEquals BAD_KEY, "fullpackage", formatter.key

		assertEquals BAD_FORMAT, "Spinal.tap(drummer)", formatter.getTestName(new TestCase(packageName: 'Spinal',
			testName: 'tap', parameters: "drummer"))
		assertEquals BAD_FORMAT, "goes.to.eleven(amp)", formatter.getTestName(new TestCase(packageName: 'goes.to',
			testName: 'eleven', parameters: "amp"))
		assertEquals BAD_FORMAT, "stonehenge(druid, dancing)", formatter.getTestName(new TestCase(packageName: '',
			testName: 'stonehenge', parameters: "druid, dancing"))   
	}

	void testClassnameFormatter() {
		def formatter = new ClassnameFormatter()
		assertEquals BAD_DESCRIPTION, "Class.testMethod", formatter.description
		assertEquals BAD_KEY, "classname", formatter.key

		assertEquals BAD_FORMAT, "Dwight.Schrute", formatter.getTestName(new TestCase(packageName: 'office.Dwight', testName: 'Schrute'))
		assertEquals BAD_FORMAT, "liz.Lemon", formatter.getTestName(new TestCase(packageName: '30.rock.liz', testName: 'Lemon'))
		assertEquals BAD_FORMAT, "joelMcHale", formatter.getTestName(new TestCase(packageName: '', testName: "joelMcHale"))
		assertEquals BAD_FORMAT, "you.up", formatter.getTestName(new TestCase(packageName: 'never.gonna.give.you', testName: 'up'))
		assertEquals BAD_FORMAT, "boring", formatter.getTestName(new TestCase(packageName: '', testName: 'boring'))
	}

	void testClassnameFormatterWithParams() {
		def formatter = new ClassnameFormatter(showParams: true)
		assertEquals BAD_DESCRIPTION, "Class.testMethod(params)", formatter.description
		assertEquals BAD_KEY, "classname", formatter.key

		assertEquals BAD_FORMAT, "Dwight.Schrute(office)", formatter.getTestName(new TestCase(packageName: 'office.Dwight',
			testName: 'Schrute', parameters: "office"))
		assertEquals BAD_FORMAT, "liz.Lemon(star)", formatter.getTestName(new TestCase(packageName: '30.rock.liz',
			testName: 'Lemon', parameters: "star"))
		assertEquals BAD_FORMAT, "joelMcHale(the, soup)", formatter.getTestName(new TestCase(packageName: '',
			testName: "joelMcHale", parameters: "the, soup"))
		assertEquals BAD_FORMAT, "you.up(Astley, Rick)", formatter.getTestName(new TestCase(packageName: 'never.gonna.give.you',
			testName: 'up', parameters: "Astley, Rick"))
		assertEquals BAD_FORMAT, "boring(yawn)", formatter.getTestName(new TestCase(packageName: '',
			testName: 'boring', parameters: "yawn"))
	}

	void testParentPackageFormatter() {
		def formatter = new ParentPackageFormatter()
		assertEquals BAD_DESCRIPTION, "parentPackage.Class.testMethod", formatter.description
		assertEquals BAD_KEY, "parentpackage", formatter.key

		assertEquals BAD_FORMAT, "not.A.contract", formatter.getTestName(new TestCase(packageName: "a.kiss.is.not.A", testName: "contract"))
		assertEquals BAD_FORMAT, "business.time", formatter.getTestName(new TestCase(packageName: "business", testName: "time"))
		assertEquals BAD_FORMAT, "inner.City.pressure", formatter.getTestName(new TestCase(packageName: "inner.City", testName: "pressure"))
	}

	void testParentPackageFormatterWithParameters() {
		def formatter = new ParentPackageFormatter(showParams: true)
		assertEquals BAD_DESCRIPTION, "parentPackage.Class.testMethod(params)", formatter.description
		assertEquals BAD_KEY, "parentpackage", formatter.key

		assertEquals BAD_FORMAT, "not.A.contract(very nice)", formatter.getTestName(new TestCase(packageName: "a.kiss.is.not.A",
			testName: "contract", parameters: "very nice"))
		assertEquals BAD_FORMAT, "business.time()", formatter.getTestName(new TestCase(packageName: "business", testName: "time"))
		assertEquals BAD_FORMAT, "inner.City.pressure()", formatter.getTestName(new TestCase(packageName: "inner.City", testName: "pressure"))
	}

	void testMethodOnlyFormatter() {
		def formatter = new MethodOnlyFormatter()
		assertEquals BAD_DESCRIPTION, "testMethod", formatter.description
		assertEquals BAD_KEY, "methodonly", formatter.key

		assertEquals BAD_FORMAT, "testFoo", formatter.getTestName(new TestCase(packageName: "blah.blah.blah", testName: "testFoo"))
		assertEquals BAD_FORMAT, "testFoo", formatter.getTestName(new TestCase(packageName: "blah", testName: "testFoo"))
		assertEquals BAD_FORMAT, "testFoo", formatter.getTestName(new TestCase(packageName: "", testName: "testFoo"))
	}

	void testMethodOnlyFormatterWithParameters() {
		def formatter = new MethodOnlyFormatter(showParams: true)
		assertEquals BAD_DESCRIPTION, "testMethod(params)", formatter.description
		assertEquals BAD_KEY, "methodonly", formatter.key

		assertEquals BAD_FORMAT, "testFoo(bar)", formatter.getTestName(new TestCase(packageName: "blah.blah.blah",
			testName: "testFoo", parameters: "bar"))
		assertEquals BAD_FORMAT, "testFoo(bar)", formatter.getTestName(new TestCase(packageName: "blah",
			testName: "testFoo", parameters: "bar"))
		assertEquals BAD_FORMAT, "testFoo(bar)", formatter.getTestName(new TestCase(packageName: "",
			testName: "testFoo", parameters: "bar"))
	}

	void testManualFormatter() {
		def formatter = new ManualFormatter()
		assertEquals BAD_DESCRIPTION, "full.package.path testName", formatter.description
		assertEquals BAD_KEY, "manual", formatter.key

		assertEquals BAD_FORMAT, "My Manual Test", formatter.getTestName(new TestCase(packageName: "My Manual", testName: "Test")) 
		assertEquals BAD_FORMAT, "Test Name", formatter.getTestName(new TestCase(packageName: "", testName: "Test Name")) 
	}

	void testManualFormatterWithParameters() {
		def formatter = new ManualFormatter(showParams: true)
		assertEquals BAD_DESCRIPTION, "full.package.path testName: params", formatter.description
		assertEquals BAD_KEY, "manual", formatter.key

		assertEquals BAD_FORMAT, "My Manual Test: tada!", formatter.getTestName(new TestCase(packageName: "My Manual",
			testName: "Test", parameters: "tada!"))
		assertEquals BAD_FORMAT, "Test Name: params", formatter.getTestName(new TestCase(packageName: "",
			testName: "Test Name", parameters: "params")) 
	}
}
