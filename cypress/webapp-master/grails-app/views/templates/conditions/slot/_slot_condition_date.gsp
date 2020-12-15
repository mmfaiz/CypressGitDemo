<div class="entry pull-left">
    <div class="header">
        <g:message code="templates.conditions.slot.slotconditiondate.message1"/>
    </div>
    <div class="details">
        ${condition.startDate.toString("${message(code: 'date.format.dateOnly')}")} - ${condition.endDate.toString("${message(code: 'date.format.dateOnly')}")}
    </div>

</div>
