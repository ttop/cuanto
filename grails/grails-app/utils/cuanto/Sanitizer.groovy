package cuanto

/**
 * Contains static methods for data sanitization.
 */
public class Sanitizer {

	/**
	 * Escapes the html <script> tags.
	 *
	 * @param s String to escape
	 * @return escaped String
	 */
	static String escapeHtmlScriptTags(String s) {
		s.replaceAll('<script', '&lt;script').replaceAll('</script>', '&lt;/script&gt;')
	}

	/**
	 * Unescapes the the html <script> tags.
	 *
	 * @param s String to unescape
	 * @return unescaped String 
	 */
	static String unescapeHtmlScriptTags(String s) {
		s.replaceAll('&lt;script', '<script').replaceAll('&lt;/script&gt;', '</script>')
	}
}