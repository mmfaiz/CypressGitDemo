<g:set var="showComment" value="${slot.booking?.showComment}"/>
<g:set var="messageCode" value="${facility.showBookingHolder ? (showComment ? 'UseComment':'ByCustomer') : ''}"/>
<g:set var="messageArg" value="${showComment ? slot.booking?.comment : (slot.booking?.hideBookingHolder ? message(code: 'templates.schedule.user.daily.full.slot.anonymousBooking') : slot.booking?.customer?.fullName())}"/>

<td slotid="${slot.id}"
     class="slot red" rel="tooltip"
     style="width: ${width}"
     data-delay="250"
     data-html="true"
     data-container="body"
    title="${g.toRichHTML(text: message(code: 'templates.schedule.user.daily.full.slot.booked' + messageCode, args: [messageArg]))}<br>
            ${g.toRichHTML(text: slot.court.name)}<br>
            ${g.toRichHTML(text: slot.interval.start.toString("HH:mm"))} - ${g.toRichHTML(text: slot.interval.end.toString("HH:mm"))}"></td>