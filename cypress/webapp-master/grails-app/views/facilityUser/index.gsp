<%@ page import="com.matchi.FacilityUserRole"%>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="b3facility"/>
    <title><g:message code="user.facility.label"/></title>
</head>

<body>
<div class="container content-container">
    <ol class="breadcrumb">
        <li><i class="ti-list"></i> <g:message code="user.facility.label"/></li>
    </ol>

    <div class="panel panel-default panel-admin">
        <div class="panel-heading table-header">
            <div class="text-right">
                <g:link action="create" class="btn btn-xs btn-white">
                    <i class="ti-plus"></i> <g:message code="button.add.label"/>
                </g:link>
            </div>
        </div>

        <table class="table table-striped table-hover table-bordered">
            <thead>
            <tr>
                <g:sortableColumn property="firstname" titleKey="default.name.label" class="vertical-padding10"/>
                <g:sortableColumn property="email" titleKey="user.email.label" class="vertical-padding10"/>
                <th class="vertical-padding10"><g:message code="facilityUser.facilityRoles.label"/></th>
            </tr>
            </thead>
            <tbody data-link="row" class="rowlink">
            <g:each in="${userInstanceList}" var="user">
                <g:if test="${!user.isInRole('ROLE_ADMIN')}">
                    <tr>
                        <td>
                            <g:link action="edit" id="${user.id}">
                                ${user.fullName()}
                            </g:link>
                        </td>
                        <td>
                            ${user.email}
                        </td>
                        <td>
                            <g:set var="facilityUser" value="${user.facilityUsers.find {it.facility.id == facility.id}}"/>
                            <g:if test="${facilityUser}">
                                ${FacilityUserRole.findAllByFacilityUser(facilityUser)*.accessRight.collect {message(code: "facilityUserRole.accessRight.${it}")}.join(", ")}
                            </g:if>
                            <g:else>
                                <g:message code="facilityUserRole.accessRight.facilityAdmin"/>
                            </g:else>
                        </td>
                    </tr>
                </g:if>
            </g:each>
            </tbody>
            <g:if test="${!userInstanceList}">
                <tfoot>
                <tr>
                    <td colspan="1" class="vertical-padding20">
                        <span class="text-muted"><g:message code="default.noElements"/></span>
                    </td>
                </tr>
                </tfoot>
            </g:if>
        </table>
    </div>

    <g:if test="${userInstanceTotal > 50}">
        <div class="text-center">
            <g:paginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered"
                                        maxsteps="0" max="50" action="index" total="${userInstanceTotal}"/>
        </div>
    </g:if>
</div>
</body>
</html>
