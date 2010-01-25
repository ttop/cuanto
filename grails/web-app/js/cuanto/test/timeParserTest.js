YAHOO.namespace('cuanto.test');

YAHOO.cuanto.test.TimeParserTest = new YAHOO.tool.TestCase({

	name: "TimeParser Tests",

	testMs : function() {
		var Assert = YAHOO.util.Assert;
		var timeParser = YAHOO.cuanto.TimeParser();

		Assert.areEqual("00:00.001", timeParser.formatMs(1));
		Assert.areEqual("00:00.011", timeParser.formatMs(11));
		Assert.areEqual("00:00.111", timeParser.formatMs(111));
		Assert.areEqual("00:00.999", timeParser.formatMs(999));
		Assert.areEqual("00:01.000", timeParser.formatMs(1000));
		Assert.areEqual("00:11.000", timeParser.formatMs(11000));
		Assert.areEqual("00:59.000", timeParser.formatMs(59000));
		Assert.areEqual("01:00.000", timeParser.formatMs(60000));
		Assert.areEqual("10:00.000", timeParser.formatMs(600000));
		Assert.areEqual("10:01.303", timeParser.formatMs(601303));
		Assert.areEqual("1:00:00.999", timeParser.formatMs(3600999));
	}

});