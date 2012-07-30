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
<%@ page import="cuanto.TestType" %>

<div id="projectDialog" class=" yui-skin-sam">
	<div id="pdHd" class="hd"></div>
	<div id="pdBd" class="bd">
		<div>
			<form id="pdForm" name="projectDialog" method="POST" action="${createLink(controller: 'project', action: 'save')}">
				<g:hiddenField id="pdProjectId" name="project" value=""/>
				<input type="hidden" name="format" value="json"/>
				<div class="projectLine">
					<label class="wideLabel" for="pdName">Project Name:</label>
					<input id="pdName" class="pdInput" type="text" name="name" value="${project?.name}"/>
					<br/>
				</div>
				<div class="projectLine">
					<label class="wideLabel" for="pdGroup">Group:</label>
					<%
						def grpName = ""
						if (project?.projectGroup) {
							grpName = project?.projectGroup?.name
						}
					%>
					<input id="pdGroup" class="pdInput" type="text" name="group" value="${grpName}"/>
					<div id="pdAutoComplete"></div>
					<br/>
				</div>
				<div class="projectLine">
					<label class="wideLabel" for="pdProjectKey">Project Key:</label>
					<input id="pdProjectKey" class="pdInput" type="text" name="projectKey" maxlength="25" value="${project?.projectKey}"/>
					<br/>
				</div>
				<div class="projectLine">
					<label class="wideLabel" for="pdUrlPattern">Bug URL Pattern:<br/>(e.g. http://url/{BUG})</label>
					<input id="pdUrlPattern" type="text" name="bugUrlPattern" value="${project?.bugUrlPattern}"/>
					<br/>
				</div>
				<div class="projectLine">
					<%
						def testTypes = TestType.listOrderByName()
						def currentTestType = project?.testType ?: TestType.findByNameIlike("JUnit")
					%>
					<label class="wideLabel" for="pdType">Test Type:</label>
					<g:select id="pdType" class="pdInput" from="${testTypes}" optionKey="name" optionValue="name" name="testType"
						value="${currentTestType?.name}" />
					<br/>
					<label class="wideLabel" for="pdPurge">Purge Runs After:</label>
					<input id="pdPurge" class="pdInput" type="text" name="purgeDays" value="${project?.purgeDays}"/>  Days
				</div>			
			</form>
			<div id="pdLoading" style="display:none"><img src="${resource(dir: 'images/progress', file: 'mozilla_blu.gif')}"
				alt="Loading icon">Loading project info from server...</div>
			<div id="pdErrorDiv" style="display:none"><img src="${resource(dir: 'js/yui/2.8.2r1/container/assets', file:'warn16_1.gif')}" 
				alt="Warning icon"/> <span id="pdError"></span></div>
		</div>
	</div>
</div>