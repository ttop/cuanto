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

<%@ page import="cuanto.TestType" %>
<div>
	<label for="name">Test Name:</label><br/>
	<g:textField id="name" name="testName" size="50" value="${testCase?.testName?.encodeAsHTML()}"/><br/><br/>
</div>
<div>
	<label for="pkg">Test Package:</label><br/>
	<g:textField id="pkg" name="packageName" size="50" value="${testCase?.packageName?.encodeAsHTML()}"/><br/><br/>
</div>
<div>
	<label for="desc">Description:</label><br/>
	<g:textArea id="desc" name="description" rows="10" cols="80" value="${testCase?.description?.encodeAsHTML()}"/><br/><br/>
</div>
<br/>
<g:hiddenField id="proj" name="project" value="${project?.id}"/>
<g:if test="${testCase}">
	<g:hiddenField id="testCase" name="id" value="${testCase?.id}"/>
</g:if>

