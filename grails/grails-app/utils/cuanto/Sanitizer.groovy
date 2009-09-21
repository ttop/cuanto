/*
 Copyright (c) 2009 Suk-Hyun Cho

This file is part of Cuanto, a test results repository and analysis program.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/
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
		s?.replaceAll('<script', '&lt;script')?.replaceAll('</script>', '&lt;/script&gt;')
	}

	/**
	 * Unescapes the the html <script> tags.
	 *
	 * @param s String to unescape
	 * @return unescaped String 
	 */
	static String unescapeHtmlScriptTags(String s) {
		s?.replaceAll('&lt;script', '<script')?.replaceAll('&lt;/script&gt;', '</script>')
	}
}