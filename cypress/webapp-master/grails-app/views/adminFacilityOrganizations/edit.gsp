<html>
<head>
    <meta name="layout" content="b3admin">
    <title><g:message code="organization.label.plural"/></title>
</head>

<body>
<div class="container vertical-padding20">
    <ol class="breadcrumb">
        <li><i class="fas fa-list"></i> <g:link controller="adminFacility" action="index">Facilities</g:link></li>
        <li><i class="fas fa-list"></i> <g:link action="index" params="[id: organization?.facility?.id]"><g:message
                code="organization.label.plural"/></g:link></li>
        <li class="active"><g:message code="organization.edit.label"/></li>
    </ol>
    <g:b3StaticErrorMessage bean="${organization}"/>

    <div class="panel panel-default panel-admin">
        <g:render template="/adminFacility/adminFacilityMenu" model="[facility: organization.facility, selected: 4]"/>

        <g:form class="form panel-body">
            <g:hiddenField name="facility.id" value="${organization.facility.id}"/>
            <g:hiddenField name="id" value="${organization.id}"/>
            <g:hiddenField name="version" value="${organization.version}"/>

            <fieldset>
                <g:render template="form"/>

                <div class="form-actions col-sm-12 vertical-margin40 text-right">
                    <g:actionSubmit action="update" class="btn btn-success"
                                    value="${message(code: 'button.update.label')}"/>
                    <g:actionSubmit action="delete" class="btn btn-inverse"
                                    onclick="return confirm('${message(code: 'default.confirm')}')"
                                    value="${message(code: 'button.delete.label')}"/>
                    <g:link action="index" id="${organization.facility.id}" class="btn btn-danger">
                        <g:message code="button.cancel.label"/>
                    </g:link>
                </div>
            </fieldset>
        </g:form>
</body>
</html>
