<head>
    <meta name="layout" content="b3noFooter" />
    <title><g:message code="resetPassword.change.newPassword"/> - MATCHi</title>
</head>

<body>
    <g:b3StaticErrorMessage bean="${cmd}"/>

    <div class="block top-margin50">
        <div class="block vertical-padding10">
            <div class="container">

                <div class="row">

                    <div class="col-md-4 col-md-offset-4">
                        <div class="page-header text-center">
                            <h1 class="h2"><g:message code="resetPassword.change.newPassword"/></h1>
                        </div>

                        <p class="vertical-padding10">
                            <g:message code="resetPassword.change.message4"/>
                        </p>

                        <g:form action="update" name="resetPasswordForm" class="form" role="form">

                            <g:hiddenField name="ticket" value="${cmd.ticket}"/>

                            <div class="form-group ${hasErrors(bean:cmd, field:'newPassword', 'has-error')}">
                                <label class="" for="newPassword"><g:message code="resetPassword.change.newPassword"/></label>
                                <div class="controls">
                                    <g:passwordField type="password" class="form-control no-bottom-margin" value="${cmd?.newPassword}" name="newPassword" id="newPassword" />
                                </div>
                            </div>

                            <div class="form-group ${hasErrors(bean:cmd, field:'email', 'has-error')}">
                                <label class="" for="newPasswordConfirm"><g:message code="resetPassword.change.newPassword.repeat"/></label>
                                <div class="controls">
                                    <g:passwordField type="password" class="form-control" value="${cmd?.newPasswordConfirm}" name="newPasswordConfirm" id="newPasswordConfirm" />
                                    <p><small><g:message code="user.password.requirements"/></small></p>
                                </div>
                            </div>

                            <g:submitButton name="sumbit" value="${message(code: 'resetPassword.change.message3')}" class="btn btn-success col-xs-12"/>

                        </g:form>

                    </div><!-- /.col-md-4 col-md-offset-4 -->

                </div><!-- /.row -->

            </div><!-- /.container -->
        </div><!-- /.block vertical-padding40 -->
    </div><!-- /.block top-margin50 -->

<g:javascript>
$(document).ready(function() {
    $('input#email_reset').focus();
});
</g:javascript>

</body>
