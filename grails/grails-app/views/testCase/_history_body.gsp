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

<div class="body yui-skin-sam">
	<div id="testCaseHeader">
		<span class="heading">History of Test Case ${testCase?.id?.encodeAsHTML()}:</span> <span id="tcHeading">${testCase?.fullName?.encodeAsHTML()}</span>
	</div>
	<br/>
	<%
		def defaultFilter
		if (filter == "allresults") {
			defaultFilter = "All Results"
		} else {
			defaultFilter = "All Failures"
		}
		def filterList = []
		filterList += [id: "allfailures", value: "All Failures"]
		filterList += [id: "allresults", value: "All Results"]
	%>
	<div>
		Filter Results: <g:select id="tcHistoryFilter" value="${defaultFilter}" optionKey="id" optionValue="value"
			from="${filterList}"/>
		<p/>
		<br/>
	</div>

	<div id='tcHistoryTable'></div>
	<div id="tcHistoryPaging"></div>
			<div id="outContainer">
			<div id="outPanel">
				<div id="outPanelBody" class="bd"></div>
				<div class="ft"></div>
			</div>
		</div>
</div>
