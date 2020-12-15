<%@ page import="com.matchi.Facility" %>
<r:require modules="jquery-multiselect-widget"/>

<div class="row">
    <div class="form-group col-sm-6 ${hasErrors(bean: formTemplate, field: 'name', 'has-errors')}">
        <label for="name"><g:message code="formTemplate.name.label"/>*</label>
            <g:textField name="name" value="${formTemplate?.name}" maxlength="255" class="form-control" required="required"/>
    </div>

    <!-- LABEL HEADING HELP TEXT -->
    <div class="col-sm-6">
        <h5 class="text-muted"><g:message code="formTemplate.name.label"/>*</h5>
        <span class="text-muted"><g:message code="formTemplate.name.description"/></span>
    </div>
</div>
<div class="row">
    <div class="form-group col-sm-6 ${hasErrors(bean: formTemplate, field: 'description', 'has-errors')}">
        <label for="description"><g:message code="formTemplate.description.label"/>*</label>
        <g:textField name="description" value="${formTemplate?.description}" class="form-control" required="required"/>
    </div>
    <div class="col-sm-6">
        <h5 class="text-muted"><g:message code="formTemplate.description.label"/>*</h5>
        <span class="text-muted"><g:message code="formTemplate.description.description"/></span>
    </div>
</div>
<div class="row">
    <div class="form-group col-sm-5 ${hasErrors(bean: formTemplate, field: 'facilities', 'has-error')}">
        <label for="facilities"><g:message code="formTemplate.facilities.label"/></label>
            <g:select name="facilities" from="${Facility.listOrderByName()}" value="${formTemplate?.facilities}"
                    optionKey="id" optionValue="name" multiple="true" class="form-control"/>
    </div>
    <div class="col-sm-6 col-sm-offset-1">
        <h5 class="text-muted"><g:message code="formTemplate.facilities.label"/></h5>
        <span class="text-muted"><g:message code="formTemplate.facilities.description"/></span>
    </div>
</div>

<hr/>

<g:render template="/templates/dynamicForms/fields"
        model="[fields: formTemplate?.templateFields, fieldsProperty: 'templateFields']"/>

<r:script>
    $(function() {
        $("#facilities").multiselect({
            classes: "multi",
            minWidth: 145,
            checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'facility.multiselect.noneSelectedText')}",
            selectedText: "${message(code: 'facility.multiselect.selectedText')}"
        });
    });
</r:script>