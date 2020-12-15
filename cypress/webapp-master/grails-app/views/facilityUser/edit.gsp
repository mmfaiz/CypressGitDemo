<!doctype html>
<html>
<head>
    <meta name="layout" content="b3facility">
    <title><g:message code="facilityUser.edit.title"/></title>
</head>

<body>
<g:b3StaticErrorMessage bean="${user}"/>

<div class="container vertical-padding20">
    <ol class="breadcrumb">
        <li><i class=" ti-write"></i><g:link action="index"><g:message code="user.facility.label"/></g:link></li>
        <li class="active"><g:message code="facilityUser.edit.title"/></li>
    </ol>

    <div class="panel panel-default">
        <g:form action="update"  class="form panel-body vertical-padding20">
            <g:hiddenField name="id" value="${user?.id}"/>
            <g:hiddenField name="version" value="${user?.version}"/>

            <div class="vertical-padding20">
                <span class="block text-muted top-margin5"><g:message code="default.form.edit.instructions"/></span>
                <hr/>
            </div>

            <g:render template="form"/>

            <div class="text-right">
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
