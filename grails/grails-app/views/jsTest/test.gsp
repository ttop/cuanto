%{--

 Copyright (c) 2008 thePlatform, Inc.

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


--}%

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
	<head>
		<title>Cuanto test suite</title>

		<style type="text/css">
		body {
			margin: 0;
			padding: 0;
		}

		#testConsole {
			margin: 20px;
		}
		</style>

		<p:css name='../js/yui/2.8.2r1/logger/assets/skins/sam/logger'/>
		<p:css name='../js/yui/2.8.2r1/fonts/fonts-min'/>
		<p:css name='../js/yui/2.8.2r1/yuitest/assets/skins/sam/yuitest'/>

		<yui:javascript dir="yahoo-dom-event" file="yahoo-dom-event.js" version="2.8.2r1"/>
		<yui:javascript dir="logger" file="logger-min.js" version="2.8.2r1"/>
		<yui:javascript dir="yuitest" file="yuitest-min.js" version="2.8.2r1"/>
		<p:javascript src="cuanto/lruCache"/>
		<p:javascript src="cuanto/test/lruCacheTest"/>
		<p:javascript src="cuanto/timeParser"/>
		<p:javascript src="cuanto/test/timeParserTest"/>
	</head>
	<body class=" yui-skin-sam">
		<div id="testConsole">
			<h2>Cuanto javascript tests</h2>
			<div id="testLogger"></div>
		</div>
		<script type="text/javascript">
			YAHOO.namespace('cuanto.test');

			YAHOO.cuanto.test.TestSuite = new YAHOO.tool.TestSuite("Cuanto Test Suite");
			YAHOO.cuanto.test.TestSuite.add(YAHOO.cuanto.test.LruCacheTest);
			YAHOO.cuanto.test.TestSuite.add(YAHOO.cuanto.test.TimeParserTest);

			YAHOO.util.Event.onDOMReady(function () {
				var logger = new YAHOO.tool.TestLogger("testLogger");
				YAHOO.tool.TestRunner.add(YAHOO.cuanto.test.TestSuite);
				YAHOO.tool.TestRunner.run();
			});
		</script>
	</body>
</html>