<html>
<head>
    <meta name="layout" content="${params.wl?'whitelabel':'b3noFooter'}" />
    <title><g:message code="userRegistration.index.message1"/></title>
    <meta name="loginReturnUrl" content="${createLink(controller: 'userProfile', action: 'home')}"/>
</head>
<body>

<g:b3StaticErrorMessage bean="${userInstance}"/>
<g:b3StaticErrorMessage bean="${cmd}"/>

<div class="block block-grey">
    <div class="container">
        <div class="row vertical-padding10">
            <div class="col-md-12 text-center">
                <p class="vertical-margin10">
                    <g:message code="userRegistration.index.message23"/> <g:link params="${params}" controller="login" action="auth"><g:message code="userRegistration.index.message24"/></g:link>
                </p>
            </div>
        </div>
    </div>
</div>

<div class="block top-margin10">
    <div class="container">
        <div class="row vertical-padding30">
            <div class="col-sm-12 col-md-4 col-md-offset-2 vertical-padding20 text-center">
                <h3 class="h4 bottom-margin30"><g:message code="userRegistration.index.message25"/></h3>
                <button class="btn btn-facebook btn-lg btn-block" name="facebookButton" id="facebookButton"><i class="fab fa-facebook"></i> | <g:message code="auth.connect.with.facebook"/></button>
                <p class="padding10"><small><g:message code="userRegistration.consent.finishWithPrompt"/></small></p>
            </div>
            <div class="col-xs-12 visible-xs sign-up-separator">
                <h6 class="text"><g:message code="default.or.label"/></h6>
                <hr>
            </div>
            <div class="col-md-4">
                <g:form class="form-signin center-block" action="save" name="userRegistration" id="userRegistration" role="form">
                    <g:hiddenField name="returnUrl" value="${params.returnUrl}"/>
                    <g:hiddenField name="wl" value="${params.wl}"/>
                    <g:hiddenField name="f" value="${params.facilityId}"/>
                    <g:hiddenField name="facilityId" value="${params.facilityId}"/>
                    <g:hiddenField name="acceptTerms" id="acceptTerms" value="false"/>
                    <g:hiddenField name="receiveNewsletters" id="receiveNewsletters" value="false"/>
                    <g:hiddenField name="receiveCustomerSurveys" id="receiveCustomerSurveys" value="false"/>
                    <div class="space-5"></div>
                    <h3 class="h4 bottom-margin30"><g:message code="userRegistration.index.message28"/></h3>
                    <g:if test="${facility}">
                        <g:hiddenField name="c" value="${facility.getRegistrationCode()}"/>
                        <div class="form-group">
                            <label for="firstname"><g:message code="userRegistration.index.message29"/></label>
                            <h3 class="form-control">${facility?.name}</h3>
                        </div>
                    </g:if>
                    <div class="form-group ${hasErrors(bean: cmd, field: "email", "has-error")}">
                        <label for="email" class="sr-only"><g:message code="user.email.label"/></label>
                        <g:textField class="form-control" name="email" id="email" value="${cmd?.email}" placeholder="${message(code: 'userRegistration.index.message18')}"/>
                    </div>
                    <div class="form-group ${hasErrors(bean: cmd, field: "firstname", "has-error")}">
                        <label for="firstname" class="sr-only"><g:message code="user.firstname.label"/></label>
                        <g:textField class="form-control" name="firstname" id="firstname" value="${cmd?.firstname}" placeholder="${message(code: 'userRegistration.index.message16')}"/>
                    </div>
                    <div class="form-group ${hasErrors(bean: cmd, field: "lastname", "has-error")}">
                        <label for="lastname" class="sr-only"><g:message code="user.lastname.label"/></label>
                        <g:textField class="form-control" name="lastname" id="lastname" value="${cmd?.lastname}" placeholder="${message(code: 'userRegistration.index.message17')}"/>
                    </div>
                    <div class="form-group ${hasErrors(bean: cmd, field: "password", "has-error")}">
                        <label for="password" class="sr-only"><g:message code="default.password.label"/></label>
                        <g:passwordField class="form-control no-bottom-margin" name="password" id="password" placeholder="${message(code: 'userRegistration.index.message20')}"/>
                    </div>
                    <div class="form-group ${hasErrors(bean: cmd, field: "password2", "has-error")}">
                        <label for="password2" class="sr-only"><g:message code="default.password.repeat.label"/></label>
                        <g:passwordField class="form-control" name="password2" id="password2" placeholder="${message(code: 'userRegistration.index.message21')}"/>
                        <p><small><g:message code="user.password.requirements"/></small></p>
                    </div>
                    <div>
                        <recaptcha:ifEnabled>
                            <recaptcha:recaptcha lang = "${g.locale().toString()}"/>
                        </recaptcha:ifEnabled>
                        <recaptcha:ifFailed>
                            <div class="alert alert-danger"><g:message code="userRegistration.captcha.error"/></div>
                        </recaptcha:ifFailed>
                        <br/>
                        <input type="button" name="save" id="saveButton" value='${message(code: 'userRegistration.index.message3')}' class="btn btn-success col-xs-12" />
                    </div>
                    <br><br>
                    <p class="padding10 text-center"><small><g:message code="userRegistration.consent.finishWithPrompt"/></small></p>
                </g:form>

            </div>
        </div>
        <div class="space-100"></div>
    </div>
</div>

<g:termsModal skipCheck="true" overrideBehaviour="true" bootstrap3="true" />

<r:script>
    $(document).ready(function() {
        $("[name='email']").focus();

        // When the Facebook button is clicked trigger the modal and set the submit action.
        $('#facebookButton').click(function(){
            resetModal();
            facebookLogin("");
        });

        // When the create account button is clicked trigger the modal and set the submit action to form submit.
        $('#saveButton').click(function(){
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

</body>
</html>




