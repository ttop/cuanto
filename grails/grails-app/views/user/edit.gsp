<head>
	<meta name="layout" content="main"/>
	<title>Cuanto: Edit User</title>

	<p:css name='../js/yui/2.6.0/button/assets/skins/sam/button'/>

	<yui:javascript dir="button" file="button-min.js" version="2.6.0"/>
	<g:javascript src="cuanto/users.js"/>
	<g:javascript>
		YAHOO.util.Event.onDOMReady(function () {
			var users = new YAHOO.cuanto.users();
			users.initDelete();
			var oButton = new YAHOO.widget.Button("updateButton");
		});
	</g:javascript>
</head>

<body>
	<div class="cuantoBody">

		<span class="smaller">
			<a href id="deleteUser">Delete User</a> |
		<g:link action="create">Add User</g:link> |
		<g:link action="list">User List</g:link> |
		</span>

		<h1>Edit User</h1>
		<g:hasErrors bean="${person}">
			<div class="errors">
				<g:renderErrors bean="${person}" as="list"/>
			</div>
		</g:hasErrors>

		<div>
			<span>ID:</span>
			<span>${person.id}</span>
		</div>

		<g:form>
			<input type="hidden" name="id" value="${person.id}"/>
			<input type="hidden" name="version" value="${person.version}"/>
			<div>
				<table class="usertable">
					<tbody>

						<tr>
							<td><label for="username" class="widerLabel">Login Name:</label></td>
							<td class="${hasErrors(bean: person, field: 'username', 'errors')}">
								<input type="text" id="username" name="username" value="${person.username?.encodeAsHTML()}"/>
							</td>
						</tr>

						<tr>
							<td><label for="userRealName" class="widerLabel">Full Name:</label></td>
							<td class="${hasErrors(bean: person, field: 'userRealName', 'errors')}">
								<input type="text" id="userRealName" name="userRealName" value="${person.userRealName?.encodeAsHTML()}"/>
							</td>
						</tr>

						<tr>
							<td><label for="passwd" class="widerLabel">Password:</label></td>
							<td class="${hasErrors(bean: person, field: 'passwd', 'errors')}">
								<input type="password" id="passwd" name="passwd" value="${person.passwd?.encodeAsHTML()}"/>
							</td>
						</tr>

						<tr>
							<td><label for="enabled" class="widerLabel">Enabled:</label></td>
							<td class="${hasErrors(bean: person, field: 'enabled', 'errors')}">
								<g:checkBox name="enabled" value="${person.enabled}"/>
							</td>
						</tr>

						<tr>
							<td><label for="description" class="widerLabel">Description:</label></td>
							<td class="${hasErrors(bean: person, field: 'description', 'errors')}">
								<input type="text" id="description" name="description" value="${person.description?.encodeAsHTML()}"/>
							</td>
						</tr>

						<tr>
							<td><label for="email" class="widerLabel">Email:</label></td>
							<td class="${hasErrors(bean: person, field: 'email', 'errors')}">
								<input type="text" id="email" name="email" value="${person?.email?.encodeAsHTML()}"/>
							</td>
						</tr>

						<tr>
							<td><label for="emailShow" class="widerLabel">Show Email:</label></td>
							<td class="${hasErrors(bean: person, field: 'emailShow', 'errors')}">
								<g:checkBox name="emailShow" value="${person.emailShow}"/>
							</td>
						</tr>

						<tr>
							<td><label for="authorities" class="widerLabel">Roles:</label></td>
							<td class="${hasErrors(bean: person, field: 'authorities', 'errors')}">
								<g:each var="entry" in="${roleMap}">
									<g:checkBox name="${entry.key.authority}" value="${entry.value}"/>
									${entry.key.authority.encodeAsHTML()}<br/>
								</g:each>
							</td>
						</tr>

					</tbody>
				</table>
			</div>

			<div>
				<span><g:actionSubmit id="updateButton" class="" value="Update"/></span>
			</div>
		</g:form>
		<form id="deleteForm" action="${createLink(action: 'delete')}" method="post">
			<input type="hidden" name="id" value="${person.id}"/>
		</form>
	</div>
</body>
