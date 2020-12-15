<%@ page import="com.matchi.Region" %>
<html>
<head>
    <meta name="layout" content="adminLayout"/>
    <title><g:message code="region.label.plural"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:message code="region.label.plural"/></li>
</ul>


<div class="action-bar">
    <div class="btn-toolbar-left">
        <g:form method="post" accept-charset="utf-8">
            <div class="input-append">
                <g:textField id="region-search-input" name="q" value="${params.q}" placeholder="${message(code: 'adminRegion.index.searchRegion')}"/>
                <span class="add-on"><i class="icon-search"></i></span>
            </div>
        </g:form>
    </div>
    <div class="btn-toolbar-right">
        <g:link action="create" class="btn btn-inverse pull-right"><g:message code="adminRegion.index.createNew"/></g:link>
    </div>
</div>

<table class="table" data-provides="rowlink">
    <thead>
    <tr height="34">
        <th width="150"><g:message code="default.name.label"/></th>
        <th class="center-text"><g:message code="default.latitude.label"/></th>
        <th class="center-text"><g:message code="default.longitude.label"/></th>
        <th class="center-text"><g:message code="adminRegion.index.zoom"/></th>
        <th width="80" class="center-text"><g:message code="adminRegion.index.municipalities"/></th>
    </tr>
    </thead>

    <g:if test="${regions.size() < 1}">
        <tr>
            <td colspan="4"><i><g:message code="adminRegion.index.noRegions"/></i></td>
        </tr>
    </g:if>

    <g:each in="${regions}" var="region">
        <tr>
            <td><g:link action="edit" id="${region.id}">${region.name}</g:link></td>
            <td class="center-text">${region.lng}</td>
            <td class="center-text">${region.lat}</td>
            <td class="center-text">${region.zoomlv}</td>
            <td class="center-text">${region.municipalities.size()}</td>
        </tr>
    </g:each>
</table>
<script type="text/javascript">
    $(document).ready(function() {
        $("#region-search-input").focus()
    });
</script>
</body>
</html>
