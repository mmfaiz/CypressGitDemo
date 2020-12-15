<%@ page import="com.matchi.Court; com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title><g:message code="court.label.plural"/></title>
</head>
<body>
<div class="container vertical-padding20">
    <ol class="breadcrumb">
        <li><i class="fas fa-list"></i> <g:link controller="adminFacility" action="index">Facilities</g:link></li>
        <li class="active"><g:message code="court.label.plural"/></li>
    </ol>

    <g:if test="${!facility.relatedBookingsCustomer && courts.find {it.parent}}">
        <div class="alert alert-danger alert-notification danger alert-dismissible" role="alert">
            <table class="notification-content">
                <tr>
                    <td class="notification-icon" width="10%">
                        <i class="fas fa-bullhorn"></i>
                    </td>
                    <td class="notification-message" width="80%">
                        <g:message code="facility.relatedBookingsCustomer.warning"/>
                    </td>
                    <td class="notification-close" width="10%">
                        <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    </td>
                </tr>
            </table>
        </div>
    </g:if>

    <g:b3StaticErrorMessage bean="${facility}"/>

    <div class="panel panel-default panel-admin">
        <g:render template="/adminFacility/adminFacilityMenu" model="[selected: 2]"/>

        <div class="panel-heading table-header">
            <div class="row">
                <div class="col-sm-6">

                </div>
                <div class="col-sm-6 text-right">
                    <g:link action="create" params="[facilityId: facility.id]" class="btn btn-xs btn-success">
                        <i class="fas fa-plus"></i> <g:message code="button.add.label"/>
                    </g:link>
                </div>
            </div>
        </div>
        <table class="table table-striped table-hover table-bordered">
            <thead>
            <tr>
                <th class="vertical-padding10"><g:message code="court.name.label"/></th>
                <th class="vertical-padding10 text-center" width="100"><g:message code="court.indoor.label"/></th>
                <th class="vertical-padding10"><g:message code="court.surface.label"/></th>
                <th class="vertical-padding10"><g:message code="court.sport.label"/></th>
                <th class="vertical-padding10 text-center" width="120"><g:message code="adminFacilityCourts.index.bookingAvailability"/></th>
                <th class="vertical-padding10 text-center" width="100"><g:message code="court.archived.label"/></th>
                <th class="vertical-padding10 text-center" width="100"><g:message code="adminFacilityCourts.cameras"/></th>
            </tr>
            </thead>
            <tbody data-link="row" class="rowlink">
            <g:each in="${ courts }" var="court">
                <tr>
                    <td>
                        <g:link action="edit" params="[id: court.id]">${court.name}
                            <g:if test="${court.parent}">
                                <small class="text-muted"><g:message code="facilityCourts.index.parentCourt"
                                        args="[court.parent.name]" encodeAs="HTML"/></small>
                            </g:if>
                            <g:if test="${court.membersOnly}"> <small><g:message code="court.membersOnly.label2"/></small></g:if>
                            <g:elseif test="${court.offlineOnly}"> <small><g:message code="court.offlineOnly.label2"/></small></g:elseif>
                        </g:link>
                    </td>
                    <td class="center-text">${court.indoor ? message(code: 'default.yes.label') : message(code: 'default.no.label')}</td>
                    <td><g:message code="court.surface.${court.surface.toString()}"/></td>
                    <td><g:message code="sport.name.${court.sport.id}"/></td>
                    <td class="center-text">
                        <g:if test="${court.restriction == Court.Restriction.MEMBERS_ONLY}">
                            <span class="label label-info"><g:message code="adminFacilityCourts.index.restriction.${court.restriction}"/></span>
                        </g:if>
                        <g:elseif test="${court.restriction == Court.Restriction.OFFLINE_ONLY}">
                            <span class="label label-danger"><g:message code="adminFacilityCourts.index.restriction.${court.restriction}"/></span>
                        </g:elseif>
                        <g:elseif test="${court.restriction == Court.Restriction.REQUIREMENT_PROFILES}">
                            <span class="label label-warning"><g:message code="adminFacilityCourts.index.restriction.${court.restriction}"/></span>
                        </g:elseif>
                        <g:else>
                            <span class="label label-success"><g:message code="adminFacilityCourts.index.restriction.${court.restriction}"/></span>
                        </g:else>
                    </td>
                    <td class="center-text">
                        <g:message code="default.${court.archived ? 'yes' : 'no'}.label"/>
                    </td>
                    <td class="center-text">
                        <g:message code="default.${court.hasCamera() ? 'yes' : 'no'}.label"/>
                    </td>
                </tr>
            </g:each>
            </tbody>
            <g:if test="${courts.size() == 0}">
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
