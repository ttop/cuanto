%{--

 Copyright (c) 2010 Todd Wells

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
		<meta name="layout" content="mainBare"/>
		<title>Cuanto: Test Run ${testRun?.dateExecuted?.encodeAsHTML()} of Project ${testRun?.project?.name?.encodeAsHTML()}</title>
		<feed:meta kind="rss" version="2.0" controller="project" action="feed" id="${project?.id}"/>

		<g:render template="/shared/yui282r1"/>

		<p:css name='../js/yui/2.8.2r1/datatable/assets/skins/sam/datatable'/>
		<p:css name='../js/yui/2.8.2r1/paginator/assets/skins/sam/paginator'/>
		<p:css name='../js/yui/2.8.2r1/button/assets/skins/sam/button'/>
		<p:css name='../js/yui/2.8.2r1/assets/skins/sam/resize'/>

		<yui:javascript dir="datasource" file="datasource-min.js" version="2.8.2r1"/>
		<yui:javascript dir="datatable" file="datatable-min.js" version="2.8.2r1"/>
		<yui:javascript dir="datatable" file="cuanto-datatable-overrides.js" version="2.8.2r1"/>

		<yui:javascript dir="paginator" file="paginator-min.js" version="2.8.2r1"/>
		<yui:javascript dir="button" file="button-min.js" version="2.8.2r1"/>
		<yui:javascript dir="animation" file="animation-min.js" version="2.8.2r1"/>
		<yui:javascript dir="resize" file="resize-min.js" version="2.8.2r1"/>
		<yui:javascript dir="json" file="json-min.js" version="2.8.2r1"/>

		<p:javascript src="jq/jquery-1.4.2.min"/>
		<p:javascript src="cuanto/events"/>
		<p:javascript src="cuanto/url"/>
		<p:javascript src="cuanto/tcRename"/>

		<script type="text/javascript">
			$(function() {
			<g:render template="/testRun/urls"/>
				new YAHOO.cuanto.testCaseRename();
			});

		</script>
	</head>
	<body class=" yui-skin-sam">
		<div class="body yui-skin-sam">
			<div id="findDialog">
				<div>
					<span class="head1">Renaming Tool for Project
						<g:link controller="show" action="${project?.projectKey}">${project?.toString()}</g:link>
					</span>
					<p id="renameText">Renaming a test case will rename it for the entire history of the test.
					Cuanto will not retain the previous name of the test case.</p>
				</div>
				<div>
					<form id="renameForm">
						<g:hiddenField name="id" value="${project?.id}"/>
						<table>
							<tr id="findRow">
								<td align="right">Find test names with:</td>
								<td>
									<g:textField name="searchTerm" id="searchTerm" class="change"/>
								</td>
								<td>
									<button name="find" id="findBtn" type="push">Preview</button>
								</td>
							</tr>

							<tr id="replaceRow">
								<td align="right">Replace with:</td>
								<td>
									<g:textField name="replaceName" id="replaceName" class="change"/>
								</td>
								<td>
									<button name="replace" id="replaceBtn" type="push">Rename Selected</button>
								</td>
								<td id="cancelCell" style="display:none">
									<button name="replaceCancel" id="cancelBtn" type="push" >Cancel</button>
								</td>
							</tr>
						</table>
					</form>
				</div>
				<div id="renameTable"></div>
			</div>
		</div>
	</body>
</html>