<head>
	<meta name='layout' content='main'/>
	<title>Cuanto: Login</title>
	<p:css name='login'/>
	<p:css name='../js/yui/2.6.0/button/assets/skins/sam/button'/>
	<yui:javascript dir="button" file="button-min.js" version="2.6.0"/>
	<g:javascript>
		YAHOO.util.Event.onDOMReady(function () {
			var oButton = new YAHOO.widget.Button("loginButton");
		});
	</g:javascript>

	<style type='text/css' media='screen'>
	</style>
</head>

<body>
	<div id='login'>
		<div class='inner'>
			<!--<div class='fheader'>Please Login</div>-->
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
	<script type='text/javascript'>
		<!--
		(function() {
			document.forms['loginForm'].elements['j_username'].focus();
		})();
		// -->
	</script>
</body>
