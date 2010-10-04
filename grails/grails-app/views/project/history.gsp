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
<g:set var="bullet" value="${grailsApplication.config.bullet}"/>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
	<head>
		<meta name="layout" content="mainBare"/>

		<g:render template="/shared/yui26"/>

		<feed:meta kind="rss" version="2.0" controller="project" action="feed" id="${project?.id}"/>
		<title>Cuanto: Test Run History for ${project?.name?.encodeAsHTML()} (id ${project?.id})</title>
		<p:css name='../js/yui/2.6.0/datatable/assets/skins/sam/datatable'/>
		<p:css name='../js/yui/2.6.0/paginator/assets/skins/sam/paginator'/>
		<p:css name='../js/yui/2.6.0/button/assets/skins/sam/button'/>
		<p:css name='columnDialog'/>

		<p:css name='../js/yui/2.6.0/autocomplete/assets/skins/sam/autocomplete'/>

		<yui:javascript dir="button" file="button-min.js" version="2.6.0"/>
		<yui:javascript dir="datasource" file="datasource-min.js" version="2.6.0"/>
		<yui:javascript dir="datatable" file="cuanto-datatable-min.js" version="2.6.0"/>
		<yui:javascript dir="animation" file="animation-min.js" version="2.6.0"/>
		<yui:javascript dir="json" file="json-min.js" version="2.6.0"/>
		<yui:javascript dir="autocomplete" file="autocomplete-min.js" version="2.6.0"/>
		<yui:javascript dir="paginator" file="paginator-min.js" version="2.6.0"/>
		<yui:javascript dir="cookie" file="cookie-min.js" version="2.6.0"/>

		<g:javascript src="jq/jquery-1.4.2.min.js"/>
		<g:javascript src="cuanto/url.js"/>
		<g:javascript src="cuanto/projectDialog.js"/>
		<g:javascript src="cuanto/deleteProjectDialog.js"/>
		<g:javascript src="cuanto/events.js"/>
		<g:javascript src="cuanto/timeParser.js"/>

		<g:javascript src="cuanto/jcolumnDialog.js"/>
		<g:javascript src="cuanto/projectHistory.js"/>

		<script type="text/javascript">

			YAHOO.util.Event.onDOMReady(function () {
			<g:render template="/testRun/urls"/>
				YAHOO.cuanto.projectHistory.initHistoryTable(<%=propNames%>);
			});

		</script>
	</head>

	<body>
		<div class="body yui-skin-sam">
			<g:render template="phHeader"/>
			
			<div id="columnPanel" style="visibility:hidden">
				<div class="hd">Columns</div>
				<div id="columnPanel-picker" class="bd">
				</div>
			</div>

			Select a test run to view the detailed results and analysis:<br/>
			<div id="testRunList">
				<div id="testRunTableDiv"></div>
				<div id="trTablePaging"></div>
			</div>
			<p/><br/>
			<g:if test="${chartUrl}">
				<div id="lineChart">
					<img src="${chartUrl}" alt="Success Rate Trend Chart" class="graph"/>
				</div>
			</g:if>
		</div>
		<g:render template="projectDialog"/>

	</body>
</html>
