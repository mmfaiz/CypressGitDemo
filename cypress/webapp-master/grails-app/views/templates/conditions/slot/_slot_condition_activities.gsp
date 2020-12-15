<div class="entry pull-left">
    <div class="header">
        <g:message code="default.activity.plural"/>
    </div>
    <div class="details">
        <g:if test="${condition.notValidForActivities}">
            <g:message code="templates.conditions.slot.slotconditionactivities.message2"/>
        </g:if>
        <g:else>
            <g:each in="${condition.activities}" var="activity" status="index">
                ${activity.name}<g:if test="${index < condition.activities.size()-1}">,
            </g:if>
            </g:each>
        </g:else>
    </div>
</div>


