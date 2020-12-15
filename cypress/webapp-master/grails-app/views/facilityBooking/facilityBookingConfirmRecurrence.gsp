<%@ page import="com.matchi.Customer; com.matchi.User; org.joda.time.DateTime" %>
<%
    def subheadline = ""
    def index = 0
    bookings.each {
        subheadline += it.slot.court.name + ", " + new DateTime(it.slot.startTime).toString("yyyy-MM-dd HH:mm")

        (index != bookings.size() - 1) ? subheadline += " :: " : ""
        index++
    }
%>

<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3><g:message code="facilityBooking.facilityBookingConfirmRecurrence.message1"/></h3>
    <span class="ellipsis pull-left" style="width: 520px;">${subheadline}</span>
    <div class="clearfix"></div>
</div>
<g:form controller="facilityBooking" action="book" name="bookingForm" class="no-margin">
    <div class="modal-body">
        <g:hiddenField name="date" value="${date}"/>

        <g:hiddenField name="customerId" value="${cmd?.customerId}"/>
        <g:hiddenField name="trainerId" value="${cmd?.trainerId}"/>
        <g:hiddenField name="email" value="${cmd?.email}"/>
        <g:hiddenField name="comments" value="${cmd?.comments}"/>
        <g:hiddenField name="type" value="${cmd?.type}"/>
        <g:hiddenField name="telephone" value="${cmd?.telephone}"/>

        <g:hiddenField name="facilityId" value="${customerBookingCommand?.facilityId}"/>
        <g:hiddenField name="firstname" value="${customerBookingCommand?.firstname}"/>
        <g:hiddenField name="lastname" value="${customerBookingCommand?.lastname}"/>
    
        <g:hiddenField name="slotId" value="${cmd?.slotId}"/>
        <g:hiddenField name="newMember" value="${cmd?.newMember}"/>
        <g:hiddenField name="memberType" value="${cmd?.memberType?.id}"/>
        <g:hiddenField name="startDate" value="${cmd?.startDate}"/>
        <g:hiddenField name="endDate" value="${cmd?.endDate}"/>
        <g:hiddenField name="gracePeriodEndDate" value="${cmd?.gracePeriodEndDate}"/>
        <g:hiddenField name="startingGracePeriodDays" value="${cmd?.startingGracePeriodDays}"/>
        <g:hiddenField name="membershipPaid" value="${cmd?.membershipPaid}"/>
        <g:hiddenField name="membershipCancel" value="${cmd?.membershipCancel}"/>
        <g:hiddenField name="newCustomer" value="${cmd?.newCustomer}"/>
        <g:hiddenField name="paid" value="${cmd?.paid}"/>
        <g:hiddenField name="useCoupon" value="${cmd?.useCoupon}"/>
        <g:hiddenField name="useGiftCard" value="${cmd?.useGiftCard}"/>
        <g:hiddenField name="showComment" value="${cmd?.showComment}"/>
        <g:hiddenField name="useRecurrence" value="${cmd?.useRecurrence}"/>
        <g:hiddenField name="sendNotification" value="${cmd?.sendNotification}"/>
        <g:hiddenField name="customerCouponId" value="${cmd?.customerCouponId}"/>
    
        <g:hiddenField name="recurrenceStart" value="${cmd?.recurrenceStart}"/>
        <g:hiddenField name="recurrenceEnd" value="${cmd?.recurrenceEnd}"/>
        <g:each in="${cmd.weekDays}">
            <g:hiddenField name="weekDays" value="${it}"/>
        </g:each>
        <g:hiddenField name="frequency" value="${cmd?.frequency}"/>
        <g:hiddenField name="interval" value="${cmd?.interval}"/>
        <g:hiddenField name="activateMpc" value="${cmd?.activateMpc}"/>

        <fieldset>
            <div class="control-group">
                <h3><g:slotCourtAndTime slot="${recurringSlots.freeSlots[0]}"/>
                    <g:if test="${recurringSlots.freeSlots.size() > 1}">
                        <small> <g:message code="facilityBooking.facilityBookingConfirmRecurrence.message2"
                                args="[recurringSlots.freeSlots.size()]"/></small>
                    </g:if>
                </h3>
            </div>
    
            <div id="booking-customer" class="control-group">
                <div class="controls well" style="padding: 15px">
                    <% def customer = customer ?: new Customer(email: cmd?.email, firstname: customerBookingCommand?.firstname, lastname: customerBookingCommand?.lastname) %>
                    <g:bookingFormCustomer customer="${customer}"/>
                    <div class="clearfix"></div>
                    <hr>
                    <g:bookingRecurrenceInfo start="${cmd?.recurrenceStart}" end="${cmd?.recurrenceEnd}" weekDays="${cmd?.weekDays}" frequency="${cmd?.frequency}" interval="${cmd?.interval}" />
                </div>
            </div>
    
            <g:if test="${recurringSlots.unavailableSlots.size() > 0}">
                <div class="alert alert-error no-margin">
                    <h4 class="alert-heading">
                        <g:message code="facilityBooking.facilityBookingConfirmRecurrence.message3"/>
                        <small><g:message code="facilityBooking.facilityBookingConfirmRecurrence.message4"
                                args="[recurringSlots.unavailableSlots.size()]"/></small>
                    </h4>
                </div>
            </g:if>

            <g:if test="${players}">
                <div id="players-wrapper" class="controls well" style="padding: 15px">
                    <a href="javascript:void(0)" onclick="togglePlayers();">
                        <h6>
                            <i class="icon-user"></i>
                            <g:message code="player.label.plural"/>
                            <i id="players-toggle-marker" class="icon-chevron-right"></i>
                        </h6>
                    </a>
                    <div id="players-wrapper-forms" style="display: none;">
                        <div class="space10"></div>
                        <ol>
                            <g:each in="${players}" var="player">
                                <li>
                                    <g:if test="${player.number}">
                                        ${player.toString().encodeAsHTML()}
                                    </g:if>
                                    <g:else>
                                        <g:message code="player.unknown.label"/>
                                        <input type="hidden" name="unknownPlayer" value="true"/>
                                    </g:else>
                                    <input type="hidden" name="playerCustomerId" value="${player.id}"/>
                                </li>
                            </g:each>
                        </ol>
                    </div>
                </div>
            </g:if>
        </fieldset>
    </div>
    <div class="modal-footer">
        <div class="pull-left">
            <g:submitButton id="submit" name="book" value="${message(code: "button.confirm.label")}" class="btn btn-success"/>
            <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-danger"><g:message code="button.cancel.label" /></a>
        </div>
    </div>
</g:form>
<script type="text/javascript">
    $(document).ready(function() {
        $('#bookingForm').preventDoubleSubmission({});

        <g:if test="${recurringSlots.freeSlots.size() > 1}">
            $("#bookings").popover({delay: 300, title: "${message(code: 'default.booking.plural')}", content: "" +
                "<table class=table style='font-size: 12px; color: #2c2c2c;'>" +
                <g:each in="${recurringSlots.freeSlots}" var="slot">
                "<tr><td><g:slotCourtAndTime slot="${slot}"/></td></tr>" +
                </g:each>
                "</table>",
            trigger: "hover",
            placement: "bottom"
            });
        </g:if>

        <g:if test="${recurringSlots.unavailableSlots.size() > 0}">
            $("#unavailableSlots").popover({delay: 300, title: "${message(code: 'facilityBooking.facilityBookingConfirmRecurrence.message6')}", content: "" +
                "<table class=table style='font-size: 12px; color: #2c2c2c;'>" +
                <g:each in="${recurringSlots.unavailableSlots}" var="slot">
                "<tr><td><g:slotCourtAndTime slot="${slot}"/></td></tr>" +
                </g:each>
                "</table>",
            trigger: "hover",
            placement: "top"
            });
        </g:if>
    });

    <g:if test="${players}">
        function togglePlayers() {
            var $toggleMarker = $("#players-toggle-marker");
            var $playerForms = $("#players-wrapper-forms");

            if($toggleMarker.hasClass("icon-chevron-right")) {
                $toggleMarker.removeClass("icon-chevron-right");
                $toggleMarker.addClass("icon-chevron-down");

                $playerForms.show();
            } else {
                $toggleMarker.removeClass("icon-chevron-down");
                $toggleMarker.addClass("icon-chevron-right");

                $playerForms.hide();
            }
        }
    </g:if>
</script>
