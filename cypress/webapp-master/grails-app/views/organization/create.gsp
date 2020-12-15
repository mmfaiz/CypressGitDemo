<%@ page import="com.matchi.facility.Organization" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="b3facility">
    <g:set var="entityName" value="${message(code: 'organization.label', default: 'Organization')}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>
<g:b3StaticErrorMessage bean="${organizationInstance}"/>

<div class="container vertical-padding20">
    <ol class="breadcrumb">
        <li><i class=" ti-write"></i><g:link action="index"><g:message code="default.home.label"/></g:link></li>
        <li class="active"><g:message code="default.create.label" args="[entityName]"/></li>
    </ol>
    <div class="panel panel-default">
        <g:form action="save" class="form panel-body vertical-padding20 ">
            <div class="vertical-padding20">
                <span class="block text-muted"><g:message code="default.form.create.instructions"/></span>
                <hr/>
            </div>
            <fieldset>
                <g:render template="form"/>
                <div class="col-sm-12 vertical-margin40 text-right">
                    <g:link action="index" class="btn btn-default"><g:message code="button.cancel.label"/></g:link>
                    <g:submitButton name="save" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
                </div>
            </fieldset>
        </g:form>
    </div>
</div>
</body>
</html>
