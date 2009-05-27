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

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Cuanto: User Profile</title>

		<p:css name='../js/yui/2.6.0/button/assets/skins/sam/button'/>
		<yui:javascript dir="button" file="button-min.js" version="2.6.0"/>
		<g:javascript>
			YAHOO.util.Event.onDOMReady(function () {
				new YAHOO.widget.Button('updateProfile');
				new YAHOO.widget.Button('submitPassword');
			});
		</g:javascript>
	</head>
	<body class="yui-skin-sam ">
		<div class="cuantoBody">
			<h1>User Profile for ${user.username}</h1>

			<h2>Profile Details</h2>

			<div id="userProfileDialog">
				<form id="userprofile" class="userform" action="save" method="POST">
					<label for="userRealName">Real Name:</label>
					<input id="userRealName" name="userRealName" value="${user?.userRealName}"/><br/>
					<label for="email">Email:</label>
					<input id="email" name="email" value="${user?.email}"/><br/>
					<label for="updateProfile"></label>
					<input id="updateProfile" name="updateProfile" type="submit" value="Update Profile"/>
				</form>
			</div>
			<p/>
			<h2>Change Password</h2>
			<form id="userpassword" class="userform" action="changePassword" method="POST">
				<label for="passwd">New Password:</label>
				<input id="passwd" name="passwd" type="password"/><br/>
				<label for="confpassword">Confirm Password:</label>
				<input id="confpassword" name="confpassword" type="password"/><br/>
				<input id="submitPassword" name="submitPassword" type="submit" value="Change Password"/>
			</form>
		</div>
	</body>
</html>