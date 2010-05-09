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

<g:set var="groupsToProjectsMap" value="${[:]}" />

<g:set var="ungroupedProjects" value="${projects.findAll {!it.projectGroup}}" />

<div class="clear"></div>
<div id="accordionMenu" class="accordionMenu">
  <g:each var="group" in="${groups}">
    %{-- togglers are inactive by default --}%
	<h3 class="inactiveToggler tog"><g:link controller="project" action="listGroup" params="['group': group]">${group}</g:link></h3>
    <% groupsToProjectsMap[group] = projects.findAll { it.projectGroup?.name == group } %>
  </g:each>

  <g:if test="${ungroupedProjects}">
    <h3 class="inactiveToggler tog">Ungrouped</h3>
    <% groupsToProjectsMap["Ungrouped"] = ungroupedProjects %>
  </g:if>

</div>

<div id="rightColProjects" style="display:none;" class="round1">
	<div id="projColInner">
		<g:each var="e" in="${groupsToProjectsMap}">
			<div class="accordion">
				<div>
					<table>
						<tr>
							<td class="tdTitle" width="70%">Name</td>
							<td class="tdTitle" width="30%">Operations</td>
						</tr>
						<g:each var="proj" in="${e.value}">
							<g:render template="projectRow" model="['proj': proj]"/>
						</g:each>
					</table>
				</div>
			</div>
          <div class="clear"></div>
		</g:each>
	</div>
</div>