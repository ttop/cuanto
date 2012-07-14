%{--

 Copyright (c) 2010 thePlatform, Inc.

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

<div id="tagSummaryDiv">
    <p></p>
    <g:if test="${stats?.tagStatistics}">

        <h1>Tag Summary</h1>

        <table class="borderedTable">
            <thead>
            <tr>
                <th>Tag</th>
                <th>Passed</th>
                <th>Failed</th>
                <th>Skipped</th>
                <th>Total</th>
                <th>Duration</th>
            </tr>
            </thead>
            <tbody>
            <g:each var="tagstat" in="${stats?.tagStatistics}">
                <tr class="brd">
                    <td align="center">${tagstat?.tag?.name}</td>
                    <td align="center">${tagstat?.passed}</td>
                    <td align="center">${tagstat?.failed}</td>
                    <td align="center">${tagstat?.skipped}</td>
                    <td align="center">${tagstat?.total}</td>
                    <td align="center"><g:formatDuration ms="${tagstat?.duration}" /></td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </g:if>
</div>