<div class="pricelist condition">
    <g:form action="add" method="POST" onsubmit="onConditionSubmit()" class="form-inline">
        <g:hiddenField name="id" value="${params.id}"/>
        <g:hiddenField name="categoryId" value="${params.categoryId}"/>
        <g:hiddenField name="type" value="TIME"/>
        <g:hiddenField name="hiddenCategoryName" value=""/>
        <div class="control-group">
            <label class="control-label" for="timeConditionFrom"><g:message code="templates.conditions.slot.slotconditiontime.form.message1"/>*</label>
            <input class="span1 price-condition-time" type="text" name="startTime" id="timeConditionFrom"
                   value="" />&nbsp;
            <label class="control-label" for="timeConditionTo" class="small"><g:message code="templates.conditions.slot.slotconditiontime.form.message2"/>*</label>
            <input class="span1 price-condition-time" type="text" name="endTime" id="timeConditionTo"
                   value="" />

        </div>
    </g:form>

</div>
