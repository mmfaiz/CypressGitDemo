<%@ page import="com.matchi.FacilityContract" %>
<%@ page import="com.matchi.FacilityContractItem" %>
<html>
<head>
    <meta name="layout" content="b3admin">
    <title><g:message code="adminFacilityContracts.edit.title"/></title>
</head>
<body>
<div class="container vertical-padding20">
    <ol class="breadcrumb">
        <li><i class="fas fa-list"></i> <g:link controller="adminFacility" action="index">Facilities</g:link><span class="divider"></span></li>
        <li><i class="fas fa-edit"></i><g:link controller="adminFacility" action="edit" id="${contract.facility.id}"><g:message code="adminFacility.edit.title"/></g:link><span class="divider"></span></li>
        <li><i class="fas fa-list"></i> <g:link action="index" params="[id: contract?.facility?.id]"><g:message code="adminFacilityContracts.index.title"/></g:link><span class="divider"></span></li>
        <li class="active"><g:message code="adminFacilityContracts.edit.title"/></li>
    </ol>

    <g:b3StaticErrorMessage bean="${contract}"/>

    <div class="panel panel-default panel-admin">
        <g:render template="/adminFacility/adminFacilityMenu" model="[facility: contract?.facility, selected: 3]"/>

        <g:form class="form panel-body">
            <g:hiddenField name="id" value="${contract.id}"/>
            <g:hiddenField name="version" value="${contract.version}"/>

            <g:render template="form"/>

            <div class="form-group col-sm-12">
                <div class="form-actions">
                    <g:actionSubmit action="update" class="btn btn-success" value="${message(code: 'button.update.label')}"/>
                    <g:actionSubmit action="copy" class="btn btn-warning" value="${message(code: 'button.copy.label')}"/>
                    <g:actionSubmit action="delete" class="btn btn-inverse" value="${message(code: 'button.delete.label')}"
                                    onclick="return confirm('${message(code: 'adminFacilityContracts.edit.confirmDelete')}')"/>
                    <g:link action="index" id="${contract.facility.id}" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
                </div>
            </div>
        </g:form>

        <!-- CONTRACT ITEMS: BEGIN -->
        <g:set var="contractItems" value="${FacilityContractItem.findAllByContract(contract)}"/>

        <div class="panel-heading table-header">
            <div class="row">
                <div class="col-sm-6">

                </div>
                <div class="col-sm-6 text-right">
                    <g:link action="createItem" params="['contract.id': contract.id]" class="btn btn-xs btn-success">
                        <i class="fas fa-plus"></i> <g:message code="button.add.label"/>
                    </g:link>
                </div>
            </div>
        </div>

        <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th class="col-md-4"><g:message code="facilityContractItem.description.label"/></th>
                <th class="col-md-2"><g:message code="default.updated.label"/></th>
                <th class="col-md-1"><g:message code="facilityContractItem.account.label"/></th>
                <th class="col-md-2"><g:message code="facilityContractItem.type.label"/></th>
                <th class="col-md-2" style="text-align: right;"><g:message code="facilityContractItem.price.label"/></th>
                <th class="col-md-1"><g:message code="button.edit.label"/></th>
            </tr>
            </thead>
            <tbody>
            <g:if test="${contractItems.size() < 1}">
                <tr>
                    <td colspan="3"><i><g:message code="adminFacilityContracts.edit.noContractItems"/></i></td>
                </tr>
            </g:if>
            <g:each in="${contractItems}" var="item">
                <tr>
                    <td><g:link action="editItem" id="${item.id}">${item.description.encodeAsHTML()}</g:link></td>
                    <td><g:formatDate date="${item.lastUpdated}" format="yyyy-MM-dd HH:mm"/></td>
                    <td>${item.account}</td>
                    <td><g:message code="facilityContractItem.type.${item.type}"/></td>
                    <td style="text-align: right;">
                        <g:formatNumber number="${item.price}" maxFractionDigits="2" minFractionDigits="2"/>
                    </td>
                    <td class="center-text">
                        <g:link action="editItem" id="${item.id}"><img src="${resource(dir: 'images', file: 'edit_btn.png')}"/></g:link>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
        <!-- CONTRACT ITEMS: END -->

    </div>
</div>
</body>
</html>
