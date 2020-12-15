<g:if test="${membership.hasGracePeriod()}">
    <span class="text-dashed" rel="tooltip"
            title="${message(code: 'membership.gracePeriodEndDate.tooltip', args: [membership.endDate.toDate()])}">
</g:if>
<g:else>
    <span>
</g:else>
    <g:formatDate date="${membership.gracePeriodEndDate.toDate()}"
            formatName="date.format.dateOnly"/>
</span>