<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3><g:message code="default.upcoming.bookings"/></h3>
    <div class="clearfix"></div>
</div>
<div class="modal-body">
    <table style="width: 100%" class="table-striped table-hover">
        <thead>
        <tr>
            <th><g:message code="default.date.label"/></th>
            <th><g:message code="default.date.time"/></th>
            <th><g:message code="court.label"/></th>
            <th width="40"></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${bookings}" var="booking">
            <tr>
                <td class="vertical-padding10"><g:formatDate date="${booking.slot.startTime}" format="yyyy-MM-dd" locale="sv"/></td>
                <td class="vertical-padding10"><g:formatDate date="${booking.slot.startTime}" format="HH:mm" locale="sv"/>-<g:formatDate date="${booking.slot.endTime}" format="HH:mm" locale="sv"/></td>
                <td class="vertical-padding10">${booking.slot.court.name}</td>
                <td class="vertical-padding10 text-right" >
                    <g:remoteLink controller="userBooking" action="cancelConfirm" update="userBookingModal"
                      onFailure="handleAjaxError()" onSuccess="showLayer('userBookingModal')" title="${message(code: 'button.unbook.label')}"
                      params="[slotId:booking.slot.id,
                               returnUrl: params.returnUrl]"><i class="icon-remove"></i></g:remoteLink>
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
