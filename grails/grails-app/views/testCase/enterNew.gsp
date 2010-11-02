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

		<p:javascript src="cuanto/url"/>
		<p:css name='../js/yui/2.8.2r1/button/assets/skins/sam/button'/>

		<script type="text/javascript">
			YAHOO.util.Event.onDOMReady(function () {
			<g:render template="/testRun/urls"/>
			});
		</script>
	</head>
	<body class=" yui-skin-sam">
		<div class="body yui-skin-sam cuantoBody ">
			<g:form controller="testCase" action="create">
				<g:render template="form_fields"/>
				<g:actionSubmit value="Create"/>
				<g:actionSubmit action="create" value="Create and Add Another"/>
			</g:form>
		</div>
	</body>
</html>