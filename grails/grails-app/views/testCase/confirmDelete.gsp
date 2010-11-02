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
		<meta name="layout" content="mainBare"/>
		<title>Cuanto: Confirm Test Case deletion</title>

		<g:render template="/shared/yui282r1"/>
		<p:css name='../js/yui/2.8.2r1/button/assets/skins/sam/button'/>

		<yui:javascript dir="button" file="button-min.js" version="2.8.2r1"/>
		<p:javascript src="jq/jquery-1.4.2"/>

		<g:javascript>
		    $(function() {
			    new YAHOO.widget.Button('tcDeleteBtn');
		    });
		</g:javascript>
	</head>
	<body class=" yui-skin-sam">
		<div class="cuantoBody yui-skin-sam">

			<h1>Confirm Test Case deletion</h1>
			<p>Are you sure you want to delete the test case <b>${testCase?.fullName.encodeAsHTML()}</b>?
			This will permanently delete the test case and any results associated with that test case from all test
			runs.  If you are sure you want to delete this test case, type YES in the box below
			and click Delete.</p>
			<g:form controller="testCase" action="delete" id="${testCase?.id}">
				
				<input id="tcConfirmDelete" type="text" name="confirmDelete" /><br/>
				<input id="tcDeleteBtn" type="submit" value="Delete" />

					<div class="clear"></div>
				</div>
			</g:form>
		</div>
	</body>

</html>