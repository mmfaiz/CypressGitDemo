<!doctype html>
<html>
<head>
    <meta name="layout" content="b3admin">
    <g:set var="entityName" value="${message(code: 'frontEndMessage.label')}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
    <r:require modules="translatable"/>
</head>

<body>
    <div class="container content-container">
        <ol class="breadcrumb">
            <li><i class=" ti-write"></i> <g:link action="index"><g:message code="frontEndMessage.label" /></g:link></li>
            <li class="active"><g:message code="default.create.label" args="[entityName]"/></li>
        </ol>

        <g:b3StaticErrorMessage bean="${frontEndMessageInstance}"/>

        <div class="panel panel-default">
            <div class="panel-heading">
                <h1 class="h3 no-top-margin"><g:message code="default.create.label" args="[entityName]"/></h1>
                <span class="block text-muted top-margin5"><g:message code="default.form.create.instructions"/></span>
            </div>



            <g:render template="form"  model="[method: 'save']"/>

        </div>
    </div>
</body>
</html>
