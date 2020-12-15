<g:hiddenField name="action" value="remove" />
<g:each in="${trainingCourts}" var="trainingCourt" status="i">
  <div class="row">
    <div class="form-group col-sm-5">
      <g:select from="${facilityCourts}" name="court-${i}" optionKey="id" optionValue="name"
                value="${trainingCourt?.court?.id}" class="form-control"
                noSelection="['': message(code: 'course.settings.courts.choose')]" disabled="disabled"/>
    </div>

    <div class="form-group col-sm-5">
      <g:textField class="form-control" name="name-${i}" value="${trainingCourt?.name}"/>
    </div>

    <div class="form-group col-sm-2 pull-right">
      <button type="submit" id="btn-${i}" class="btn btn-block btn-danger" data-id="${trainingCourt?.id}">
          <i class="ti ti-trash"></i><g:message code="button.delete.label"/>
      </button>
    </div>
  </div>
</g:each>