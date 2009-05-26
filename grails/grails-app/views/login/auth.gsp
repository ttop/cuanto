%{--

 Copyright (c) 2009 Todd Wells

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

<head>
	<meta name='layout' content='main'/>
	<title>Cuanto: Login</title>
	<p:css name='login'/>
	<p:css name='../js/yui/2.6.0/button/assets/skins/sam/button'/>
	<yui:javascript dir="button" file="button-min.js" version="2.6.0"/>
	<g:javascript>
		YAHOO.util.Event.onDOMReady(function () {
			var oButton = new YAHOO.widget.Button("loginButton");
			$('j_username').focus();
		});
	</g:javascript>

	<style type='text/css' media='screen'>
	</style>
</head>

<body>
	<div id='login'>
		<div class='inner'>
			<form action='${postUrl}' method='POST' id='loginForm' class='cssform'>
				<p>
					<label for='j_username'>Login ID</label>
					<input type='text' class='text_' name='j_username' id='j_username' value='${request.remoteUser}'/>
				</p>
				<p>
					<label for='j_password'>Password</label>
					<input type='password' class='text_' name='j_password' id='j_password'/>
				</p>
				<p>
					<label for='remember_me'>Remember me</label>
					<input type='checkbox' class='chk' name='_spring_security_remember_me' id='remember_me'
						<g:if test='${hasCookie}'>checked='checked'</g:if>/>
				</p>
				<p>
					<input id="loginButton" type='submit' value='Login'/>
				</p>
			</form>
		</div>
	</div>
</body>
