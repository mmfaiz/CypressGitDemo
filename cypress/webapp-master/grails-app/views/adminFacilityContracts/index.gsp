<html>
<head>
    <meta name="layout" content="b3admin">
    <title><g:message code="adminFacilityContracts.index.title"/></title>
</head>
<body>
<div class="container vertical-padding20">
    <ol class="breadcrumb">
        <li><i class="fas fa-list"></i> <g:link controller="adminFacility" action="index">Facilities</g:link></li>
        <li class="active"><g:message code="adminFacilityContracts.index.title"/></li>
    </ol>

    <g:b3StaticErrorMessage bean="${facility}"/>

    <div class="panel panel-default panel-admin">
        <g:render template="/adminFacility/adminFacilityMenu" model="[selected: 3]"/>

        <div class="panel-heading table-header">
            <div class="row">
                <div class="col-sm-6">

                </div>
                <div class="col-sm-6 text-right">
                    <g:link action="create" params="['facility.id': facility.id]" class="btn btn-xs btn-success">
                        <i class="fas fa-plus"></i> <g:message code="button.add.label"/>
                    </g:link>
                </div>
            </div>
        </div>
        <table class="table table-striped table-hover table-bordered">
            <thead>
            <tr>
                <th class="vertical-padding10"><g:message code="facilityContract.name.label"/></th>
                <th class="vertical-padding10" width="110"><g:message code="facilityContract.dateValidFrom.label"/></th>
            </tr>
            </thead>
            <tbody data-link="row" class="rowlink">
            <g:each in="${contracts}" var="contract">
                    <tr>
                        <td><g:link action="edit" id="${contract.id}">${contract.name.encodeAsHTML()}</g:link></td>
                        <td><g:formatDate date="${contract.dateValidFrom}" format="yyyy-MM-dd"/></td>
                    </tr>
                </g:each>
            </tbody>
            <g:if test="${contracts.size() == 0}">
                <tfoot>
                <tr>
                    <td colspan="7" class="vertical-padding20">
                        <span class="text-muted text-md"><i class="ti-info-alt"></i><g:message code="default.noElements"/></span>
                    </td>
                </tr>
                </tfoot>
            </g:if>
        </table>
    </div>
</div>
</body>
</html>
