<head>
	<meta name="layout" content="main" />
	<title>Cuanto: Show Role</title>
</head>

<body>


	<div class="cuantoBody">
		<span class="smaller">
			<span><g:link class="list" action="list">Role List</g:link></span>
			<span><g:link class="create" action="create">New Role</g:link></span>
		</span>
		<h1>Show Role</h1>
		<div>
			<table>
			<tbody>

				<tr>
					<td>ID:</td>
					<td>${authority.id}</td>
				</tr>

				<tr>
					<td>Role Name:</td>
					<td>${authority.authority}</td>
				</tr>

				<tr>
					<td>Description:</td>
					<td>${authority.description}</td>
				</tr>

				<tr>
					<td>People:</td>
					<td>
						<g:each in="${authority.people}" var="person">
							${person.username}<br/>
						</g:each>
					</td>
				</tr>

			</tbody>
			</table>
		</div>

		<div class="buttons">
			<g:form>
				<input type="hidden" name="id" value="${authority?.id}" />
				<span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
				<span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
			</g:form>
		</div>

	</div>

</body>
