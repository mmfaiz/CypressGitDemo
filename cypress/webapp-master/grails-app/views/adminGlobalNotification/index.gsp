<!doctype html>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <g:set var="entityName" value="${message(code: 'globalNotification.label')}"/>
    <title>MATCHi - <g:message code="globalNotification.label"/></title>
</head>

<body>
    <div class="container content-container">
        <ol class="breadcrumb">
            <li><i class=" ti-write"></i> <g:message code="globalNotification.label"/></li>
        </ol>

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
                        <g:sortableColumn property="title" titleKey="globalNotification.title.label" class="vertical-padding20"/>
                        <g:sortableColumn property="publishDate" titleKey="globalNotification.publishDate.label" class="vertical-padding20"/>
                        <g:sortableColumn property="endDate" titleKey="globalNotification.endDate.label" class="vertical-padding20"/>
                    </tr>
                </thead>
                <tbody data-link="row" class="rowlink">
                    <g:each in="${globalNotificationInstanceList}" status="i" var="globalNotificationInstance">
                        <tr>
                            <td><g:link action="edit"
                                    id="${globalNotificationInstance.id}">${fieldValue(bean: globalNotificationInstance, field: "title")}</g:link></td>
                            <td><g:formatDate date="${globalNotificationInstance.publishDate}" formatName="date.format.dateOnly"/></td>
                            <td><g:formatDate date="${globalNotificationInstance.endDate}" formatName="date.format.dateOnly"/></td>
                        </tr>
                    </g:each>
                </tbody>
                <g:if test="${!globalNotificationInstanceList}">
                    <tfoot>
                        <tr>
                            <td colspan="6" class="vertical-padding20">
                                <span class="text-muted"><g:message code="default.noElements"/></span>
                            </td>
                        </tr>
                    </tfoot>
                </g:if>
            </table>
        </div>

        <g:if test="${globalNotificationInstanceTotal > 50}">
            <div class="text-center">
                <g:b3PaginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered"
                        maxsteps="0" max="50" action="index" total="${globalNotificationInstanceTotal}"/>
            </div>
        </g:if>
    </div>
</body>
</html>
