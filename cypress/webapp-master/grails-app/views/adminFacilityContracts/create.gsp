<%@ page import="com.matchi.FacilityContract" %>
<html>
<head>
    <meta name="layout" content="b3admin">
    <title><g:message code="adminFacilityContracts.create.title"/></title>
</head>
<body>
<div class="container vertical-padding20">
    <ol class="breadcrumb">
        <li><i class="fas fa-list"></i> <g:link controller="adminFacility" action="index">Facilities</g:link><span class="divider"></span></li>
        <li><i class="fas fa-edit"></i><g:link controller="adminFacility" action="edit" id="${contract.facility.id}"><g:message code="adminFacility.edit.title"/></g:link><span class="divider"></span></li>
        <li><i class="fas fa-list"></i> <g:link action="index" params="[id: contract?.facility?.id]"><g:message code="adminFacilityContracts.index.title"/></g:link><span class="divider"></span></li>
        <li class="active"><g:message code="adminFacilityContracts.create.title"/></li>
    </ol>

    <g:b3StaticErrorMessage bean="${contract}"/>

    <div class="panel panel-default panel-admin">
        <g:render template="/adminFacility/adminFacilityMenu" model="[facility: contract?.facility, selected: 3]"/>

        <g:form class="form panel-body">
            <g:hiddenField name="facility.id" value="${contract.facility.id}"/>

            <g:render template="form"/>

            <div class="form-group col-sm-12">
                <div class="form-actions">
                    <g:actionSubmit action="save" class="btn btn-success" value="${message(code: 'button.save.label')}"/>
                    <g:link action="index" id="${contract.facility.id}" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
                </div>
            </div>
        </g:form>
    </div>
</div>
</body>
</html>
