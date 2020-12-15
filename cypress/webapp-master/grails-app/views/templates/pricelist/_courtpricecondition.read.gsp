<div class="pricelist condition">
    <g:form action="remove" method="GET" onsubmit="onConditionRemove()">
        <g:if test="${condition.id}">
            <g:hiddenField name="conditionId" value="${condition.id}" />
        </g:if>
        <g:else>
            <g:hiddenField name="conditionHashCode" value="${condition.hashCode()}"/>
        </g:else>

        <g:hiddenField name="categoryId" value="${params.categoryId}"/>
        <g:hiddenField name="id" value="${params.id}"/>
        <g:hiddenField name="type" value="COURT"/>
        <g:hiddenField name="hiddenCategoryName" value=""/>

        <g:if test="${condition.id}">
            <div class="entry" style="float:left">
                <div class="header">
                    <g:message code="court.label.plural"/>
                </div>
                <div class="details">
                    <g:render template="/templates/pricelist/status" model="[condition: condition]"/>
                    <g:each in="${condition.courts}" var="court" status="index">
                        ${court.name}<g:if test="${index < condition.courts.size()-1}">,
                    </g:if>
                    </g:each>
                </div>
            </div>
        </g:if>
        <g:else>
            <div class="entry" style="float:left">
                <div class="header">
                    <g:message code="court.label.plural"/>
                </div>
                <div class="details">
                    <g:render template="/templates/pricelist/status" model="[condition: condition]"/>
                    <g:each in="${condition.courtsToBeSaved}" var="court" status="index">
                        ${court.name}<g:if test="${index < condition.courtsToBeSaved.size()-1}">,
                    </g:if>
                    </g:each>
                </div>

            </div>
        </g:else>

        <g:actionSubmit action="remove" id="${params.id}" value="${message(code: 'button.delete.label')}" class="btn btn-danger right" style="margin-top: 10px;"/>

    </g:form>

    <div class="clear"></div>
</div>

