<!doctype html>
<html>
<head>
    <meta name="layout" content="b3admin">
    <g:set var="entityName" value="${message(code: 'globalNotification.label')}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
    <r:require modules="translatable"/>
</head>

<body>
    <div class="container content-container">
        <ol class="breadcrumb">
            <li><i class=" ti-write"></i> <g:link action="index"><g:message code="globalNotification.label" /></g:link></li>
            <li class="active"><g:message code="default.create.label" args="[entityName]"/></li>
        </ol>

        <g:b3StaticErrorMessage bean="${globalNotificationInstance}"/>

        <div class="panel panel-default">
            <div class="panel-heading">
                <h1 class="h3 no-top-margin"><g:message code="default.create.label" args="[entityName]"/></h1>
                <span class="block text-muted top-margin5"><g:message code="default.form.create.instructions"/></span>
            </div>

            <g:form action="save" class="form panel-body">

                <g:render template="form"/>

                <div class="col-sm-12 vertical-margin40 text-right">
                    <g:submitButton name="save" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
                    <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
                </div>

            </g:form>
        </div>
    </div>
</body>
</html>
