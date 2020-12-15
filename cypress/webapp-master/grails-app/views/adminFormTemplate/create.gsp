<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title><g:message code="adminFormTemplate.create.title"/></title>
</head>

<body>
<g:errorMessage bean="${formTemplate}"/>

<div class="container vertical-padding20">

    <ol class="breadcrumb">
        <li><g:link action="index"><g:message code="formTemplate.label.plural"/></g:link><span class="divider">/</span></li>
        <li class="active"><g:message code="adminFormTemplate.create.title"/></li>
    </ol>

    <g:form action="save" class="form panel-body">
        <div class="form-header">
            <div class="vertical-padding20">
                <h3 class="h4"><g:message code="adminFormTemplate.create.title"/></h3>
                <span class="block text-muted"><g:message code="default.form.create.instructions"/></span>
                <hr/>
            </div>
        </div>

        <g:render template="form"/>

        <div class="text-right">
            <g:link action="index" class="btn btn-default"><g:message code="button.cancel.label"/></g:link>
            <g:submitButton name="save" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
        </div>
    </g:form>
</div>
</body>
</html>
