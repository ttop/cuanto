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
<span class="head1">Projects</span>
<span class="smaller">(<a href="#addProject" id="addProject">Add Project</a>)</span>
<h2>By Group</h2>
<div class="accordionMenu">
	<g:each var="group" in="${groups.findAll { it }}">
		<h3 class="toggler"><g:link controller="project" action="listGroup" params="['group': group]">${group}</g:link></h3>
		<div class="hidden accordion">
			<table>
				<tr>
					<td class="tdTitle" width="70%">Name</td>
					<td class="tdTitle" width="30%">Operations</td>
				</tr>
				<g:set var="projectsInThisGroup" value="${projects.findAll { it.projectGroup?.name == group}}"/>
				<g:each var="proj" in="${projectsInThisGroup}">
					<g:render template="projectRow" model="['proj': proj]"/>
				</g:each>
			</table>
		</div>
	</g:each>
</div>

<g:set var="projectsWithoutGroup" value="${projects.findAll { !it.projectGroup }}"/>
<g:if test="${projectsWithoutGroup.size() > 0}">
	<div class="toggler"><h2>Ungrouped</h2></div>
	<div class="accordionMenu">
		<div class="hidden accordion">
			<table>
				<tr>
					<td class="tdTitle" width="70%">Name</td>
					<td class="tdTitle" width="30%">Operations</td>
				</tr>
				<g:each var="proj" in="${projectsWithoutGroup}">
					<g:render template="projectRow" model="['proj': proj]"/>
				</g:each>
			</table>
		</div>
	</div>
</g:if>