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

<div id="bugSummary">
	<g:if test="${bugSummary}">
		<h1>Bug Summary <span class="progress"></span></h1>
		<table class="cuanto ">
			<tr><th>Bug</th><th>Total Occurrences</th></tr>
			<g:each in="${bugSummary}">
				<tr>
					<g:if test="${it.bug?.url && it.bug?.title}">
						<td><a href="${it.bug?.url}">${it.bug?.title}</a></td>
					</g:if>
					<g:elseif test="${it.bug?.url}">
						<td><a href="${it.bug?.url}">${it.bug?.url}</a></td>
					</g:elseif>
					<g:else>
						<td>${it.bug?.title}</td>
					</g:else>
					<td>${it.total}</td>
				</tr>
			</g:each>
		</table>
	</g:if>
</div>
