package cuanto.adapter.util;

import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * @author Suk-Hyun Cho
 */
public class ArgumentParserTests {

	@Test
	public void testSingleKeyWithValueSeparatorOnly() {
		Map<String, String> actual = ArgumentParser.parseMap("key1:");
		Map<String, String> expected = makeMap("key1", "");
		assertEquals(actual, expected);
	}

	@Test
	public void testSingleKeyWithoutValueSeparator() {
		Map<String, String> actual = ArgumentParser.parseMap("key1");
		Map<String, String> expected = makeMap("key1", null);
		assertEquals(actual, expected);
	}

	@Test
	public void testMultipleKeysWithMismatchingValuePairsWithValueSeparator() {
		Map<String, String> actual = ArgumentParser.parseMap("key1:,key2:val2");
		Map<String, String> expected = makeMap("key1", "", "key2", "val2");
		assertEquals(actual, expected);
	}

	@Test
	public void testMultipleKeysWithMismatchingValuePairsAtEndWithValueSeparator() {
		Map<String, String> actual = ArgumentParser.parseMap("key1:val1,key2:");
		Map<String, String> expected = makeMap("key1", "val1", "key2", "");
		assertEquals(actual, expected);
	}

	@Test
	public void testMultipleKeysWithMismatchingValuePairsWithoutValueSeparator() {
		Map<String, String> actual = ArgumentParser.parseMap("key1,key2:val2");
		Map<String, String> expected = makeMap("key1", null, "key2", "val2");
		assertEquals(actual, expected);
	}

	@Test
	public void testMultipleKeysWithMismatchingValuePairsAtEndWithoutValueSeparator() {
		Map<String, String> actual = ArgumentParser.parseMap("key1:val1,key2");
		Map<String, String> expected = makeMap("key1", "val1", "key2", null);
		assertEquals(actual, expected);
	}

	@Test
	public void testSingleKeyValuePair() {
		Map<String, String> actual = ArgumentParser.parseMap("key1:val1");
		Map<String, String> expected = makeMap("key1", "val1");
		assertEquals(actual, expected);
	}

	@Test
	public void testMultipleKeyValuePairs() {
		Map<String, String> actual = ArgumentParser.parseMap("key1:val1,key2:val2");
		Map<String, String> expected = makeMap("key1", "val1", "key2", "val2");
		assertEquals(actual, expected);
	}

	@Test
	public void testKeyWithUrlValue() {
		Map<String, String> actual = ArgumentParser.parseMap("xkcd:http://www.xkcd.com");
		Map<String, String> expected = makeMap("xkcd", "http://www.xkcd.com");
		assertEquals(actual, expected);
	}

	@Test
	public void testMultipleKeysWithUrlValue() {
		Map<String, String> actual = ArgumentParser.parseMap("xkcd:http://www.xkcd.com,reddit:http://www.reddit.com");
		Map<String, String> expected = makeMap("xkcd", "http://www.xkcd.com", "reddit", "http://www.reddit.com");
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
