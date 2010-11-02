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

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
	<%
		def grp
		if (projects) {
			grp = projects[0].projectGroup
		}
	%>
  <head>
	<title>Cuanto: Projects: ${grp?.name?.encodeAsHTML()}</title>

	<meta name="layout" content="mainBare"/>
	<g:render template="/shared/yui282r1"/>

	<p:css name='../js/yui/2.8.2r1/button/assets/skins/sam/button'/>
	<p:css name='../js/yui/2.8.2r1/autocomplete/assets/skins/sam/autocomplete'/>
	<p:css name='../js/yui/2.8.2r1/datatable/assets/skins/sam/datatable'/>
	<p:css name='../js/yui/2.8.2r1/paginator/assets/skins/sam/paginator'/>

	<yui:javascript dir="button" file="button-min.js" version="2.8.2r1"/>

	<yui:javascript dir="datasource" file="datasource-min.js" version="2.8.2r1"/>
	<yui:javascript dir="datatable" file="datatable-min.js" version="2.8.2r1"/>
	<yui:javascript dir="datatable" file="cuanto-datatable-overrides.js" version="2.8.2r1"/>

	<yui:javascript dir="animation" file="animation-min.js" version="2.8.2r1"/>
	<yui:javascript dir="json" file="json-min.js" version="2.8.2r1"/>
	<yui:javascript dir="autocomplete" file="autocomplete-min.js" version="2.8.2r1"/>

	<p:javascript src="jq/jquery-1.4.2.min"/>
	<p:javascript src="cuanto/url"/>
	<p:javascript src="cuanto/projectDialog"/>
	<p:javascript src="cuanto/deleteProjectDialog"/>
	<p:javascript src="cuanto/events"/>
	<p:javascript src="cuanto/timeParser"/>
	<p:javascript src="cuanto/listGroup"/>
	<p:javascript src="cuanto/groupHistory"/>

	<script type="text/javascript">

	YAHOO.util.Event.onDOMReady(function () {
		<g:render template="/testRun/urls"/>
		YAHOO.cuanto.groupHistory.initGroupHistoryTable();
		new YAHOO.cuanto.ListGroup();
	});

	</script>

  </head>
  <body >
	  <div class=" body yui-skin-sam">
		  <h1>Project Group: ${group}</h1>
		  <input id="groupId" type="hidden" value="${grp?.id}"/>
		  <div id="listGroupTableDiv">
			  <g:render template="listGroupTable" model="['projects': projects]"/>
		  </div>
		  <br/>
		  <div class=" yui-skin-sam">
			  <h1>Most Recent Test Run<g:if test="${projects.size() > 1}">s</g:if></h1>

			  Select a test run to view the analysis:
			  <div id="testRunList">
				  <div id="testRunTableDiv"></div>
				  <div id="trTablePaging"></div>
			  </div>
			  <p/><br/>
			  <g:if test="${chartUrl}">
				  <div id="lineChart">
					  <img src="${chartUrl}" alt="Success Rate Trend Chart" class="graph"/>
				  </div>
			  </g:if>
		  </div>

	  </div>
	  <g:render template="projectDialog"/>
	  <g:render template="deleteProjectDialog"/>
  </body>
</html>
