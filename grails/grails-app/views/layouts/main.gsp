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

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
	"http://www.w3.org/TR/html4/loose.dtd">

<html>
	<head>
		<title><g:layoutTitle default="Cuanto"/></title>
		<p:css name='cuanto'/>
		<p:css name='nav'/>
		<p:css name='../js/yui/2.6.0/fonts/fonts-min'/>
		<p:css name='../js/yui/2.6.0/container/assets/skins/sam/container'/>

		<link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon"/>

		<yui:javascript dir="yahoo" file="yahoo-min.js" version="2.6.0"/>
		<yui:javascript dir="event" file="event-min.js" version="2.6.0"/>
		<yui:javascript dir="yahoo-dom-event" file="yahoo-dom-event.js" version="2.6.0"/>
		<yui:javascript dir="connection" file="connection-min.js" version="2.6.0"/>
		<yui:javascript dir="dragdrop" file="dragdrop-min.js" version="2.6.0"/>
		<yui:javascript dir="container" file="container-min.js" version="2.6.0"/>
		<yui:javascript dir="element" file="element-beta-min.js" version="2.6.0"/>
		<g:javascript library="prototype"/>

		<g:layoutHead/>

	</head>
	<body class=" yui-skin-sam">
		<div id="nav" class="nav">
			<ul>
				<li id="clogo"><g:link controller="project" action="mason"><img src="${resource(dir: 'images/logos', file:'cuanto-logo-16.gif')}" alt="Cuanto logo"/>
Cuanto</g:link></li>
				<li class="first navitem">
					<g:link controller="project" action="mason">Projects</g:link>
				</li>
				<li class="last navitem">
					<a href="${resource(file: 'help')}">Help</a>
				</li>
			</ul>
		</div>
		<% def flashStyle = "" %>
		<g:if test="${!flash.message}">
			<% flashStyle = "display:none" %>
		</g:if>

		<div id="flashMsg" class="message" style="${flashStyle}">${flash.message}</div>
		<br/>
		<g:layoutBody/>
	</body>
</html>
