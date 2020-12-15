<%@ page import="com.matchi.Court;" %>

<table class="facility-schedule" width="100%" border="0" cellpadding="0" cellspacing="0">
    <thead>
    <tr>
        <th class="court" width="80">&nbsp;</th>
        <g:each in="${timeSpans}" var="span">
            <th>
                ${span.getStartTimeFormatted("HH")}
            </th>
        </g:each>
    </tr>
    </thead>
    <tbody>
    <g:if test="${courtGroup}">
        <g:each in="${courtGroup.courts.findAll {!it.archived}.sort {it.listPosition}}" var="court" >
            <g:render template="/templates/bookingSchedules/facilityDailyScheduleRow" model="[court: court, timeSpans: timeSpans, schedule: schedule ]"/>
        </g:each>
    </g:if>
    <g:else>
        <g:each in="${Court.available(facility).list()}" var="court">
            <g:render template="/templates/bookingSchedules/facilityDailyScheduleRow" model="[court: court, timeSpans: timeSpans, schedule: schedule ]"/>
        </g:each>
    </g:else>
    </tbody>
</table>

