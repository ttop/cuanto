import cuanto.test.WordGenerator

/**
 * Created by IntelliJ IDEA.
 * User: Todd Wells
 * Date: May 11, 2008
 * Time: 1:12:41 AM
 * 
 */
class WordGenTests extends GroovyTestCase {

	void testCamelize() {
		WordGenerator wordGen = new WordGenerator()
		assertEquals("Foo", wordGen.camelizeWord("fOo"))
		assertEquals("A", wordGen.camelizeWord("a"))
		assertEquals "", wordGen.camelizeWord("")
		assertEquals("Yayayada", wordGen.camelizeWord("YaYaYADa"))
	}
}