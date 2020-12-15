<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3><g:message code="facilityBooking.facilityMoveBookingForm.message1"/></h3>
    <div class="clearfix"></div>
</div>
<g:form name="moveBookingForm" class="no-margin" controller="facilityBooking" action="move">
    <g:if test="${bookingSlot && slot}">
    <div class="modal-body">
        <g:hiddenField name="returnUrl" value="${params.returnUrl ?: ''}"/>
        <g:hiddenField name="date" value="${params.date}"/>
        <g:hiddenField name="bookingSlotId" value="${bookingSlot.id}"/>
        <g:hiddenField name="slotId" value="${slot.id}"/>

        <g:if test="${moveError}">
            <div class="alert alert-error">${moveError}</div>
        </g:if>

        <div class="well">
            <p class="no-bottom-margin">
                <g:message code="facilityBooking.facilityMoveBookingForm.message3" args="[bookingSlot.booking.customer]"/>
                <g:if test="${bookingSlot.booking.group}">(${bookingSlot.booking.group.type.name})</g:if>
                <g:if test="${!slot.booking}"><g:message code="facilityBooking.facilityMoveBookingForm.message8"/>:</g:if>
            </p>
            <h3 class="no-margin-padding"><g:slotCourtAndTime slot="${bookingSlot}"/> </h3>

            <hr>
            <p class="no-bottom-margin">
                <g:if test="${slot.booking}">
                    <g:message code="facilityBooking.facilityMoveBookingForm.message4"/>
                    <g:message code="facilityBooking.facilityMoveBookingForm.message7"/>:
                </g:if>
                <g:else><g:message code="facilityBooking.facilityMoveBookingForm.message2"/>:</g:else>
            </p>
            <h3 class="no-margin-padding">
                <g:if test="${slot.booking}">
                    <g:message code="facilityBooking.facilityMoveBookingForm.message3" args="[slot.booking.customer]"/>
                    <g:if test="${slot.booking.group}">(${slot.booking.group.type.name})</g:if>
                </g:if>
                <g:slotCourtAndTime slot="${slot}"/>
            </h3>
        </div>
        <g:if test="${!moveError}">
            <g:textArea class="span6" name="message" id="message" value="" rows="2" cols="40" placeholder="${message(code: 'facilityBooking.facilityBookingCancel.message6')}"/>
            <label class="checkbox">
                <g:checkBox name="notify" value="${true}"/><g:message code="facilityBooking.facilityMoveBookingForm.message6"/> <g:if test="${slot.booking}"><g:message code="facilityBooking.facilityMoveBookingForm.message9"/></g:if><g:else><g:message code="facilityBooking.facilityMoveBookingForm.message10"/></g:else>
            </label>
        </g:if>
    </div>
    <div class="modal-footer">
        <div class="pull-left">
            <g:if test="${!moveError}">
                <g:submitButton onclick="moveBooking.stopMove()" name="submit" id="formSubmit" value="${message(code: "button.move.label")}"
                                class="btn btn-md btn-success" tabindex="9"/>
            </g:if>
            <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-danger"><g:message code="button.cancel.label" default="Avbryt"/></a>
        </div>
    </div>
    </g:if>
    <g:else>
        <div class="alert alert-error"><g:message code="facilityBooking.facilityMoveBookingForm.error"/></div>
    </g:else>
</g:form>
<script type="text/javascript">
    $(document).ready(function() {
        Mousetrap.bindGlobal('y', function(e) {
            $("#formSubmit").trigger("click");
        });
    });
</script>