<%@ page import="com.matchi.enums.BookingGroupType; com.matchi.payment.PaymentMethod; org.joda.time.DateTime" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="popup" />
    <title><g:message code="userBooking.cancel.message1"/></title>
</head>
<body>
<div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only"><g:message code="button.close.label"/></span></button>
            <h4 class="modal-title"><g:message code="userBooking.showByTicket.heading"/> <strong>${slot.court.facility.name}</strong></h4>
        </div>
        <div class="modal-body">
            <h2 class="h3"><g:message code="userBooking.showByTicket.booking"/></h2>

            <g:if test="${accessCode}">
                <h1 class="h3">
                    <p><g:message code="facilityAccessCode.content.label"/>: <strong>${accessCode}</strong></p>
                </h1>
            </g:if>

            <div class="row">
                <div class="col-sm-6">
                    <h1 class="h6 weight400">
                        <span class="fa-stack text-success">
                            <i class="fas fa-circle fa-stack-2x"></i>
                            <i class="fas fa-flag fa-stack-1x fa-inverse"></i>
                        </span> ${slot.court.name}
                    </h1>
                </div>
                <div class="col-sm-6">
                    <h2 class="h6 weight400">
                        <span class="fa-stack">
                            <i class="fas fa-circle fa-stack-2x"></i>
                            <i class="fas fa-map-marker fa-stack-1x fa-inverse"></i>
                        </span>
                        ${slot.court.facility.name}
                    </h2>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-6">
                    <h3 class="h6 weight400">
                        <span class="fa-stack">
                            <i class="fas fa-circle fa-stack-2x"></i>
                            <i class="fa fa-calendar fa-stack-1x fa-inverse"></i>
                        </span> <span class="text-muted"><g:message code="bookingService.date"/> :</span> <g:humanDateFormat date="${new org.joda.time.DateTime(slot.startTime)}"/>
                    </h3>
                </div>
                <div class="col-sm-6">
                    <h3 class="h6 weight400">
                        <span class="fa-stack">
                            <i class="fas fa-circle fa-stack-2x"></i>
                            <i class="fa fa-clock-o fa-stack-1x fa-inverse"></i>
                        </span> <span class="text-muted"><g:message code="facilityStatistic.payment.message13"/> :</span> <g:formatDate format="HH:mm" date="${slot.startTime}" />-<g:formatDate format="HH:mm" date="${slot.endTime}" />
                    </h3>
                </div>
            </div>
            <hr/>

            <!-- IF PAYMENT IS MADE WITH CASH -->
            <g:if test="${payment && !payment.isCash()}">

                <h2 class="h3"><g:message code="payment.label"/></h2>
                <p><g:paymentShortSummary payment="${payment}"/> </p>

                <g:if test="${authCancel}">
                    <h3 class="h5"><g:message code="userBooking.cancel.message7"/></h3>
                    <strong><g:message code="userBooking.cancel.message8"/></strong><br>
                    <g:if test="${payment.isRefundable()}">
                        <g:if test="${payment.method.equals(PaymentMethod.COUPON)}">
                            <i class="fas fa-info-circle"></i> <g:message code="payment.refund.coupon"/>
                        </g:if>
                        <g:else>
                            <i class="fas fa-info-circle"></i> <g:message code="payment.refund.servicefee"
                                    args="[serviceFeeValue(currency: payment.facility.currency)]"/>
                        </g:else>
                    </g:if>

                </g:if>
                <hr>
            </g:if>

        <!-- IF PAYMENT IS MADE WITH COUPON OR CREDIT CARD -->
            <g:if test="${slot?.booking?.order?.hasPayments()}">
                <h2 class="h3"><g:message code="payment.label"/></h2>
                <g:paymentShortSummary order="${slot?.booking?.order}"/>
                <hr>
            </g:if>

            <g:if test="${slot?.booking?.players?.size() > 1}">
                <h2 class="h3"><g:message code="player.label.plural"/></h2>
                <ul class="list-inline">
                    <g:bookingPlayers players="${slot.booking.players}" var="playerName">
                        <li><i class="fas fa-user"></i> ${playerName}</li>
                    </g:bookingPlayers>
                </ul>
                <hr>
            </g:if>


            <!-- IF PAYMENT IS MADE WITH CASH -->
            <g:if test="${payment && !payment.isCash()}">
                <h2 class="h3"><g:message code="userBooking.showByTicket.refund"/></h2>
                <g:if test="${!authCancel}">
                    <g:message code="userBooking.cancel.message14"/>
                </g:if>
                <hr/>
            </g:if>
            <!-- IF PAYMENT IS MADE WITH COUPON OR CREDIT CARD -->
            <g:if test="${slot?.booking?.order?.hasRefundablePayment() && slot?.booking?.group?.type != BookingGroupType.SUBSCRIPTION}">
                <h2 class="h3"><g:message code="userBooking.showByTicket.refund"/></h2>
                <g:refundTerms slot="${slot}"/>
                <hr/>
            </g:if>

            <!-- IF THIS IS A SUBSCRIPTION BOOKING -->
            <g:if test="${redeemBooking}">
                <h2 class="h3"><g:message code="userBooking.showByTicket.refund"/></h2>
                <g:message code="subscriptionRedeem.redeemAt.${slot.court.facility.subscriptionRedeem.redeemAt}.${slot.court.facility.subscriptionRedeem.strategy.getType()}.userCancel"/>
                <hr/>
            </g:if>

            <!-- CONTACT FACILITY INFO -->
            <h2 class="h3"><g:message code="userBooking.cancel.questions"/></h2>
            <span class="text-sm text-muted">
                <h6><g:message code="userBooking.cancel.contact"/> ${slot.court.facility.name}:</h6>
                <ul class="list-inline">
                    <li>
                        <i class="fas fa-phone"></i> <a href="javascript:void(0)">${slot.court.facility.telephone}</a>
                    </li>
                    <li>
                        <i class="fas fa-envelope"></i> <a href="mailto:${slot.court.facility.email}" target="_blank">${slot.court.facility.email}</a>
                    </li>
                    <li>
                        <i class="fas fa-map-marker"></i> <a href="http://maps.google.com/maps?q=${(slot.court.facility.address + ", " + slot.court.facility.zipcode.encodeAsURL() + " " + slot.court.facility.city).encodeAsURL()}" target="_blank">${slot.court.facility.address},  ${slot.court.facility.zipcode} ${slot.court.facility.city}</a>
                    </li>
                </ul>
            </span>

        </div><!-- /.modal-body -->

        <div class="modal-footer">
            <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-default"><g:message code="button.close.label" default="Stäng"/></a>
            <g:if test="${authCancel}">
                <g:remoteLink action="cancel" controller="userBooking" params="[slotId:slot?.id, returnUrl:returnUrl]" class="btn btn-md btn-danger" update="userBookingModal">
                    <g:message code="button.unbook.label" default="Avboka"/>
                </g:remoteLink>
            </g:if>
            <g:else>
                <button disabled id="noCancel" class="btn btn-md btn-danger"><g:message code="button.unbook.label" default="Avboka"/></button>
            </g:else>
        </div>
    </div><!-- /.modal-content -->
</div><!-- /.modal-dialog -->
</body>
</html>
