<!doctype html>
<html>
<head>
    <meta name="layout" content="b3facility">
    <g:set var="entityName" value="${message(code: 'trainer.label', default: 'Trainer')}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
    <r:require modules="bootstrap3-wysiwyg, select2, matchi-customerselect, bootstrap-switch"/>
</head>

<body>
<div class="container vertical-padding20">
    <g:b3StaticErrorMessage bean="${trainerInstance}"/>

    <ol class="breadcrumb">
        <li><i class=" ti-user"></i> <g:link action="index"><g:message code="trainer.label"/></g:link></li>
        <li class="active"><g:message code="default.create.label" args="[entityName]"/></li>
    </ol>

    <g:if test="${!facility?.isMasterFacility()}">
        <div class="panel panel-default">
            <g:form action="save"  class="form panel-body vertical-padding20" enctype="multipart/form-data">
                <div class="verical-padding20">
                    <span class="block text-muted"><g:message code="default.form.create.instructions"/></span>
                    <hr/>
                </div>

                <g:render template="form"/>

                <div class="top-margin20 text-right">
                    <g:link action="index" class="btn btn-default"><g:message code="button.cancel.label"/></g:link>
                    <g:submitButton name="save" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
                </div>
            </g:form>
        </div>
    </g:if>
    <g:else><g:message code="facility.onlyLocal"/></g:else>
</div>
</body>
</html>
