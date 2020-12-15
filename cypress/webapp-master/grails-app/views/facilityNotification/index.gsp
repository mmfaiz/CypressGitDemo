<!doctype html>
<html>
<head>
    <meta name="layout" content="b3facility"/>
    <title><g:message code="facilityNotification.label.plural"/></title>
</head>

<body>
<div class="container content-container">
    <ol class="breadcrumb">
        <li><i class="ti-list"></i> <g:message code="facilityNotification.label.plural"/></li>
    </ol>
    <g:if test="${!facility?.isMasterFacility()}">
        <g:render template="tabs"/>
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
                        <th class="vertical-padding20"><g:message code="facilityNotification.title.label"/></th>
                        <th class="vertical-padding20"><g:message code="facilityNotification.publishDate.label"/></th>
                        <th class="vertical-padding20"><g:message code="facilityNotification.endDate.label"/></th>
                    </tr>
                </thead>
                <tbody data-link="row" class="rowlink">
                    <g:each in="${facilityNotificationInstanceList}" status="i" var="facilityNotification">
                        <tr data-id="${facilityNotification.id}">
                            <td><g:link action="edit"
                                    id="${facilityNotification.id}">${fieldValue(bean: facilityNotification, field: "headline")}</g:link></td>
                            <td><g:formatDate date="${facilityNotification.validFrom}" formatName="date.format.dateOnly"/></td>
                            <td><g:formatDate date="${facilityNotification.validTo}" formatName="date.format.dateOnly"/></td>
                            <g:if test="${facilityNotificationInstanceList.size() > 1}">
                                <td class="rowlink-skip center-text text-nowrap">
                                    <a href="javascript: void(0)" class="right-margin5"><i class="fa fa-arrow-up"></i></a>
                                    <a href="javascript: void(0)"><i class="fa fa-arrow-down"></i></a>
                                </td>
                            </g:if>
                        </tr>
                    </g:each>
                </tbody>
                <g:if test="${!facilityNotificationInstanceList}">
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

        <g:if test="${facilityNotificationInstanceTotal > 50}">
            <div class="text-center">
                <g:b3PaginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered"
                        maxsteps="0" max="50" action="index" total="${facilityNotificationInstanceTotal}"/>
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
