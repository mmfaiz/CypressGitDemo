<%@ page import="com.matchi.membership.Membership" %>
<p class="lead header" xmlns="http://www.w3.org/1999/html">
    <span class="${!family?"transparent-60":""}"><g:message code="templates.customer.customerMembershipFamily.message1"/></span>
    <g:if test="${!customer.archived && !family && customer.getCurrentNonGracedMembership()}">
        <g:remoteLink controller="facilityCustomerFamily" action="familyForm" update="customerModal" class="pull-right"
                      onFailure="handleAjaxError()" onSuccess="showLayer('customerModal')"
                      params="[ 'customerId': customer.id ]"><i class="icon-plus"></i></g:remoteLink>
        <i id="alertSuggestedFamilies" class="icon-exclamation-sign blink" rel="tooltip" style="display: none;vertical-align: baseline;"
           title="${message(code: 'templates.customer.customerMembershipFamily.message8')}"></i>
    </g:if>
    <g:if test="${family}">
        <i id="alertSuggestedFamilymembers" class="icon-exclamation-sign blink" rel="tooltip" style="display: none;vertical-align: baseline;"
           title="${message(code: 'templates.customer.customerMembershipFamily.message9')}"></i>
    </g:if>
    <g:else>
        <br>
        <small class="empty"><g:message code="templates.customer.customerMembershipFamily.message2"/>.</small>
        <g:if test="${!customer.getCurrentNonGracedMembership()}">
            <small class="empty"><g:message code="templates.customer.customerMembershipFamily.needCurrentMembership"/></small>
        </g:if>
    </g:else>
</p>
<g:if test="${family}">
    <table class="table table-transparent table-condensed table-noborder table-striped table-fixed no-bottom-margin" data-provides="rowlink">
        <thead class="table-header-transparent">
        <th width="50"><g:message code="default.status.label"/></th>
        <th><g:message code="default.name.label"/></th>
        <th><g:message code="templates.customer.customerMembershipFamily.message6"/></th>
        <th><g:message code="customer.email.label"/></th>
        <th width="20" class="center-text">
            <g:remoteLink controller="facilityCustomerFamily" action="familyEditForm" update="customerModal"
                          onFailure="handleAjaxError()" onSuccess="showLayer('customerModal')"
                          params="[ 'customerId': customer.id ]" title="${message(code: 'templates.customer.customerMembershipFamily.message10')}"><i class="icon-edit"></i></g:remoteLink>
        </th>
        </thead>
        <tbody>
        <g:each in="${family.members}">
            <% def mark = (it.customer == customer)  %>
            <tr class="${mark ? 'info':''}">
                <td>
                    <g:link class="rowlink" controller="facilityCustomer" action="show" id="${it.customer.id}">${it.customer.equals(family.contact) ? message(code: 'membershipFamily.contact.label') : message(code: 'default.member.label')}</g:link>
                </td>
                <td class="ellipsis">${it.customer.fullName()}</td>
                <td class="ellipsis">${it.customer.telephone}</td>
                <td class="ellipsis">${it.customer.email}</td>
                <g:if test="${!it.customer.equals(family.contact)}">
                    <td class="nolink center-text">
                        <g:link controller="facilityCustomerFamily" action="removeFromFamily" title="Ta bort familjemedlem"
                                onclick="return confirm('${message(code: 'templates.customer.customerMembershipFamily.message14')}')"
                                params="[customerId: customer.id, memberId: it.id]">
                            <i class="icon-remove"></i>
                        </g:link>
                    </td>
                </g:if>
                <g:else>
                    <td></td>
                </g:else>
            </tr>
        </g:each>
        </tbody>
    </table>
</g:if>
<r:script>
    $(document).ready(function() {
        <g:if test="${customer.membership && !family}">
            $.ajax({
                url: '<g:createLink controller="facilityCustomerFamily" action="remoteGetSuggestedFamilies" params="[memberId: customer.membership?.id]"/>',
                type: 'GET',
                success: function(data) {
                    if (data.length > 0 && data[0].suggested > 0) {
                        //blink();
                        $(".blink").show();
                    }
                }
            });
        </g:if>
        <g:elseif test="${family}">
            $.ajax({
                url: '<g:createLink controller="facilityCustomerFamily" action="remoteGetSuggestedFamilyMembers" params="[memberId: customer.membership?.id]"/>',
                type: 'GET',
                success: function(data) {
                    if (data.length > 0 && data[0].suggested > 0) {
                        //blink();
                        $(".blink").show();
                    }
                }
            });
        </g:elseif>
    });

    function blink(){
        $(".blink").delay(300).fadeTo(50,0.5).delay(300).fadeTo(50,1, blink);
    }
</r:script>