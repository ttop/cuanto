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

<div id="phHeader">
	<span class="head1">Test Run History for
		<g:if test="${project?.projectGroup}">
			<g:link controller="project" action="listGroup"
				params="['group': project?.projectGroup]">${project?.projectGroup?.name?.encodeAsHTML()}</g:link>/</g:if><g:link controller="project" action="history" params="[projectKey: project?.projectKey]">${project?.name?.encodeAsHTML()}</g:link>
		<g:link controller="project" action="feed" id="${project?.id}">
			<g:set var="feedTxt" value="RSS feed"/>
			<img id="feedImg" src="${resource(dir: 'images/feedicons-standard', file: 'feed-icon-14x14.png')}"
				alt="RSS Feed" title="Subscribe to the RSS feed for ${project?.toString()?.encodeAsHTML()}"/></g:link>
	</span>
	<span class="smaller hdActions">
		<a id="chooseColumns" class="selectCmd" alt="Choose Columns" href="#chooseColumns">Choose Columns</a> ${bullet}
		<span >
			<g:link controller="testCase" action="show" id="${project?.id}">Show Test Cases</g:link> ${bullet}
		    <g:link controller="testCase" action="rename" id="${project.projectKey}">Renaming Tool</g:link>
		
			<g:if test="${project?.testType?.name == 'Manual'}">${bullet}
				<g:link controller="testRun" action="createManual" id="${project?.id}">Create Manual Test Run</g:link>
			</g:if></span>
		${bullet}
		<a href="#bulk" id="bulk${project?.id}" class="bulk">Delete Runs</a>
		${bullet}
		<a href="#deleteProject" id="delete${project?.id}" class="deleteProj">Delete Project</a>
		${bullet}
		<a href="#editProject" id="edit${project?.id}" class="editProj">Edit Project</a>
	</span>
	<br/>
	<div class="propsAndLinks">
		<input type="hidden" name="projectId" id="projectId" value="${project?.id}"/>
		<span class="heading">Project Key:</span><span class="text">${project?.projectKey}</span>
	</div>
</div>
