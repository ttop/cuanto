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

<div id="testRunHeader">
	<span class="headone">Test Run ${testRun?.dateExecuted?.encodeAsHTML()}</span>
	<span class="smaller">
		<span>(<g:link controller="testRun" action="results" id="${testRun?.id}">Permalink</g:link> ${bullet}
			<g:link action="edit" id="${testRun.id}">Edit</g:link>
		</span> ${bullet}
		<span><a id="deleteTestRun" href="#deleteTestRun">Delete</a></span>)
	</span>
	<br/>
	<g:render template="/project/header" model="[project:testRun.project]"/> ${bullet}
	<span class="heading">Test Run ID: </span><span class="text" id="trhId">${testRun?.id}</span>
	<br/>
	<span id="trhTestProps">
		<g:if test="${testRun?.testProperties}">
			<g:each in="${testRun?.testProperties}" var="testProp" status="idx">
				<span class="heading">${testProp.name}: </span><span class="text">${testProp.value}</span>
				<g:if test="${idx < testRun.testProperties.size() - 1}"> ${bullet} </g:if>
			</g:each>
		</g:if>
	</span>
	<br/>

	<g:if test="${testRun?.links}">
		<g:each in="${testRun?.links}" var="link" status="idx">
			<a href="${link.url}">${link.description}</a>
			<g:if test="${idx < testRun.links.size() - 1}"> &bull; </g:if>
		</g:each>
		<br/>
	</g:if>
	<span class="heading">Note </span>
	<a id="editNote" href="#editNote" class="smaller">(Edit)</a>
	<span id="noteOps" class="smaller" style="display:none">
		(<a id="cancelNote" href="#cancelNote" class="smaller">Cancel</a> ${bullet}
		<a id="saveNote" href="#saveNote" class="smaller">Save</a>)
	</span>:
	<span id="noteContainer" class="text"><span id="trhNote">${testRun?.note}</span></span>

	<% def validStyle = testRun?.valid ? "display:none" : "" %>
	<span id="trhIsInvalid" class="highlighted" style="${validStyle}"><br/>This test run has been marked invalid.</span>
	<input id="testRunId" type="hidden" value="${testRun?.id}"/>
</div>
<p/><br/>
