%{--

    Copyright 2009 COPYRIGHT_HOLDER

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

--}%

Analysis for Test Run: <span id="testRun">${outcome?.testRun?.dateExecuted?.encodeAsHTML()}</span>
				(<span id="currentRecord"></span> of <span id="totalRecords"></span>)
<g:form name="anData">
	<table id="anTable">
		<col class="fieldSelect"/>
		<col class="fieldName"/>
		<tr><th colspan="3">Include fields</th><th></th></tr>
		<tr><td class="fieldSelect"><g:checkBox name="srcfield" value="testResult"/></td><td class="intable">Result:</td>
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
	<button id="olderAnlz" type="button">Older</button>
	<button id="newerAnlz" type="button">Newer</button>
	<span id="applyStatus"></span>
	
</g:form>
<div id="outputParentDiv">
	Output for this analysis:<br/>
	<div id="anOutputDiv" class="testOutput"></div>
</div>
