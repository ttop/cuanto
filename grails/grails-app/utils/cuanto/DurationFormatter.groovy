package cuanto
/**
 * User: Todd Wells
 * Date: Jan 30, 2010
 * Time: 8:52:59 AM
 * 
 */

public class DurationFormatter {

	final static Double SECOND = 1000
	final static Double MINUTE = 60 * SECOND
	final static Double HOUR = 60 * MINUTE
	def math = new Math()


	public formatMilliseconds(Long milliseconds) {
		Double workingMs = Double.valueOf(milliseconds as String)
		Double hrs = math.floor(workingMs / HOUR)
		workingMs -= hrs * HOUR

		Double minutes = math.floor(workingMs / MINUTE)
		workingMs -= minutes * MINUTE

		Double seconds = math.floor(workingMs / SECOND)
		workingMs -= seconds * SECOND

		def str = "";
		if (hrs > 0) {
			str = hrs.toLong().toString() + ":";
		}

		String paddedMin = pad(minutes, 2)
		String paddedSec = pad(seconds, 2)
		String paddedMs = pad(workingMs, 3)
		str += paddedMin + ":" + paddedSec + "." + paddedMs;
		return str
	}

	public formatMilliseconds(BigDecimal milliseconds) {
		formatMilliseconds(milliseconds.toLong())
	}

	private String pad(Double num, len) {
		num.toLong().toString().padLeft(len, "0")
	}

}