<div class="pricelist condition">
  <g:form action="add" method="POST" onsubmit="onConditionSubmit()" class="form-inline">
    <g:hiddenField name="id" value="${params.id}"/>
    <g:hiddenField name="categoryId" value="${params.categoryId}"/>
    <g:hiddenField name="type" value="TIMEBEFORE"/>
    <div class="control-group">
      <label class="control-label"><g:message code="timeBeforeBooking.condition.title"/>:</label>
      <input type="number" name="hours" class="span1" value="${params.hours}"/>h &nbsp;
      <input type="number" name="minutes" class="span1" value="${params.minutes}"/>min

      <g:actionSubmit action="add" value="+ ${message(code: 'button.add.label')}" class="btn btn-success right"/>
    </div>
  </g:form>

</div>