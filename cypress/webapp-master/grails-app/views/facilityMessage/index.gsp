<%@ page import="com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilityMessage.index.message1"/></title>
</head>
<body>

<ul class="breadcrumb">
    <li><g:link controller="facilityAdministration" action="index"><g:message code="facilityMessage.index.message2"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityMessage.index.message1"/></li>
</ul>

<ul class="nav nav-tabs">
    <li><g:link controller="facilityAdministration" action="index"><g:message code="facility.label2"/></g:link></li>
    <li>
        <g:link controller="facilityAdministration" action="settings"><g:message code="adminFacility.adminFacilityMenu.settings"/></g:link>
    </li>
    <li class="active">
        <g:link controller="facilityMessage" action="index"><g:message code="facilityMessage.index.message1"/></g:link>
    </li>
    <li>
        <g:link controller="facilityAccessCode" action="index"><g:message code="facilityAccessCode.label.plural"/></g:link>
    </li>
</ul>

<div class="action-bar">
    <div class="btn-toolbar-left">

    </div>
    <div class="btn-toolbar-right">
        <g:link action="form" class="btn btn-inverse">
            <span><g:message code="button.add.label"/></span>
        </g:link>
    </div>
</div>
<table class="table table-striped table-bordered table-hover" data-provides="rowlink">
    <thead>
    <tr height="34">
        <g:sortableColumn property="headline" params="${params}" titleKey="default.name.label"/>
        <g:sortableColumn property="active" params="${params}" titleKey="facilityMessage.active.label" width="40"/>
        <g:sortableColumn property="validFrom" params="${params}" titleKey="facilityMessage.index.message11" width="130"/>
        <g:sortableColumn property="validTo" params="${params}" titleKey="facilityMessage.index.message12" width="130"/>

    </tr>
    </thead>
    <tbody>
    <g:each in="${messages}">
        <tr>
            <td><g:link action="form" params="[id: it.id]" class="rowlink">${it.headline}</g:link></td>
            <td>${it.active ? message(code: 'default.yes.label') : message(code: 'default.no.label')}</td>
            <td><joda:format value="${it.validFrom}" pattern="yyyy-MM-dd HH:mm"/></td>
            <td><joda:format value="${it.validTo}" pattern="yyyy-MM-dd HH:mm"/></td>

        </tr>
    </g:each>
    </tbody>
</table>

<r:script>
    $(document).ready(function() {
        $(".search").focus();
    });
</r:script>
</body>
</html>
