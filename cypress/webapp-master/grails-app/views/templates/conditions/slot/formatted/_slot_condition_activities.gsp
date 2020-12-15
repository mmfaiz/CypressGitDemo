<g:if test="${condition.notValidForActivities}">
    <g:message code="templates.conditions.slot.formatted.slotconditionactivities.message1"/>
</g:if>
<g:else>
    <g:each in="${condition.activities}" var="activity" status="index">
        ${activity.name}<g:if test="${index < condition.activities.size()-1}">,
    </g:if>
    </g:each>
</g:else>


