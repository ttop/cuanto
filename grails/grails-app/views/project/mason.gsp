%{--

 Copyright (c) 2010 Todd Wells

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

<%@ page contentType="text/html;charset=UTF-8" %>

<html>
	<head>
		<p:css name='mason'/>
		<meta name="layout" content="mainBare"/>

		<g:render template="/shared/yui26"/>
		<g:javascript src="jq/jquery-1.4.2.min.js"/>
		<g:javascript src="jq/jquery.masonry.min.js"/>
		%{--<g:javascript src="cuanto/url.js"/>--}%
		<g:javascript src="cuanto/events.js"/>
		%{--<g:javascript src="cuanto/projectDialog.js"/>--}%
		%{--<g:javascript src="cuanto/deleteProjectDialog.js"/>--}%
		<g:javascript src="cuanto/projectMason.js"/>
		<g:javascript>
			$(function() {
				var pm = new YAHOO.cuanto.ProjectMason();
				pm.init();
			});

		</g:javascript>
	</head>
	<body class=" yui-skin-sam">
		<div class=" ">
			<div id="projectBody">
				<g:set var="groupsToProjectsMap" value="${[:]}"/>
				<g:set var="ungroupedProjects" value="${projects.findAll {!it.projectGroup}}"/>

				<g:each var="group" in="${groups}">
					<% groupsToProjectsMap[group] = projects.findAll { it.projectGroup?.name == group } %>
				</g:each>

				<g:if test="${ungroupedProjects}">
					<% groupsToProjectsMap["Ungrouped"] = ungroupedProjects %>
				</g:if>



				<div id="groups" class="wrap">
					<g:each var="group" in="${groupsToProjectsMap}">
						<div class="box pg">
							<div class="projGroup">${group.key}</div>

							<div>
								<g:each in="${group.value}" var="proj">
									<div class="proj">
										<g:link controller="project" action="history" params="[projectKey: proj.projectKey]"><span class="pName" id="pName${proj.id}">${proj.name}</span></g:link>
									</div>
								</g:each>
							</div>
						</div>
					</g:each>
				</div>
			</div>
		</div>
	</body>

</html>
