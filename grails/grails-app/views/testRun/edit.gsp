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
		<meta name="layout" content="mainBare"/>

		<title>Cuanto: Edit Test Run ${testRun?.dateExecuted?.encodeAsHTML()} of Project ${testRun?.project?.name?.encodeAsHTML()}</title>

		<p:css name='../js/yui/2.8.2r1/button/assets/skins/sam/button'/>
		<g:render template="/shared/yui282r1"/>


		<yui:javascript dir="button" file="button-min.js" version="2.8.2r1"/>
		<p:javascript src="jq/jquery-1.4.2.min"/>
		<p:javascript src="cuanto/url"/>
		<p:javascript src="cuanto/editTestRun"/>

		<script type="text/javascript">
			YAHOO.util.Event.onDOMReady(function () {
				<g:render template="urls"/>
				new YAHOO.cuanto.EditTestRun(${testRun?.id});
			});
		</script>
	</head>
	<body class=" yui-skin-sam">
		<div id="trEditBody" class="body yui-skin-sam">
			<div>
				<span class="headone">Test Run ${testRun?.dateExecuted?.encodeAsHTML()}</span>
				<g:render template="/project/header" model="[project:testRun.project]"/> ${bullet}
				<span class="hdActions">
					<g:link action="results" id="${testRun?.id}" class="smaller">Analysis</g:link><br/>
				</span>
				<div class="propsAndLinks">
				<span class="heading">Test Run ID:</span><span class="text" id="trhId">${testRun?.id}</span>
				</div>
			</div>
			<div id="testRunForm">
				<g:form name="editTestRun" action="update" id="${testRun?.id}">
					<label class="narrowLabel">Test Run is valid? </label><g:checkBox name="valid" value="${testRun?.valid}"/>
					<div class="clear"></div>
					<br/>
					<label class="narrowLabel">Pin test run?</label><g:checkBox name="pinRun" value="${!testRun?.allowPurge}"/>
					<div class="clear"></div>
					<br/>

					<label class="narrowLabel">Note: </label><g:textField name="note" value="${testRun?.note}" size="81"/>
					<br/><br/>
					<span class="headone">Properties:</span><br/><br/>
					<g:each var="prop" in="${testRun?.testProperties}" status="i">
						<span class="property">
							<label class="narrowLabel">${prop.name}:</label>
							<g:hiddenField class="propName" name="propName[${i}]" value="${prop.name}"/>
							<g:hiddenField class="propId" name="propId[${i}]" value="${prop.id}"/>
							<g:textField name="prop[${i}]" value="${prop.value}"/>
							<a class="deleteProp" href="#deleteProperty">Delete</a>
						</span>
						<div class="clear"></div>
					</g:each>

					<span id="newProps"></span>
					<a id="addProperty" href="#addProperty">Add Property</a>
					<div class="clear"></div>
					<br/><br/>


					<span class="headone">Links:</span><br/><br/>
					<g:each var="link" in="${testRun?.links}" status="i">
						<div class="link">
							<g:hiddenField class="linkId" name="linkId[${i}]" value="${link.id}"/>
							<label class="narrowLabel">Link description: </label>
							<g:textField name="linkDescr[${i}]" class="linkDescr" value="${link.description}"/>
							<label class="nonClearLabel">URL: </label>
							<g:textField name="linkUrl[${i}]" class="linkUrl" value="${link.url}"/>
							<a class="deleteLink" href="#deleteLink">Delete</a>
						</div>
					</g:each>
					<span id="newLinks"></span>
					<a id="addLink" href="#addLink">Add Link</a>
					<div class="clear"></div>
					<br/><br/>

					<input id="submitBtn" type="submit" name="submit" value="Save"/>
				</g:form>
			</div>
		</div>

	</body>
</html>