<%
    def queryString = params.findAll { !['linkId', 'action', 'controller'].contains(it.key) }.collect { it }.join('&')

    def parameters = [returnUrl:
            createLink(controller: 'facility', action: 'show', params: [name: facility.shortname]) + '?' + (queryString) ]

    parameters += params
%>
<sec:ifLoggedIn>
    <g:remoteLink
            elementId="s${id}"
            class="courtLinks"
            controller="bookingPayment"
            action="confirm"
            params="[ start: start.toDate().getTime(), end: end.toDate().getTime(), facilityId: facility.id, sportIds: sport?.id, indoor: indoor, controllerName: this.controllerName ]"
            update="userBookingModal"
            onFailure="handleAjaxError(XMLHttpRequest, textStatus, errorThrown)"
            onSuccess="showLayer('userBookingModal')">
    </g:remoteLink>
</sec:ifLoggedIn>
<sec:ifNotLoggedIn>
    <g:link elementId="s${id}"
            controller="login" action="auth"
            params="${parameters}"
            onclick="loginBeforeBooking(this.href, '${id}');">
    </g:link>
</sec:ifNotLoggedIn>

<td slotid="${id}"
    rel="tooltip"
    title="${message(code: 'templates.schedule.user.freeslot.message1')}"
    data-delay="250"
    data-html="true"
    data-container="body"
    class="slot free" style="position:inherit;"></td>