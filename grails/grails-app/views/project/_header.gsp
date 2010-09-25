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
<span class="heading">
	<span id="projectHeading">
		<span class="text">
			for
			<g:if test="${project?.projectGroup}">
				<g:link controller="project" action="listGroup"
					params="['group': project?.projectGroup]">${project?.projectGroup?.name?.encodeAsHTML()}</g:link>/</g:if><g:link controller="project" action="history" params="[projectKey: project.projectKey]">${project?.name?.encodeAsHTML()}</g:link></span>
		<g:link controller="project" action="feed" id="${project?.id}">
			<g:set var="feedTxt" value="RSS feed"/>
			<img id="feedImg" src="${resource(dir: 'images/feedicons-standard', file: 'feed-icon-14x14.png')}"
				alt="RSS Feed" title="Subscribe to the RSS feed for ${project?.toString()?.encodeAsHTML()}"/></g:link>
	</span>
</span>
