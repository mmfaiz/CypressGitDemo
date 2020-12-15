<div class="pricelist condition">
    <g:form action="add" method="POST" onsubmit="onConditionSubmit()" class="form-inline">
        <g:hiddenField name="id" value="${params.id}"/>
        <g:hiddenField name="categoryId" value="${params.categoryId}"/>
        <g:hiddenField name="type" value="TIME"/>
        <g:hiddenField name="hiddenCategoryName" value=""/>
        <div class="control-group">
            <label class="control-label" for="timeConditionFrom"><g:message code="templates.pricelist.timepricecondition.form.message1"/>*</label>
            <input class="span1 price-condition-time" type="text" name="timeConditionFrom" id="timeConditionFrom"
                   value="" />&nbsp;
            <label class="control-label" for="timeConditionTo" class="small"><g:message code="templates.pricelist.timepricecondition.form.message2"/>*</label>
            <input class="span1 price-condition-time" type="text" name="timeConditionTo" id="timeConditionTo"
                   value="" />

            <g:actionSubmit action="add" value="+ ${message(code: 'button.add.label')}" class="btn btn-success right"/>
        </div>
    </g:form>

</div>
