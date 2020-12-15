<div class="entry pull-left">
    <div class="header">
        <g:message code="templates.conditions.slot.slotconditioncourts.label"/>
    </div>
    <div class="details">
        <g:each in="${condition.courts}" var="court" status="index">
            ${court.name}<g:if test="${index < condition.courts.size()-1}">,
        </g:if>
        </g:each>
    </div>
</div>


