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
		<g:render template="/shared/yui282r1"/>
	</head>
	<body class=" yui-skin-sam">
		<div class="body yui-skin-sam cuantoBody ">
			<g:form controller="testCase" action="update" class="wideForm">
				<g:render template="form_fields"/>
				<g:actionSubmit value="Update"/>
			</g:form>
		</div>
	</body>
</html>