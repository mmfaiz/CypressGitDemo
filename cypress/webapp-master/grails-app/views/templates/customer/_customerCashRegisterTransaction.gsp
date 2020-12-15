<p class="lead header">
    <span class="${transactions?.size() < 1?"transparent-60":""}">
        <g:message code="templates.customer.customerCashRegisterTransaction.message7"/> ${transactions?.size() > 0 ? " - ${transactions?.size()}${message(code: 'unit.st')}" : ""}
    </span>

    <g:if test="${transactions?.size() > 4}">
        <g:remoteLink controller="facilityCustomer" action="showCashRegisterTransactions" update="customerModal" class="btn btn-small"
                      style="vertical-align: text-bottom;" title="${message(code: 'templates.customer.customerCashRegisterTransaction.message8')}"
                     onFailure="handleAjaxError()" onSuccess="showLayer('customerModal')"
                     params="[ 'transactionIds': transactions.collect { it.id }, 'customerId':customer.id ]"><g:message code="default.multiselect.checkAllText"/></g:remoteLink>
    </g:if>

    <g:if test="${transactions?.size() < 1}">
        <br>
        <small class="empty"><g:message code="templates.customer.customerCashRegisterTransaction.message2"/></small>
    </g:if>
</p>
<g:if test="${transactions?.size() > 0}">
    <table class="table table-transparent table-condensed table-noborder table-striped no-bottom-margin">
        <thead class="table-header-transparent">
        <th width="210"><g:message code="templates.customer.customerCashRegisterTransaction.message3"/></th>
        <th width="50"><g:message code="default.date.label"/></th>
        <th width="50"><g:message code="templates.customer.customerCashRegisterTransaction.message5"/></th>
        <th width="50"><g:message code="cashRegisterTransaction.method.label"/></th>
        </thead>
        <tbody>
        <g:each in="${transactions}" status="i" var="transaction">
            <tr class="${i > 3 ? "hidden":""}">
                <td class="ellipsis">${transaction.title}</td>
                <td><g:formatDate date="${transaction.date}" formatName="date.format.dateOnly"/></td>
                <td><g:formatMoney value="${transaction.paidAmount}" facility="${transaction.customer?.facility}"/></td>
                <td><g:message code="payment.method.${transaction.method}"/></td>
            </tr>
        </g:each>
        </tbody>
    </table>
</g:if>