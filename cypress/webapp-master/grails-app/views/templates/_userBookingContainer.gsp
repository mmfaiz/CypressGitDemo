<%@ page import="com.matchi.ColorFetcher; org.joda.time.LocalDate" %>
<!-- TODO: stale template? Should be removed? -->
<table border="0" width="auto" >
    <%
        def lastWeek = 0;
        def row = 0;
    %>
    <thead>
        <tr>
            <td><g:message code="time.halfWeekDay.1"/></td><td><g:message code="time.halfWeekDay.2"/></td><td><g:message code="time.halfWeekDay.3"/></td><td><g:message code="templates.userBookingContainer.message5"/></td><td><g:message code="time.halfWeekDay.5"/></td><td><g:message code="time.halfWeekDay.6"/></td><td><g:message code="time.halfWeekDay.7"/></td>
        </tr>
    </thead>
    <tr>
    <g:each in="${dates}" var="date">
        <%
            def booking
        %>

        <g:if test="${lastWeek != 0 && date.getWeekOfWeekyear() != lastWeek}">
            <%row++ %>
            </tr><tr>
        </g:if>

        <td height="46" width="90" valign="top" class="${new LocalDate() > (new LocalDate(date)) ? 'passedDate':''}">
            <div class="content ${dateSchedule.colorForDate(new LocalDate(date))}
            <g:if test="${new LocalDate().equals(new LocalDate(date))}">today</g:if>">
                <div id="calendarDay">
                    <g:if test="${new LocalDate() > (new LocalDate(date))}">
                        <div class="date passedDate">
                            ${date.getDayOfMonth()}.
                        </div>
                    </g:if>
                    <g:else>
                        <g:link action="index" params="[date: new LocalDate(date), facilitySelect: facilityIds, courtSelect: surfaces, sportSelect: sportIds]">
                            <div class="date">
                                ${date.getDayOfMonth()}.
                            </div>
                            <div class="freeSlots">
                                <g:if test="${dateSchedule.getNumFreeSlots(new LocalDate(date)) > 0}">
                                    <g:message code="templates.userBookingContainer.message9"/>
                                </g:if>
                            </div>
                        </g:link>
                    </g:else>
                </div>
            </div>
            <g:if test="${booking != null}">
                <div class="tooltip">
                    <div class="left-text">
                        <g:message code="templates.userBookingContainer.message10"/>:<br>
                        <g:message code="default.date.place"/>:<br>
                        <g:message code="default.date.label"/>:<br>
                        <g:message code="default.date.time"/>:<br>
                    </div>
                    <div class="right-text">
                        <g:message code="sport.name.1"/><br>
                    </div>
                </div>
            </g:if>
        </td>
        <%
             lastWeek = date.getWeekOfWeekyear()
         %>
    </g:each>
    </tr>
</table>
<div id="color-faq">
    <ul class="inline">
        <li><i class="green"></i><g:message code="booking.paid.faq"/></li>
        <li><i class="yellow"></i><g:message code="templates.userBookingContainer.message16"/></li>
        <li><i class="blue"></i><g:message code="templates.userBookingContainer.message17"/></li>
        <li><i class="red"></i><g:message code="templates.userBookingContainer.message18"/></li>
    </ul>
</div>

<script type="text/javascript">
$(document).ready(function() {
    $('div.booked').tooltip({
        position: 'bottom center',
        offset: [-85, 60],
        effect: 'slide'
    });
    /*
    $("#bookingContainer").bind("ajaxSend", function() {
        $(this).fadeOut(100);
    }).bind("ajaxComplete", function() {
        $(this).fadeIn(200);
    });
    */
    $('div.bookingContainer table').on('mousedown', 'td:not(.passedDate)', function() {
        $('div.bookingContainer table td').removeClass('active');
        $(this).addClass('active');
    });
    $('div.bookingContainer table').on('mouseleave', 'td:not(.passedDate)', function() {
        $('div.bookingContainer table td').removeClass('active');
    });
});
$("#dateLeftNav").click(function() {
    window.location.href = '?month=${g.forJavaScript(data: previous.getMonthOfYear())}&year=${g.forJavaScript(data: previous.getYear())}'
});
$("#dateRightNav").click(function() {
    window.location.href = '?month=${g.forJavaScript(data: next.getMonthOfYear())}&year=${g.forJavaScript(data: next.getYear())}'
});
</script>