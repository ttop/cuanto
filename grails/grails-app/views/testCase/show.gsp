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
		<title>Cuanto: Test Cases for ${project?.toString()}</title>
		<meta name="layout" content="mainBare"/>

		<p:css name='../js/yui/2.8.2r1/datatable/assets/skins/sam/datatable'/>
		<p:css name='../js/yui/2.8.2r1/button/assets/skins/sam/button'/>
		<p:css name='../js/yui/2.8.2r1/paginator/assets/skins/sam/paginator'/>

		<g:render template="/shared/yui282r1"/>
		<yui:javascript dir="datasource" file="datasource-min.js" version="2.8.2r1"/>
		<yui:javascript dir="datatable" file="datatable-min.js" version="2.8.2r1"/>
		<yui:javascript dir="paginator" file="paginator-min.js" version="2.8.2r1"/>

		<p:javascript src="jq/jquery-1.4.2.min"/>
		<p:javascript src="cuanto/url"/>
		<p:javascript src="cuanto/formatBug"/>
        <p:javascript src="cuanto/testcases"/>

		<script type="text/javascript">
			YAHOO.util.Event.onDOMReady(function () {
				<g:render template="/testRun/urls"/>
				YAHOO.cuanto.testCases.initTestCaseTable(${project?.id});
			});
	</script>
	</head>
	<body class=" yui-skin-sam">
		<div class="body yui-skin-sam">
			<div id="tcTable"></div>
			<div id="tcPaging"></div>
			 <ul>
			  <li>
				  <g:link controller="testCase" action="enterNew" id="${project?.id}">Add New Test Case</g:link>
			  </li>
		  </ul>
		</div>
	</body>
</html>