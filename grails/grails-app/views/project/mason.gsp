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
<g:set var="bullet" value="${grailsApplication.config.bullet}"/>

<html>
	<head>
		<p:css name='mason'/>
		<meta name="layout" content="mainBare"/>

		<g:render template="/shared/yui282r1"/>
		<p:css name='../js/yui/2.8.2r1/button/assets/skins/sam/button'/>
		<p:css name='../js/yui/2.8.2r1/autocomplete/assets/skins/sam/autocomplete'/>

		<yui:javascript dir="button" file="button-min.js" version="2.8.2r1"/>
		<yui:javascript dir="datasource" file="datasource-min.js" version="2.8.2r1"/>
		<yui:javascript dir="animation" file="animation-min.js" version="2.8.2r1"/>
		<yui:javascript dir="json" file="json-min.js" version="2.8.2r1"/>
		<yui:javascript dir="autocomplete" file="autocomplete-min.js" version="2.8.2r1"/>
		<yui:javascript dir="cookie" file="cookie-min.js" version="2.8.2r1"/>

		<p:javascript src="jq/jquery-1.4.2.min"/>
		<p:javascript src="jq/jquery.masonry.min"/>


		<p:javascript src="cuanto/url"/>
		<p:javascript src="cuanto/events"/>
		<p:javascript src="cuanto/projectDialog"/>
		<p:javascript src="cuanto/projectMason"/>
		<g:javascript>
			<g:render template="/testRun/urls"/>

			$(function() {
				var pm = new YAHOO.cuanto.ProjectMason();
				pm.init();
			});

		</g:javascript>
	</head>
	<body class=" yui-skin-sam">
		<div class=" ">
			<div id="projectBody">
				<span class="headone">Projects</span>
				<span class="smaller hdActions">
					<a id="expandAll" href="#expandAll">Expand All</a>
					${bullet}
					<a id="collapseAll" href="#collapseAll">Collapse All</a>
					${bullet}
					<a id="addProject" href="#addProject">Add Project</a>
				</span>
				<g:render template="mason"/>
				<g:render template="projectDialog"/>
			</div>
		</div>
	</body>

</html>
