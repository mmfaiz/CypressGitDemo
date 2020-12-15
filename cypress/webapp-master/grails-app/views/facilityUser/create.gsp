<!doctype html>
<html>
<head>
    <meta name="layout" content="b3facility">
    <title><g:message code="facilityUser.create.title"/></title>
</head>

<body>
<g:b3StaticErrorMessage bean="${user}"/>

<div class="container vertical-padding20">
    <ol class="breadcrumb">
        <li><i class=" ti-write"></i><g:link action="index"><g:message code="user.facility.label"/></g:link></li>
        <li class="active"><g:message code="facilityUser.create.title"/></li>
    </ol>

    <div class="panel panel-default">
        <g:form action="save"  class="form panel-body vertical-padding20">
            <div class="vertical-padding20">
                <span class="block text-muted top-margin5"><g:message code="default.form.create.instructions"/></span>
                <hr/>
            </div>

            <g:render template="form"/>

            <div class="top-margin20 text-right">
                <g:submitButton name="save" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
                <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
            </div>
        </g:form>
    </div>
</div>
</body>
</html>
