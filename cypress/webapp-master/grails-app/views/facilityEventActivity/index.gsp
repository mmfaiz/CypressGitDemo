<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="b3facility"/>
    <title><g:message code="eventActivity.label.plural"/></title>
</head>

<body>
    <div class="container content-container">
        <ol class="breadcrumb">
            <li><i class="ti-list"></i> <g:message code="eventActivity.label.plural"/></li>
        </ol>

        <g:if test="${!facility?.isMasterFacility()}">
            <div class="panel panel-default panel-admin">
                <div class="panel-heading no-padding">
                    <div class="tabs tabs-style-underline" style="width: 70%;">
                        <nav>
                            <ul>
                                <li class="${params.action == 'index' ? 'active tab-current' : ''}">
                                    <g:link action="index">
                                        <i class="fas fa-list"></i>
                                        <span><g:message code="eventActivity.active.label"/></span>
                                    </g:link>
                                </li>
                                <li class="${params.action == 'archive' ? 'active tab-current' : ''}">
                                    <g:link action="archive">
                                        <i class="fas fa-list"></i>
                                        <span><g:message code="eventActivity.archive.label"/></span>
                                    </g:link>
                                </li>
                            </ul>
                        </nav>
                    </div>
                </div>
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
                            <g:sortableColumn property="name" titleKey="course.name.label" class="vertical-padding10"/>
                            <th class="vertical-padding10"><g:message code="course.form.label"/></th>
                            <g:sortableColumn property="startDate" titleKey="course.startDate.label" class="vertical-padding10"/>
                            <g:sortableColumn property="endDate" titleKey="course.endDate.label" class="vertical-padding10"/>
                            <th class="vertical-padding10 text-center" width="20"><g:message code="eventActivity.label.plural"/></th>
                        </tr>
                    </thead>
                    <tbody data-link="row" class="rowlink">
                        <g:each in="${eventActivityInstanceList}" status="i" var="eventActivityInstance">
                            <tr>
                                <td>
                                    <g:link action="submissions" id="${eventActivityInstance.id}">
                                        ${fieldValue(bean: eventActivityInstance, field: "name")}
                                    </g:link>
                                </td>
                                <td class="rowlink-skip">
                                    <g:if test="eventActivityInstance?.form">
                                        <g:link controller="form" action="show" params="[hash: eventActivityInstance?.form?.hash]" absolute="true" target="_blank">
                                            ${createLink(controller: "form", action: "show", params: [hash: eventActivityInstance?.form?.hash], absolute: true)}
                                        </g:link>
                                    </g:if>
                                </td>
                                <td><g:formatDate date="${eventActivityInstance.startDate}" formatName="date.format.dateOnly"/></td>
                                <td><g:formatDate date="${eventActivityInstance.endDate}" formatName="date.format.dateOnly"/></td>
                                <td class="text-center">${eventActivityInstance?.form?.submissions?.size()}</td>
                            </tr>
                        </g:each>
                    </tbody>
                    <g:if test="${!eventActivityInstanceList}">
                        <tfoot>
                            <tr>
                                <td colspan="5" class="vertical-padding20">
                                    <span class="text-muted text-md"><g:message code="default.noElements"/></span>
                                </td>
                            </tr>
                        </tfoot>
                    </g:if>
                </table>
            </div>

            <g:if test="${eventActivityInstanceTotal > 50}">
                <div class="text-center">
                    <g:b3PaginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered"
                            maxsteps="0" max="50" action="index" total="${eventActivityInstanceTotal}"/>
                </div>
            </g:if>
        </g:if>
        <g:else><g:message code="facility.onlyLocal"/></g:else>
    </div>
</body>
</html>
