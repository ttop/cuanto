package cuanto.adapter.util;

import org.testng.annotations.Test;

import java.util.*;

import static org.testng.Assert.assertEquals;

/**
 * @author Suk-Hyun Cho
 */
public class ArgumentParserTests {

	@Test
	public void testParseMapForNullString() {
		Map<String, String> actual = ArgumentParser.parseMap(null);
		Map<String, String> expected = new LinkedHashMap<String, String>();
		assertEquals(actual, expected);
	}

	@Test
	public void testParseMapForEmptyString() {
		Map<String, String> actual = ArgumentParser.parseMap("");
		Map<String, String> expected = new LinkedHashMap<String, String>();
		assertEquals(actual, expected);
	}

	@Test
	public void testParseMapForSingleKeyWithValueSeparatorOnly() {
		Map<String, String> actual = ArgumentParser.parseMap("key1:");
		Map<String, String> expected = makeMap("key1", "");
		assertEquals(actual, expected);
	}

	@Test
	public void testParseMapForSingleKeyWithoutValueSeparator() {
		Map<String, String> actual = ArgumentParser.parseMap("key1");
		Map<String, String> expected = makeMap("key1", null);
		assertEquals(actual, expected);
	}

	@Test
	public void testParseMapForMultipleKeysWithMismatchingValuePairsWithValueSeparator() {
		Map<String, String> actual = ArgumentParser.parseMap("key1:,key2:val2");
		Map<String, String> expected = makeMap("key1", "", "key2", "val2");
		assertEquals(actual, expected);
	}

	@Test
	public void testParseMapForMultipleKeysWithMismatchingValuePairsAtEndWithValueSeparator() {
		Map<String, String> actual = ArgumentParser.parseMap("key1:val1,key2:");
		Map<String, String> expected = makeMap("key1", "val1", "key2", "");
		assertEquals(actual, expected);
	}

	@Test
	public void testParseMapForMultipleKeysWithMismatchingValuePairsWithoutValueSeparator() {
		Map<String, String> actual = ArgumentParser.parseMap("key1,key2:val2");
		Map<String, String> expected = makeMap("key1", null, "key2", "val2");
		assertEquals(actual, expected);
	}

	@Test
	public void testParseMapForMultipleKeysWithMismatchingValuePairsAtEndWithoutValueSeparator() {
		Map<String, String> actual = ArgumentParser.parseMap("key1:val1,key2");
		Map<String, String> expected = makeMap("key1", "val1", "key2", null);
		assertEquals(actual, expected);
	}

	@Test
	public void testParseMapForSingleKeyValuePair() {
		Map<String, String> actual = ArgumentParser.parseMap("key1:val1");
		Map<String, String> expected = makeMap("key1", "val1");
		assertEquals(actual, expected);
	}

	@Test
	public void testParseMapForMultipleKeyValuePairs() {
		Map<String, String> actual = ArgumentParser.parseMap("key1:val1,key2:val2");
		Map<String, String> expected = makeMap("key1", "val1", "key2", "val2");
		assertEquals(actual, expected);
	}

	@Test
	public void testParseMapForKeyWithUrlValue() {
		Map<String, String> actual = ArgumentParser.parseMap("xkcd:http://www.xkcd.com");
		Map<String, String> expected = makeMap("xkcd", "http://www.xkcd.com");
		assertEquals(actual, expected);
	}

	@Test
	public void testParseMapForMultipleKeysWithUrlValue() {
		Map<String, String> actual = ArgumentParser.parseMap("xkcd:http://www.xkcd.com,reddit:http://www.reddit.com");
		Map<String, String> expected = makeMap("xkcd", "http://www.xkcd.com", "reddit", "http://www.reddit.com");
		assertEquals(actual, expected);
	}

	@Test
	public void testParseListForNullString() {
		List<String> actual = ArgumentParser.parseList(null);
		List<String> expected = new LinkedList<String>();
		assertEquals(actual, expected);
	}

	@Test
	public void testParseListForEmptyString() {
		List<String> actual = ArgumentParser.parseList("");
		List<String> expected = new LinkedList<String>();
		assertEquals(actual, expected);
	}

	@Test
	public void testParseListForSingleSeparatorOnly() {
		List<String> actual = ArgumentParser.parseList(",");
		List<String> expected = new LinkedList<String>();
		assertEquals(actual, expected);
	}

	@Test
	public void testParseListForSingleElement() {
		List<String> actual = ArgumentParser.parseList("foo");
		List<String> expected = Arrays.asList("foo");
		assertEquals(actual, expected);
	}

	@Test
	public void testParseListForSingleElementStartingWithSeparator() {
		List<String> actual = ArgumentParser.parseList(",foo");
		List<String> expected = Arrays.asList("foo");
		assertEquals(actual, expected);
	}

	@Test
	public void testParseListForSingleElementEndingWithSeparator() {
		List<String> actual = ArgumentParser.parseList("foo,");
		List<String> expected = Arrays.asList("foo");
		assertEquals(actual, expected);
	}

	@Test
	public void testParseListForSingleElementStartingAndEndingWithSeparator() {
		List<String> actual = ArgumentParser.parseList(",foo,");
		List<String> expected = Arrays.asList("foo");
		assertEquals(actual, expected);
	}

	@Test
	public void testParseListForMultipleElements() {
		List<String> actual = ArgumentParser.parseList("foo,bar");
		List<String> expected = Arrays.asList("foo", "bar");
		assertEquals(actual, expected);
	}

	@Test
	public void testParseListForMultipleElementsEndingWithSeparator() {
		List<String> actual = ArgumentParser.parseList("foo,bar,");
		List<String> expected = Arrays.asList("foo", "bar");
		assertEquals(actual, expected);
	}

	/**
	 * Make a map out of the variable arguments, where the first and the second arguments
	 * become the first key-value pair, the third and fourth arguments become the second key-value pair, and so on.
	 *
	 * @param args variable number of argments, but it must be greater than 2 and be of even number.
	 * @return Map of key-value pairs as provided
	 */
	private Map<String, String> makeMap(String... args) {
		if (args.length < 2 || args.length % 2 == 1)
			throw new IllegalArgumentException(
				"Please provide arguments in the format of (key1, val1, key2, val2, ...)");

		Map<String, String> map = new LinkedHashMap<String, String>();
		for (int i = 0; i < args.length - 1; i += 2) {
			map.put(args[i], args[i + 1]);
		}

		return map;
	}
}
