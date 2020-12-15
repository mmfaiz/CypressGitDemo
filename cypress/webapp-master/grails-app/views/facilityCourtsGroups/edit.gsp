<%@ page import="com.matchi.Sport; com.matchi.Court"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="courtGroup.edit.title"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="courtGroup.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="courtGroup.edit.title"/></li>
</ul>
<g:errorMessage bean="${courtGroupInstance}"/>

<g:form action="update" class="form-horizontal form-well">
    <g:hiddenField name="id" value="${courtGroupInstance?.id}" />
    <g:hiddenField name="version" value="${courtGroupInstance?.version}" />
    <div class="form-header">
        <g:message code="facilityCourts.edit.message8"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
    </div>
    <fieldset>
        <g:render template="form"/>

        <div class="form-actions">
            <g:submitButton name="update" value="${message(code: 'button.update.label')}" class="btn btn-success"/>
            <g:actionSubmit onclick="return confirm('${message(code: 'button.delete.confirm.message')}')"
                            action="delete" name="btnSumbit" value="${message(code: 'button.delete.label')}" class="btn btn-inverse"/>
            <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
        </div>
    </fieldset>
</g:form>
</body>
</html>
