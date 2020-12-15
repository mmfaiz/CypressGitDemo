<%@ page import="org.joda.time.DateTime; com.matchi.schedule.TimeSpan; com.matchi.DateUtil; com.matchi.ColorFetcher; com.matchi.Booking; com.matchi.Court" %>
<tr>
    <td class="court">
        <div class="name">
            <g:if test="${court?.facility?.hasCameraFeature() && court.hasCamera()}">
                <i class="fas fa-video"></i>
            </g:if>
            <g:if test="${court.membersOnly}">
                <span class="label label-info" rel="tooltip" title="${message(code: 'adminFacilityCourts.create.restriction.' + Court.Restriction.MEMBERS_ONLY)}">
                    ${court.name}
                </span>
            </g:if>
            <g:elseif test="${court.offlineOnly}">
                <span class="label" rel="tooltip" title="${message(code: 'adminFacilityCourts.create.restriction.' + Court.Restriction.OFFLINE_ONLY)}">
                    ${court.name}
                </span>
            </g:elseif>
            <g:elseif test="${court.isRestrictedByRequirementProfile()}">
                <span class="label label-warning" rel="tooltip" title="${message(code: 'adminFacilityCourts.create.restriction.' + Court.Restriction.REQUIREMENT_PROFILES)}">
                    ${court.name}
                </span>
            </g:elseif>
            <g:else>
                ${court.name}
            </g:else>
        </div>
    </td>
    <td colspan="${timeSpans.size()}" width="40">
        <%
            def start = timeSpans.first().start
            def end   = timeSpans.last().end
            def slots = schedule.getSlots(new TimeSpan(start,end), court)
            def period = end.millis - start.millis // period in millis
        %>
        <table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
            <tr>

                <g:set var="lastSlotEndMillis" value="${start.millis}"/>

                <g:if test="${!slots.isEmpty()}">
                    <g:each in="${slots}" var="slot" status="idx">
                        <%
                            def slotStart = slot.start.millis
                            def slotEnd = slot.end.millis

                            def slotStartPixel = ((slotStart-start.millis) / period) * 100
                            def slotWidthPixel = ((slotEnd-slotStart) / period) * 100
                            def timeDiffToLast = slotStart - lastSlotEndMillis

                            def spaceToEnd = (end.millis - slotEnd)
                        %>

                    <%--
                        Adding a "not available" slot if there is a time
                        difference to last drawed slot (or starttime)
                    --%>
                        <g:if test="${timeDiffToLast > 0}">
                            <g:scheduleSlot width="${((slotStart-lastSlotEndMillis) / period) * 100}%" schedule="${schedule}" slot="${null}" timespan="${span}"/>
                        </g:if>

                        <g:scheduleSlot left="${slotStartPixel}%" width="${slotWidthPixel}%" slot="${slot}"/>

                    <%--
                        Adding a "not available" slot if this is the last slot and current time span
                        is stretching further in time.
                    --%>
                        <g:if test="${idx.intValue() == (slots.size()-1) && spaceToEnd > 0}">
                            <g:scheduleSlot  width="${(spaceToEnd / period)*100}%" schedule="${schedule}" slot="${null}" timespan="${span}"/>
                        </g:if>

                        <g:set var="lastSlotEndMillis" value="${slotEnd}"/>
                    </g:each>
                </g:if>
                <g:else>
                    <g:scheduleSlot  width="100%" schedule="${schedule}" slot="${null}" timespan="${span}"/>
                </g:else>
            </tr>
        </table>
    </td>
</tr>