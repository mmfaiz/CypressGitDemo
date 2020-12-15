<p class="lead header">
    <span class="${customer.customerGroups?.size() < 1?"transparent-60":""}">
        <g:message code="group.label.plural"/> ${customer.customerGroups?.size() > 0 ? " - ${customer.customerGroups?.size()}${message(code: 'unit.st')}" : ""}
    </span>
    <g:if test="${!customer.archived}">
        <g:remoteLink class="pull-right" controller="facilityGroup" action="addGroupForm" update="customerModal"
                      onFailure="handleAjaxError()" onSuccess="showLayer('customerModal')"
                      params="[ 'customerId': customer.id ]"><i class="icon-plus"></i></g:remoteLink>
    </g:if>

    <g:if test="${customer.customerGroups?.size() < 1}">
        <br>
        <small class="empty"><g:message code="templates.customer.customerGroup.message1"/></small>
    </g:if>
</p>
<g:if test="${customer.customerGroups?.size() > 0}">
    <table class="table table-transparent table-condensed table-noborder table-striped no-bottom-margin">
        <thead class="table-header-transparent">
        <th><g:message code="templates.customer.customerGroup.message2"/></th>
        <th></th>
        </thead>
        <tbody>
        <g:each in="${customer.customerGroups}" var="customerGroup">
            <tr>
                <td>${customerGroup.group.name}</td>
                <td class="center-text">
                    <g:link class="pull-right" controller="facilityGroup" action="removeCustomer" title="Ta bort ur grupp"
                            onclick="return confirm('${message(code: 'templates.customer.customerGroup.message4', args: [customerGroup.group.name])}')"
                            params="[ id: customerGroup.group.id, customerId: customer.id,
                                    'returnUrl': g.createLink(absolute: true, action: 'show', id: customer.id)]">
                        <i class="icon-remove"></i>
                    </g:link>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
</g:if>