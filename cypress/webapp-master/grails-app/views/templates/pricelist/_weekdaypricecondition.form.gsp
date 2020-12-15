<div class="pricelist condition">
    <g:form action="add" method="POST"  onsubmit="onConditionSubmit()" class="form-inline">
        <g:hiddenField name="id" value="${params.id}"/>
        <g:hiddenField name="categoryId" value="${params.categoryId}"/>
        <g:hiddenField name="type" value="WEEKDAYS"/>
        <g:hiddenField name="hiddenCategoryName" value=""/>

        <div class="control-group">

            <g:each in="${(1..7)}">
                <label class="checkbox" for="weekDays_${it}" style="">
                    <g:checkBox name="weekDays" id="weekDays_${it}" style="margin-left: 10px;"
                                            checked="false" value="${it}"/>
                    <g:message code="time.shortWeekDay.${it}"/>
                </label>
            </g:each>

            <g:actionSubmit action="add" value="+ ${message(code: 'button.add.label')}" class="btn btn-success right"/>

        </div>
    </g:form>

</div>
