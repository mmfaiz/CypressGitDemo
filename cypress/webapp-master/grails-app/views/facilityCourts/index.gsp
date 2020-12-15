<%@ page import="com.matchi.Court; com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="court.label.plural"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:message code="facilityCourts.index.message14" args="[courts.size()]"/></li>
</ul>

<g:if test="${!facility?.isMasterFacility()}">
    <ul class="nav nav-tabs">
        <li class="${actionName == 'index' ? 'active' : ''}"><g:link action="index"><g:message code="court.active.label"/></g:link></li>
        <li class="${actionName == 'archive' ? 'active' : ''}"><g:link action="archive"><g:message code="court.archive.plural.label"/></g:link></li>
    </ul>

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
            <!--<td class="center-text"><input type="checkbox" class="styled"/></td>-->
            <th><g:message code="court.name.label"/></th>
            <th width="100" class="center-text"><g:message code="court.indoor.label"/></th>
            <th><g:message code="court.surface.label"/></th>
            <th><g:message code="court.sport.label"/></th>
            <th width="120" class="center-text"><g:message code="adminFacilityCourts.index.bookingAvailability"/></th>
            <g:if test="${courts.size() > 1}">
                <th width="45"></th>
            </g:if>
        </tr>
        </thead>

        <g:if test="${courts.size() == 0}">
            <tr>
                <td colspan="4"><i><g:message code="facilityCourts.index.message8"/></i></td>
            </tr>
        </g:if>

        <g:each in="${ courts }" var="court">
            <tr data-id="${court.id}">
                <!--<td class="center-text"><input type="checkbox" class="styled"/></td>-->
                <td>
                    <g:link action="edit" params="[id: court.id]">${court.name}
                        <g:if test="${court.parent}">
                            <span class="parent-court"><g:message code="facilityCourts.index.parentCourt"
                                                                  args="[court.parent.name]" encodeAs="HTML"/></span>
                        </g:if>
                        <g:if test="${court.membersOnly}"> <small><g:message code="court.membersOnly.label2"/></small></g:if>
                        <g:elseif test="${court.offlineOnly}"> <small><g:message code="court.offlineOnly.label2"/></small></g:elseif>
                    </g:link>
                </td>
                <td class="center-text">${court.indoor ? message(code: 'default.yes.label') : message(code: 'default.no.label')}</td>
                <td><g:message code="court.surface.${court.surface.toString()}"/></td>
                <td><g:message code="sport.name.${court.sport.id}"/></td>
                <td class="center-text">
                    <g:if test="${court.restriction == Court.Restriction.NONE}">
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
                <g:if test="${courts.size() > 1}">
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
