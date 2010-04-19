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

<div id="tabContainer" class="yui-skin-sam yui-navset">
	<ul class="yui-nav">
		<li><a href="${createLink(action: 'results', 'id': testRun.id)}"><em>Analysis</em></a></li>
		<li><a href="${createLink(action: 'summary', 'id': testRun.id)}"><em>Summary</em></a></li>
	</ul>
	<div class="yui-content">
		<div id='resultsTab'>
			<div id="resultsTabContents">
				<br/>
				<div>
					<span>Filter Results: <g:select id="trDetailsFilter" value="${filter}" optionKey="id" optionValue="value"
						from="${filterList}"/> </span>
					<span id="nameFormat" class="formHeader">Name Format:
						<g:select id="tcFormat" from="${formatters}" optionKey="key" optionValue="description" name="tcFormat"
							value="${tcFormat}"/>
					</span>
					<span id="searchSpan" class="formHeader"> Search: 
						<form id="searchForm" name="searchForm">
							<g:select id="searchTerm" name="search" from="${['Name', 'Note', 'Output', 'Owner']}"/>
							<g:textField id="searchQry" name="qry"/>
							<g:submitButton id="searchSubmit" name="submit" value="Search"/>
						</form>
					</span>
					<br/><span id="currentSearchSpan">Current Search: <span id="currentSearch" class="highlighted"></span></span>
					<br/>
                    <g:render template="tags"/>
					<a id="showSelectCol" class="selectCmd" alt="Show Select Column" href="#showSelect">Select</a>
				    <span id="showSelectOptions" class="selectCmd">
					    <a id="hideSelectCol" alt="Hide Select Column" href="#hideSelect">Hide Select</a> ${bullet}
					    <a id="selectPage" alt="Select Page" href="#selectPage">Select Page</a> ${bullet}
					    <a id="deselectPage" alt="Deselect Page" href="#deselectPage">Deselect Page</a> ${bullet}
					    <a id="deselectAll" alt="Deselect All" href="#selectNone">Deselect All</a> ${bullet}
					    <span id="currentSelected" alt="Currently Selected">0 Tests Selected</span>
				    </span>
					<a id="chooseColumns" class="selectCmd" alt="Choose Columns" href="#chooseColumns">Choose Columns</a>
				<br/>
				</div>
				<div id='trDetailsTable'>Loading...</div>
				<div id="trDetailsPaging"></div>
			</div>

		</div>
		<div id="summaryTab">
			<g:render template="summaryTable"/>
			<p/><br/>
			<g:render template="pieChart"/>
			<g:render template="bugSummary"/>
            <g:render template="tagSummary"/>
		</div>
	</div>
</div>