<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityGroup.edit.message1"/></title>
</head>
<body>
<g:errorMessage bean="${group}"/>

<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="group.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityGroup.edit.message1"/></li>
</ul>

<ul class="nav nav-tabs">
    <li><g:link action="customers" id="${group?.id}"><g:message code="customer.label.plural"/></g:link></li>
    <li class="active">
        <g:link action="edit" id="${group?.id}"><g:message code="button.edit.label"/></g:link>
    </li>
</ul>

<g:form action="update" class="form-horizontal form-well" name="facilityGroupsEditFrm">
    <g:hiddenField name="id" value="${group?.id}" />
    <g:hiddenField name="version" value="${group?.version}" />
    <div class="form-header">
        <g:message code="facilityGroup.edit.message1"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
    </div>
    <fieldset>
        <div class="control-group ${hasErrors(bean:group, field:'name', 'error')}">
            <label class="control-label" for="name"><g:message code="group.name.label" default="Namn" /></label>
            <div class="controls">
                <g:textField name="name" value="${group?.name}" class="span8"/>
            </div>
        </div>
        <div class="control-group ${hasErrors(bean:group, field:'description', 'error')}">
            <label class="control-label" for="description"><g:message code="group.description.label" default="Beskrivning" /></label>
            <div class="controls">
                <g:textArea name="description" rows="10" cols="30" value="${group?.description}" class="span8"/>
            </div>
        </div>
        <div class="form-actions">
            <g:actionSubmit action="update" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
            <g:actionSubmit onclick="return confirm('${message(code: 'facilityGroup.edit.message8')}')"
                            action="delete" name="btnSumbit" value="${message(code: 'button.delete.label')}" class="btn btn-inverse"/>
            <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
        </div>
    </fieldset>
</g:form>
<r:script>
    $("body").on("submit", "form", function() {
        $(this).submit(function() {
            return false;
        });
        return true;
    });
    $("#name").focus();
</r:script>
</body>
</html>