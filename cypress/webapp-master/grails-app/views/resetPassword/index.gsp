<head>
    <meta name="layout" content="${params.wl?'whitelabel':'b3noFooter'}" />
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
                            <g:message code="resetPassword.index.message1"/>
                        </p>

                        <g:form action="reset" name="resetPasswordForm" class="form" role="form">

                            <g:hiddenField name="returnUrl" value="${params.returnUrl}"/>
                            <g:hiddenField name="wl" value="${params.wl}"/>

                            <div class="input-group ${hasErrors(bean:cmd, field:'email', 'error')}">
                                <input type="email" class="form-control" name="email" value="${cmd?.email}" id="email_reset" />
                                 <span class="input-group-btn">
                                     <g:submitButton id="formSubmit" name="sumbit" value="${message(code: 'button.submit.label')}" class="btn btn-info"/>
                                 </span>
                            </div><!-- /.input-group -->

                        </g:form>

                    </div><!-- /.col-md-6 col-md-offset-3 text-center -->

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
