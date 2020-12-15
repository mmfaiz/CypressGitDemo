
<div class="entry pull-left">

    <div class="header"><g:message code="templates.conditions.slot.slotconditionweekdays.message1"/></div>
    <div class="details">
        <g:each in="${condition.weekdays}" var="day" status="index">
            <g:message code="time.weekDay.${day}"/><g:if test="${index < condition.weekdays.size()-2}">,
        </g:if><g:elseif test="${index < condition.weekdays.size()-1}"> <g:message code="templates.conditions.slot.slotconditionweekdays.message2"/> </g:elseif>
        </g:each>
    </div>
</div>

