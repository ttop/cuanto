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

<head>
	<meta name="layout" content="main"/>
	<title>Cuanto: Show User</title>

	<p:css name='../js/yui/2.6.0/button/assets/skins/sam/button'/>
	<yui:javascript dir="button" file="button-min.js" version="2.6.0"/>
	<g:javascript src="cuanto/users.js"/>
	<g:javascript>
		YAHOO.util.Event.onDOMReady(function () {
			var users = new YAHOO.cuanto.users();
			users.initDelete();
		});

	</g:javascript>
</head>

<body class=" yui-skin-sam ">
	<div class="cuantoBody">
		<span class="smaller">
			<g:link id="${person.id}" action="edit">Edit User</g:link> |
			<a href="${createLink(action:'delete')}" id="deleteUser">Delete User</a> |
			<g:link action="create">Add User</g:link> |
			<g:link action="list">User List</g:link> |
		</span>

		<h1>Show User</h1>
		<div >
			<table class="usertable">
				<tbody>

					<tr>
						<td>ID:</td>
						<td>${person.id}</td>
					</tr>

					<tr>
						<td>Login Name:</td>
						<td>${person.username?.encodeAsHTML()}</td>
					</tr>

					<tr>
						<td>Full Name:</td>
						<td>${person.userRealName?.encodeAsHTML()}</td>
					</tr>

					<tr>
						<td>Enabled:</td>
						<td>${person.enabled}</td>
					</tr>

					<tr>
						<td>Email:</td>
						<td>${person.email?.encodeAsHTML()}</td>
					</tr>

					<tr>
						<td>Roles:</td>
						<td>
							<g:each in="${roleNames}" var='name'>
								${name}<br/>
							</g:each>
						</td>
					</tr>

				</tbody>
			</table>
			<form id="deleteForm" action="${createLink(action:'delete')}" method="post">
				<input type="hidden" name="id" value="${person.id}"/>
			</form>
		</div>
	</div>
</body>
