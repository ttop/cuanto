<%@ page import="cuanto.User" %><head>
	<meta name="layout" content="main" />
	<title>User List</title>
    <p:css name='../js/yui/2.6.0/datatable/assets/skins/sam/datatable'/>
    <yui:javascript dir="datasource" file="datasource-min.js" version="2.6.0"/>
    <yui:javascript dir="datatable" file="cuanto-datatable-min.js" version="2.6.0"/>
    <g:javascript src="cuanto/users.js"/>

	<script type="text/javascript">
	YAHOO.util.Event.onDOMReady(function () {
		var users = new YAHOO.cuanto.users();
		
	});


	</script>
</head>

<body class="yui-skin-sam">

	%{--
	<div class="nav">
		<span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
		<span class="menuButton"><g:link class="create" action="create">New User</g:link></span>
	</div>
	--}%

	<div class="body ">

		<h1>User List</h1>
		<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
		</g:if>
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
					
					%{--
					<td class="actionButtons">
						<span class="actionButton">
							<g:link action="show" id="${person.id}">Show</g:link>
						</span>
					</td>
					--}%
				</tr>
			</g:each>
			</tbody>
			</table>
		</div>

	</div>
</body>
