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
<div class="projList" style="display:none">
	<g:each in="${projects}" var="proj">
		<div class="proj">
			<span class="projId" style="display:none">${proj.id}</span>
			<span class="pName"><g:link controller="project" action="history" params="[projectKey: proj.projectKey]" class="projLink">${proj.name}</g:link></span>
		</div>
	</g:each>
</div>
