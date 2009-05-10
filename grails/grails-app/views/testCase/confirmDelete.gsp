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
		<title>Cuanto: Confirm Test Case deletion</title>
	</head>
	<body class=" yui-skin-sam">
		<div class="cuantoBody yui-skin-sam">

			<h1>Confirm Test Case deletion</h1>
			<p>Are you sure you want to delete the test case <b>${testCase?.fullName.encodeAsHTML()}</b>?
			This will permanently delete the test case and any results associated with that test case from all test
			runs.  If you are sure you want to delete this test case, type YES in the box below
			and click Delete.</p>
			<g:form controller="testCase" action="delete" id="${testCase?.id}">
				<input id="confirmDelete" type="text" name="confirmDelete" style="margin-left:15%; width:40px"/><br/>
				<div class="buttons">
					<span class="button">
						<input id="delete" class="delete" type="submit" value="Delete" style="margin-left:15%"/>
					</span>
					<div class="clear"></div>
				</div>
			</g:form>
		</div>
	</body>

</html>