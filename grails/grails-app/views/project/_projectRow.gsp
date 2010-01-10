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

<tr class="projRow">
	<td class="projName">
		<g:link controller="project" action="history" params="[projectKey: proj.projectKey]"><span class="pName" id="pName${proj.id}">${proj.name}</span></g:link>
	</td>
	<td><a id="edit${proj.id}" class="editProj" href="#editProj">Edit</a> ${bullet}
		<a id="delete${proj.id}" class="deleteProj" href="#deleteProj">Delete</a> ${bullet}
	<g:link controller="project" action="history" params="[projectKey: proj.projectKey]">Show</g:link>
	</td>
</tr>
