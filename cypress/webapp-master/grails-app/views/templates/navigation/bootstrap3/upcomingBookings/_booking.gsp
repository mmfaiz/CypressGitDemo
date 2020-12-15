<%@ page import="com.matchi.Booking" %>
<g:set var="booking" value="${reservation as Booking}" />
<li>
    <g:remoteLink class="userCancelBooking" action="cancelConfirm" controller="userBooking"
                  update="userBookingModal"
                  onSuccess="showLayer('userBookingModal')"
                  params="[slotId:booking.slot.id]">
        <div class="media">
            <div class="media-left">
                <div class="avatar-square-xs text-lg text-center">
                    <i class="fa fa-calendar fa-lg"></i>
                </div>
            </div>
            <div class="media-body">
                <h5 class="media-heading">${booking.slot.court.facility.encodeAsHTML()}</h5>
                ${booking.slot.court.name.encodeAsHTML()}
            </div>
            <div class="media-right">
                <g:formatDate format="dd/MM HH:mm" date="${booking.slot.startTime}" />
            </div>
        </div>
    </g:remoteLink>
</li>