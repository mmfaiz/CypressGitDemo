<head>
    <meta name="layout" content="main" />
    <title><g:message code="userRegistration.change.message1"/></title>
</head>

<body>

<g:errorMessage bean="${cmd}"/>

<div class="content-container">
    <div class="header">
        <h1><g:message code="userRegistration.change.message2"/></h1>
    </div>
    <div class="block hero-block vertical-padding20 center-text">
        <h2><g:message code="userRegistration.change.greeting"/> ${userInstance.fullName()}!</h2>
        <span class="calvert" style="font-size: 14px;"><g:message code="userRegistration.change.message6"/><br>
            <g:message code="userRegistration.change.message7"/></span>
    </div>
    <div class="block no-border vertical-padding20">
        <g:form action="enableWithPassword" class="form-center">
            <g:hiddenField name="ac" value="${cmd?.ac}"/>
            <fieldset>
                <div class="control-group ${hasErrors(bean:cmd, field:'newPassword', 'error')}">
                    <label class="control-label" for='newPassword'><g:message code="resetPassword.change.newPassword"/></label>
                    <div class="controls">
                        <input placeholder='Lösenord' class="span4" type='password' value="${cmd?.newPassword}" name='newPassword' id='newPassword' />
                    </div>
                </div>
                <div class="control-group ${hasErrors(bean:cmd, field:'newPasswordConfirm', 'error')}">
                    <label class="control-label" for='newPasswordConfirm'><g:message code="resetPassword.change.newPassword.repeat"/></label>
                    <div class="controls">
                        <input placeholder="${message(code: 'default.password.repeat.label')}" class="span4" type='password' value="${cmd?.newPasswordConfirm}" name='newPasswordConfirm' id='newPasswordConfirm' />
                    </div>
                </div>
                <div class="control-group">
                    <div class="controls left">
                        <g:submitButton name="save" id="saveButton" value="${message(code: 'userRegistration.change.message9')}" class="btn btn-success"/>
                    </div>
                </div>
                <div class="space10"></div>

                <div class="clear"></div>
            </fieldset>
        </g:form>
    </div>
</div>
</body>
