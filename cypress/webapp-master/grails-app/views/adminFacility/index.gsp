<%@ page import="com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title><g:message code="facility.label.plural"/></title>
</head>
<body>
<div class="container content-container">
    <ol class="breadcrumb">
        <li><i class="fas fa-list"></i> <g:message code="facility.label.plural"/> (${facilities.size()} <g:message code="unit.st"/>)</li>
    </ol>

    <form method="GET" class="form well no-bottom-padding">
        <div class="row">
            <div class="form-group col-sm-4 no-top-margin">
                <g:textField name="facilityName" class="form-control" placeholder="facilityName" value="${cmd.facilityName}"/>
            </div>
            <div class="form-group col-sm-2 no-top-margin">
                <g:textField name="fortnoxId" class="form-control" placeholder="fortnoxId" value="${cmd.fortnoxId}"/>
            </div>
            <div class="form-group col-sm-2 no-top-margin pull-right">
                <button type="submit" class="btn btn-block btn-info"><g:message code="button.filter.label"/></button>
            </div>
        </div>
    </form>

    <div class="panel panel-default panel-admin">
        <g:render template="tabs"/>

        <div class="panel-heading table-header">
            <div class="row">
                <div class="col-sm-6">

                </div>
                <div class="col-sm-6">
                    <div class="text-right">
                        <g:link action="create" class="btn btn-xs btn-success">
                            <i class="fas fa-plus"></i> <g:message code="button.add.label"/>
                        </g:link>
                    </div>
                </div>
            </div>
        </div>

        <table class="table table-striped table-hover table-bordered">
            <thead>
            <tr>
                <th class="vertical-padding10" width="280"><g:message code="facility.name.label"/></th>
                <th class="vertical-padding10 text-nowrap" width="20"><g:message code="facility.property.FORTNOX3_CUSTOMER_NUMBER.id"/></th>
                <th class="vertical-padding10" width="150"><g:message code="facility.email.label"/></th>
                <th class="vertical-padding10" width="150"><g:message code="facility.telephone.label"/></th>
                <th class="vertical-padding10" width="120"><g:message code="adminFacility.index.municipality"/></th>
                <th class="vertical-padding10" width="40"><g:message code="court.label.plural"/></th>
                <th class="vertical-padding10 text-center" width="40"><g:message code="facility.enabled.label"/></th>
                <th class="vertical-padding10 text-center" width="40"><g:message code="facility.active.label"/></th>
                <th class="vertical-padding10 text-center" width="40"><g:message code="facility.bookable.label"/></th>
            </tr>
            </thead>
            <tbody data-link="row" class="rowlink">
            <g:each in="${facilities}" var="facility">
                <tr>
                    <td><g:link action="edit" id="${facility.id}">${facility.name}</g:link></td>
                    <td>${facility.getFortnoxCustomerId()}</td>
                    <td>${facility.email}</td>
                    <td>${facility.telephone}</td>
                    <td>${facility.municipality}</td>
                    <td class="center-text"><b>${facility.courts.size()}</b> <g:message code="unit.st"/></td>
                    <td class="center-text"><span class="label ${facility.enabled ? 'label-success':'label-danger'}">${facility.enabled}</span></td>
                    <td class="center-text"><span class="label ${facility.active ? 'label-success':'label-danger'}">${facility.active}</span></td>
                    <td class="center-text"><span class="label ${facility.bookable ? 'label-success':'label-danger'}">${facility.bookable}</span></td>
                </tr>
            </g:each>
            </tbody>
            <g:if test="${facilities.size() == 0}">
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
