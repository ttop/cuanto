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

<%@ page import="cuanto.User" %><head>
	<meta name="layout" content="main" />
	<title>Cuanto: User List</title>
    <p:css name='../js/yui/2.6.0/datatable/assets/skins/sam/datatable'/>
    <yui:javascript dir="datasource" file="datasource-min.js" version="2.6.0"/>
    <yui:javascript dir="datatable" file="cuanto-datatable-min.js" version="2.6.0"/>
    <g:javascript src="cuanto/users.js"/>

	<g:javascript>
	YAHOO.util.Event.onDOMReady(function () {
		var users = new YAHOO.cuanto.users();
		users.initUserList();
	});
	</g:javascript>
</head>

<body class="yui-skin-sam">

	<div class="body ">

		<g:link class="smaller" action="create">Add User</g:link>
		<h1>User List</h1>
		<p/>
		<div id="userlistdiv" >
			<table id="userlist">
			<thead>
				<tr>
					<th>ID</th>
					<th>Username</th>
					<th>Real Name</th>
					<th>Enabled</th>
					<th>Roles</th>
				</tr>
			</thead>
			<tbody>
			<g:each in="${personList}" var="person">
				<tr>
					<td>${person.id}</td>
					<td><g:link action="show" id="${person.id}">${person.username?.encodeAsHTML()}</g:link></td>
					<td>${person.userRealName?.encodeAsHTML()}</td>
					<td>${person.enabled?.encodeAsHTML()}</td>

					<%
					    def roles = ""
					    person.authorities.eachWithIndex { role, idx ->
						    roles += role
						    if (idx < person.authorities.size() - 1) {
							    roles += ", "
						    }
					    }
					%>
					
					<td>${roles}</td>
				</tr>
			</g:each>
			</tbody>
			</table>
		</div>

	</div>
</body>
