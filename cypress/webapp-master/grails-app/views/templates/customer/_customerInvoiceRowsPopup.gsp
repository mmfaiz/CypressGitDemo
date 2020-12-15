<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3><g:message code="templates.customer.customerInvoiceRowsPopup.message4" args="[customer.fullName()]"/></h3>
    <div class="clearfix"></div>
</div>
<div class="modal-body">
    <table style="width: 100%" class="table-striped table-fixed">
        <thead>
        <tr>
            <th style="text-align: left;"><g:message code="invoiceRow.description.label"/></th>
            <th style="text-align: left;"><g:message code="default.price.label"/></th>
            <th style="text-align: left;"><g:message code="default.created.label"/></th>
            <th width="40"></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${invoiceRows}" var="row">
            <tr>
                <td class="ellipsis">${row.description}</td>
                <td><g:formatMoney value="${row.price}" facility="${customer.facility}"/></td>
                <td><g:formatDate date="${row.dateCreated.toDate()}" formatName="date.format.dateOnly"/></td>
                <td class="center-text">
                    <g:link controller="facilityInvoiceRow" action="remove" class="pull-right" title="${message(code: 'button.delete.label')}"
                            onclick="return confirm('${message(code: 'templates.customer.customerInvoiceRowsPopup.message5')}')"
                            params="['rowIds': row.id,
                                    'returnUrl': g.createLink(absolute: true, action: 'show', id: customer.id)]"><i class="icon-remove"></i></g:link>
                </td>
            </tr>
        </g:each>
        <g:if test="${invoiceRows?.count() > 0}">
            <tr style="border-top: 1px solid grey;">
                <td colspan="4" style="text-align: right"><g:message code="default.total.label"/>: <strong><g:formatMoney value="${totalSum}" facility="${customer.facility}" /></strong></td>
            </tr>
        </g:if>
        </tbody>
    </table>
</div>
<div class="modal-footer">
    <div class="pull-left">
        <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-danger btn-md"><g:message code="button.close.label" default="Stäng"/></a>
    </div>
</div>