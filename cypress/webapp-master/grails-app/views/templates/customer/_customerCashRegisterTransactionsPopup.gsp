<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3><g:message code="templates.customer.customerCashRegisterTransactionsPopup.message5" args="[customer.fullName()]"/></h3>
    <div class="clearfix"></div>
</div>
<div class="modal-body">
    <table style="width: 100%" class="table-striped table-fixed">
        <thead>
        <tr>
            <th width="130" class="ellipsis" style="text-align: left;"><g:message code="templates.customer.customerCashRegisterTransactionsPopup.message1"/></th>
            <th class="center-text" width="100"><g:message code="default.date.label"/></th>
            <th class="center-text" width="100"><g:message code="cashRegisterTransaction.paidAmount.label"/></th>
            <th class="center-text" width="100"><g:message code="cashRegisterTransaction.method.label"/></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${transactions}" var="transaction">
            <tr>
                <td class="ellipsis">${transaction.title}</td>
                <td class="center-text">
                    <g:formatDate date="${transaction.date}" formatName="date.format.dateOnly"/>
                </td>
                <td class="center-text">
                    ${transaction.paidAmount} <g:currentFacilityCurrency facility="${customer?.facility}"/>
                </td>
                <td class="center-text"><g:message code="payment.method.${transaction.method}"/></td>
            </tr>
        </g:each>
        </tbody>
    </table>
</div>
<div class="modal-footer">
    <div class="pull-left">
        <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-danger btn-md"><g:message code="button.close.label" default="Stäng"/></a>
    </div>
</div>