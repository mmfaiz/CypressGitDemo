<div class="pricelist condition">
    <g:form  action="add" name="courtForm" method="POST" onsubmit="onConditionSubmit()" class="form-inline">
        <g:hiddenField name="id" value="${params.id}"/>
        <g:hiddenField name="categoryId" value="${params.categoryId}"/>
        <g:hiddenField name="type" value="COURT"/>
        <g:hiddenField name="hiddenCategoryName" value=""/>


        <div class="control-group">
            <g:each in="${courts}" var="court">
                <label class="checkbox inline" for="courts_${court.id}" style="margin-left: 10px;">
                    <g:checkBox name="courtIds" id="courts_${court.id}" checked="false" value="${ court.id }"/>
                    ${court.name}
                </label>
            </g:each>
            <g:actionSubmit action="add" value="+ ${message(code: 'button.add.label')}" class="btn btn-success right"/>
        </div>
    </g:form>
    <div class="clear"></div>
</div>
