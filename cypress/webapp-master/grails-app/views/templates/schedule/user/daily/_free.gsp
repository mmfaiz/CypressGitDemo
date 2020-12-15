<%
    def queryString = params.findAll { !['linkId', 'action', 'controller'].contains(it.key) }.collect { it }.join('&')

    def parameters = [returnUrl:
                              createLink(controller: 'facility', action: 'show', params: [name :facility?.shortname]) + '?' + (queryString) ]

    parameters += params
%>
<sec:ifLoggedIn>
    <g:remoteLink
            elementId="s${slot.id}"
            class="courtLinks"
            controller="bookingPayment"
            action="confirm"
            params="[ slotIds: slot.id, facilityId: facility.id, start: slot.start.toDate().getTime(), end: slot.end.toDate().getTime(), sportIds: sport?.id, indoor: indoor, controllerName: this.controllerName ]"
            update="userBookingModal"
            before="if(typeof blockBooking !== 'undefined' && blockBooking.isStarted()) return false"
            onFailure="handleAjaxError(XMLHttpRequest, textStatus, errorThrown)"
            onSuccess="showLayer('userBookingModal')">
    </g:remoteLink>
</sec:ifLoggedIn>
<sec:ifNotLoggedIn>
    <g:link elementId="s${slot.id}"
            controller="login" action="auth"
            params="${parameters}" onclick="loginBeforeBooking(this.href, '${slot.id}');">
    </g:link>
</sec:ifNotLoggedIn>

<td slotid="${slot.id}"
    class="slot free" rel="tooltip"
    style="width: ${width}"
    data-delay="250"
    data-html="true"
    data-container="body"
    title="${message(code: 'templates.schedule.user.daily.free.message1')}<br>${g.toRichHTML(text: slot.court.name)}<br> ${g.toRichHTML(text: slot.interval.start.toString("HH:mm"))} - ${g.toRichHTML(text: slot.interval.end.toString("HH:mm"))}">
</td>