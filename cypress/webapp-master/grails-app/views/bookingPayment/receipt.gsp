<%@ page import="com.matchi.Booking; com.matchi.orders.Order; com.matchi.Facility; com.matchi.payment.PaymentMethod; org.joda.time.DateTime" %>

<%
    def firstOrderRow = orders.first()
    Booking firstBooking = firstOrderRow.booking
    Facility facility = firstBooking.slot.court.facility
    Order firstOrder = firstOrderRow.order
%>

<g:applyLayout name="${showPage ? 'paymentFinishPage' : 'paymentFinishModal'}"
               model="${[onclick: "window.self.location.href = setParam(removeParam('comeback', window.self.location.href), 'date', '${firstBooking.slot.startTime.format(g.message(code:'date.format.dateOnly'))}');"]}">

<!-- NOTE! This is modal dialog that is loaded with Ajax and only body will be included so scripts in head will not be included, only body content. -->
<g:render template="/templates/googleTagManager" model="[facility: facility, orders: orders.toList().collect {it.order}]" />

    <h1 class="h3"><g:message code="payment.receipt.message3"/></h1>
    <p><g:message code="payment.receipt.message7" args="[firstBooking.customer.email]"/></p>
    <p>
        <g:cancellationTerms slot="${firstBooking.slot}"/>
        <g:if test="${firstOrder.isPaidByCreditCard()}">
            <br><g:message code="payment.receipt.message8"
                           args="[serviceFeeValue(currency: firstBooking.customer.facility.currency)]"/>
        </g:if>
    </p>

    <hr>

    <h3>${facility.name}</h3>
    <g:if test="${firstOrderRow.accessCode}">
        <p><g:message code="facilityAccessCode.content.label"/>: <strong>${firstOrderRow.accessCode}</strong></p>
    </g:if>

    <hr>

    <g:each in="${orders}" var="order">
        <% Booking booking = order.booking %>
        <div class="row">
            <div class="col-xs-4">
                <h6><g:message code="default.date.label"/></h6>
                <p><g:humanDateFormat date="${new DateTime(booking.slot.startTime)}"/></p>
            </div>
            <div class="col-xs-4">
                <h6><g:message code="default.date.time"/></h6>
                <p><g:formatDate format="HH:mm" date="${booking.slot.startTime}" />-<g:formatDate format="HH:mm" date="${booking.slot.endTime}" /></p>
            </div>
            <div class="col-xs-4">
                <h6><g:message code="court.label"/></h6>
                <p class="ellipsis">${booking.slot.court.name}</p>
            </div>
        </div>
    </g:each>
</g:applyLayout>