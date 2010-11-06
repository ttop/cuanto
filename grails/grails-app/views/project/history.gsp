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

		<g:render template="/shared/yui282r1"/>

		<feed:meta kind="rss" version="2.0" controller="project" action="feed" id="${project?.id}"/>
		<title>Cuanto: Test Run History for ${project?.name?.encodeAsHTML()} (id ${project?.id})</title>
		<p:css name='../js/yui/2.8.2r1/datatable/assets/skins/sam/datatable'/>
		<p:css name='../js/yui/2.8.2r1/paginator/assets/skins/sam/paginator'/>
		<p:css name='../js/yui/2.8.2r1/button/assets/skins/sam/button'/>
		<p:css name='columnDialog'/>

		<p:css name='../js/yui/2.8.2r1/autocomplete/assets/skins/sam/autocomplete'/>

		<yui:javascript dir="button" file="button-min.js" version="2.8.2r1"/>
		<yui:javascript dir="datasource" file="datasource-min.js" version="2.8.2r1"/>
		<yui:javascript dir="datatable" file="datatable-min.js" version="2.8.2r1"/>
		<yui:javascript dir="datatable" file="cuanto-datatable-overrides.js" version="2.8.2r1"/>
		<yui:javascript dir="datatable" file="datatable-min.js" version="2.8.2r1"/>
		<yui:javascript dir="animation" file="animation-min.js" version="2.8.2r1"/>
		<yui:javascript dir="json" file="json-min.js" version="2.8.2r1"/>
		<yui:javascript dir="autocomplete" file="autocomplete-min.js" version="2.8.2r1"/>
		<yui:javascript dir="paginator" file="paginator-min.js" version="2.8.2r1"/>
		<yui:javascript dir="cookie" file="cookie-min.js" version="2.8.2r1"/>

		<p:javascript src="jq/jquery-1.4.2.min"/>
		<p:javascript src="cuanto/url"/>
		<p:javascript src="cuanto/projectDialog"/>
		<p:javascript src="cuanto/deleteProjectDialog"/>
		<p:javascript src="cuanto/events"/>
		<p:javascript src="cuanto/timeParser"/>

		<p:javascript src="cuanto/columnDialog"/>
		<p:javascript src="cuanto/selectControl"/>
		<p:javascript src="cuanto/projectHistory"/>

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
			
			<div id="columnPanel" style="display:none">
				<div class="hd">Columns</div>
				<div id="columnPanel-picker" class="bd">
				</div>
			</div>

			<div id="bulkButtons" style="display:none">
				<button name="delete" id="deleteBtn" type="push" style="display:none">Delete Selected Test Runs</button>
				<button name="cancelDelete" id="cancelDeleteBtn" type="push" style="display:none">Cancel Deletion</button>
			</div>
			<div id="deleteText" style="display:none">Deleting Test Runs... <img src="${resource(dir:'images', file:'spinner.gif')}" alt="spinner"/></div>
			<div id="selectTrText">Select a test run to view the detailed results and analysis:</div>
			
			<div id="testRunList">
				<g:render template="/shared/selectOptions"/>
				<div id="testRunTableDiv"></div>
				<div id="trTablePaging"></div>
			</div>
			<p/><br/>
			<g:if test="${chartUrl}">
				<div id="lineChart">
					<img src="${chartUrl}" alt="Success Rate Trend Chart" class="graph"/>
				</div>
			</g:if>
			<div id="myLogger"></div>
			
		</div>
		<g:render template="projectDialog"/>
		<g:render template="deleteProjectDialog"/>
	</body>
</html>
