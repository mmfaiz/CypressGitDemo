<div class="pricelist condition">
    <g:form action="remove" method="POST" onsubmit="onConditionRemove()">
        <g:if test="${condition.id}">
            <g:hiddenField name="conditionId" value="${condition.id}"/>
        </g:if>
        <g:else>
            <g:hiddenField name="conditionHashCode" value="${condition.hashCode()}"/>
        </g:else>

        <g:hiddenField name="categoryId" value="${params.categoryId}"/>
        <g:hiddenField name="id" value="${params.id}"/>
        <g:hiddenField name="type" value="DATE"/>
        <g:hiddenField name="hiddenCategoryName" value=""/>


        <div class="entry" style="float:left">
            <div class="header">
                <g:message code="templates.pricelist.datepricecondition.read.message1"/>
            </div>
            <div class="details">
                <g:render template="/templates/pricelist/status" model="[condition: condition]"/>
                <g:message code="templates.pricelist.datepricecondition.read.message2" args="[
                        formatDate(date: condition.startDate, formatName: 'date.format.dateOnly'),
                        formatDate(date: condition.endDate, formatName: 'date.format.dateOnly')
                ]"/>
            </div>

        </div>

        <g:actionSubmit action="remove" id="${params.id}" value="${message(code: 'button.delete.label')}" class="btn btn-danger right" style="margin-top: 10px;"/>
    </g:form>

    <div class="clear"></div>
</div>

