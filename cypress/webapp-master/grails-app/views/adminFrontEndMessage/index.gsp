<!doctype html>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <g:set var="entityName" value="${message(code: 'frontEndMessage.label')}"/>
    <title>MATCHi - <g:message code="frontEndMessage.label"/></title>
</head>

<body>
    <div class="container content-container">
        <ol class="breadcrumb">
            <li><i class=" ti-write"></i> <g:message code="frontEndMessage.label"/></li>
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
                        <g:sortableColumn property="title" titleKey="frontEndMessage.title.label" class="vertical-padding20"/>
                        <g:sortableColumn property="publishDate" titleKey="frontEndMessage.publishDate.label" class="vertical-padding20"/>
                        <g:sortableColumn property="endDate" titleKey="frontEndMessage.endDate.label" class="vertical-padding20"/>
                    </tr>
                </thead>
                <tbody data-link="row" class="rowlink">
                    <g:each in="${frontEndMessageList}" status="i" var="frontEndMessage">
                        <tr>
                            <td><g:link action="edit"
                                    id="${frontEndMessage.id}">${fieldValue(bean: frontEndMessage, field: "name")}</g:link></td>
                            <td><g:formatDate date="${frontEndMessage.publishDate}" formatName="date.format.dateOnly"/></td>
                            <td><g:formatDate date="${frontEndMessage.endDate}" formatName="date.format.dateOnly"/></td>
                        </tr>
                    </g:each>
                </tbody>
                <g:if test="${!frontEndMessageList}">
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

        <g:if test="${frontEndMessageTotal > 50}">
            <div class="text-center">
                <g:b3PaginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered"
                        maxsteps="0" max="50" action="index" total="${frontEndMessageTotal}"/>
            </div>
        </g:if>
    </div>
</body>
</html>
