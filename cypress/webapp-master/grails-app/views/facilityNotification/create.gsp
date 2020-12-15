<!doctype html>
<html>
<head>
    <meta name="layout" content="b3facility">
    <g:set var="entityName" value="${message(code: 'facilityNotification.label')}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>
<div class="container content-container">
    <ol class="breadcrumb">
        <li><i class=" ti-write"></i> <g:link action="index"><g:message code="facilityNotification.label.plural" /></g:link></li>
        <li class="active"><g:message code="default.create.label" args="[entityName]"/></li>
    </ol>

    <g:if test="${!facility?.isMasterFacility()}">
        <g:b3StaticErrorMessage bean="${facilityNotificationInstance}"/>

        <div class="panel panel-default">
            <div class="panel-heading">
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
    </g:if>
    <g:else><g:message code="facility.onlyLocal"/></g:else>
</div>
</body>
</html>
