<!doctype html>
<html>
<head>
    <meta name="layout" content="b3facility">
    <g:set var="entityName" value="${message(code: 'eventActivity.label')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
    <r:require modules="bootstrap3-wysiwyg, select2, bootstrap-switch"/>
</head>

<body>
<div class="container content-container">
    <ol class="breadcrumb">
        <li><i class="ti-list"></i><g:link action="index"><g:message code="eventActivity.label.plural"/></g:link></li>
        <li class="active">${eventActivityInstance.name}</li>
    </ol>

    <g:b3StaticErrorMessage bean="${eventActivityInstance}"/>

    <div class="panel panel-default">
        <g:render template="tabs"/>

        <g:form name="update" action="update" class="form panel-body vertical-padding20">
            <g:hiddenField name="id" value="${eventActivityInstance?.id}"/>
            <g:hiddenField name="version" value="${eventActivityInstance?.version}"/>

            <div class="vertical-padding20">
                <span class="block text-muted top-margin5"><g:message code="default.form.edit.instructions"/></span>
                <hr/>
            </div>

            <g:render template="form"/>

            <div class="text-right">
                <g:link action="index" class="btn btn-default"><g:message code="button.cancel.label"/></g:link>
                <g:actionSubmit action="preview" class="btn btn-info"
                        value="${message(code: 'button.preview.label')}" formtarget="_blank"/>
                <g:actionSubmit action="delete" value="${message(code: 'button.delete.label')}" class="btn btn-danger"
                        onclick="return confirm('${message(code: 'button.delete.confirm.message')}')"/>
                <g:submitButton name="update" value="${message(code: 'button.update.label')}" class="btn btn-success"/>
            </div>
        </g:form>
    </div>
</div>
</body>
</html>
