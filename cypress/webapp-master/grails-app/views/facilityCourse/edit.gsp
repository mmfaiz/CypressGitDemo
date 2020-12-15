<!doctype html>
<html>
<head>
    <meta name="layout" content="b3facility">
    <g:set var="entityName" value="${message(code: 'course.label', default: 'Course')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
    <r:require modules="bootstrap3-wysiwyg, select2, bootstrap-switch"/>
</head>

<body>
<div class="container vertical-padding20">
    <ol class="breadcrumb">
        <li><i class="ti-list"></i><g:link action="index"><g:message code="course.label.plural"/></g:link></li>
        <li class="active">${courseInstance.name}</li>
    </ol>

    <g:b3StaticErrorMessage bean="${courseInstance}"/>

    <div class="panel panel-default">
        <g:form name="update" action="update" class="form panel-body vertical-padding20">
            <g:hiddenField name="id" value="${courseInstance?.id}"/>
            <g:hiddenField name="version" value="${courseInstance?.version}"/>

            <div class="vertical-padding20">
                <span class="block text-muted top-margin5"><g:message code="default.form.edit.instructions"/></span>
                <hr/>
            </div>

            <g:render template="form"/>

            <div class="text-right">
                <g:link action="index" class="btn btn-default"><g:message code="button.cancel.label"/></g:link>
                <g:link controller="facilityCourseFlow" action="copy" id="${courseInstance?.id}" class="btn btn-warning">
                    <g:message code="button.copy.label"/>
                </g:link>
                <g:actionSubmit action="preview" value="${message(code: 'button.preview.label')}"
                        class="btn btn-info" formtarget="_blank"/>
                <g:actionSubmit action="delete" value="${message(code: 'button.delete.label')}" class="btn btn-danger"
                                onclick="return confirm('${message(code: 'button.delete.confirm.message')}')"/>
                <g:submitButton name="update" value="${message(code: 'button.update.label')}" class="btn btn-success"/>
            </div>
        </g:form>
    </div>
</div>
</body>
</html>
