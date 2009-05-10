%{--

 [LICENSE_HEADER]

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