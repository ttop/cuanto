/**
 * User: Todd Wells
 * Date: Dec 2, 2008
 * Time: 6:12:47 PM
 * 
 */
class BreakAtThresholdTests extends GroovyTestCase {
	def toc

	// this test is here to preserve the breakAtThreshold method -- this used to live somewhere else and is
	// currently unused, but I wanted it easily findable if it is needed again rather than browsing through old
	// revisions

	void setUp() {
		//toc = new TestOutcomeController()
	}

	void testNoBreak() {
		assertEquals "test", breakAtThreshold("test", 5)
	}

	void testSingleBreak() {
		assertEquals "testa<br/>b", breakAtThreshold("testab", 5)
	}

	void testMultipleBreaks() {
		assertEquals "abcde<br/>fghij<br/>klmno", breakAtThreshold("abcdefghijklmno", 5)
	}

	def breakAtThreshold(str, maxLine) {
		def outputToRender = new StringBuffer()
		def tokenized = str.split("\n")
		tokenized.each { line ->
			String temp = line
			def start = 0
			def end = start + getMaxIndex(temp, maxLine)
			while (temp.length() > maxLine) {
				def toAdd = temp.substring(start, end)
				outputToRender << "${toAdd}<br/>"
				temp = temp.substring(end)
				end = start + getMaxIndex(temp, maxLine)
			}
			outputToRender << temp.substring(start, end)
		}
		return outputToRender.toString()
	}

	def getMaxIndex(str, max) {
		if (str.length() > max) {
			return max
		}
		else {
			return str.length()
		}
	}
}