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
		<feed:meta kind="rss" version="2.0" controller="project" action="feed" id="${project?.id}"/>
		<title>Cuanto: Test Run History for ${project?.name?.encodeAsHTML()} (id ${project?.id})</title>
		<p:css name='../js/yui/2.6.0/datatable/assets/skins/sam/datatable'/>
		<p:css name='../js/yui/2.6.0/paginator/assets/skins/sam/paginator'/>

		<yui:javascript dir="datasource" file="datasource-min.js" version="2.6.0"/>
		<yui:javascript dir="datatable" file="cuanto-datatable-min.js" version="2.6.0"/>
		<yui:javascript dir="paginator" file="paginator-min.js" version="2.6.0"/>

		<g:javascript src="cuanto/url.js"/>
		<g:javascript src="cuanto/projectHistory.js"/>

		<script type="text/javascript">

		YAHOO.util.Event.onDOMReady(function () {
			<g:render template="/testRun/urls"/>
			YAHOO.cuanto.projectHistory.initHistoryTable();
		});

		</script>
	</head>

	<g:set var="bullet" value="${grailsApplication.config.bullet}"/>

	<body class=" yui-skin-sam">
		<div class="body yui-skin-sam">
			<span class="head1">Test Run History for
				<g:if test="${project?.projectGroup}">
				<g:link controller="project" action="listGroup"
					params="['group': project?.projectGroup]">${project?.projectGroup?.name?.encodeAsHTML()}</g:link>/</g:if><g:link controller="project" action="history" params="[projectKey: project?.projectKey]">${project?.name?.encodeAsHTML()}</g:link>
				<g:link controller="project" action="feed" id="${project?.id}">
					<g:set var="feedTxt" value="RSS feed"/>
					<img id="feedImg" src="${resource(dir: 'images/feedicons-standard', file: 'feed-icon-14x14.png')}"
						alt="RSS Feed" title="Subscribe to the RSS feed for ${project?.toString()?.encodeAsHTML()}"/></g:link>
			</span>
			<br/>
			<span class="smaller">( <a href="${createLink(controller: 'testRun', action:'latest')}/${project?.projectKey}">Most Recent</a> ${bullet}
			<g:link controller="testCase" action="show" id="${project?.id}">Show Test Cases</g:link>
			<g:if test="${project?.testType?.name == 'Manual'}">${bullet}
				<g:link controller="testRun" action="createManual" id="${project?.id}">Create Manual Test Run</g:link>
			</g:if>)</span>
			<p/><br/>
			Select a test run to view the detailed results and analysis:
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

	</body>
</html>
