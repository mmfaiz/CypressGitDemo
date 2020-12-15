<%@ page import="com.matchi.enums.RedeemType; com.matchi.payment.PaymentStatus; org.joda.time.DateTime; com.matchi.enums.RefundOption" %>
<%
    def isMultipleBookings = bookings.size() > 1
    def firstBooking = bookings.get(0)
    def headline = (isMultipleBookings ? message(code: 'facilityBooking.facilityBookingCancel.message2') : firstBooking.slot.court.name)
    def hasCashRegisterPayment = false
    def subheadline = ""
    def index = 0
    bookings.each {
        subheadline += it.slot.court.name + ", " + new DateTime(it.slot.startTime).toString("yyyy-MM-dd HH:mm")

        (index != bookings.size() - 1) ? subheadline += " :: " : ""
        index++

        if (!hasCashRegisterPayment) {
            hasCashRegisterPayment = !facility.boxnet ? false : (it.payment?.status == PaymentStatus.PARTLY || it.payment?.status == PaymentStatus.OK)
        }
    }
%>

<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3>${headline}</h3>
    <span class="ellipsis pull-left" style="width: 520px;">${subheadline}</span>
    <div class="clearfix"></div>
</div>
<g:form controller="facilityBooking" action="cancel" name="bookingForm" class="no-margin">
    <div class="modal-body">
        <g:flashError/>
        <g:if test="${hasCashRegisterPayment}">
            <p style="color: red;"><g:message code="facilityBooking.facilityBookingCancel.message1"/></p>
        </g:if>

        <g:hiddenField name="cancelSlotsData" value="${params.cancelSlotsData}"/>
        <g:hiddenField name="returnUrl" value="${params.returnUrl}"/>
        <g:if test="${warnNoRefund}">
            <p><strong class="text-error"><g:message code="facilityBooking.facilityBookingCancel.message19" />: </strong><g:message code="facilityBooking.facilityBookingCancel.message20" /></p>
        </g:if>
        <g:textArea class="span6" name="message" id="message" value="" rows="2" cols="40" placeholder="${message(code: 'facilityBooking.facilityBookingCancel.message6')}"/>
        <g:if test="${optionRedeem}">
            <p>
                <label class="radio">
                    <g:radio name="redeemType" value="${RedeemType.NORMAL}" checked="true"/><g:message code="facilityBooking.facilityBookingCancel.message7"/>
                </label>
                <g:if test="${facility.hasApplicationInvoice() && facility.subscriptionRedeem?.strategy?.type?.equals("INVOICE_ROW")}">
                    <label class="radio">
                        <g:radio name="redeemType" value="${RedeemType.FULL}"/><g:message code="facilityBooking.facilityBookingCancel.message8"/>
                    </label>
                </g:if>
                <label class="radio">
                    <g:radio name="redeemType" value="${RedeemType.EMPTY}"/><g:message code="facilityBooking.facilityBookingCancel.message9"/>
                </label>
            </p>
        </g:if>
        <g:else>
            <g:if test="${allRefundOption}">
                <p>
                    <label class="radio">
                        <g:radio name="refundOption" value="${RefundOption.FULL_REFUND}"/><g:message code="facilityBooking.facilityBookingCancel.message21"/>
                    </label>
                    <label class="radio">
                        <g:radio name="refundOption" value="${RefundOption.CUSTOMER_PAYS_FEE}"/><g:message code="facilityBooking.facilityBookingCancel.message22"/>
                    </label>
                    <label class="radio">
                        <g:radio name="refundOption" value="${RefundOption.NO_REFUND}" checked="true"/><g:message code="facilityBooking.facilityBookingCancel.message23"/>
                    </label>
                </p>
            </g:if>
            <g:if test="${refundOption}">
                <p>
                    <label class="radio">
                        <g:radio name="refundOption" value="${RefundOption.FULL_REFUND}"/><g:message code="facilityBooking.facilityBookingCancel.message21"/>
                    </label>
                    <label class="radio">
                        <g:radio name="refundOption" value="${RefundOption.CUSTOMER_PAYS_FEE}"/><g:message code="facilityBooking.facilityBookingCancel.message22"/>
                    </label>
                </p>
            </g:if>
            <g:if test="${noRefundOption}">
                <p>
                    <label class="radio">
                        <g:radio name="refundOption" value="${RefundOption.NO_REFUND}" checked="true"/><g:message code="facilityBooking.facilityBookingCancel.message23"/>
                    </label>
                </p>
            </g:if>
        </g:else>
        <g:if test="${optionCancelRecurring}">
            <div class="well well-small">
                <p style="font-size: 12px;">
                    <strong>${isMultipleBookings ? message(code: 'facilityBooking.facilityBookingCancel.message10') : message(code: 'facilityBooking.facilityBookingCancel.message11')}.</strong>
                </p>

                <label class="checkbox">
                    <g:checkBox name="removeRecurrence" /><g:message code="facilityBooking.facilityBookingCancel.message12"/> <g:inputHelp title="${message(code: 'facilityBooking.facilityBookingCancel.message13')}"/>
                </label>
            </div>
        </g:if>

        <g:if test="${!bookingsToBeRefunded.isEmpty()}">
            <p>
            <g:if test="${bookingsToBeRefunded.size() == 1}">
                <g:message code="facilityBooking.facilityBookingCancel.message14"
                    args="[paymentSummary(payment: bookingsToBeRefunded.get(0).payment)]"/>
            </g:if><g:else>
                <g:message code="facilityBooking.facilityBookingCancel.message15" args="[bookingsToBeRefunded.size()]"/>
            </g:else>
            </p>
        </g:if>
        <label class="checkbox" for="sendNotification">
            <g:checkBox id="sendNotification" name="sendNotification" tabindex="7" value="${true}"/><g:message code="facilityBooking.facilityBookingCancel.message16"/>
        </label>
    </div>
    <div class="modal-footer">
        <div class="pull-left">
            <button id="cancelBtn" name="book" class="btn btn-md btn-success"><g:message code="button.unbook.label2"/></button>
            <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-danger"><g:message code="button.closewindow.label" /></a>
        </div>
    </div>
</g:form>
<script type="text/javascript">
    $(document).ready(function() {
        $("#bookingForm").preventDoubleSubmission({});
        $("[rel=tooltip]").tooltip();

        Mousetrap.bindGlobal('a', function(e) {
            $("#cancelBtn").trigger("click");
        });
    });

    <g:if test="${!bookingsToBeRefunded.isEmpty()}">
    $("#additionalBookings").popover({delay: 200,title: "${message(code: 'facilityBooking.facilityBookingCancel.message18')}", content: "" +
            "<table class=table style='font-size: 12px; color: #2c2c2c;'>" +
            <g:each in="${bookingsToBeRefunded}" var="booking">
            "<tr><td nowrap><g:slotCourtAndTime slot="${booking.slot}"/><br><g:paymentSummary payment="${booking.payment}"/></td></tr>" +
            </g:each>
            "</table>",
        trigger: "hover",
        placement: "bottom"
    });
    </g:if>
</script>
