<%@ page import="com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="season.label.plural"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:message code="facilitySeason.index.message10" args="[seasons.size()]"/></li>
</ul>

<g:if test="${!facility?.isMasterFacility()}">
    <div class="action-bar">
        <div class="btn-toolbar-left">
            <g:form method="get">
                <div class="input-append">
                    <g:textField id="seasons-search-input" class="search" name="q" value="${params.q}" placeholder="${message(code: 'facilitySeason.index.message11')}"/>
                    <span class="add-on"><i class="icon-search"></i></span>
                </div>
            </g:form>
        </div>
        <div class="btn-toolbar-right">
            <g:if test="${!facility.anySeasonUpdating}">
                <g:link action="create" class="btn btn-inverse">
                    <span><g:message code="button.add.label"/></span>
                </g:link>
            </g:if>
        </div>
    </div>

    <table class="table table-striped table-bordered" data-provides="rowlink">
        <thead>
        <tr height="34">
            <th><g:message code="season.name.label"/></th>
            <th width="120" class="center-text"><g:message code="facilitySeason.index.message4"/></th>
            <th width="120" class="center-text"><g:message code="facilitySeason.index.message5"/></th>
            <th width="80" class="center-text"><g:message code="season.active.label"/></th>
        </tr>
        </thead>

        <g:if test="${seasons.size() == 0}">
            <tr>
                <td colspan="4"><i><g:message code="facilitySeason.index.message12"/> <g:link action="create" class=""><g:message code="facilitySeason.index.message7"/></g:link></i></td>
            </tr>
        </g:if>

        <g:each in="${seasons}" var="season">
            <tr class="${season.endTime < new Date() ? "transparent-60":""}">
                <td>
                    <g:link action="edit" id="${season.id}">
                        ${season.name}
                    </g:link>
                    <g:if test="season.description">
                        <br/><small>${season.description}</small>
                    </g:if>
                </td>
                <td class="center-text"><g:formatDate date="${season.startTime}" formatName="date.format.dateOnly" /></td>
                <td class="center-text"><g:formatDate date="${season.endTime}" formatName="date.format.dateOnly" /></td>
                <td class="center-text">
                    <g:if test="${season.endTime > new Date() && season.startTime < new Date()}">
                        <span class="label label-info"><g:message code="season.active.label"/></span>
                    </g:if>
                </td>
            </tr>
        </g:each>
    </table>
    <r:script>
        $(document).ready(function() {
            $(".search").focus()
        });
    </r:script>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
</body>
</html>
