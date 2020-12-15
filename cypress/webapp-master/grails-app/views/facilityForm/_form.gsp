<div class="row">

    <!-- FORM NAME -->
    <div class="col-sm-6">
        <div class="form-group ${hasErrors(bean: formInstance, field: 'name', 'has-error')}">
            <label for="name"><g:message code="form.name.label"/>*</label>
            <g:textField name="name" value="${formInstance?.name}" maxlength="255" class="form-control" required="required"/>
        </div>
    </div><!-- /.col-sm-6 -->

    <!-- FORM ACTIVE DATES -->
    <div class="col-sm-6">
        <div class="row">
            <div class="col-sm-6">
                <div class="form-group">
                    <label for="activeFrom"><g:message code="form.activeFrom.label"/>*</label>
                    <div class="input-group">
                        <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
                        <input class="form-control" readonly="true" type="text" name="activeFrom" id="activeFrom"
                            value="${formatDate(date: formInstance?.activeFrom, formatName: 'date.format.dateOnly')}"/>
                    </div>
                </div>
            </div>
            <div class="col-sm-6">
                <div class="form-group">
                    <label for="activeTo"><g:message code="form.activeTo.label.short"/>*</label>
                    <div class="input-group">
                        <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
                        <input class="form-control" readonly="true" type="text" name="activeTo" id="activeTo"
                            value="${formatDate(date: formInstance?.activeTo, formatName: 'date.format.dateOnly')}"/>
                    </div>
                </div>
            </div>
        </div><!-- /.row -->
    </div><!-- /.col-sm-6 -->

</div><!-- /.row -->

<div class="row">
    <!-- MAX SUBMISSIONS -->
    <div class="col-sm-6">
        <div class="form-group ${hasErrors(bean: formInstance, field: 'maxSubmissions', 'has-error')}">
            <label for="maxSubmissions"><g:message code="form.maxSubmissions.label"/></label>
            <g:field type="number" name="maxSubmissions" value="${formInstance?.maxSubmissions}" maxlength="9" onkeydown="javascript: return event.keyCode != 69" class="form-control"/>
        </div>
    </div>

</div>

<div class="row">
    <!-- FORM DESCRIPTION -->
    <div class="col-sm-6">
        <div class="form-group ${hasErrors(bean: formInstance, field: 'description', 'has-error')}">
            <label for="description"><g:message code="form.description.label"/></label>
            <g:textArea name="description" value="${formInstance?.description}" class="form-control"/>
        </div>
    </div>
</div>

<div class="row">

    <div class="col-sm-6">
        <div class="checkbox">
            <g:checkBox name="membershipRequired" value="${formInstance?.membershipRequired}"/>
            <label for="membershipRequired"><g:message code="form.membershipRequired.label"/></label>
        </div>
        <div class="checkbox">
            <g:checkBox name="paymentRequired" value="${formInstance?.paymentRequired}"/>
            <label for="paymentRequired"><g:message code="form.paymentRequired.label"/></label>
        </div>
    </div>

</div>

<div class="space-40 clearfix"></div>
<g:render template="/templates/dynamicForms/fields" model="[fields: formInstance?.fields, fieldsProperty: 'fields']"/>

<r:script>
    $(function() {
        $("#activeFrom, #activeTo").datepicker({
            autoSize: true,
            dateFormat: '<g:message code="date.format.dateOnly.small"/>'
        });
    });
</r:script>
