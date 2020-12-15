<%@ page import="com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="courtGroup.label.plural"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:message code="courtGroup.total.message" args="[groups.size()]"/></li>
</ul>

<g:if test="${!facility?.isMasterFacility()}">
    <div class="alert alert-info">
        <g:message code="courtGroup.allCourts.info"
                args="[createLink(controller: 'facilityAdministration', action: 'settings')]"/>
    </div>

    <div class="action-bar">
        <div class="btn-toolbar-right">
            <g:link action="create" class="btn btn-inverse">
                <span><g:message code="button.add.label"/></span>
            </g:link>
        </div>
    </div>
    <table class="table table-striped table-bordered" data-provides="rowlink">
        <thead>
        <tr height="34">
            <th><g:message code="courtGroup.name.label"/></th>
            <th><g:message code="courtGroup.courts.label"/></th>
            <g:if test="${facility.hasBookingLimitPerCourtGroup()}">
            <th><g:message code="courtGroup.label.restriction"/></th>
            </g:if>
            <g:if test="${groups.size() > 1}">
                <th width="45"></th>
            </g:if>
        </tr>
        </thead>

        <g:if test="${groups.size() == 0}">
            <tr>
                <td colspan="4"><i><g:message code="courtGroup.noGroups.message"/></i></td>
            </tr>
        </g:if>

        <g:each in="${ groups }" var="group">
            <tr data-id="${group.id}">
                <td><g:link action="edit" params="[id: group.id]">${group.name}</g:link></td>
                <td>${group.courts?.size()}</td>
                <g:if test="${facility.hasBookingLimitPerCourtGroup()}">
                <td>${group.maxNumberOfBookings}</td>
                </g:if>
                <g:if test="${groups.size() > 1}">
                    <td class="center-text nolink">
                        <a href="javascript: void(0)" class="right-margin5"><i class="icon-arrow-up"></i></a>
                        <a href="javascript: void(0)"><i class="icon-arrow-down"></i></a>
                    </td>
                </g:if>
            </tr>
        </g:each>
    </table>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
<r:script>
    $(document).ready(function() {
        $("table").find("a").has("i.icon-arrow-up").click(function() {
            var currentRow = $(this).closest("tr");
            var prevRow = currentRow.prev();
            if (prevRow.length) {
                $.post("${g.forJavaScript(data: createLink(action: 'swapListPosition'))}",
                        {id1: currentRow.attr("data-id"), id2: prevRow.attr("data-id")});
                prevRow.before(currentRow);
            }
        });

        $("table").find("a").has("i.icon-arrow-down").click(function() {
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
