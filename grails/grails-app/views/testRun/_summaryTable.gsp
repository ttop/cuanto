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

<div id="summaryTable">
	<table class="cuanto ">
		<tr>
			<th>Total Tests</th>
			<th>Passed</th>
			<th>Failed</th>
			<th>Skipped</th>
            <th>Quarantined</th>
			<th>Success</th>
			<th>Duration</th>
			<th>Avg Duration</th>
		</tr>
		<tr>
			<td>${stats?.tests?.encodeAsHTML()}</td>
			<td>${stats?.passed?.encodeAsHTML()}</td>
			<td>${stats?.failed?.encodeAsHTML()}</td>
			<td>${stats?.skipped?.encodeAsHTML()}</td>
            <td>${stats?.quarantined?.encodeAsHTML()}</td>
			<td>${stats?.successRate?.encodeAsHTML()} %</td>
			<td><g:formatDuration ms="${stats?.totalDuration}"/></td>
			<td><g:formatDuration ms="${stats?.averageDuration}"/></td>
			<td><span class="progress"></span></td>
		</tr>
	</table>

</div>