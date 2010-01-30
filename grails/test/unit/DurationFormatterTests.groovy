import cuanto.DurationFormatter

/**
 * User: Todd Wells
 * Date: Jan 30, 2010
 * Time: 9:08:45 AM
 * 
 */

public class DurationFormatterTests extends GroovyTestCase {

	def durationFormatter = new DurationFormatter() 
	
	       void testFormatDuration() {
		    assertEquals "00:00.001", durationFormatter.formatMilliseconds(1)
		    assertEquals "00:00.011", durationFormatter.formatMilliseconds(11)
		    assertEquals "00:00.999", durationFormatter.formatMilliseconds(999)
		    assertEquals "00:01.000", durationFormatter.formatMilliseconds(1000)
		    assertEquals "00:11.000", durationFormatter.formatMilliseconds(11000)
		    assertEquals "00:59.000", durationFormatter.formatMilliseconds(59000)
		    assertEquals "01:00.000", durationFormatter.formatMilliseconds(60000)
		    assertEquals "10:00.000", durationFormatter.formatMilliseconds(600000)
		    assertEquals "10:01.303", durationFormatter.formatMilliseconds(601303)
		    assertEquals "1:00:00.999", durationFormatter.formatMilliseconds(3600999)
	    }

}