<div class="condition">
        <g:hiddenField name="id" value="${params.id}"/>
        <g:hiddenField name="categoryId" value="${params.categoryId}"/>
        <g:hiddenField name="type" value="DATE"/>
        <g:hiddenField name="hiddenCategoryName" value=""/>
        <div class="control-group">
            <label class="control-label" for="startDate"><g:message code="default.from.label"/>*</label>
            <input class="price-condition-date" type="text" name="start" id="start" value="" style="width: 90px;"/>&nbsp;
            <g:hiddenField  name="startDate" id="startDate" />
            <label class="control-label" for="endDate"><g:message code="default.to.label"/>*</label>
            <input class="price-condition-date" type="text" name="end" id="end" value="" style="width: 90px;"/>
            <g:hiddenField  name="endDate" id="endDate" />
        </div>
        <div class="clear"></div>
</div>

