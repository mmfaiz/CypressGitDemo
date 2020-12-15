<li role="presentation" class="dropdown-submenu">
    <a role="menuitem" tabindex="-1" href="#"><g:message code="button.export.label"/></a>
    <ul class="dropdown-menu">
        <li>
            <a href="javascript:void(0)" onclick="submitFormTo('#submissions', '${createLink(action: 'exportSubmissions', params: [returnUrl: returnUrl])}');">
                <g:message code="facilityCustomer.index.export.submissionData"/>
            </a>
        </li>
    </ul>
</li>
<li role="presentation">
    <a role="menuitem" tabindex="-1" href="javascript:void(0)"
       onclick="submitFormTo('#submissions', '${createLink(action: 'customerAction', params: [returnUrl: returnUrl, targetController: 'facilityCustomerMessage', targetAction: 'message'])}');">
        <g:message code="facilityCustomer.index.message25"/>
    </a>
</li>
<g:if test="${facility.hasSMS()}">
    <li class="presentation">
        <a role="menuitem" tabindex="-1" href="javascript:void(0)"
           onclick="submitFormTo('#submissions', '${createLink(action: 'customerAction', params: [returnUrl: returnUrl, targetController: 'facilityCustomerSMSMessage', targetAction: 'message'])}');">
            <g:message code="facilityCustomer.index.message24"/>
        </a>
    </li>
</g:if>