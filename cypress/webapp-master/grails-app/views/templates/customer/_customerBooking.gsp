<%@ page import="com.matchi.payment.PaymentStatus; com.matchi.Slot; com.matchi.ColorFetcher" %>
<p class="lead header">
    <span class="${bookings?.size() < 1?"transparent-60":""}">
        <g:message code="templates.customer.customerBooking.message9"/> ${bookings?.size() > 0 ? " - ${bookings?.size()}${message(code: 'unit.st')}" : ""}
    </span>

    <g:if test="${bookings.size() > 4}">
        <g:remoteLink controller="facilityCustomer" action="showBookings" update="customerModal" class="btn btn-small"
                      style="vertical-align: text-bottom;" title="${message(code: 'templates.customer.customerBooking.message10')}"
                     onFailure="handleAjaxError()" onSuccess="showLayer('customerModal')"
                     params="[ 'bookingIds': bookings.collect { it.id }, 'customerId':customer.id ]"><g:message code="default.multiselect.checkAllText"/></g:remoteLink>
    </g:if>

    <g:if test="${bookings?.size() < 1}">
        <br>
        <small class="empty"><g:message code="templates.customer.customerBooking.message2"/></small>
    </g:if>
</p>
<g:if test="${bookings?.size() > 0}">
    <table class="table table-transparent table-condensed table-noborder table-striped no-bottom-margin">
        <thead class="table-header-transparent">
        <th><g:message code="default.date.label"/></th>
        <th><g:message code="default.date.time"/></th>
        <th><g:message code="court.label"/></th>
        <th class="center-text"><g:message code="payment.paid.label"/></th>
        <th width="30"></th>
        </thead>
        <tbody>

        <g:each in="${bookings}" var="booking" status="i">
            <tr class="${i > 3 ? "hidden":""}">
                <td>
                    <g:formatDate date="${booking.slot.startTime}" formatName="date.format.dateOnly"/>
                </td>
                <td><g:formatDate date="${booking.slot.startTime}" format="HH:mm" />-<g:formatDate date="${booking.slot.endTime}" format="HH:mm" /></td>
                <td>${booking.slot.court.name}</td>
                <td class="center-text">
                    <g:if test="${booking.payment?.status == PaymentStatus.OK}">
                        <span class="label label-success"><g:message code="default.yes.label"/></span>
                    </g:if>
                    <g:elseif test="${booking.payment?.status == PaymentStatus.PARTLY}">
                        <span class="label label-warning"><g:message code="templates.customer.customerBooking.message8"/></span>
                    </g:elseif>
                    <g:else>
                        <span class="label label-${booking.isFinalPaid() ? "success":"important"}">${booking.isFinalPaid() ? message(code: 'default.yes.label') : message(code: 'default.no.label')}</span>
                    </g:else>
                </td>
                <td class="center-text">
                    <g:remoteLink controller="facilityBooking" action="cancelForm" update="customerModal" class="pull-right"
                                  onFailure="handleAjaxError()" onSuccess="showLayer('customerModal')" title="${message(code: 'button.unbook.label')}"
                                  params="['cancelSlotsData': booking.slot.id,
                                          'returnUrl': g.createLink(absolute: false, action: 'show', id: customer.id)]"><i class="icon-remove"></i></g:remoteLink>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
</g:if>