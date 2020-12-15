<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="b3facility"/>
    <title><g:message code="course.label.plural"/></title>
    <g:set var="entityName" value="${message(code: 'course.label')}"/>
</head>

<body>
    <div class="container content-container">
        <ol class="breadcrumb">
            <li><i class="ti-list"></i> <g:message code="course.label.plural"/></li>
        </ol>

        <g:if test="${!facility?.isMasterFacility()}">
            <div class="panel panel-default panel-admin">
                <g:render template="tabs"/>

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
                            <th class="vertical-padding10 text-center" width="20"><g:message code="courseParticipant.label.simple"/></th>
                            <th class="vertical-padding10 text-center" width="20"><i class="ti ti-calendar"></i></th>
                            <g:if test="${courseInstanceList.size() > 1}">
                                <th class="vertical-padding10 text-center" width="20"></th>
                            </g:if>
                        </tr>
                    </thead>
                    <tbody data-link="row" class="rowlink">
                        <g:each in="${courseInstanceList}" status="i" var="courseInstance">
                            <tr data-id="${courseInstance.id}">
                                <td>
                                    <g:link action="edit" id="${courseInstance.id}">
                                        ${fieldValue(bean: courseInstance, field: "name")}
                                    </g:link>
                                </td>
                                <td class="rowlink-skip">
                                    <g:if test="courseInstance?.form">
                                        <g:link controller="form" action="show" params="[hash: courseInstance?.form?.hash]" absolute="true" target="_blank">
                                            ${createLink(controller: "form", action: "show", params: [hash: courseInstance?.form?.hash], absolute: true)}
                                        </g:link>
                                    </g:if>
                                </td>
                                <td><g:formatDate date="${courseInstance.startDate}" formatName="date.format.dateOnly"/></td>
                                <td><g:formatDate date="${courseInstance.endDate}" formatName="date.format.dateOnly"/></td>
                                <td class="text-center">${courseInstance?.participants?.size()}</td>
                                <td class="rowlink-skip text-center"><g:link action="planning" params="[courseIds: courseInstance.id]"><i class="ti ti-calendar"></i></g:link></td>
                                <g:if test="${courseInstanceList.size() > 1}">
                                    <td class="rowlink-skip center-text text-nowrap">
                                        <a href="javascript: void(0)" class="right-margin5"><i class="fas fa-arrow-up"></i></a>
                                        <a href="javascript: void(0)"><i class="fas fa-arrow-down"></i></a>
                                    </td>
                                </g:if>
                            </tr>
                        </g:each>
                    </tbody>
                    <g:if test="${!courseInstanceList}">
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

            <g:if test="${courseInstanceTotal > 50}">
                <div class="text-center">
                    <g:b3PaginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered"
                            maxsteps="0" max="50" action="${params.action}" total="${courseInstanceTotal}"/>
                </div>
            </g:if>
        </g:if>
        <g:else><g:message code="facility.onlyLocal"/></g:else>
    </div>
<r:script>
    $(document).ready(function() {
        $("table").find("a").has("i.fa-arrow-up").click(function() {
            var currentRow = $(this).closest("tr");
            var prevRow = currentRow.prev();
            if (prevRow.length) {
                $.post("${g.forJavaScript(data: createLink(action: 'swapListPosition'))}",
                        {id1: currentRow.attr("data-id"), id2: prevRow.attr("data-id")});
                prevRow.before(currentRow);
            }
        });

        $("table").find("a").has("i.fa-arrow-down").click(function() {
            var currentRow = $(this).closest("tr");
            var nextRow = currentRow.next();
            if (nextRow.length) {
                $.post("${g.forJavaScript(data: createLink(action: 'swapListPosition'))}",
                        {id1: currentRow.attr("data-id"), id2: nextRow.attr("data-id")});
                nextRow.after(currentRow);
            }
        });
    });
</r:script>
</body>
</html>
