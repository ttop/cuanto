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
		<li><a href="#groupedOutput"><em>Grouped Output</em></a></li>
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
							<g:select id="searchTerm" name="search" from="${['Name', 'Note', 'Output', 'Owner', 'Properties']}"/>
							<span class="propSearch" style="display:none">Name: <g:textField id="propName" name='propName'/></span>
							<span class="propSearch" style="display:none">Value: </span><g:textField id="searchQry" name="qry"/>
							<g:submitButton id="searchSubmit" name="submit" value="Search"/>
						</form>
					</span>
					<br/><span id="currentSearchSpan">Current Search: <span id="currentSearch" class="highlighted"></span></span>
					<br/>
                    <g:render template="tags"/>
					<a id="showSelectCol" class="selectCmd" alt="Show Select Column" href="#showSelect">Select</a>
					<g:render template="/shared/selectOptions"/>
					<a id="chooseColumns" class="selectCmd" alt="Choose Columns" href="#chooseColumns">Choose Columns</a>
				<br/>
				</div>
				<div id='trDetailsTable'>Loading...</div>
				<div id="trDetailsPaging"></div>
				<span id="trTotalRows">0</span> total rows
			</div>

		</div>

        <div id="outputTab">
	        <span>Filter Results: <g:select id="trOutputFilter" value="${outputFilter}" optionKey="id" optionValue="value"
		        from="${outputFilterList}"/> </span>
            <div id='trOutputTable'>Loading...</div>
            <div id='trOutputPaging'></div>
	        <span id="grpOutputTotalRows">0</span> total rows
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