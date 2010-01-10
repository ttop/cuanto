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

<%@ page import="cuanto.Defaults; java.text.SimpleDateFormat" %>
<div id="testRunDialog">
	<%
		def dateFormatter = new SimpleDateFormat(Defaults.dateFormat)
		def displayDate = new Date(testRun.dateExecuted?.getTime())
		String myDate = dateFormatter.format(displayDate)
	%>
    <div class="hd">Edit Test Run <span id="testRunName">${myDate.encodeAsHTML()}</span>
    (ID: <span id="trDialogId">${testRun?.id}</span>)</div>
    <div class="bd">
	    <g:set var="fieldSize" value="30"/>
        <form name="testRunForm" method="POST" action="${createLink(controller: 'testRun', action: 'update')}">
	        <input class="trInput" id="trdId" name="id" type="hidden" value="${testRun?.id}"/>
            <label for="trdNote" class="dialogLabel">Note:</label>
	    <input id="trdNote" class="trInput" type="text" name="note" value="${testRun?.note}" size="${fieldSize}"/><br/>
	        <label for="trdValid" class="dialogLabel">Test Run Valid: </label>
	    
	    <input id="trdValid" class="trInput" type="checkbox" name="valid" checked="${testRun?.valid}"/><br/>
        </form>
	    <div id="trdLoading"><img src="${resource(dir: 'images/progress', file: 'mozilla_blu.gif')}" 
		    alt="Loading icon">Refreshing test run info from server...</div>
    </div>
</div>
