YAHOO.namespace('cuanto.test');

YAHOO.cuanto.test.LruCacheTest = new YAHOO.tool.TestCase({

	name: "LruCache Tests",

	testAddItem : function() {
		var Assert = YAHOO.util.Assert;

		var cache = new YAHOO.cuanto.LruCache(3);

		var beginTime = new Date().getTime();

		this.wait(function() {
			cache.addItem("a", "abc");
			var endTime = new Date().getTime();
			Assert.areEqual(1, cache.getCount());

			var item = cache.getRawItem("a");
			Assert.isObject(item);

			Assert.isTrue(item.date > beginTime);
			Assert.isTrue(item.date <= endTime);
			Assert.areEqual(item["value"], "abc");
		}, 10);
	},

	testMultipleItems : function() {
		var Assert = YAHOO.util.Assert;
		var cache = new YAHOO.cuanto.LruCache(3);

		cache.addItem("a", "abc");
		cache.addItem("b", "bcd");
		cache.addItem("num", 123);

		var a = cache.getItem("a");
		Assert.areEqual("abc", cache.getItem("a"));
		Assert.areEqual("bcd", cache.getItem("b"));
		Assert.areEqual(123, cache.getItem("num"));
	},

	testExceedCacheCapacity: function() {
		var Assert = YAHOO.util.Assert;
		var cache = new YAHOO.cuanto.LruCache(3);

		cache.addItem("a", "abc");

		this.wait(function() {
			cache.addItem("b", "bcd");
			cache.addItem("c", "cde");
			cache.addItem("num", 123);

			Assert.areEqual(3, cache.getCount());
			Assert.isString(cache.getItem("b"));
			Assert.isString(cache.getItem("c"));
			Assert.isNumber(cache.getItem("num"));
			Assert.isUndefined(cache.getItem("a"));

			cache.addItem("bb", "Bobby");
			cache.addItem("mk", "Mike");
			cache.addItem("p", "Peter");
			cache.addItem("mr", "Marcia");
			cache.addItem("j", "Jan");
			cache.addItem("c", "Cindy");

			Assert.areEqual(3, cache.getCount());
		}, 10);
	},

	testFlushCache: function() {
		var Assert = YAHOO.util.Assert;
		var cache = new YAHOO.cuanto.LruCache(3);
		cache.addItem("b", "bcd");
		cache.addItem("c", "cde");
		cache.addItem("num", 123);
		cache.addItem("bb", "Bobby");
		cache.addItem("mk", "Mike");
		cache.addItem("p", "Peter");
		cache.addItem("mr", "Marcia");
		cache.addItem("j", "Jan");
		cache.addItem("c", "Cindy");
		cache.flush();
		Assert.areEqual(0, cache.getCount());
	},

	testRemoveItem: function() {
		var Assert = YAHOO.util.Assert;
		var cache = new YAHOO.cuanto.LruCache(3);

		cache.addItem("a", "Dwight");
		cache.addItem("b", "Jim");
		cache.addItem("c", "Pam");

		var non = cache.removeItem("nonExistent");
		Assert.isUndefined(non);
		Assert.areEqual(3, cache.getCount());

		Assert.areEqual("Dwight", cache.removeItem("a"));
		Assert.areEqual(2, cache.getCount());

		cache.removeItem("b");
		Assert.areEqual(1, cache.getCount());

		cache.removeItem("c");
		Assert.areEqual(0, cache.getCount());

		cache.removeItem("non two");
		Assert.areEqual(0, cache.getCount());
	}

});