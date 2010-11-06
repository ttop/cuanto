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
<g:set var="bullet" value="${grailsApplication.config.bullet}"/>

<table id="listGroupTable" class="cuanto ">
	<g:if test="${projects}">
		<tr>
			<th>Name</th>
			<th>Operations</th>
		</tr>
		<g:each var="proj" in="${projects}">
			<tr>
				<td class="projName" id="pName${proj.id}">${proj.name?.encodeAsHTML()}</td>
				<td><a href="#editProject" class="editProj" id="edit${proj.id}">Edit</a> ${bullet}
					<a href="#deleteProject" class="deleteProj" id="delete${proj.id}">Delete</a> ${bullet}
					<g:link controller="project" action="history" params="[projectKey: proj.projectKey]">Show</g:link>
			</tr>
		</g:each>
	</g:if>
	<g:else>
		(No projects for this group)
	</g:else>
</table>