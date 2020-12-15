<g:if test="${!membership.activated}">
    <span class="label label-info" rel="tooltip" title="${message(code: 'membership.status.PENDING.tooltip')}">
        <g:message code="membership.status.PENDING"/>
    </span>
</g:if>
<g:elseif test="${!membership.paid}">
    <g:if test="${membership.inStartingGracePeriod}">
        <span class="label label-success" rel="tooltip" title="${message(code: 'membership.startingGracePeriodDays.tooltip', args: [membership.startingGracePeriodDays])}">
    </g:if>
    <g:else>
        <span class="label label-important label-danger" rel="tooltip" title="${message(code: 'membership.status.UNPAID.tooltip')}">
    </g:else>
        <g:message code="membership.status.UNPAID"/>
    </span>
</g:elseif>
<g:else>
    <g:if test="${membership.cancel}">
        <span class="label label-warning" rel="tooltip" title="${message(code: 'membership.cancel.tooltip')}">
    </g:if>
    <g:else>
        <span class="label label-success">
    </g:else>

    <g:message code="membership.status.PAID"/>
    </span>
</g:else>