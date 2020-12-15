<%@ page import="org.joda.time.DateTime" %>
<div class="row">
    <div class="span2">
        <span class="info-label"><g:message code="templates.booking.facilityBookingRecurrenceInfo.message1"/>:</span> <g:formatDate date="${start.toDate()}" formatName="date.format.dateOnly"/>
    </div>
    <div class="span2">
        <span class="info-label"><g:message code="templates.booking.facilityBookingRecurrenceInfo.message2"/>:</span> <g:formatDate date="${end.toDate()}" formatName="date.format.dateOnly"/>
    </div>
</div>
<div class="row">
    <div class="span5">
        <span class="info-label"><g:message code="templates.booking.facilityBookingRecurrenceInfo.message3"/>:</span>
        <g:each in="${weekDays}" status="i" var="day">
            <g:message code="time.weekDay.${day}"/>${i < weekDays.size()-1 ? ", ":""}
        </g:each>
    </div>
</div>
<div class="row">
    <div class="span2">
        <span class="info-label"><g:message code="templates.booking.facilityBookingRecurrenceInfo.message4"/>:</span> <g:message code="bookingGroupFrequency.name.${frequency}"/>
    </div>
    <div class="span2">
        <span class="info-label"><g:message code="templates.booking.facilityBookingRecurrenceInfo.message5"/>:</span> ${interval} <g:message code="bookingGroupFrequency.interval.${frequency}"/>
    </div>
</div>