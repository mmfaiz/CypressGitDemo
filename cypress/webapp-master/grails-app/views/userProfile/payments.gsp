<%@ page import="com.matchi.activities.ActivityOccasion; com.matchi.adyen.AdyenService; org.joda.time.DateTime; com.matchi.DateUtil; com.matchi.orders.AdyenOrderPayment; java.text.SimpleDateFormat; com.matchi.User; com.matchi.orders.OrderPayment; com.matchi.orders.Order; com.matchi.membership.Membership" %>
<html>
<head>
    <meta name="layout" content="b3main" />
    <meta name="showFacebookNagger" content="${true}"/>
    <title><g:message code="templates.navigation.myActivity"/> - MATCHi</title>
</head>
<body>
<section class="block block-white vertical-padding30">
    <div class="container">
        <h2 class="page-header no-top-margin"><i class="fas fa-list"></i> <g:message code="templates.navigation.myActivity"/></h2>

        <g:if test="${recurringMemberships}">
            <h4 class="page-header">
                <g:message code="userProfile.activity.upcomingPayments"/>
            </h4>

            <div class="row">
                <div class="col-md-12">
                    <div class="panel panel-default">
                        <header class="panel-heading">
                            <div class="row">
                                <div class="col-sm-4"><g:message code="default.date.place"/></div>
                                <div class="col-sm-2"><g:message code="userProfile.activity.upcomingPayments.amount"/></div>
                                <div class="col-sm-2"><g:message code="userProfile.activity.upcomingPayments.date"/></div>
                                <div class="col-sm-4"><g:message code="userProfile.activity.upcomingPayments.description"/></div>
                            </div>
                        </header>

                        <div class="list-group alt">
                            <g:each in="${recurringMemberships}" var="membership">
                                <div class="list-group-item row">
                                    <div class="col-sm-4">
                                        <div class="media">
                                            <div class="media-left">
                                                <div class="avatar-square-xs avatar-bordered">
                                                    <g:link controller="facility" action="show" params="[name: membership.customer.facility.shortname]">
                                                        <g:fileArchiveFacilityLogoImage file="${membership.customer.facility.facilityLogotypeImage}" alt="${membership.customer.facility.name}"/>
                                                    </g:link>
                                                </div>
                                            </div>
                                            <div class="media-body">
                                                <h6 class="media-heading">
                                                    <g:link controller="facility" action="show" params="[name: membership.customer.facility.shortname]">${membership.customer.facility.name}</g:link>
                                                </h6>
                                                <span class="block text-sm text-muted"><i class="fas fa-map-marker"></i> ${membership.customer.facility.municipality}</span>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="col-sm-2">
                                        <strong><g:formatMoney value="${membership.recurringPrice}"
                                                               facility="${membership.customer.facility}"/></strong><br/>
                                        <span class="block text-sm text-muted">
                                            <g:message code="payment.method.CREDIT_CARD_RECUR"/>
                                        </span>
                                    </div>

                                    <div class="col-sm-2">
                                        <strong><g:formatDate date="${membership.endDate.toDate()}"
                                                              formatName="date.format.daterangepicker"/></strong>
                                    </div>

                                    <div class="col-sm-4">
                                        <g:message code="membership.label"/>:
                                        <strong>${membership.type.name}</strong><br/>
                                        <g:message code="membership.buy.valid.label"/>:
                                        <g:set var="upcomingMembership" value="${Membership.newInstanceWithDates(membership.type, membership.customer.facility, membership.endDate.plusDays(1))}"/>
                                        <strong><g:formatDate date="${upcomingMembership.startDate.toDate()}" formatName="date.format.daterangepicker.short"/>
                                        -
                                            <g:formatDate date="${upcomingMembership.endDate.toDate()}" formatName="date.format.daterangepicker.short"/></strong>

                                        <g:if test="${membership.type.recurring}">
                                            <br/>
                                            <g:message code="membership.buy.type.label"/>:
                                            <strong>${message(code: 'membershipType.paidOnRenewal.recurring.tooltip')}</strong>
                                        </g:if>
                                    </div>
                                </div>
                            </g:each>
                        </div>
                    </div>
                </div>
            </div>
        </g:if>

        <h4 class="page-header"><g:message code="userProfile.activity.message8"/></h4>

        <div class="row">
            <div class="col-md-12">
                <div class="panel panel-default">
                    <header class="panel-heading">
                        <div class="row">
                            <div class="col-sm-2"><g:message code="default.reference.label"/></div>
                            <div class="col-sm-3"><g:message code="default.description.label"/></div>
                            <div class="col-sm-3"><g:message code="default.payment.label"/></div>
                            <div class="col-sm-2"><g:message code="default.payment.method.label"/></div>
                            <div class="col-sm-2"><g:message code="default.receipt.label"/></div>
                        </div>
                    </header>
                    <div class="list-group alt">
                        <g:each in="${orders}" var="order">
                            <g:if test="${order.status != Order.Status.CANCELLED && !order.payments.every { it.status == OrderPayment.Status.NEW }}">
                                <div class="list-group-item row">
                                    <div class="col-sm-2">
                                        <strong>${order.id}</strong>
                                    </div>
                                    <div class="col-sm-3">
                                        <strong>
                                            <g:message code="order.article.${order.article}"/>
                                        </strong><br>
                                        <p class="text-sm">
                                            ${order.description}
                                        </p>
                                    </div>
                                    <g:each in="${order.payments}">
                                        <div class="col-sm-3">

                                            <ul class="list-table">
                                                <li>
                                                    <g:if test="${it.status != OrderPayment.Status.NEW}">
                                                        <g:if test="${it.type == 'Netaxept'}">
                                                            <g:if test="${it.status == OrderPayment.Status.CREDITED}">
                                                                <strong><g:formatMoney value="${it.amount}" forceZero="true" facility="${order?.facility}"/></strong> <span class="text-sm"><g:message code="userProfile.payments.wasWithdrawn" args="[it.dateCreated.format(message(code: 'date.format.dateOnly'))]" /></span><br/>
                                                                <strong><g:formatMoney value="${it.credited}" forceZero="true" facility="${order?.facility}"/></strong> <span class="text-sm"><g:message code="userProfile.payments.wasReturnedAfterCancellation" args="[it.lastUpdated.format(message(code: 'date.format.dateOnly'))]" /></span>
                                                            </g:if>
                                                            <g:elseif test="${it.status == OrderPayment.Status.CAPTURED}">
                                                                <strong><g:formatMoney value="${it.amount}" facility="${order?.facility}" forceZero="true"/></strong> <span class="text-sm"><g:message code="userProfile.payments.wasWithdrawn" args="[it.dateCreated.format(message(code: 'date.format.dateOnly'))]" /></span>
                                                            </g:elseif>
                                                        </g:if>
                                                        <g:elseif test="${it.type == 'Adyen'}">
                                                            <% AdyenOrderPayment adyenOrderPayment = it as AdyenOrderPayment %>
                                                            <g:if test="${it.status == OrderPayment.Status.AUTHED}">
                                                                <strong><g:formatMoney value="${it.amount}" forceZero="true" facility="${order?.facility}"/></strong> <span class="text-sm"><g:message code="userProfile.payments.isReservedSince" args="[it.dateCreated.format(message(code: 'date.format.dateOnly'))]"  /><br>
                                                                <g:message code="userProfile.payments.willBeWithdrawn" args="[adyenOrderPayment.getPredictedDateOfCapture().format(message(code: 'date.format.dateOnly'))]" /></span>
                                                            </g:if>
                                                            <g:elseif test="${it.status == OrderPayment.Status.CAPTURED && order.status == Order.Status.ANNULLED && !it.credited}">
                                                                <g:set var="cancelLimit" value="${ActivityOccasion.get(order?.metadata?.activityOccasionId)?.activity?.cancelLimitWithFallback ?: order?.facility?.getBookingCancellationLimit()}" />
                                                                <strong><g:formatMoney value="${it.amount}" forceZero="true"/></strong> <span class="text-sm"><g:message code="userProfile.payments.wasWithdrawnNoRefund" args="[cancelLimit]"/></span>
                                                                <g:inputHelp title="${message(code: 'userProfile.payments.wasWithdrawnNoRefundHint', args: [cancelLimit])}"/>
                                                            </g:elseif>
                                                            <g:elseif test="${it.status == OrderPayment.Status.CAPTURED && order.status == Order.Status.ANNULLED ||
                                                                    it.status == OrderPayment.Status.CREDITED && order.status == Order.Status.ANNULLED &&
                                                                    (order.dateDelivery.before(order.dateCreated.plus(AdyenService.MAX_WAIT_CAPTURE_DAYS)))}">
                                                                <%
                                                                    // Date where we returned reserved money immediately on cancellation
                                                                    String code = "userProfile.payments.wasWithdrawnUponCancellation"
                                                                    if(it?.lastUpdated?.before(new org.joda.time.DateTime(com.matchi.adyen.AdyenService.DATE_OF_PAYMENT_LOGIC_CHANGE).toDate())) {
                                                                        code += "Old"
                                                                    }
                                                                %>
                                                                <strong><g:formatMoney value="${it.amount - it.credited}" forceZero="true"/></strong> <span class="text-sm"><g:message code="${code}" args="[it.lastUpdated.format(message(code: 'date.format.dateOnly')), formatMoney(value: it.credited, forceZero: true)]" /></span>
                                                                <g:inputHelp title="${message(code: 'userProfile.payments.wasWithdrawnUponCancellationHint', args: [formatMoney(value: it.amount - it.credited, forceZero: true)])}"/>
                                                            </g:elseif>
                                                            <g:elseif test="${it.status == OrderPayment.Status.CAPTURED && order.status == Order.Status.COMPLETED}">
                                                                <strong><g:formatMoney value="${it.amount}" facility="${order?.facility}"/></strong> <span class="text-sm"><g:message code="userProfile.payments.wasWithdrawn" args="[adyenOrderPayment.getPredictedDateOfCapture().format(message(code: 'date.format.dateOnly'))]" /></span>
                                                            </g:elseif>
                                                            <g:elseif test="${it.status == OrderPayment.Status.CREDITED && order.status == Order.Status.COMPLETED}">
                                                                <strong><g:formatMoney value="${it.amount}" facility="${order?.facility}"/></strong> <span class="text-sm"><g:message code="userProfile.payments.wasWithdrawn" args="[adyenOrderPayment.getPredictedDateOfCapture().format(message(code: 'date.format.dateOnly'))]" /></span> <br/>
                                                                <strong><g:formatMoney value="${it.credited}" facility="${order?.facility}"/></strong> <span class="text-sm"><g:message code="userProfile.payments.wasReturnedAfterCredit" args="[it.lastUpdated.format(message(code: 'date.format.dateOnly'))]" /></span>
                                                            </g:elseif>
                                                            <g:elseif test="${it.status == OrderPayment.Status.CREDITED && order.status == Order.Status.ANNULLED}">
                                                                <strong><g:formatMoney value="${it.amount}" facility="${order?.facility}"/></strong> <span class="text-sm"><g:message code="userProfile.payments.wasWithdrawnAfter7Days" args="[adyenOrderPayment.getPredictedDateOfCapture().format(message(code: 'date.format.dateOnly'))]" /></span> <br/>
                                                                <strong><g:formatMoney value="${it.credited}" facility="${order?.facility}"/></strong> <span class="text-sm"><g:message code="userProfile.payments.wasReturnedAfterCancellation" args="[it.lastUpdated.format(message(code: 'date.format.dateOnly'))]" /></span>
                                                                <g:inputHelp title="${message(code: 'userProfile.payments.wasWithdrawnAfter7DaysHint')}"/>
                                                            </g:elseif>
                                                        </g:elseif>
                                                        <g:else>

                                                        </g:else>
                                                    </g:if>
                                                </li>
                                            </ul>
                                        </div>
                                        <div class="col-sm-2">
                                            <g:if test="${it.method}">
                                                <strong>${message(code:"payment.method.${it.method}")}</strong>
                                            </g:if>
                                            <g:else>
                                                <i class="fa fa-<g:message code="order.payment.type.icon.${it.getType()}"/>"></i>
                                            </g:else>
                                        </div>
                                    </g:each>
                                    <div class="col-sm-2">
                                        <g:if test="${order.getTotalAmountPaid() > 0}">
                                            <g:link action="printReceipt" id="${order?.id}" target="_blank" class="btn btn-sm btn-info"><i class="fas fa-print fa-fw"></i> <g:message code="button.print.label"/></g:link>
                                        </g:if>
                                    </div>
                                </div>
                            </g:if>
                        </g:each>
                    </div>
                    <div class="text-center">
                        <g:b3PaginateTwitterBootstrap next="&raquo;" prev="&laquo;" class="pagination-centered"
                                                      maxsteps="0" max="${10}" params="${params}"
                                                      action="payments" total="${orders?.getTotalCount()}" />
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="space-40"></div>
</section>

<r:script>
    $(document).ready(function() {
        $("*[rel=tooltip]").tooltip();

        if(!getCookie("hideFacebookNagger")) {
            $("#fbConnect").show();
        }
    });
</r:script>
</body>
</html>