<div class="row">
    <span class="span1 inline" style="width: 45px">
        <g:staleFileArchiveUserImage size="small" id="${customer?.user?.id}" />
    </span>

    <span class="span5" style="width: 425px">
        <g:link controller="facilityCustomer" action="show" id="${customer.id}" target="_blank">
            #${customer?.number}
            <strong>${customer.fullName()}</strong>
            <g:if test="${invoiceAlert}">
                <i class="fas fa-exclamation-circle text-warning" rel="tooltip" title="${message(code: 'facility.customer.invoice.overdue.warning')}"></i>
            </g:if>
        </g:link>
        <g:memberBadge customer="${customer}"/>
        <br>
        <span id="show-email">${customer.email}</span>
        <g:if test="${customer.telephone}">
            <span id="show-phone">${customer.email ? " / ":""}${customer.telephone}</span>
        </g:if>
        <g:if test="${customer.cellphone}">
            <span id="show-phone">${customer.email || customer.telephone ? " / ":""}${customer.cellphone}</span>
        </g:if>
    </span>
</div>