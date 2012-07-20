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
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head>
	  <meta name="layout" content="main"/>
	  <title>Cuanto: Export Test Run ${testRun?.dateExecuted?.encodeAsHTML()} of Project ${testRun?.project?.name?.encodeAsHTML()}</title>
  </head>
  <body class=" yui-skin-sam">
	  <div class="body">
		  <div class="headone">Export Test Run ${testRun?.dateExecuted?.encodeAsHTML()} of Project ${testRun?.project?.name?.encodeAsHTML()}</div>
		  <ul>
			  <li><g:link controller="testRun" action="csv" id="TestRun_${testRun?.id}.csv">Comma Separated (CSV)</g:link></li>
			  <li><g:link controller="testRun" action="tab" id="TestRun_${testRun?.id}.tsv">Tab Separated (TSV)</g:link></li>
			  <li><g:link controller="testRun" action="xml" id="TestRun_${testRun?.id}.xml">XML</g:link></li>
              <li><g:link controller="testRun" action="testNgBuckets" id="TestRun_${testRun?.id}.xml">Bucketed TestNG Suite</g:link></li>
		  </ul>
		  <p/>
		  <br/>
		  <g:link controller="testRun" action="results" id="${testRun?.id}">Return to analysis</g:link>
	  </div>
  </body>
</html>