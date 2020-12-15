<%@ page import="java.text.SimpleDateFormat; com.matchi.User" %>
<html>
<head>
    <meta name="layout" content="b3main" />
    <meta name="facebookReturnUrl" content="${createLink(action: 'account')}"/>
    <meta name="showFacebookNagger" content="${true}"/>
    <title><g:message code="userProfile.account.message2"/> - MATCHi</title>
</head>
<body>
<g:b3StaticErrorMessage bean="${cmd}"/>
<g:b3StaticErrorMessage bean="${user}"/>
<section class="block block-white vertical-padding30">

    <div class="container">
        <h2 class="page-header no-top-margin"><i class="fas fa-cog"></i> <g:message code="user.account.header.title"/></h2>

        <div id="accountSettingsInfoMessage" class="alert alert-info vertical-margin20" style="display: none;">
            <button type="button" class="close" onclick="removeAccountSettingsInfoMessage()" data-dismiss="alert" rel="tooltip" title="<g:message code="default.do.not.show.again"/>"><span aria-hidden="true">&times;</span><span class="sr-only"><g:message code="button.close.label"/></span></button>
            <p>
                <span class="fa-stack fa-lg">
                    <i class="fas fa-circle fa-stack-2x"></i>
                    <i class="fa fa-lightbulb-o fa-stack-1x fa-inverse"></i>
                </span>
                <g:message code="user.account.info.description"/>
            </p>
        </div>
        <div class="space-20"></div>

        <div class="row">
            <div class="col-md-6 vertical-margin10">
                <h2 class="h4 no-bottom-margin text-muted">
                    <g:message code="user.account.header.notifications.visibility"/>
                </h2>
                <hr class="top-margin5">
                <g:form action="updateAccount">
                    <div class="form-group ${hasErrors(bean:cmd, field:'receiveNewsletters', 'error')}
                    ${hasErrors(bean:cmd, field:'receiveCustomerSurveys', 'error')}
                    ${hasErrors(bean:cmd, field:'searchable', 'error')}  ${hasErrors(bean:cmd, field:'matchable', 'error')}">
                        <div class="checkbox">
                            <g:checkBox class="" name="newsletters" value="${user?.receiveNewsletters}"/>
                            <label for="newsletters">
                                <g:message code="user.account.label.newsletters"/>
                            </label>
                        </div>
                        <div class="checkbox">
                            <g:checkBox class="" name="customerSurveys" value="${user?.receiveCustomerSurveys}"/>
                            <label for="customerSurveys">
                                <g:message code="user.account.label.customerSurveys"/>
                            </label>
                        </div>
                        <div class="checkbox">
                            <g:checkBox name="searchable" value="${user?.searchable}"/>
                            <label for="searchable">
                                <g:message code="user.account.label.searchable"/>
                            </label>
                        </div>
                        <div class="checkbox">
                            <g:checkBox name="matchable" value="${user?.matchable}"/>
                            <label for="matchable">
                                <g:message code="user.account.label.matchable"/>
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <g:submitButton name="submit" class="btn btn-success btn-sm" value="${message(code: "button.save.label")}"/>
                        </div>
                    </div>
                </g:form>
            </div><div class="col-md-6 vertical-margin10">
            <h2 class="h4 no-bottom-margin text-muted">
                <g:message code="user.account.savedcard.header"/>
            </h2>
            <hr class="top-margin5">
            <g:if test="${paymentInfo}">
                <p><g:message code="user.account.savedcard.savedsince" args="[formatDate(date: paymentInfo.dateCreated, format: message(code: 'date.format.dateOnly'))]"/><p>
                <div class="panel panel-default">
                    <div class="panel-body">
                        <g:message code="user.account.savedcard.issuer"/>: <strong><g:message code="payment.method.adyen.${paymentInfo.issuer}"/></strong><br/>
                        <g:message code="user.account.savedcard.holder"/>: <strong>${paymentInfo.holderName}</strong><br/>
                        <g:message code="user.account.savedcard.number"/>: <strong>.... .... .... ${paymentInfo.number}</strong><br/>
                        <g:message code="user.account.savedcard.expiry"/>: <strong>${paymentInfo.formatExpiryDate()}</strong>
                        <br/><br/>
                        <ul class="list-inline">
                            <li>
                                <g:link onclick="return confirm('${message(code: 'user.account.msg.confirm.remove.savedcard')}')"
                                        action="forgetPaymentInfos" class="btn btn-danger btn-sm"><g:message code="user.account.savedcard.btn.remove"/></g:link>
                            </li>
                            <li>
                                <g:remoteLink class="btn btn-success btn-sm"
                                              controller="paymentInfoUpdate"
                                              action="confirm"
                                              update="userBookingModal"
                                              onFailure="handleAjaxError()"
                                              onSuccess="showLayer('userBookingModal')"><g:message code="user.account.savedcard.btn.change"/></g:remoteLink>
                            </li>
                        </ul>
                    </div>
                </div>
            </g:if>
            <g:else>
                <p><g:message code="user.account.savedcard.not.active"/></p>
                <p>
                    <g:message code="user.account.savedcard.description.1"/>
                    <br><br>
                    <g:message code="user.account.savedcard.description.2"/>
                    <br><br>
                    <!--<g:link action="addPaymentInfo" class="btn btn-success btn-sm"><g:message code="user.account.savedcard.btn.add"/></g:link>-->

                    <g:remoteLink class="btn btn-success btn-sm"
                                  controller="paymentInfoUpdate"
                                  action="confirm"
                                  update="userBookingModal"
                                  onFailure="handleAjaxError()"
                                  onSuccess="showLayer('userBookingModal')"><g:message code="user.account.savedcard.btn.add"/></g:remoteLink>
                </p>
            </g:else>
        </div>

        </div>
        <div class="space-40"></div>
        <div class="row">
            <div class="col-md-6 vertical-margin10">
                <h2 class="h4 no-bottom-margin text-muted">
                    <g:message code="default.password.label"/>
                </h2>
                <hr class="top-margin5">
                <p>
                    <g:message code="user.account.password.description.click.to.change.password"/>
                </p>
                <p>
                    <g:link controller="resetPassword" action="index" class="btn btn-success btn-sm"><g:message code="resetPassword.change.newPassword"/></g:link>
                </p>
            </div>
            <div class="col-md-6 vertical-margin10">
                <h2 class="h4 no-bottom-margin text-muted">
                    <g:message code="user.account.header.facebook.connect"/>
                </h2>
                <hr class="top-margin5">
                <g:if test="${user?.facebookUID}">
                    <p><g:message code="user.account.facebook.your.profile"/> <strong><g:message code="user.account.facebook.connected"/></strong> <g:message code="user.account.facebook.with.your.fb.account"/></p>
                </g:if>
                <g:else>
                    <p><g:message code="user.account.facebook.your.profile"/> <strong><g:message code="user.account.facebook.not.connected"/></strong> <g:message code="user.account.facebook.with.your.fb.account"/></p>
                </g:else>
                <g:if test="${user?.facebookUID}">
                    <g:link onclick="return confirm('${message(code: 'user.account.msg.confirm.disconnect.facebook')}')" action="forgetFacebookConnect" class="btn btn-danger btn-sm"><g:message code="user.account.btn.disconnect.facebook"/></g:link>
                </g:if>
                <g:else>
                    <button class="btn btn-facebook btn-sm" onclick="facebookLogin()"><i class="fab fa-facebook"></i> | <g:message code="auth.connect.with.facebook"/></button>
                </g:else>
            </div>
        </div>
    </div>
    <div class="space-40"></div>
</section>
<div id="userBookingModal" class="modal hide fade"></div>
<g:javascript>
    $(document).ready(function() {
        <g:if test="${params.comeback}">
            <g:remoteFunction controller="paymentInfoUpdate" action="confirm" params="${params}" update="userBookingModal" onSuccess="showLayer('userBookingModal')" onFailure="handleAjaxError()" />
        </g:if>

        <g:if test="${paymentFlow}">
            <g:if test="${paymentFlow.errorMessage}">
                <g:remoteFunction controller="${paymentFlow.paymentController}" action="${paymentFlow.getFinalAction()}" params="${paymentFlow.getModalParams()}" update="userBookingModal" onSuccess="showLayer('userBookingModal')" onFailure="handleAjaxError()" />
            </g:if>
            <g:else>
                window.location.replace("<g:createLink controller="${paymentFlow.paymentController}" action="${paymentFlow.getFinalAction()}" params="${paymentFlow.getModalParams()}" />");
            </g:else>
        </g:if>

        $("*[rel=tooltip]").tooltip();

        if(!getCookie("hideFacebookNagger")) {
             $("#fbConnect").show();
        }
        if(!getCookie("hideAccountSettingsInfo")) {
             $("#accountSettingsInfoMessage").show();
        }
    });
</g:javascript>
</body>
</html>
