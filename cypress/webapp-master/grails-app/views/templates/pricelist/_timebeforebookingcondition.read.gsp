<div class="pricelist condition">
  <g:form action="remove" method="GET" onsubmit="onConditionRemove()">
    <g:if test="${condition.id}">
      <g:hiddenField name="conditionId" value="${condition.id}"/>
    </g:if>
    <g:else>
      <g:hiddenField name="conditionHashCode" value="${condition.hashCode()}"/>
    </g:else>

    <g:hiddenField name="categoryId" value="${params.categoryId}"/>
    <g:hiddenField name="id" value="${params.id}"/>
    <g:hiddenField name="type" value="TIMEBEFORE"/>

    <div class="entry" style="float:left">
      <div class="header">
        <g:message code="timeBeforeBooking.condition.label"/>
      </div>
      <div class="details">
        <g:render template="/templates/pricelist/status" model="[condition: condition]"/>
        <g:message code="timeBeforeBooking.condition.title"/>: ${condition.hours ? condition.hours + 'h' : ''} ${condition.minutes ? condition.minutes + 'min' : ''}
      </div>
    </div>

    <g:actionSubmit action="remove" id="${params.id}" value="Ta bort" class="btn btn-danger right" style="margin-top: 10px;"/>

  </g:form>

  <div class="clear"></div>
</div>

