%{--

 Copyright (c) 2009 Todd Wells

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
		<title>Cuanto: User Profile</title>

		<p:css name='../js/yui/2.6.0/button/assets/skins/sam/button'/>
		<yui:javascript dir="button" file="button-min.js" version="2.6.0"/>
		<g:javascript src="cuanto/userprofile.js"/>
		<g:javascript>
			YAHOO.util.Event.onDOMReady(function () {

			});
		</g:javascript>
	</head>
	<body>
		<div class="cuantoBody">
			<h1>User Profile for ${user.username}</h1>

			Real Name: ${user.userRealName}<br/>
			Email: ${user.email}<br/>

		</div>
	</body>
</html>