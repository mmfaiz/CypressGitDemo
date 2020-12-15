<%@ page import="com.matchi.payment.PaymentStatus; com.matchi.enums.BookingGroupType" %>
<g:remoteLink action="cancelConfirm" controller="userBooking"
              update="userBookingModal"
              id="s${slot.id}"
              onSuccess="showLayer('userBookingModal')"
              params="[ slotId:slot.id,
                      returnUrl: g.createLink(absolute: true, controller: params.controller,
                              action: params.action,
                              params: params)]">
            </g:remoteLink>
<td width="${width}"
    onclick="$('#s${slot.id}').click();"
    slotid="${slot.id}"
    class="slot ${color}"
    valign="top"
    rel="tooltip"
    data-delay="250"
    data-html="true"
    data-container="body"
    title="${message(code: 'templates.schedule.user.daily.booking.message1')}<br>${g.toRichHTML(text: slot.court.name)}<br>${g.toRichHTML(text: slot.interval.start.toString("HH:mm"))} - ${g.toRichHTML(text: slot.interval.end.toString("HH:mm"))}">
</td>
