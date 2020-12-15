<head>
    <meta name="layout" content="${params.wl == '1'?'whitelabel':'b3noFooter'}" />
    <title><g:message code="login.auth.message1"/></title>
</head>
<body>

<div class="block block-grey">
    <div class="container">
        <div class="row vertical-padding10">
            <div class="col-md-12 text-center">
                <p class="vertical-margin10">
                    <g:message code="login.auth.message2"/> <g:link params="${params}" controller="userRegistration"><g:message code="login.auth.message3"/></g:link>
                </p>
            </div>
        </div>
    </div>
</div>

<div class="block top-margin10">
    <div class="container">

        <div class="row vertical-padding30">

            <div class="col-sm-12 col-md-4 col-md-offset-2 vertical-padding20 text-center">
                <h3 class="h4 bottom-margin30"><g:message code="login.auth.facebook"/></h3>
                <button class="btn btn-facebook btn-lg btn-block" onclick="facebookLogin()"><i class="fab fa-facebook"></i> | <g:message code="auth.connect.with.facebook"/></button>
            </div>
            <div class="col-xs-12 hidden-md hidden-lg sign-up-separator">
                <h6 class="text"><g:message code="default.or.label"/></h6>
                <hr>
            </div><!-- /.col-sm-12 col-md-4 col-md-offset-2 vertical-padding20 text-center -->

            <div class="col-sm-12 col-md-4">

                <form class="form-signin center-block" action="${postUrl}" method="POST" id="loginForm" autocomplete="off" role="form">
                    <div class="space-5"></div>
                    <h3 class="h4 bottom-margin30"><g:message code="login.auth.message11"/></h3>
                    <div class="form-group ">
                        <label for="username" class="sr-only"><g:message code="default.username.label"/></label>
                        <input id="username" name="j_username" type="email" class="form-control" placeholder="${message(code: 'login.auth.message13')}"/>
                    </div>
                    <div class="form-group">
                        <label for="password" class="sr-only"><g:message code="default.password.label"/></label>
                        <input id="password" name="j_password" type="password" class="form-control no-bottom-margin" placeholder="${message(code: 'default.password.label')}"/>
                        <g:link controller="resetPassword" params="${params}" action="index" class="forgot right"><small><g:message code="login.auth.forgotPassword"/></small></g:link>
                    </div>
                    <div class="checkbox">
                        <input type="checkbox" name="${rememberMeParameter}" id="remember_me"
                               <g:if test="${hasCookie}">checked="checked"</g:if>/>
                        <label for="remember_me">
                            <g:message code="auth.login.rememberme"/>
                        </label>
                    </div>

                    <button class="btn btn-lg btn-success btn-block" type="submit"><g:message code="default.navigation.login"/></button>
                </form>

            </div><!-- /.col-md-4 -->

        </div><!-- /.row -->

    </div><!-- /.container -->
</div><!-- /.block -->

<g:termsModal skipCheck="true" overrideBehaviour="true" bootstrap3="true" />
<g:cookiesModal bootstrap3="true" />

<r:script>
    $(document).ready(function() {

        if (!navigator.cookieEnabled) {
            $('#cookiesModal').modal('show');
        }

        $('input#username').focus();

        // When the Facebook button is clicked trigger the modal and set the submit action.
        $('#facebookButton').click(function(){
            $("#acceptConsentModalCheckBoxTerms").attr('checked', false);
            $("#acceptConsentModalCheckBoxReceiveNewsletter").attr('checked', false);
            $("#acceptConsentModalCheckBoxReceiveCustomerSurveys").attr('checked', false);
            $('#acceptConsentModalSubmit').addClass('disabled');

            facebookLogin("");
        });
    });
</r:script>
</body>
