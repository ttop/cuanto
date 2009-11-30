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
<div id="tableDiv" class="anlzElem">
	<span class="colHdr">Analysis for Test Run: <span id="testRun">${outcome?.testRun?.dateExecuted?.encodeAsHTML()}</span></span>
	<g:form name="anData" id="anForm">
		<table id="anTable">
			<col class="fieldSelect"/>
			<col class="fieldName"/>
			<tr><th colspan="2" class="smaller" id="fieldHdr">Include fields</th><th></th></tr>
			<tr><td class="fieldSelect"><g:checkBox name="srcfield" value="testResult" checked="false"/></td><td class="intable">Result:</td>
				<td id="testResult"/></tr>
			<tr><td class="fieldSelect"><g:checkBox name="srcfield" value="analysisState"/></td>
				<td class="intable">Reason:</td>
				<td id="analysisState"/></tr>
			<tr><td class="fieldSelect"><g:checkBox name="srcfield" value="bug"/></td><td class="intable">Bug:</td>
				<td>
					<a id="bug"/>
				</td>
			</tr>
			<tr><td class="fieldSelect"><g:checkBox name="srcfield" value="owner"/></td><td class="intable">Owner:</td>
				<td id="owner"/></tr>
			<tr><td class="fieldSelect"><g:checkBox name="srcfield" value="note"/></td>	<td class="intable">Note:</td>
				<td id="note"/></tr>
		</table>
		<button id="anApply" type="button">Apply to selected tests</button>
		<span id="applyStatus"></span>
	</g:form>
</div>

<div id="prevAnlz" class="anlzElem">
	<span class="colHdr">Analysis History for This Test<br/></span>
	<form id="prevAnlzForm">
		<select id="anlzHistory"></select>
	</form>
</div>
<br class="nofloat"/>

<br/>
<div id="outputParentDiv">
	Output for this analysis:<br/>
	<div id="anOutputDiv" class="testOutput"></div>
</div>
