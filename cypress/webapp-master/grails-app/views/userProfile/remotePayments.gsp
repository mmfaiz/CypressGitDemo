<%@ page import="com.matchi.adyen.AdyenService; org.joda.time.DateTime; com.matchi.DateUtil; com.matchi.orders.AdyenOrderPayment; java.text.SimpleDateFormat; com.matchi.User; com.matchi.orders.OrderPayment; com.matchi.orders.Order" %>
<html>
<head>
    <meta name="layout" content="b3main"/>
    <meta name="showFacebookNagger" content="${true}"/>
    <title><g:message code="userProfile.remotePayments.label"/> - MATCHi</title>
</head>

<body>
<section class="block block-white vertical-padding30">
    <div class="container">
        <h2 class="page-header no-top-margin"><g:message code="userProfile.remotePayments.label"/></h2>

        <div class="row">
            <div class="col-md-12">
                <p><g:message code="userProfile.remotePayments.description"/></p>
            </div>
        </div>

        <g:if test="${orders}">
            <div class="row">
                <div class="col-md-12">
                    <div class="panel panel-default">
                        <header class="panel-heading">
                            <div class="row row-full">
                                <div class="col-sm-2"></div>

                                <div class="col-sm-6"><g:message code="default.description.label"/></div>

                                <div class="col-sm-4"></div>
                            </div>
                        </header>

                        <div class="list-group alt">
                            <g:each in="${orders}" var="order">
                                <g:set var="facility" value="${order.customer.facility}"/>
                                <g:set var="customer" value="${order.customer}"/>

                                <g:set var="familyPaymentAllowed"
                                       value="${familyMemberships[order.id]?.contactMembership && familyMemberships[order.id]?.contactMembership.id == customer.getUnpaidPayableMembership()?.id && familyMemberships[order.id]?.nonContactMemberships.any { !it.paid }}"/>

                                <g:set var="familyMembershipRequestAllowed"
                                       value="${facility.familyMembershipRequestAllowed && (!familyMemberships[order.id]?.family || familyMemberships[order.id]?.isFamilyContact())}"/>

                                <g:set var="remoteMembership"
                                       value="${familyMemberships[order.id]?.contactMembership}"/>
<g:if test="${remoteMembership}">
                                <div class="list-group-item row row-full">
                                    <div class="col-sm-2">
                                        <g:link controller="facility" action="show" params="[name: facility.shortname]">
                                            <g:fileArchiveFacilityLogoImage file="${facility.facilityLogotypeImage}"
                                                                            alt="${facility.name}"
                                                                            class="img-responsive center-block"
                                                                            height="50"/>
                                        </g:link>
                                    </div>

                                    <div class="col-sm-6">
                                        <strong>
                                            <g:message code="order.article.${order.article}"/>
                                        </strong><br>

                                        <p class="text-sm">
                                            <g:if test="${familyMemberships[order.id]?.nonContactMemberships}">
                                                <g:if test="${familyMemberships[order.id].contactMembership}">
                                                    ${familyMemberships[order.id].contactMembership.customer.fullName()}
                                                    -
                                                    ${familyMemberships[order.id].contactMembership.order.description}
                                                    <br/>
                                                </g:if>

                                                <g:each in="${familyMemberships[order.id].nonContactMemberships}"
                                                        var="m">
                                                    ${m.customer.fullName()}
                                                    -
                                                    ${m.order.description}
                                                    <g:if test="${m.order.isFree()}">
                                                        <span class="text-muted">(<g:message
                                                                code="remotePayment.freeMembership"/>)</span>
                                                    </g:if>
                                                    <g:elseif test="${m.paid}">
                                                        <span class="text-muted">(<g:message
                                                                code="remotePayment.paidMembership"/>)</span>
                                                    </g:elseif>
                                                    <br/>
                                                </g:each>
                                            </g:if>
                                            <g:else>
                                                ${order.description}
                                            </g:else>
                                        </p>
                                    </div>

                                    <div class="col-sm-4">
                                    <g:if test="${familyMemberships[order.id]?.familyPaymentAllowed || (facility.familyMembershipRequestAllowed && remoteMembership)}">
                                        <div class="btn-group full-width">
                                            <button type="button"
                                                    class="btn btn-sm btn-success dropdown-toggle"
                                                    data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                                <g:message code="button.finish.purchase.label"/>
                                                <span class="caret"></span>
                                            </button>
                                            <ul class="dropdown-menu">
                                                <li>
                                                    <g:remoteLink
                                                            controller="remotePayment"
                                                            params="[id: remoteMembership.order.id, finishUrl: createLink([controller: 'facility', action: 'show', absolute: 'true', params: ['name': facility.shortname, 'orderId': remoteMembership.order.id]])]"
                                                            action="confirm"
                                                            update="userBookingModal"
                                                            onFailure="handleAjaxError()"
                                                            onSuccess="showLayer('userBookingModal')">
                                                        <g:if test="${remoteMembership.family?.members?.size() > 1}">
                                                            <g:message code="remotePayment.button.payForMyMembership"/>
                                                        </g:if>
                                                        <g:else>
                                                            <g:message code="remotePayment.button.payForMembership"/>
                                                        </g:else>
                                                    </g:remoteLink>
                                                </li>
                                                <g:if test="${!(familyMembershipRequestAllowed && remoteMembership.family?.members?.size() > 1) && facility?.recieveMembershipRequests}">
                                                    <li>
                                                        <g:link controller="membershipRequest" action="index"
                                                                params="[name: facility.shortname, baseMembership: remoteMembership.id]">
                                                            <g:message code="remotePayment.button.editMembership"/>
                                                        </g:link>
                                                    </li>
                                                </g:if>
                                                <g:if test="${remoteMembership.family?.members?.size() > 1}">
                                                    <li>
                                                        <g:remoteLink
                                                                controller="remotePayment"
                                                                params="[id: remoteMembership.order.id, finishUrl: createLink([controller: 'facility', action: 'show', absolute: 'true', params: ['name': facility.shortname, 'orderId': remoteMembership.order.id]]), familyMembership: true]"
                                                                action="confirm"
                                                                update="userBookingModal"
                                                                onFailure="handleAjaxError()"
                                                                onSuccess="showLayer('userBookingModal')">
                                                            <g:message
                                                                    code="remotePayment.button.payForFamilyMembership"/>
                                                        </g:remoteLink>
                                                    </li>
                                                </g:if>
                                                <g:if test="${familyMembershipRequestAllowed}">
                                                    <li>
                                                        <g:link controller="membershipRequest" action="index"
                                                                params="[name: facility.shortname, baseMembership: remoteMembership.id, applyForFamilyMembership: true]">
                                                            <g:if test="${remoteMembership.family?.members?.size() > 1}">
                                                                <g:message code="remotePayment.button.editFamily"/>
                                                            </g:if>
                                                            <g:else>
                                                                <g:message code="remotePayment.button.createFamily"/>
                                                            </g:else>
                                                        </g:link>
                                                    </li>
                                                </g:if>
                                            </ul>
                                        </div>
                                    </g:if>
                                    <g:else>
                                        <g:remoteLink
                                                class="btn btn-sm btn-success"
                                                controller="remotePayment"
                                                params="[id: order.id, finishUrl: createLink([controller: 'facility', action: 'show', absolute: 'true', params: ['name': facility.shortname, 'orderId': order.id]])]"
                                                action="confirm"
                                                update="userBookingModal"
                                                onFailure="handleAjaxError()"
                                                onSuccess="showLayer('userBookingModal')"><g:message code="button.finish.purchase.label"/></g:remoteLink>
                                    </g:else>
                                    </div>
                                </div>
                                </g:if>
                                <g:else>
                                    <g:render template="remotePaymentBooking" model="[order:order]"/>
                                </g:else>
                            </g:each>
                        </div>
                        <g:if test="${orders?.getTotalCount() > 10}">
                            <div class="text-center">
                                <g:b3PaginateTwitterBootstrap next="&raquo;" prev="&laquo;" class="pagination-centered"
                                                              maxsteps="0" max="${10}" params="${params}"
                                                              action="payments"
                                                              total="${orders?.getTotalCount() ?: 0}"/>
                            </div>
                        </g:if>
                    </div>
                </div>
            </div>
        </g:if>
    </div>

    <div class="space-40"></div>
</section>

<r:script>
    $(document).ready(function() {
        $("*[rel=tooltip]").tooltip();

        if(!getCookie("hideFacebookNagger")) {
             $("#fbConnect").show();
        }

        <g:if test="${paymentFlow}">
    <g:remoteFunction controller="${paymentFlow.paymentController}" action="${paymentFlow.getFinalAction()}"
                      params="${paymentFlow.getModalParams()}" update="userBookingModal"
                      onSuccess="showLayer('userBookingModal')" onFailure="handleAjaxError()"/>
</g:if>
    });
</r:script>
</body>
</html>
