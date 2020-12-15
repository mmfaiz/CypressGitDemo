<html>
<head>
    <meta name="layout" content="${params.wl?'whitelabel':'b3noFooter'}" />
    <title><g:message code="info.invite.message1"/></title>

    <r:require modules="jquery-validate"/>
</head>
<body>

<!-- Messages -->
<g:flashMessage />
<g:flashError/>
<g:errorMessage bean="${cmd}"/>

<div class="block top-margin50 vertical-padding40">
    <div class="container">

        <div class="row">
            <div class="col-md-8 col-md-offset-2 col-xs-12 text-center">
                <div class="page-header text-center">
                    <h1 class="h2"><g:message code="info.invite.message16" args="[invitedCustomer.facility]" encodeAs="HTML"/></h1>
                </div>

                <h2><g:message code="info.invite.message18" args="[invitedCustomer]" encodeAs="HTML"/></h2>

                <p class="lead">
                    <g:message code="info.invite.message17" args="[invitedCustomer.facility]"/>
                </p>

                <hr>

                <h3><g:message code="login.auth.facebook"/></h3>

                <p class="text-muted">
                    <g:message code="info.invite.message28"/>
                </p>
                <button class="btn btn-facebook btn-lg" id="facebookButton"><i class="fab fa-facebook"></i> | <g:message code="auth.connect.with.facebook"/></button>
                <div><p class="padding10 text-center"><small><g:message code="userRegistration.consent.finishWithPrompt"/></small></p></div>
            </div>

            <div class="col-md-8 col-md-offset-2 col-xs-12 sign-up-separator sign-up-separator-grey vertical-margin20">
                <h6 class="text"><g:message code="default.or.label"/></h6>
                <hr>
            </div>

            <div class="clearfix"></div>

            <ul class="list-inline vertical-margin20 text-center">
                <li>
                    <div id="login-control">
                        <a class="btn btn-default" href="javascript:void(0)" onclick="toggleLogin()">
                            <g:message code="info.invite.message5"/>
                        </a>
                    </div>
                </li>
                <li>
                    <div id="register-control">
                        <a class="btn btn-default" href="javascript:void(0)" onclick="toggleRegistration()">
                            <g:message code="info.invite.message6"/>
                        </a>
                    </div>
                </li>
            </ul>

        </div><!-- ./row -->

        <div class="row">

            <div class="col-sm-4 col-sm-offset-4 col-xs-12">
                <div id="login-content" class="vertical-padding20" style="display: none;">
                    <hr>
                    <g:form class="form-center" action="login" method="POST" id="loginForm" autocomplete="off">
                        <g:hiddenField name="ticket" value="${ticket}"/>
                        <g:hiddenField name="wl" value="${params.wl}"/>
                        <div class="form-group ">
                            <label for="username" class="sr-only"><g:message code="default.username.label"/></label>
                            <input id="username" name="j_username" type="email" class="form-control" placeholder="${message(code: 'default.username.label')}"/>
                        </div>
                        <div class="form-group">
                            <label for="password" class="sr-only"><g:message code="default.password.label"/></label>
                            <input id="password" name="j_password" type="password" class="form-control no-bottom-margin" placeholder="${message(code: 'default.password.label')}"/>
                            <g:link controller="resetPassword" params="${params}" action="index" class="forgot right"><small><g:message code="login.auth.forgotPassword"/></small></g:link>
                        </div>
                        <div class="checkbox">
                            <input type="checkbox" name="${rememberMeParameter}" id="remember_me"
                                   <g:if test="${hasCookie}">checked="checked"</g:if>/>
                            <label for="${rememberMeParameter}">
                                <g:message code="auth.login.rememberme"/>
                            </label>
                        </div>
                        <div class="space-5"></div>
                        <button class="btn btn-lg btn-primary btn-block" type="submit"><g:message code="default.navigation.login"/></button>
                    </g:form>
                </div><!-- /.#login-content -->

                <div id="register-content" class="vertical-padding20" style="display: ${cmd ? "block":"none"};">
                    <hr>
                    <g:form action="register" name="userRegistration" role="form">
                        <g:hiddenField name="ticket" value="${ticket}"/>
                        <g:hiddenField name="telephone" value="${cmd?.telephone ?: invitedCustomer.telephone}"/>
                        <g:hiddenField name="wl" value="${params.wl}"/>
                        <g:hiddenField name="f" value="${params.f}"/>
                        <g:hiddenField name="returnUrl" value="${params.returnUrl}"/>
                        <g:hiddenField name="acceptTerms" id="acceptTerms" value="false"/>
                        <g:hiddenField name="receiveNewsletters" id="receiveNewsletters" value="false"/>
                        <g:hiddenField name="receiveCustomerSurveys" id="receiveCustomerSurveys" value="false"/>
                        <div class="space-5"></div>
                        <h3><g:message code="info.invite.message35"/></h3>
                        <g:if test="${facility}">
                            <g:hiddenField name="c" value="${facility.getRegistrationCode()}"/>
                            <div class="form-group">
                                <label for="firstname"><g:message code="info.invite.message36"/></label>
                                <h3 class="form-control">${facility?.name}</h3>
                            </div>
                        </g:if>
                        <div class="form-group ${hasErrors(bean: cmd, field: "firstname", "has-error")}">
                            <label for="firstname" class="sr-only"><g:message code="default.firstname.label"/></label>
                            <g:textField class="form-control" name="firstname" id="firstname" value="${cmd?.firstname}" placeholder="${message(code: 'info.invite.message20')}"/>
                        </div>
                        <div class="form-group ${hasErrors(bean: cmd, field: "lastname", "has-error")}">
                            <label for="lastname" class="sr-only"><g:message code="default.lastname.label"/></label>
                            <g:textField class="form-control" name="lastname" id="lastname" value="${cmd?.lastname}" placeholder="${message(code: 'info.invite.message21')}"/>
                        </div>
                        <div class="form-group ${hasErrors(bean: cmd, field: "email", "has-error")}">
                            <label for="email" class="sr-only"><g:message code="default.email.label"/></label>
                            <g:textField class="form-control" name="email" id="email" value="${cmd?.email}" placeholder="${message(code: 'info.invite.message22')}"/>
                        </div>
                        <div class="form-group ${hasErrors(bean: cmd, field: "password", "has-error")}">
                            <label for="password" class="sr-only"><g:message code="default.password.label"/></label>
                            <g:passwordField class="form-control no-bottom-margin" name="password" id="password" placeholder="${message(code: 'userRegistration.index.message20')}"/>
                        </div>
                        <div class="form-group ${hasErrors(bean: cmd, field: "password2", "has-error")}">
                            <label for="password2" class="sr-only">${message(code: 'default.password.repeat.label')}</label>
                            <g:passwordField class="form-control" name="password2" id="password2" placeholder="${message(code: 'info.invite.message24')}"/>
                            <p><small><g:message code="user.password.requirements"/></small></p>
                        </div>
                        <g:submitButton name="save" id="saveButton" value='${message(code: 'info.invite.message26')}' class="btn btn-success col-xs-12"/>
                        <p class="padding10 text-center"><small><g:message code="userRegistration.consent.finishWithPrompt"/></small></p>
                    </g:form>
                </div><!-- /.#register-content -->

            </div><!-- /.col-sm-4 col-sm-offset-4 col-xs-12 -->

        </div><!-- ./row -->

    </div><!-- /.container -->

    <div class="space-60"></div>
</div><!-- /.block -->

<g:termsModal skipCheck="true" overrideBehaviour="true" bootstrap3="true" />

<r:script>
    $(document).ready(function() {
        $("[name='email']").focus();

        // When the Facebook button is clicked trigger the modal and set the submit action.
        $('#facebookButton').click(function(){
            resetModal();
            facebookLogin("${g.forJavaScript(data: params.ticket)}");
        });

        // When the create account button is clicked trigger the modal and set the submit action to form submit.
        $('#saveButton').click(function(e){
            e.preventDefault();
            $('#acceptConsentModalSubmit').click(function(){
                $("#acceptTerms").val($("#acceptConsentModalCheckBoxTerms").is(":checked"));
                $('#userRegistration').submit();
            });

            resetModal();
            $('#acceptConsentModal').modal('show');
        });

        // Clear these whenever opening again to avoid being accused of sneaky business
        function resetModal() {
            $("#acceptConsentModalCheckBoxTerms").attr('checked', false);
            $("#acceptConsentModalCheckBoxReceiveNewsletter").attr('checked', false);
            $("#acceptConsentModalCheckBoxReceiveCustomerSurveys").attr('checked', false);
            $('#acceptConsentModalSubmit').addClass('disabled');
        }

        // When the receiveNewsletter in modal changes replicate value to hidden field in form.
        $('#acceptConsentModalCheckBoxReceiveNewsletter').change(function() {
            $('#receiveNewsletters').val(this.checked);
        });

        // When the receiveNewsletter in modal changes replicate value to hidden field in form.
        $('#acceptConsentModalCheckBoxReceiveCustomerSurveys').change(function() {
            $('#receiveCustomerSurveys').val(this.checked);
        });
    });
</r:script>

<r:script>
    $loginControl    = $("#login-control");
    $loginContent    = $("#login-content");
    $registerControl = $("#register-control");
    $registerContent = $("#register-content");

    function toggleLogin() {
        if($registerContent.is(":visible")) {
            $registerContent.hide();
        }

        if(!$loginContent.is(":visible")) {
            $loginContent.show();
        } else {
            $loginContent.hide();
        }
    }
    function toggleRegistration() {
        if($loginContent.is(":visible")) {
            $loginContent.hide();
        }

        if(!$registerContent.is(":visible")) {
            $registerContent.show();
        } else {
            $registerContent.hide();
        }
    }
</r:script>
</body>
</html>
