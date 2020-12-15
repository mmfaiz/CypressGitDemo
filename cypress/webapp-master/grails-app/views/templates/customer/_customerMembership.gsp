<p class="lead header">
    <span class="${!currentMemberships?"transparent-60":""}">
        <g:message code="membership.label"/>
    </span>
    <g:if test="${!customer.archived}">
        <g:remoteLink class="pull-right" controller="facilityCustomerMembers" action="createMembershipForm"
                      update="customerModal" onFailure="handleAjaxError()" onSuccess="showLayer('customerModal')"
                      params="[ 'customerId': customer.id ]"><i class="icon-plus"></i></g:remoteLink>
    </g:if>
    <g:if test="${!currentMemberships && !upcomingMemberships}">
        <br>
        <small class="empty"><g:message code="templates.customer.customerMembership.message2"/></small>
    </g:if>
</p>
<g:if test="${currentMemberships || endedMemberships || upcomingMemberships}">
    <table class="table table-transparent table-condensed table-noborder table-striped no-bottom-margin">
        <colgroup>
            <col/>
            <col style="width: 21%"/>
            <col style="width: 21%"/>
            <col style="width: 16%"/>
            <col style="width: 14px"/>
            <g:if test="${customer.facility.hasApplicationInvoice()}">
                <col style="width: 14px"/>
            </g:if>
        </colgroup>
        <thead class="table-header-transparent">
        <th><g:message code="templates.customer.customerMembership.message3"/></th>
        <th><g:message code="membership.startDate.label"/></th>
        <th><g:message code="membership.endDate.label"/></th>
        <th class="center-text"><g:message code="default.status.label"/></th>
        <th></th>
        <g:if test="${customer.facility.hasApplicationInvoice()}">
            <th></th>
        </g:if>
        </thead>
        <tbody>
        <g:if test="${currentMemberships}">
            <g:render template="/templates/customer/customerMembershipItems"
                    model="[membershipItems: currentMemberships]"/>
        </g:if>

        <g:if test="${upcomingMemberships}">
            <tr>
                <td class="upcomingMembershipsControl" onclick="toggleUpcomingMemberships()" style="cursor: pointer;"
                        colspan="${customer.facility.hasApplicationInvoice() ? '7' : '6'}">
                    <em>
                        <g:message code="membership.widget.upcomingList.header" args="[upcomingMemberships.size()]"/>
                        <i id="upcomingMemberships-indicator" class="icon-chevron-right"></i>
                    </em>
                </td>
            </tr>
            <g:render template="/templates/customer/customerMembershipItems"
                    model="[membershipItems: upcomingMemberships, rowClass: 'upcomingMemberships', hideInvoice: true]"/>
        </g:if>

        <g:if test="${endedMemberships}">
            <tr>
                <td class="endedMembershipsControl" onclick="toggleEndedMemberships()" style="cursor: pointer;"
                        colspan="${customer.facility.hasApplicationInvoice() ? '7' : '6'}">
                    <em>
                        <g:message code="membership.widget.endedList.header" args="[endedMemberships.size()]"/>
                        <i id="endedMemberships-indicator" class="icon-chevron-right"></i>
                    </em>
                </td>
            </tr>
            <g:render template="/templates/customer/customerMembershipItems"
                    model="[membershipItems: endedMemberships, rowClass: 'endedMemberships', hideInvoice: true, hideEdit: true, includeInvoiceDates: true]"/>
        </g:if>
        </tbody>
    </table>

    <r:script>
        function toggleEndedMemberships() {
            if($("#endedMemberships-indicator").hasClass("icon-chevron-right")) {
                $("#endedMemberships-indicator").removeClass("icon-chevron-right").addClass("icon-chevron-down");
            } else {
                $("#endedMemberships-indicator").removeClass("icon-chevron-down").addClass("icon-chevron-right");
            }
            $(".endedMemberships").toggle();
        }

        function toggleUpcomingMemberships() {
            if($("#upcomingMemberships-indicator").hasClass("icon-chevron-right")) {
                $("#upcomingMemberships-indicator").removeClass("icon-chevron-right").addClass("icon-chevron-down");
            } else {
                $("#upcomingMemberships-indicator").removeClass("icon-chevron-down").addClass("icon-chevron-right");
            }
            $(".upcomingMemberships").toggle();
        }
    </r:script>
</g:if>