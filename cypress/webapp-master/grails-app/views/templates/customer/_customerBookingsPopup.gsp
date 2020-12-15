<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3><g:message code="templates.customer.customerBookingsPopup.message5" args="[customer.fullName()]"/></h3>
    <div class="clearfix"></div>
</div>
<div class="modal-body">
    <table style="width: 100%" class="table-striped">
        <thead>
        <tr>
            <th style="text-align: left;"><g:message code="default.date.label"/></th>
            <th style="text-align: left;"><g:message code="default.date.time"/></th>
            <th style="text-align: left;"><g:message code="court.label"/></th>
            <th><g:message code="payment.paid.label"/></th>
            <th width="40"></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${bookings}" var="booking">
            <tr>
                <td><g:formatDate date="${booking.slot.startTime}" formatName="date.format.dateOnly"/></td>
                <td><g:formatDate date="${booking.slot.startTime}" format="HH:mm" locale="sv"/>-<g:formatDate date="${booking.slot.endTime}" format="HH:mm" locale="sv"/></td>
                <td>${booking.slot.court.name}</td>
                <td class="center-text"><span class="label label-${booking.isFinalPaid() ? "success":"important"}">${booking.isFinalPaid() ? message(code: 'default.yes.label') : message(code: 'default.no.label')}</span></td>
                <td class="center-text" >
                    <g:remoteLink controller="facilityBooking" action="cancelForm" update="customerModal"
                      onFailure="handleAjaxError()" onSuccess="showLayer('customerModal')"
                      params="['cancelSlotsData': booking.slot.id,
                              'returnUrl': g.createLink(absolute: false, action: 'show', id: customer.id)]"><i class="icon-remove"></i></g:remoteLink>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
</div>
<div class="modal-footer">
    <div class="pull-left">
        <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-danger"><g:message code="button.close.label" default="Stäng"/></a>
    </div>
</div>