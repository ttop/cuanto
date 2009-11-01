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

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
	<head>
		<meta name="layout" content="main"/>
		<g:javascript src="cuanto/lruCache.js"/>
		<g:javascript src="cuanto/outputProxy.js"/>
		<g:javascript src="cuanto/testoutput.js"/>

		<g:render template="history_hdr"/>
		<yui:javascript dir="json" file="json-min.js" version="2.6.0"/>
		<yui:javascript dir="animation" file="animation-min.js" version="2.6.0"/>
		<script type="text/javascript">
			YAHOO.util.Event.onDOMReady(function () {
			<g:render template="/testRun/urls"/>

				YAHOO.cuanto.testCaseHistory.initTcHistoryTable(${testCase?.id});
			});
		</script>
	</head>
	<body class=" yui-skin-sam">
		<g:render template="history_body"/>
	</body>
</html>