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

<g:set var="isManualTestCase" value="${testOutcome?.testCase?.testType.name.equalsIgnoreCase('Manual')}"></g:set>
<div class="hd">${testOutcome?.testCase?.testType?.name} Test Result <span id="headerOutcomeId"></span> for Test Run <span id="testRun">${testOutcome?.testRun?.dateExecuted}</span></div>
<div class="bd">
	<g:form controller="testOutcome" action="saveDetails">
		<g:hiddenField id="outcomeId" name="id" value="${testOutcome?.id}"/>

		<label for="testName">Test Case :</label><div id="testName" class="outcomeElement">${testOutcome?.testCase?.fullName}</div>

		<g:if test="${isManualTestCase}">
			<label for="descr">Description:</label>
			<div id="descr" class="outcomeElement">${testOutcome?.testCase?.description}</div>
		</g:if>

		<label for="testResult">Result:</label>

		<g:select name="testResult" optionKey="name" optionValue="name"
			from="${cuanto.TestResult.listOrderByName()}" value="${testOutcome?.testResult?.name}"></g:select>

		<g:if test="${!isManualTestCase}">
			<label for="duration">Duration:</label>
			<p id="duration">${testOutcome?.duration}</p>
		</g:if>
		<div class="clear"></div>
		<label for="reason.id">Failure Reason:</label>

		<g:select name="analysisState" optionKey="id" optionValue="name"
			from="${cuanto.AnalysisState.listOrderByName()}" noSelection="['':'-None-']" value="${testOutcome?.analysisState?.id}"/>
		<div class="clear"></div>

		<g:hiddenField id="bugId" name="bugId" value="${testOutcome?.bug?.id}"/>

		<label for="bugTitle">Bug:</label><input id="bugTitle" type="textbox" name="bugTitle"
		value="${testOutcome?.bug?.title}"/>
		<div class="clear"></div>

		<label for="bugUrl">Bug URL:</label>
		<input id="bugUrl" type="textbox" name="bugUrl" style="width:250px;" value="${testOutcome?.bug?.url}"/>
		<div class="clear"></div>

		<label for="owner">Owner:</label><input id="owner" type="textbox" name="owner" value="${testOutcome?.owner}"/>
		<div class="clear"></div>

		<label for="note">Note:</label><textarea id="note" name="note">${testOutcome?.note}</textarea>
		<div class="clear"></div>
	</g:form>
	<g:if test="${!isManualTestCase}">
		<label for="output">Test Output:</label>
		<g:textArea name="output" value="${testOutcome?.testOutput}" rows="25" cols="60" style="height:150px;" readonly="true"></g:textArea>
	</g:if>
	<div class="clear"></div>
	<a href="${createLink(controller: 'testCase', action: 'history', id: testOutcome?.testCase?.id)}"
		class="outcomeElement" id="showHistoryLink">Show History</a>
</div>
