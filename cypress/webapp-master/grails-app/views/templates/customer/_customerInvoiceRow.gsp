<p class="lead header">
    <span class="${rows?.size() < 1?"transparent-60":""}">
        <g:message code="templates.customer.customerInvoiceRow.message7"/> ${rows?.size() > 0 ? " - ${rows?.size()}${message(code: 'unit.st')}" : ""}
    </span>
    <g:if test="${rows.size() > 4}">
        <g:remoteLink controller="facilityCustomer" action="showInvoiceRows" update="customerModal" class="btn btn-small"
                      style="vertical-align: text-bottom;" title="${message(code: 'templates.customer.customerInvoiceRow.message6')}"
                      onFailure="handleAjaxError()" onSuccess="showLayer('customerModal')"
                      params="[ 'invoiceRowIds': rows.collect { it.id }, 'customerId':customer.id ]"><g:message code="default.multiselect.checkAllText"/></g:remoteLink>
    </g:if>

    <g:if test="${!customer.archived}">
        <g:link class="pull-right" controller="facilityInvoiceRowFlow"
                params="[customerId: [customer.id], 'returnUrl': g.createLink(absolute: true, action: 'show', id: customer.id)]" action="createInvoiceRow"><i class="icon-plus"></i></g:link>
    </g:if>

    <g:if test="${rows?.size() < 1}">
        <br>
        <small class="empty"><g:message code="templates.customer.customerInvoiceRow.message2"/></small>
    </g:if>
</p>
<g:if test="${rows?.size() > 0}">
    <table class="table table-transparent table-condensed table-noborder table-striped table-fixed no-bottom-margin">
        <thead class="table-header-transparent table-condensed table-noborder">
        <th width="180" class="ellipsis"><g:message code="invoiceRow.description.label"/></th>
        <th width="80"><g:message code="templates.customer.customerInvoiceRow.message4"/></th>
        <th width="60"><g:message code="default.created.label"/></th>
        <th width="40"></th>
        </thead>
        <tbody>
        <g:each in="${rows}" status="i" var="row">
            <tr class="${i > 3 ? "hidden":""}">
                <td class="ellipsis">${row.description}</td>
                <td><g:formatMoney value="${row.getTotalIncludingVAT()}" facility="${customer?.facility}"/></td>
                <td><g:formatDate date="${row.dateCreated.toDate()}" formatName="date.format.dateOnly"/></td>
                <td class="center-text">
                    <g:link controller="facilityInvoiceRow" action="remove" class="pull-right" title="${message(code: 'button.delete.label')}"
                            onclick="return confirm('${message(code: 'templates.customer.customerInvoiceRow.message8')}')"
                            params="['rowIds': row.id, 'returnUrl': g.createLink(absolute: false, action: 'show', id: customer.id)]">
                        <i class="icon-remove"></i></g:link>
                </td>
            </tr>
        </g:each>
        <g:if test="${rows?.size() > 0}">
            <tr style="border-top: 1px solid grey;">
                <td colspan="4" style="text-align: right"><g:message code="default.total.label"/>: <strong><g:formatMoney value="${totalSum}" facility="${customer?.facility}"/></strong></td>
            </tr>
        </g:if>
        </tbody>
    </table>
</g:if>