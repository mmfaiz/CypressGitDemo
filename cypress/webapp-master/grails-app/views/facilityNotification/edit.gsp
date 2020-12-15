<!doctype html>
<html>
<head>
    <meta name="layout" content="b3facility">
    <g:set var="entityName" value="${message(code: 'facilityNotification.label')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>
    <div class="container content-container">
        <ol class="breadcrumb">
            <li><i class=" ti-write"></i> <g:link action="index"><g:message code="facilityNotification.label.plural"/></g:link></li>
            <li class="active"><g:message code="default.edit.label" args="[entityName]"/></li>
        </ol>

        <g:b3StaticErrorMessage bean="${facilityNotificationInstance}"/>

        <div class="panel panel-default">
            <div class="panel-heading">
                <span class="block text-muted"><g:message code="default.form.edit.instructions"/></span>
            </div>

            <g:form action="update" class="form panel-body">
                <g:hiddenField name="id" value="${facilityNotificationInstance?.id}"/>
                <g:hiddenField name="version" value="${facilityNotificationInstance?.version}"/>

                <g:render template="form"/>

                <div class="col-sm-12 vertical-margin40 text-right">
                    <g:link action="index" class="btn btn-default"><g:message code="button.cancel.label"/></g:link>
                    <g:actionSubmit action="delete" value="${message(code: 'button.delete.label')}" class="btn btn-danger"
                            onclick="return confirm('${message(code: 'button.delete.confirm.message')}')"/>
                    <g:submitButton name="update" value="${message(code: 'button.update.label')}" class="btn btn-success"/>
                </div>
            </g:form>
        </div>
    </div>
</body>
</html>
