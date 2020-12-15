<%@ page import="com.matchi.FacilityContractItem" %>
<html>
<head>
    <meta name="layout" content="b3admin">
    <title><g:message code="adminFacilityContracts.createItem.title"/></title>
</head>
<body>
<div class="container vertical-padding20">
    <ol class="breadcrumb">
        <li><i class="fas fa-list"></i> <g:link controller="adminFacility" action="index">Facilities</g:link><span class="divider"></span></li>
        <li><i class="fas fa-edit"></i><g:link controller="adminFacility" action="edit" id="${item?.contract?.facility?.id}"><g:message code="adminFacility.edit.title"/></g:link><span class="divider"></span></li>
        <li><i class="fas fa-list"></i> <g:link action="index" params="[id: item?.contract?.facility?.id]"><g:message code="adminFacilityContracts.index.title"/></g:link><span class="divider"></span></li>
        <li><i class="fas fa-edit"></i> <g:link action="edit" params="[id: item?.contract?.id]"><g:message code="adminFacilityContracts.edit.title"/></g:link><span class="divider"></span></li>
        <li class="active"><g:message code="adminFacilityContracts.createItem.title"/></li>
    </ol>

    <g:b3StaticErrorMessage bean="${item}"/>

    <div class="panel panel-default panel-admin">
        <g:render template="/adminFacility/adminFacilityMenu" model="[facility: item?.contract?.facility, selected: 3]"/>

        <g:form name="contract-item" class="form panel-body">
            <g:hiddenField name="contract.id" value="${item?.contract?.id}"/>

            <g:render template="itemForm"/>

            <div class="form-group col-sm-12">
                <div class="form-actions">
                    <g:actionSubmit action="saveItem" class="btn btn-success" value="${message(code: 'button.save.label')}"/>
                    <g:link action="edit" id="${item.contract.id}" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
                </div>
            </div>
        </g:form>
    </div>
</div>

</body>
</html>
