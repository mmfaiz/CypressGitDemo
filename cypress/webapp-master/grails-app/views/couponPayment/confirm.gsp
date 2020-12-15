<%@ page import="com.matchi.payment.PaymentMethod; com.matchi.coupon.CustomerCoupon; com.matchi.coupon.Coupon" contentType="text/html;charset=UTF-8" %>
<g:set var="offerType" value="${coupon.getOfferTypeString()}"/>
<html>
<head>
    <meta name="layout" content="popup" />
</head>
<body>
<!-- NOTE! This is modal dialog that is loaded with Ajax and only body will be included so scripts in head will not be included, only body content. -->
<g:render template="/templates/googleTagManager" model="[facility: facility]" />

<div class="modal-dialog">
    <div class="modal-content">
        <g:form name="confirmForm" class="no-margin form-horizontal">
            <g:hiddenField name="orderId" value="${order.id}"/>
            <g:hiddenField name="id" value="${coupon.id}"/>

            <div class="modal-header flex-center separate">
                <h4 class="modal-title"><g:message code="${offerType}Payment.confirm.title"/></h4>
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times-circle"></i></button>
            </div>

            <div class="modal-body">
                <g:b3StaticFlashError/>
                <g:b3StaticErrorMessage bean="${command}"/>

                <div class="row">
                    <div class="col-sm-6">
                        <h1 class="h6 ellipsis">
                            <span class="fa-stack fa-lg text-warning">
                              <i class="fas fa-circle fa-stack-2x"></i>
                              <i class="fa fa-ticket fa-stack-1x fa-inverse"></i>
                            </span> ${coupon}
                        </h1>
                    </div>
                    <div class="col-sm-6">
                        <h2 class="h6">
                            <span class="fa-stack fa-lg text-grey-light">
                              <i class="fas fa-circle fa-stack-2x"></i>
                              <i class="fas fa-map-marker fa-stack-1x fa-inverse"></i>
                            </span>
                            ${coupon.facility.name}
                        </h2>
                    </div>
                </div>

                <hr>

                <div class="row">
                    <div class="col-sm-4">
                        <p>
                            <span class="fa-stack">
                              <i class="fas fa-circle fa-stack-2x"></i>
                              <i class="fas fa-check fa-stack-1x fa-inverse"></i>
                            </span> <span class="text-muted"><g:message code="${offerType}.nrOfTickets.label2"/>:</span>
                            <g:if test="${coupon?.unlimited}">
                                <span class="text-md">&infin;</span>
                            </g:if>
                            <g:else>
                                ${coupon?.nrOfTickets} <g:message code="unit.st"/>
                            </g:else>
                        </p>
                    </div>

                    <div class="col-sm-4">
                        <p>
                            <span class="fa-stack">
                              <i class="fas fa-circle fa-stack-2x"></i>
                              <i class="fa fa-calendar fa-stack-1x fa-inverse"></i>
                          </span> <span rel="tooltip" data-original-title="${message(code: 'couponPayment.confirm.validTo')}"> <g:formatDate date="${coupon?.getExpireDate()?.toDate()}" formatName="date.format.dateOnly"/></span>
                        </p>
                    </div>

                    <div class="col-sm-4">
                        <p>
                            <span class="fa-stack">
                              <i class="fas fa-circle fa-stack-2x"></i>
                              <i class="fas fa-credit-card fa-stack-1x fa-inverse"></i>
                          </span>
                          <span id="bookingPrice" rel="tooltip" data-original-title="${message(code: 'couponPayment.confirm.vat')}"><g:formatMoney value="${order?.price}" facility="${order?.facility}"/></span>
                        </p>
                    </div>
                </div>

                <hr>

                <g:if test="${coupon.couponConditionGroups
                        || (coupon.instanceOf(Coupon) && coupon.nrOfBookingsInPeriod && coupon.conditionPeriod)}">
                    <div>
                        <p>
                            <span class="fa-stack">
                                <i class="fas fa-circle fa-stack-2x"></i>
                                <i class="fas fa-info fa-stack-1x fa-inverse"></i>
                            </span>
                            <span><g:message code="default.terms.label"/></span>
                        </p>
                    </div>
                    <div>
                        <g:if test="${coupon.instanceOf(Coupon) && coupon.nrOfBookingsInPeriod && coupon.conditionPeriod}">
                            <div class="condition">
                                <div class="entry pull-left">
                                    <div class="details">
                                        <g:message code="coupon.bookingCondition.message${coupon.totalBookingsInPeriod ? '1' : '2'}"
                                                args="[coupon.nrOfBookingsInPeriod,
                                                        message(code: 'coupon.conditionPeriod.' + coupon.conditionPeriod.name(), args: [coupon.conditionPeriod]).toLowerCase()]"/>
                                    </div>
                                </div>
                            </div>
                        </g:if>

                        <g:each in="${coupon.couponConditionGroups}" var="conditionGroup">
                            <g:each in="${conditionGroup.slotConditionSets}" var="conditionSet">
                                <div class="condition">
                                    <g:each in="${conditionSet.slotConditions}" var="condition">
                                        <g:slotConditionEntry condition="${condition}"/>
                                    </g:each>
                                </div>
                            </g:each>
                        </g:each>
                    </div>
                    <hr>
                </g:if>

                <g:render template="/templates/payments/paymentMethod" model="${paymentMethodsModel}" />
            </div>

            <div class="modal-footer">
                <button class="btn btn-md btn-default" data-dismiss="modal" aria-hidden="true"><g:message code="button.cancel.label"/></button>
                <g:render template="/templates/payments/submitBtnAndHandler"
                        model="[targetController: 'couponPayment', facility: coupon.facility]"/>
            </div>
        </g:form>
    </div>
</div>

<r:script>
    $(document).ready(function () {
        $("[rel='tooltip']").tooltip();
    });
</r:script>
</body>
</html>
