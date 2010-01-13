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

<%@ page import="cuanto.Defaults; java.text.SimpleDateFormat; cuanto.TestRun" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Create Manual Test Run</title>
</head>
<body>
<div class="body">
	<h1>Create Manual Test Run</h1>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<g:form class="wideForm" method="post">
		<input type="hidden" name="id" value="${project?.id}"/>
		<div class="dialog">
			<div>
				<label>Project:</label>
				<span>${project?.toString().encodeAsHTML()}</span>
			</div>

			<div>
				<label for="userDateExecuted">Date Executed:</label>
				<%
					def dateFormatter = new SimpleDateFormat(Defaults.dateFormat)
					def displayDate = new Date()
					String myDate = dateFormatter.format(displayDate)
				%>
				<input type="text" id="userDateExecuted" name="userDateExecuted" value="${myDate}"/>
			</div>

			<div>
				<label for="note">Note:</label>
				<input type="text" id="note" name="note"/>
			</div>

			<div>
				<label for="valid">Is this Test Run Valid?</label>
				<g:checkBox id="valid" name="valid" value="true"></g:checkBox>
			</div>

		</div>
		<div class="buttons">
			<g:actionSubmit class="save" value="Save" action="manual"/>
		</div>
	</g:form>
</div>
</body>
</html>
