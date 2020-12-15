<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title><g:message code="adminFormTemplate.edit.title"/></title>
</head>

<body>
<g:errorMessage bean="${formTemplate}"/>

<div class="container vertical-padding20">

    <ol class="breadcrumb">
        <li><g:link action="index"><g:message code="formTemplate.label.plural"/></g:link><span class="divider">/</span></li>
        <li class="active"><g:message code="adminFormTemplate.edit.title"/></li>
    </ol>

    <div class="panel panel-default">

        <g:form action="update" class="form panel-body">
            <g:hiddenField name="id" value="${formTemplate.id}"/>
            <g:hiddenField name="version" value="${formTemplate.version}"/>

            <div class="vertical-padding20">
                <h3 class="h4"><g:message code="adminFormTemplate.edit.title"/></h3>
                <span class="block text-muted"><g:message code="default.form.edit.instructions"/></span>
                <hr/>
            </div>

            <g:render template="form"/>

            <div class="text-right">
                <g:link action="index" class="btn btn-default"><g:message code="button.cancel.label"/></g:link>
                <g:actionSubmit action="delete" value="${message(code: 'button.delete.label')}" class="btn btn-danger"
                                onclick="return confirm('${message(code: 'facilityForm.delete.confirm')}')"/>
                <g:submitButton name="update" value="${message(code: 'button.update.label')}" class="btn btn-success"/>
            </div>
        </g:form>

    </div><!-- /.panel -->
</div>
</body>
</html>
