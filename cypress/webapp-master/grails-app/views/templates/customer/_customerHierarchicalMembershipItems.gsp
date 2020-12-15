<g:each in="${membershipItems}" var="m">
    <tr class="${rowClass}" >
        <td>
            <g:if test="${m.type}">${m.type.name}</g:if>
            <g:else><i><g:message code="templates.customer.customerMembership.message5"/></i></g:else>
        </td>
        <td>${m.customer.facility.name}</td>
        <td><g:formatDate date="${m.startDate.toDate()}" formatName="date.format.dateOnly"/></td>
        <td>
            <g:membershipEndDate membership="${m}"/>
        </td>
        <td class="center-text">
            <g:membershipStatus membership="${m}"/> <g:membershipPaymentHistory membership="${m}"/>
        </td>
        <td class="center-text">
            <g:if test="${!hideEdit}">
                <g:remoteLink controller="facilityCustomerMembers" action="editMembershipForm" id="${m.id}"
                              update="customerModal" onFailure="handleAjaxError()" onSuccess="showLayer('customerModal')"
                              title="${message(code: 'templates.customer.customerMembership.message9')}">
                    <i class="icon-edit"></i>
                </g:remoteLink>
            </g:if>
        </td>
        <g:if test="${customer.facility.hasApplicationInvoice()}">
            <td class="center-text">
                <g:if test="${!hideInvoice && m.type && m.order.total() && !m.inGracePeriod}">
                    <g:link class="pull-right" title="${message(code: 'templates.customer.customerMembership.message10')}"
                            controller="facilityCustomerMembersInvoice" action="createMembershipInvoice"
                            params="[customerId: customer.id, 'returnUrl': g.createLink(absolute: true, action: 'show', id: customer.id)]">
                        <i class="icon-shopping-cart"></i>
                    </g:link>
                </g:if>
            </td>
        </g:if>
    </tr>
</g:each>