<!doctype html>
<html>
<head>
    <meta name="layout" content="b3facility"/>
    <g:set var="entityName" value="${message(code: 'trainer.label')}"/>
    <title>MATCHi - <g:message code="trainer.label"/></title>
</head>

<body>
<div class="container content-container">
    <ol class="breadcrumb">
        <li><i class=" ti-user"></i> <g:message code="trainer.label"/></li>
    </ol>

    <g:if test="${!facility?.isMasterFacility()}">
        <div class="panel panel-default panel-admin">

            <div class="panel-heading table-header">
                <div class="text-right">
                    <g:link action="create" class="btn btn-xs btn-success">
                        <i class="ti-plus"></i> <g:message code="button.add.label"/>
                    </g:link>
                </div>
            </div>
            <table class="table table-striped table-hover table-bordered">
                <thead>
                <tr>
                    <g:sortableColumn property="firstName" titleKey="trainer.firstName.label" class="vertical-padding10"/>
                    <g:sortableColumn property="email" titleKey="trainer.email.label" class="vertical-padding10"/>
                    <g:sortableColumn property="phone" titleKey="trainer.phone.label" class="vertical-padding10"/>
                    <th class="vertical-padding10"><g:message code="trainer.sport.label"/></th>
                    <th class="vertical-padding10 text-center"><g:message code="trainer.isActive.label"/></th>
                    <th class="vertical-padding10 text-center"><g:message code="trainer.showOnline.label"/></th>
                </tr>
                </thead>
                <tbody data-link="row" class="rowlink">

                <g:each in="${trainerInstanceList}" status="i" var="trainerInstance">
                    <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

                        <td><g:link action="edit" id="${trainerInstance.id}">${trainerInstance}</g:link></td>

                        <td>${fieldValue(bean: trainerInstance, field: "email")}</td>

                        <td>${fieldValue(bean: trainerInstance, field: "phone")}</td>

                        <td><g:message code="sport.name.${trainerInstance?.sport?.id}"/></td>

                        <td class="text-center">
                            <g:if test="${trainerInstance.isActive}">
                                <span class="label label-success"><g:message code="default.yes.label"/></span>
                            </g:if>
                            <g:else>
                                <span class="label label-danger"><g:message code="default.no.label"/></span>
                            </g:else>
                        </td>

                        <td class="text-center">
                            <g:if test="${trainerInstance.showOnline}">
                                <span class="label label-success"><g:message code="default.yes.label"/></span>
                            </g:if>
                            <g:else>
                                <span class="label label-danger"><g:message code="default.no.label"/></span>
                            </g:else>
                        </td>
                    </tr>
                </g:each>
                </tbody>
                <g:if test="${!trainerInstanceList}">
                    <tfoot>
                    <tr>
                        <td colspan="6" class="vertical-padding20">
                            <span class="text-muted text-md"><i class="ti-info-alt"></i><g:message code="default.noElements"/></span>
                        </td>
                    </tr>
                    </tfoot>
                </g:if>
            </table>

        </div><!-- /.panel -->

        <g:if test="${trainerInstanceTotal > 50}">
            <div class="text-center">
                <g:b3PaginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered" maxsteps="0" max="50" action="index" total="${trainerInstanceTotal}"/>
            </div>
        </g:if>
    </g:if>
    <g:else><g:message code="facility.onlyLocal"/></g:else>
</div><!-- /.container -->
</body>
</html>
