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

<div id="deleteProjectDialog" class=" yui-skin-sam">
	<div class="hd">Delete Project</div>
	<div class="bd">
	<form method="POST" action="${createLink(controller:'project', action:'delete')}">
		<input id="dpdProjectId" type="hidden" name="id"/>
		<span class="head1">Are you sure you want to delete the project <b><span id="dpdProjectName"><g:if test="${project}">${project.name}</g:if></span></b>?</span><br/>
			This will permanently delete the project and any saved test results associated with
			the project.  If you are sure you want to delete this project, type YES in the box below
			and click Delete.<br/>
		<input id="dpdConfirmDelete" type="text" name="confirmDelete" style="width:40px"/><br/>
	</form>
		<div id="dpdMessage"></div>
	</div>
</div>
