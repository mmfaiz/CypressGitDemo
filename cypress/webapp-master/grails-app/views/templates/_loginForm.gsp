<div id="login_popup">
	<form action='${postUrl}' method='POST' id='loginForm' class='cssform' autocomplete='off'>
		<fieldset>
            <label for='username'><g:message code="user.email.label"/></label>
			<input type='text' size="47" name='j_username' id='username' placeholder="${message(code: 'templates.loginForm.message5')}" value="" tabindex="1"/>
		</fieldset>
		<fieldset>
            <label for='password'><g:message code="default.password.label"/></label>
            <g:link controller="resetPassword" action="index" class="forgot right"><g:message code="login.auth.forgotPassword"/></g:link><br>
			<input type='password' size="47" name='j_password' id='password' placeholder="${message(code: 'templates.loginForm.message6')}" value="" tabindex="2"/>
		</fieldset>

		<fieldset>
            <input type='submit' value="${message(code: 'templates.loginForm.message7')}" tabindex="4" class="btn btn-inverse right" style="margin-right: 5px;"/>
            <div class="pull-right" style="margin-right: 10px; padding: 7px 0;">
                <label for='remember_me' class="no_float"><g:message code="templates.loginForm.message4"/></label>
                <input type='checkbox' name='${rememberMeParameter}' id='remember_me' class="no_float"
                           <g:if test='${hasCookie}'>checked='checked'</g:if> tabindex="3" />
            </div>
		</fieldset>

        <hr>

        <fieldset>
            <center>
                <div class="fb-button" onclick="facebookLogin()">
                    <g:message code="auth.login.facebook"/>
                </div>
            </center>
        </fieldset>
	</form>
</div>
