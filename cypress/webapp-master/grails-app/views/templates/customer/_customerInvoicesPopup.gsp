<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3><g:message code="templates.customer.customerInvoicesPopup.message5" args="[customer.fullName()]"/></h3>
    <div class="clearfix"></div>
</div>
<div class="modal-body">
    <table style="width: 100%" class="table-striped table-fixed">
        <thead>
        <tr>
            <th style="text-align: left;"><g:message code="default.created.label"/></th>
            <th style="text-align: left;"><g:message code="templates.customer.customerInvoicesPopup.message2"/></th>
            <th style="text-align: left;"><g:message code="default.amount.label"/></th>
            <th style="text-align: left;"><g:message code="default.status.label"/></th>
            <th width="40"></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${invoices}" var="invoice">
            <tr>
                <td><g:formatDate date="${invoice.dateCreated.toDate()}" formatName="date.format.dateOnly"/></td>
                <td><g:formatDate date="${invoice.expirationDate.toDate()}" formatName="date.format.dateOnly"/></td>
                <td><g:formatMoney value="${invoice.getTotalIncludingVAT()}" facility="${customer.facility}"/></td>
                <td><g:message code="invoice.status.${invoice.status}"/></td>
                <td class="center-text">
                    <g:link class="pull-right" controller="facilityInvoice" action="edit" id="${invoice.id}" title="${message(code: 'templates.customer.customerInvoice.editInvoice')}"><i class="icon-edit"></i></g:link>
                </td>
            </tr>
        </g:each>
        <g:if test="${invoices?.count() > 0}">
            <tr style="border-top: 1px solid grey;">
                <td colspan="5" style="text-align: right;"><g:message code="default.total.label"/>: <strong><g:formatMoney value="${totalSum}" facility="${customer.facility}"/></strong></td>
            </tr>
        </g:if>
        </tbody>
    </table>
</div>
<div class="modal-footer">
    <div class="pull-left">
        <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-danger"><g:message code="button.close.label" default="Stäng"/></a>
    </div>
</div>