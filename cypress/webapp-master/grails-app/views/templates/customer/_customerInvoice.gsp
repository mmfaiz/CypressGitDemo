<p class="lead header">
    <span class="${invoices?.size() < 1?"transparent-60":""}">
        <g:message code="invoice.label.plural"/> ${invoices?.size() > 0 ? " - ${invoices?.size()}${message(code: 'unit.st')}" : ""}
    </span>

    <g:if test="${invoices?.size() > 4}">
        <g:remoteLink controller="facilityCustomer" action="showInvoices" update="customerModal" class="btn btn-small"
                      style="vertical-align: text-bottom;" title="${message(code: 'templates.customer.customerInvoice.message8')}"
                     onFailure="handleAjaxError()" onSuccess="showLayer('customerModal')"
                     params="[ 'invoiceIds': invoices.collect { it.id }, 'customerId':customer.id ]"><g:message code="default.multiselect.checkAllText"/></g:remoteLink>
    </g:if>
    
    <g:if test="${!customer.archived}">
        <g:customerInvoiceAddIcon returnUrl="${g.createLink(absolute: true, action: 'show', id: customer.id)}" rowsIds="${invoiceRows*.id}"/>
    </g:if>

    <g:if test="${invoices?.size() < 1}">
        <br>
        <small class="empty"><g:message code="templates.customer.customerInvoice.message2"/></small>
    </g:if>
</p>
<g:if test="${invoices?.size() > 0}">
    <table class="table table-transparent table-condensed table-noborder table-striped no-bottom-margin">
        <thead class="table-header-transparent">
        <th><g:message code="default.created.label"/></th>
        <th><g:message code="templates.customer.customerInvoice.message4"/></th>
        <th><g:message code="default.amount.label"/></th>
        <th><g:message code="default.status.label"/></th>
        <th width="14"></th>
        <g:if test="${!customer.facility.hasFortnox()}">
            <th width="14"></th>
        </g:if>
        </thead>
        <tbody>
        <g:each in="${invoices}" status="i" var="invoice">
            <tr class="${i > 3 ? "hidden":""}">
                <td><g:formatDate date="${invoice.dateCreated.toDate()}" formatName="date.format.dateOnly"/></td>
                <td><g:formatDate date="${invoice.expirationDate.toDate()}" formatName="date.format.dateOnly"/></td>
                <td><g:formatMoney value="${invoice.getTotalIncludingVAT()}" facility="${invoice.customer?.facility}"/></td>
                <td>
                    <g:message code="invoice.status.${invoice.status}"/>
                    <g:if test="${invoice.status == com.matchi.invoice.Invoice.InvoiceStatus.CREDITED}">
                        <i class="fa fa-refresh" rel="tooltip" title="${message(code: 'payment.status.CREDITED')}"></i>
                    </g:if>
                </td>
                <td>
                    <g:link controller="facilityInvoice" action="edit"
                            params="[id: invoice.id, returnUrl: g.createLink(absolute: false, action: 'show', id: customer.id)]" title="${message(code: 'templates.customer.customerInvoice.editInvoice')}"><i class="icon-edit"></i></g:link>
                </td>
                <g:if test="${!customer.facility.hasFortnox()}">
                    <td>
                        <g:link controller="facilityInvoice" action="print" target="_blank"
                                params="[invoiceIds: invoice.id]" title="${message(code: 'button.print.label')}"><i class="icon-search"></i></g:link>
                    </td>
                </g:if>
            </tr>
        </g:each>
        <g:if test="${invoices?.size() > 0}">
            <tr style="border-top: 1px solid grey;">
                <td colspan="${customer.facility.hasFortnox() ? '5' : '6'}" style="text-align: right;"><g:message code="default.total.label"/>: <strong><g:formatMoney value="${totalSum}" /></strong></td>
            </tr>
        </g:if>
        </tbody>
    </table>
</g:if>