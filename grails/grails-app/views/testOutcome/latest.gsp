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

<%--
  User: Todd Wells
  Date: May 22, 2008
  Time: 11:15:14 PM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Cuanto: Test Outcome ${testOutcome?.testCase?.fullName} for Test Run ${testOutcome.testRun?.dateExecuted.encodeAsHTML()} of Project ${testRun?.project?.name}</title>
		<feed:meta kind="rss" version="2.0" controller="project" action="feed" id="${project?.id}"/>

		<p:css name='../js/yui/2.6.0/button/assets/skins/sam/button'/>
		
		<yui:javascript dir="button" file="button-min.js"/>
		<yui:javascript dir="get" file="get-min.js"/>
		<g:javascript src="cuanto/events.js"/>
		<g:javascript src="cuanto/formatBug.js"/>
		<g:javascript src="cuanto/url.js"/>

		<script type="text/javascript">

		YAHOO.util.Event.onDOMReady(function () {
			<g:render template="/testRun/urls"/>
			YAHOO.cuanto.events.displayOutcomeEvent.fire("${testOutcome.id}", false);
		});


	</script>
	</head>
	<body class=" yui-skin-sam">
		<div class="body yui-skin-sam">
			Return to test run <g:link controller="testRun" action="results" id="${testOutcome?.testRun?.id}">${testOutcome?.testRun?.dateExecuted}</g:link>
			<g:formRemote name= "searchForm" url="[controller:'testCase', action:'search']"
				update="[success:'searchResult',failure:'error']" >
				<g:textField name="qry"/> <g:submitButton name="submit" value="Search"/>
				<g:hiddenField name="project" value="${testOutcome?.testRun?.project?.id}"/>
			</g:formRemote>
			<div id="searchResult"></div>
		</div>
	</body>
</html>