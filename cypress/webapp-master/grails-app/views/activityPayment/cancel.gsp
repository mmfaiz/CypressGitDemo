<%@ page import="com.matchi.payment.PaymentMethod" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="popup" />
</head>
<body>
<div class="modal-dialog">
    <div class="modal-content">
        <g:form name="confirmForm" action="cancel" class="no-margin form-horizontal" style="width: 100%">
            <g:hiddenField name="id" value="${occasion.id}"/>
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 id="modal-title"><g:message code="activityPayment.cancel.title"/></h4>
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
                        <h3 class="h6 weight400">
                            <span class="fa-stack">
                              <i class="fas fa-circle fa-stack-2x"></i>
                              <i class="fa fa-calendar fa-stack-1x fa-inverse"></i>
                          </span> <g:humanDateFormat date="${new org.joda.time.DateTime(occasion.date.toDate())}"/>
                        </h3>
                    </div>
                    <div class="col-sm-4">
                        <h3 class="h6 weight400">
                            <span class="fa-stack">
                              <i class="fas fa-circle fa-stack-2x"></i>
                              <i class="fa fa-clock-o fa-stack-1x fa-inverse"></i>
                          </span> <span rel="tooltip" data-original-title="${occasion.lengthInMinutes()}min">${occasion.startTime.toString("HH:mm")}</span>
                        </h3>
                    </div>
                    <div class="col-sm-4">
                        <h3 class="h6 weight400">
                          <span class="fa-stack">
                            <i class="fas fa-circle fa-stack-2x"></i>
                            <i class="fas fa-credit-card fa-stack-1x fa-inverse"></i>
                          </span>
                          <span rel="tooltip"
                                data-original-title="${message(code: 'activityPayment.confirm.vat')}, ${participation?.order?.vat()}">
                            <g:formatMoney value="${participation?.order?.total()}"
                                           facility="${participation?.customer?.facility}"/>
                          </span>
                        </h3>
                    </div>
                </div>

                <hr>

                <g:set var="payment" value="${participation.payment}"/>
                <g:set var="order" value="${participation.order}"/>

                <g:if test="${payment || order}">

                    <h2 class="h3"><g:message code="payment.label"/></h2>
                    <p><g:paymentShortSummary payment="${payment}" order="${order}"/></p>

                    <hr>

                    <div class="row">
                        <div class="col-xs-1">
                            <h3 class="h6 weight400">
                                <span class="fa-stack text-info">
                                  <i class="fas fa-circle fa-stack-2x"></i>
                                  <i class="fas fa-info fa-stack-1x fa-inverse"></i>
                              </span>
                            </h3>
                        </div>
                        <div class="col-xs-11">
                            <p class="vertical-padding5">
                                <g:if test="${!participation.isRefundable()}">
                                    <g:message code="activityPayment.cancel.note1" args="[occasion.activity.cancelLimitWithFallback]"/>
                                </g:if>
                                <g:else>
                                    <g:if test="${[PaymentMethod.COUPON, PaymentMethod.GIFT_CARD].contains(payment?.method) || order?.isPaidByCoupon()}">
                                        <g:message code="activityPayment.cancel.note2"/>
                                    </g:if>
                                    <g:else>
                                        <g:message code="payment.refund.servicefee"
                                                args="[serviceFeeValue(currency: occasion.activity.facility.currency)]"/>
                                    </g:else>
                                </g:else>
                            </p>
                        </div>
                    </div>
                </g:if>

            </div>

            <div class="modal-footer">
                <button class="btn btn-md btn-default" data-dismiss="modal" aria-hidden="true"><g:message code="button.cancel.label"/></button>
                <g:submitToRemote url="[action: 'cancel']" onLoading="onLoading()" class="btn btn-danger" update="userBookingModal"
                                  id="btnSubmit" value="${message(code: 'button.unbook.label')}" />
            </div>

        </g:form>
    </div>
</div>

<r:script>
    var onLoading = function() {
        $("#btnSubmit").attr("disabled", "disabled");
    };
    $("[rel='tooltip']").tooltip();
</r:script>

</body>
</html>
