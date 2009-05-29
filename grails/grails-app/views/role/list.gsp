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

<%@ page import="cuanto.Role" %><head>
	<meta name="layout" content="main" />
	<title>Cuanto: Role List</title>
	<p:css name='../js/yui/2.6.0/datatable/assets/skins/sam/datatable'/>
	<yui:javascript dir="datasource" file="datasource-min.js" version="2.6.0"/>
	<yui:javascript dir="datatable" file="cuanto-datatable-min.js" version="2.6.0"/>
	<g:javascript src="cuanto/roles.js"/>

	<g:javascript>
		YAHOO.util.Event.onDOMReady(function () {
			var roles = new YAHOO.cuanto.roles();
			roles.initRoleList();
		});
	</g:javascript>

</head>

<body>
	<div class="cuantoBody yui-skin-sam ">
		<span class="smaller"><g:link class="create" action="create">New Role</g:link></span>
		<h1>Role List</h1>
		<p/>
		<div id="rolelistdiv">
			<table id="rolelist">
			<thead>
				<tr>
					<th>ID</th>
					<th>Role Name</th>
					<th>Description</th>
				</tr>
			</thead>
			<tbody>
			<g:each in="${authorityList}" var="authority">
				<tr>
					<td>${authority.id}</td>
					<td><g:link action="show" id="${authority.id}">${authority.authority?.encodeAsHTML()}</g:link></td>
					<td>${authority.description?.encodeAsHTML()}</td>
				</tr>
			</g:each>
			</tbody>
			</table>
		</div>
	</div>
</body>
