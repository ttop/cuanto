import cuanto.ParsingService

/**
 * User: Todd Wells
 * Date: Dec 11, 2008
 * Time: 5:51:03 PM
 * 
 */
class ParsingServiceUnitTests extends GroovyTestCase {

	def urls = ["http://foo/bar?baz=goop&blah=poof", "http://meta/bar?baz=goop&blah=poof",
		"http://foo/", "http://bar/baz?", "http://bar/baz?foo="]

	void testUrlParsing() {
		def parser = new ParsingService()

		//assertNotNull parser.parseUrls(null)
		//assertNotNull parser.parseUrls("")
		//assertEquals 0, parser.parseUrls("").size()
		//assertEquals 0, parser.parseUrls("foobar").size()
		/*
		assertEquals 0, parser.parseUrls("""

			""").size()
		*/

		def parsed = parser.parseUrls(urls[0])
		//assertEquals "single url not parsed", 1, parsed.size()
		//assertEquals "single url not parsed", urls[0], parsed[0]



		def multiUrls = "${urls[0]} dada ${urls[1]}"
		parsed = parser.parseUrls(multiUrls)
		assertEquals 2, parsed.size()
		assertEquals urls[0], parsed[0]
		assertEquals urls[1], parsed[1]

		multiUrls = " blah blah blah ${urls[0]} oh boy ${urls[1]} for sure ${urls[2]} ${urls[3]} and even ${urls[4]}"
		parsed = parser.parseUrls(multiUrls)
		assertEquals 5, parsed.size()
		parsed.eachWithIndex { it, indx ->
			assertEquals urls[indx], it
		}

		def multiLine = """${urls[0]}
${urls[1]}
		"""
		parsed = parser.parseUrls(multiLine)
		assertEquals 2, parsed.size()
		parsed.eachWithIndex { it, indx ->
			assertEquals urls[indx], it
		}

		multiLine = """${urls[0]} alkdjfldk20934095723 ${urls[1]} ;kldf
abcd ${urls[2]} lfsf ${urls[3]}
\t ${urls[4]}
		"""
		parsed = parser.parseUrls(multiLine)
		assertEquals 5, parsed.size()
		parsed.eachWithIndex { it, indx ->
			assertEquals urls[indx], it
		}
	}
}