<%@ page import="com.matchi.DateUtil; org.joda.time.DateTime; com.matchi.payment.PaymentMethod; com.matchi.coupon.CustomerCoupon" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="popup" />
</head>
<body>
<!-- NOTE! This is modal dialog that is loaded with Ajax and only body will be included so scripts in head will not be included, only body content. -->
<g:render template="/templates/googleTagManager" model="[facility: facility]" />

<div class="modal-dialog">
    <div class="modal-content">
        <g:form name="confirmForm" class="no-margin">
            <g:hiddenField name="orderId" value="${order.id}"/>
            <g:hiddenField name="id" value="${occasion.id}"/>

            <div class="modal-header flex-center separate">
                <h4 class="modal-title"><g:message code="activityPayment.confirm.title"/></h4>
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times-circle"></i></button>
            </div>

            <div class="modal-body">

                <g:b3StaticFlashError/>
                <g:b3StaticErrorMessage bean="${command}"/>

                <div class="row">
                    <div class="col-sm-6">
                        <h1 class="h6">
                            <span class="fa-stack fa-lg text-danger">
                              <i class="fas fa-circle fa-stack-2x"></i>
                              <i class="fas fa-users fa-stack-1x fa-inverse"></i>
                            </span> ${occasion.activity.name}
                        </h1>
                    </div>
                    <div class="col-sm-6">
                        <h2 class="h6">
                            <span class="fa-stack fa-lg">
                              <i class="fas fa-circle fa-stack-2x"></i>
                              <i class="fas fa-map-marker fa-stack-1x fa-inverse"></i>
                            </span>
                            ${occasion.activity.facility.name}
                        </h2>
                    </div>
                </div>

                <hr>

                <div class="row">
                    <div class="col-sm-4">
                        <p>
                            <span class="fa-stack">
                              <i class="fas fa-circle fa-stack-2x"></i>
                              <i class="fa fa-calendar fa-stack-1x fa-inverse"></i>
                          </span> ${occasion.date.toString("${message(code: 'date.format.dateOnly')}")}
                        </p>
                    </div>
                    <div class="col-sm-4">
                        <p>
                            <span class="fa-stack">
                              <i class="fas fa-circle fa-stack-2x"></i>
                              <i class="fa fa-clock-o fa-stack-1x fa-inverse"></i>
                          </span> <span rel="tooltip" data-original-title="${occasion.lengthInMinutes()}${message(code: 'unit.min')}">${occasion.startTime.toString("HH:mm")}</span>
                        </p>
                    </div>
                    <div class="col-sm-4">
                        <p>
                            <span class="fa-stack">
                                <i class="fas fa-circle fa-stack-2x"></i>
                                <i class="fas fa-credit-card fa-stack-1x fa-inverse"></i>
                            </span>
                            <span id="bookingPrice" rel="tooltip" data-original-title="${message(code: 'activityPayment.confirm.vat')}, ${order.vat()}">
                              <g:formatMoney value="${order.total()}" facility="${occasion.activity?.facility}" />
                            </span>
                        </p>
                    </div>
                </div>

                <g:if test="${occasion.mayBeCancelledAutomatically()}">
                    <hr>
                    <div class="row">
                        <div class="col-sm-12">
                            <h2 class="h6 text-center">
                                <span class="fa-stack fa-lg text-warning">
                                    <i class="fa fa-circle fa-stack-2x"></i>
                                    <i class="fa fa-exclamation fa-stack-1x fa-inverse"></i>
                                </span>
                                <strong><g:message code="default.note.label.withExclamation" /></strong>
                            </h2>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-sm-12">
                            <p><g:message code="activityPayment.confirm.automaticCancellationWarning" args="${[occasion.minNumParticipants, occasion.automaticCancellationDateTime.format(DateUtil.DATE_AND_TIME_FORMAT)]}" /></p>
                        </div>
                    </div>
                </g:if>

                <hr>

                <g:render template="/templates/payments/paymentMethod" model="${paymentMethodsModel}"/>

                <hr/>

                <div class="row">
                    <div class="col-xs-1">
                        <p>
                            <span class="fa-stack text-info">
                              <i class="fas fa-circle fa-stack-2x"></i>
                              <i class="fas fa-info fa-stack-1x fa-inverse"></i>
                          </span>
                        </p>
                    </div>
                    <div class="col-xs-11">
                        <div class="${occasion.activity.cancelByUser ? 'vertical-padding5' : 'vertical-padding15'}">
                            <p>
                                <g:if test="${occasion.activity.cancelByUser}">
                                    <g:message code="activityPayment.confirm.note"
                                        args="[createLink(controller: 'home', action: 'useragreement', fragment: 'Betalning'), occasion.activity.cancelLimitWithFallback]"/>
                                </g:if>
                                <g:else>
                                    <g:message code="activityPayment.confirm.cancelByUser.disabled"/>
                                </g:else>
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button class="btn btn-md btn-default" data-dismiss="modal" aria-hidden="true"><g:message code="button.cancel.label"/></button>
                <g:render template="/templates/payments/submitBtnAndHandler"
                        model="[targetController: 'activityPayment', facility: occasion.activity.facility]"/>
            </div>
        </g:form>
    </div>
</div>
<r:script>
    $(document).ready(function() {
        // Disable enter on form (since we submit remote)
        $("#confirmForm").bind("keypress", function(e) {
            if (e.keyCode == 13) return false;
        });

        $("[rel='tooltip']").tooltip();
    });
</r:script>
</body>
</html>
