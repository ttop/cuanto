import cuanto.ParsingService
/**
 * Created by IntelliJ IDEA.
 * User: ttop
 * Date: 10/9/11
 * Time: 11:45 AM
 * To change this template use File | Settings | File Templates.
 */
class DateFormatTests extends GroovyTestCase{

	void testDateFormat() {
		ParsingService parsingService = new ParsingService()
		Date sourceDate = new Date(1318186094773)
		// Sun Oct 09 11:48:14 PDT 2011
		// 9 Oct 2011 18:48:14 GMT
		// 1318186094773
		Date parsedDate = parsingService.getDateFromString("1318186094773")
		assertEquals("Wrong date for Long", sourceDate.time, parsedDate.time)

		parsedDate = parsingService.getDateFromString("2011-10-09 11:48:14")
		assertWithinSecond(sourceDate, parsedDate)

		parsedDate = parsingService.getDateFromString("2011-10-09T11:48:14-0700")
		assertWithinSecond(sourceDate, parsedDate)

		parsedDate = parsingService.getDateFromString("2011-10-09T18:48:14-0000")
		assertWithinSecond(sourceDate, parsedDate)

		parsedDate = parsingService.getDateFromString("2011-10-09T11:48:14.000-0700")
		assertWithinSecond(sourceDate, parsedDate)
	}

	void assertWithinSecond(Date expected, Date actual) {
		def difference = Math.abs(expected.time - actual.time)
		assertTrue("Not within one second", difference < 1000)
	}
}
