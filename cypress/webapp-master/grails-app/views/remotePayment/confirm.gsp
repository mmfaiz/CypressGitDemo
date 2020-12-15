<%@ page import="org.joda.time.DateTime; com.matchi.payment.PaymentMethod; com.matchi.coupon.CustomerCoupon" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="popup" />
</head>
<body>
<div class="modal-dialog">
    <div class="modal-content">
        <g:form name="confirmForm" class="no-margin">
            <g:hiddenField name="orderId" value="${order.id}"/>

            <div class="modal-header flex-center separate">
                <h4 class="modal-title"><g:message code="remotePayment.confirm.title"/></h4>
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times-circle"></i></button>
            </div>

            <div class="modal-body">

                <g:b3StaticFlashError/>
                <g:b3StaticErrorMessage bean="${command}"/>

                <div class="row">
                    <div class="col-sm-12">
                        <p>
                            <span class="fa-stack">
                                <i class="fas fa-circle fa-stack-2x"></i>
                                <i class="fa fa-calendar fa-stack-1x fa-inverse"></i>
                            </span> <strong><g:message code="default.facility.label" />:</strong> ${order.facility.name}
                        </p>
                    </div>
                </div>
                <div class="row">
                    <div class="col-sm-12">
                        <p>
                            <g:if test="${memberships}">
                                <span class="fa-stack">
                                    <i class="fas fa-circle fa-stack-2x"></i>
                                    <i class="fa fa-id-card-o fa-stack-1x fa-inverse"></i>
                                </span>

                                <g:set var="membershipTypeText" value="${!memberships[0].type?.recurring ? "" :  ", " + message(code: 'membership.buy.type.label') + ": " + message(code: 'membershipType.paidOnRenewal.recurring.tooltip')}" />
                                <g:if test="${memberships.size() > 1}">
                                    <strong><g:message code="remotePayment.familyMembership.title"/>:</strong>
                                    <g:each in="${memberships}" var="m">
                                        <p class="left-padding30">
                                            &#160;${m.customer.fullName()} -
                                            <g:message code="remotePayment.description.${order.article.name()}"
                                                    args="[m.type?.name, m.startDate.toDate(), m.endDate.toDate(), membershipTypeText]"/>
                                        </p>
                                    </g:each>
                                </g:if>
                                <g:else>
                                    <strong><g:message code="order.article.${order.article.name()}"/>:</strong>
                                    <g:message code="remotePayment.description.${order.article.name()}"
                                            args="[memberships[0].type?.name, memberships[0].startDate.toDate(), memberships[0].endDate.toDate(), membershipTypeText]"/>
                                </g:else>
                            </g:if>
                            <g:else>
                                <span class="fa-stack">
                                    <i class="fas fa-circle fa-stack-2x"></i>
                                    <i class="fa fa-clock-o fa-stack-1x fa-inverse"></i>
                                </span> <strong><g:message code="order.article.${order.article.name()}" />:</strong> ${order.description}.
                            </g:else>
                        </p>
                    </div>
                </div>
                <div class="row">
                    <div class="col-sm-12">
                        <p>
                            <span class="fa-stack">
                                <i class="fas fa-circle fa-stack-2x"></i>
                                <i class="fas fa-credit-card fa-stack-1x fa-inverse"></i>
                            </span>
                            <span id="bookingPrice" rel="tooltip">
                                <strong><g:message code="default.price.label" />:</strong> <g:formatMoney value="${order.total()}" facility="${facility}" />
                            </span>
                        </p>
                    </div>
                </div>

                <hr>
                <g:render template="/templates/payments/paymentMethod" model="${paymentMethodsModel}"/>
            </div>

            <div class="modal-footer">
                <button class="btn btn-md btn-default" data-dismiss="modal" aria-hidden="true"><g:message code="button.cancel.label"/></button>
                <g:render template="/templates/payments/submitBtnAndHandler"
                          model="[targetController: 'remotePayment', facility: facility]"/>
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
