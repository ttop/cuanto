import cuanto.DurationFormatter

class CuantoTagLib {

	def durationFormatter = new DurationFormatter()

	def formatDuration = {attrs, body ->
		if (attrs.ms) {
			out << durationFormatter.formatMilliseconds(Long.valueOf(attrs.ms))
		}
	}
}
