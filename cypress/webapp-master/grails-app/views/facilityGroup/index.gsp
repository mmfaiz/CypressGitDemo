<%@ page import="com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="group.label.plural"/></title>
</head>
<body>

<ul class="breadcrumb">
    <li><g:message code="group.label.plural"/></li>
</ul>

<g:if test="${!facility?.isMasterFacility()}">
    <div class="action-bar">
        <div class="btn-toolbar-left">
            <g:form method="get">
                <div class="input-append">
                    <g:textField id="group-search-input" class="search" name="q" value="${params.q}" placeholder="${message(code: 'facilityGroup.index.message6')}"/>
                    <span class="add-on"><i class="icon-search"></i></span>
                </div>
            </g:form>
        </div>
        <div class="btn-toolbar-right">
            <g:link action="create" class="btn btn-inverse">
                <span><g:message code="button.add.label"/></span>
            </g:link>
        </div>
    </div>
    <table class="table table-striped table-bordered table-hover" data-provides="rowlink">
        <thead>
        <tr height="34">
            <g:sortableColumn property="name" params="${params}" titleKey="group.name.label"/>
            <g:sortableColumn property="dateCreated" params="${params}" titleKey="default.created.label" width="130"/>
            <th width="130"><g:message code="customer.label.plural"/></th>
        </tr>
        </thead>

        <g:if test="${groups.size() == 0}">
            <tr>
                <td colspan="4"><i><g:message code="facilityGroup.index.message5"/></i></td>
            </tr>
        </g:if>

        <g:each in="${groups}" var="group">
            <tr>
                <td><g:link action="customers" params="[id: group.id]" class="rowlink">${group.name}</g:link></td>
                <td><g:formatDate date="${group.dateCreated}" formatName="date.format.dateOnly"/></td>
                <td><g:message code="facilityGroup.index.message9" args="[group.customerGroups.size()]"/></td>
            </tr>
        </g:each>
    </table>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
<r:script>
    $(document).ready(function() {
        $(".search").focus();
    });
</r:script>
</body>
</html>
