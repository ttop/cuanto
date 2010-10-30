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

<%@ page import="java.text.SimpleDateFormat; cuanto.TestResult" contentType="text/html;charset=UTF-8" %>
<html>
	<head>
		<meta name="layout" content="mainBare"/>
		<title>Cuanto: Test Run ${testRun?.dateExecuted?.encodeAsHTML()} of Project ${testRun?.project?.name?.encodeAsHTML()}</title>
		<feed:meta kind="rss" version="2.0" controller="project" action="feed" id="${project?.id}"/>

		<g:render template="/shared/yui282r1"/>

		<p:css name='analysis'/>
		<p:css name='columnDialog'/>
		<p:css name='../js/yui/2.8.2r1/datatable/assets/skins/sam/datatable'/>
		<p:css name='../js/yui/2.8.2r1/tabview/assets/skins/sam/tabview'/>
		<p:css name='../js/yui/2.8.2r1/paginator/assets/skins/sam/paginator'/>
		<p:css name='../js/yui/2.8.2r1/button/assets/skins/sam/button'/>
		<p:css name='../js/yui/2.8.2r1/assets/skins/sam/resize'/>

		<yui:javascript dir="tabview" file="tabview-min.js" version="2.8.2r1"/>
		<yui:javascript dir="datasource" file="datasource-min.js" version="2.8.2r1"/>
		<yui:javascript dir="datatable" file="cuanto-datatable-min.js" version="2.8.2r1"/>
		<yui:javascript dir="paginator" file="paginator-min.js" version="2.8.2r1"/>
		<yui:javascript dir="button" file="button-min.js" version="2.8.2r1"/>
		<yui:javascript dir="animation" file="animation-min.js" version="2.8.2r1"/>
		<yui:javascript dir="resize" file="resize-min.js" version="2.8.2r1"/>
		<yui:javascript dir="json" file="json-min.js" version="2.8.2r1"/>
		<yui:javascript dir="history" file="history-min.js" version="2.8.2r1"/>
		<yui:javascript dir="cookie" file="cookie-min.js" version="2.8.2r1"/>

		<g:javascript src="jq/jquery-1.4.2.min.js"/>
		<g:javascript src="cuanto/events.js"/>
		<g:javascript src="cuanto/formatBug.js"/>
		<g:javascript src="cuanto/url.js"/>
		<g:javascript src="cuanto/tableHelper.js"/>
		<g:javascript src="cuanto/lruCache.js"/>
		<g:javascript src="cuanto/outputProxy.js"/>
		<g:javascript src="cuanto/testoutput.js"/>
		<g:javascript src="cuanto/timeParser.js"/>
		<g:javascript src="cuanto/analysisTable.js"/>
		<g:javascript src="cuanto/summaryTab.js"/>
		<g:javascript src="cuanto/analysisDialog.js"/>
		<g:javascript src="cuanto/columnDialog.js"/>
		<g:javascript src="cuanto/groupedOutput.js"/>

		<script type="text/javascript">

		YAHOO.util.Event.onDOMReady(function () {
			<g:render template="urls"/>
            var newWidth = $(window).width() * .95;
            $('#tabContainer').width(newWidth);
            tabView = new YAHOO.widget.TabView("tabContainer");
            
			new YAHOO.cuanto.SummaryTab();
			new YAHOO.cuanto.AnalysisTable(${testResultList}, ${analysisStateList});
            new YAHOO.cuanto.GroupedOutput();
		});
		</script>
	</head>
	<body class=" yui-skin-sam">
		<div class="body yui-skin-sam">
			<g:render template="header"/>
			<g:render template="tabs"/>
		</div>
		<div id="outContainer">
			<div id="outPanel" style="visibility:hidden">
				<div id="outPanelBody" class="bd"></div>
				<div class="ft"></div>
			</div>
		</div>
		<div id="anContainer">
			<div id="anPanel" style="visibility:hidden" class="hidden">
				<div class="hd">Test Analysis for <span id="testCase"></span></div>
				<div id="anPanelBody" class="bd">
					<g:render template="/testCase/analysis"/>
				</div>
				<div class="ft"></div>
			</div>
		</div>
		<div id="columnPanel" style="visibility:hidden">
			<div class="hd">Columns</div>
			<div id="columnPanel-picker" class="bd"></div>
		</div>
		<div id="failContainer"></div>
		<iframe id="yui-history-iframe" src="${resource(dir: 'images/tango/16x16', file: 'accessories-text-editor.png')}"></iframe>
		<input id="yui-history-field" type="hidden">
	</body>
</html>
