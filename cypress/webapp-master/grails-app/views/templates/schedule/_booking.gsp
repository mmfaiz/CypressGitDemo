<%@ page import="com.matchi.ScheduleTagLib; com.matchi.Player; com.matchi.payment.PaymentStatus; com.matchi.enums.BookingGroupType" %>
<td width="${width}"
    onclick="showForm('${slot.id}');return false;"
    slotid="${slot.id}"
    bookingid="${slot?.booking?.id}"
    class="slot ${slot?.booking?.paidStatus} ${slot.color} ${(slot.booking?"":"free")} ${slot.booking?.groupId?"group_"+slot.booking?.groupId:""} ${slot?.booking?.online ? "online":""} ${slot?.restricted ? "restricted":""}"
    valign="top"
    rel="tooltip"
    data-delay="300"
    title="
    ${g.toRichHTML(text: slot?.booking?.customer?.fullName()?"${slot?.booking?.customer?.fullName()}<br>":"")}
    ${g.toRichHTML(text: !slot?.booking?.customer?.id?.equals(slot?.subscription?.customer?.id) && slot?.subscription? message(code: 'templates.schedule.booking.message2') + " ${slot?.subscription?.customer?.fullName()}<br>":"")}
    ${g.toRichHTML(text: slot.court.name)}<br>
    ${g.toRichHTML(text: slot.interval.start.toString("HH:mm"))} - ${g.toRichHTML(text: slot.interval.end.toString("HH:mm"))}${g.toRichHTML(text: paidStatus.equals(PaymentStatus.OK)?"<br>" + message(code: 'templates.schedule.booking.message3'):"")}">
    %{--${slot?.booking?.players && slot.booking.players - 1 ? "<br>" + g.schedulePlayers(bookingId: slot.booking.id, customerId: slot.booking.customer.id).toString() : ""}">--}%


    <div class="slot-info">
        <g:if test="${slot?.booking?.showComment}">
            <span class="comment">${slot.booking?.comment?:''}</span>
        </g:if>
        <g:elseif test="${slot?.booking?.customer}">
            <g:if test="${slot?.booking?.groupId && BookingGroupType.valueOf(slot?.booking?.type).equals(BookingGroupType.ACTIVITY)}">
                <span class="name"><g:scheduleActivityName bookingId="${slot?.booking?.id}"/></span>
            </g:if>
            <g:else>
                <span class="name">${slot.booking?.customer?.fullName() ?: slot.booking?.customer?.email}</span>
                <span class="other">
                    <g:if test="${slot?.booking?.groupId && !BookingGroupType.valueOf(slot?.booking?.type).equals(BookingGroupType.DEFAULT)}">
                        <g:message code="bookingGroup.name.${slot.booking.type}" /><br>
                    </g:if>
                </span>
            </g:else>
        </g:elseif>
        <g:elseif test="${slot.subscription}">
            <span class="name">${slot.subscription.customer.fullName()}</span>
            <span class="other"><g:message code="templates.schedule.booking.message1"/></span>
        </g:elseif>
    </div>
    <g:if test="${slot?.booking?.online}">
        <div class="online-status"></div>
    </g:if>
    <div class="slot-icons">
        <g:if test="${slot?.seasonCard}">
            <span class="fa fa-ticket season-card"></span>
        </g:if>
        <g:if test="${warnAboutCodeRequest}">
            <span class="fas fa-exclamation-triangle"></span>
        </g:if>
        %{--<g:if test="${slot?.booking?.players && --slot.booking.players}">
            <span class="fas fa-user"><small>&#160;&#43;${slot.booking.players}</small></span>
        </g:if>--}%
    </div>
</td>
