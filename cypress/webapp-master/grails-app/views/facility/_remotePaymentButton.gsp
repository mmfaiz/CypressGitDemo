<g:set var="familyPaymentAllowed" value="${remoteMembership.isFamilyContact() && remoteMembership.family?.membersNotContact.any {!it.paid}}"/>
<g:set var="familyMembershipRequestAllowed" value="${facility.familyMembershipRequestAllowed && (!remoteMembership.family || remoteMembership.isFamilyContact())}"/>

<div class="btn-group full-width">
    <button type="button" class="btn btn-sm btn-success full-width dropdown-toggle"
            data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
        <g:message code="button.finish.purchase.label"/>
        <span class="caret"></span>
    </button>
    <ul class="dropdown-menu">
        <li>
            <g:remoteLink
                    controller="remotePayment" params="[id: remoteMembership.order.id, finishUrl: createLink([controller: 'facility', action: 'show', absolute: 'true', params: ['name': facility.shortname, 'orderId': remoteMembership.order.id]])]" action="confirm"
                    update="userBookingModal"
                    onFailure="handleAjaxError()"
                    onSuccess="showLayer('userBookingModal')">
                <g:if test="${remoteMembership.family?.members?.size() > 1}">
                    <g:message code="remotePayment.button.payForMyMembership"/>
                </g:if>
                <g:else>
                    <g:message code="remotePayment.button.payForMembership"/>
                </g:else>
            </g:remoteLink>
        </li>
        <g:if test="${!(familyMembershipRequestAllowed && remoteMembership.family?.members?.size() > 1) && facility?.recieveMembershipRequests}">
            <li>
                <g:link controller="membershipRequest" action="index"
                        params="[name: facility.shortname, baseMembership: remoteMembership.id]">
                    <g:message code="remotePayment.button.editMembership"/>
                </g:link>
            </li>
        </g:if>
        <g:if test="${remoteMembership.family?.members?.size() > 1}">
            <li>
                <g:remoteLink
                        controller="remotePayment" params="[id: remoteMembership.order.id, finishUrl: createLink([controller: 'facility', action: 'show', absolute: 'true', params: ['name': facility.shortname, 'orderId': remoteMembership.order.id]]), familyMembership: true]" action="confirm"
                        update="userBookingModal"
                        onFailure="handleAjaxError()"
                        onSuccess="showLayer('userBookingModal')">
                    <g:message code="remotePayment.button.payForFamilyMembership"/>
                </g:remoteLink>
            </li>
        </g:if>
        <g:if test="${familyMembershipRequestAllowed}">
            <li>
                <g:link controller="membershipRequest" action="index"
                        params="[name: facility.shortname, baseMembership: remoteMembership.id, applyForFamilyMembership: true]">
                    <g:if test="${remoteMembership.family?.members?.size() > 1}">
                        <g:message code="remotePayment.button.editFamily"/>
                    </g:if>
                    <g:else>
                        <g:message code="remotePayment.button.createFamily"/>
                    </g:else>
                </g:link>
            </li>
        </g:if>
    </ul>
</div>