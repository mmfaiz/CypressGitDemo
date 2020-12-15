<%@ page import="com.matchi.Booking" %>
<g:set var="booking" value="${upcomingReservation as Booking}" />
<div class="col-xs-6 top-padding10">
    <h6 class="media-heading">
        ${booking.slot.court.facility.name}
    </h6>
    <small class="block top-margin5">
        ${booking.slot.court.name}
    </small>
    <div class="top-margin5">
        <ul class="list-inline text-sm">
            <!-- CANCEL BOOKING -->
            <li class="text-xs">
                <g:remoteLink action="cancelConfirm" controller="userBooking"
                              update="userBookingModal"
                              onSuccess="showLayer('userBookingModal')"
                              params="[slotId:booking.slot.id,
                                       returnUrl: g.createLink(absolute: false, action: 'home')]">
                    <i class="fas fa-times"></i> <g:message code="button.unbook.label"/>
                </g:remoteLink>
            </li>
        </ul>
    </div>
</div><!-- /.col-xs-6 -->
<div class="col-xs-4 text-right">
    <h6 class="top-margin10 bottom-margin5 text-right">
        <g:humanDateFormat date="${new org.joda.time.DateTime(booking.slot.startTime)}"/>
    </h6>
    <small class="block">
        <g:formatDate format="HH:mm" date="${booking.slot.startTime}" />
    </small>
</div><!-- /.col-xs-4 -->