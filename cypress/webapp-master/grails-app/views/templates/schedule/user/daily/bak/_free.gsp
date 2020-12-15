<%
    def queryString = params.findAll { !['linkId', 'action', 'controller'].contains(it.key) }.collect { it }.join('&')

    def parameters = [returnUrl:
            request.forwardURI + '?' + (queryString) ]

    parameters += params
%>
<sec:ifLoggedIn>
    <g:remoteLink
            elementId="s${slot.id}"
            class="courtLinks"
            controller="bookingPayment"
            action="confirm"
            params="[ slotId: slot.id, facilityId: facility.id, start: slot.start.toDate().getTime(), end: slot.end.toDate().getTime(), sportIds: sport?.id, indoor: indoor, controllerName: this.controllerName ]"
            update="userBookingModal"
            onFailure="handleAjaxError()"
            before="showLayer('userBookingModal')">
    </g:remoteLink>
</sec:ifLoggedIn>
<sec:ifNotLoggedIn>
    <g:link elementId="s${slot.id}"
            controller="login" action="auth"
            params="${parameters}" onclick="loginBeforeBooking(this.href, '${slot.id}');">
    </g:link>
</sec:ifNotLoggedIn>

<td onclick="$('#s${slot.id}').trigger('click');" slotid="${slot.id}"
     class="slot free" rel="tooltip" data-delay="500"
     style="width: ${width}"
     title="${message(code: 'templates.schedule.user.daily.free.message1')}<br>${g.toRichHTML(text: slot.court.name)}<br> ${g.toRichHTML(text: slot.interval.start.toString("HH:mm"))} - ${g.toRichHTML(text: slot.interval.end.toString("HH:mm"))}"></td>