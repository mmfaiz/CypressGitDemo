<%@ page import="com.matchi.activities.Participation; com.matchi.Sport; com.matchi.Court" %>
<html>
<head>
    <meta name="layout" content="b3facility"/>
    <title><g:message code="default.activity.plural"/></title>
</head>

<body>
<div class="container vertical-padding20">
    <ul class="breadcrumb">
        <li><g:link controller="facilityActivity" action="index"><g:message
            code="default.activity.plural"/></g:link></li>
        <li class="active"><g:message code="facilityActivity.edit.message3"/></li>
    </ul>

    <g:b3StaticErrorMessage bean="${classActivityInstance}"/>

    <ul class="nav nav-tabs">
        <li><g:link action="occasions" id="${classActivityInstance.id}"><g:message
            code="facilityActivity.edit.message4"/></g:link></li>
        <li class="active">
            <g:link action="edit" id="${classActivityInstance.id}"><g:message code="button.edit.label"/></g:link>
        </li>
    </ul>

    <div class="panel panel-default">
        <div class="panel-heading">
            <span class="block text-muted top-margin5"><g:message code="facilityActivity.edit.message6"/></span>
        </div>

        <g:form name="update" action="update" class="form panel-body vertical-padding20">
            <g:hiddenField name="id" value="${classActivityInstance?.id}"/>
            <g:hiddenField name="version" value="${classActivityInstance?.version}"/>
            <g:render template="form"/>

            <div class="form-actions">
                <g:actionSubmit action="update" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
                <g:actionSubmit onclick="return confirm('${message(code: 'facilityActivity.edit.message18')}')"
                                action="archiveActivity" name="btnArchive"
                                value="${message(code: 'button.archive.label')}" class="btn btn-warning"/>
                <g:actionSubmit onclick="return confirm('${message(code: 'facilityActivity.edit.message20')}')"
                                action="delete" name="btnSumbit" value="${message(code: 'button.delete.label')}"
                                class="btn btn-inverse"/>
                <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
            </div>
        </g:form>

        <div class="panel-heading">
            <span class="block text-muted top-margin5"><g:message code="facilityActivity.edit.message8"/></span>
        </div>

        <div class="panel-body">
            <g:fileArchiveImageUpload image="${classActivityInstance.largeImage}"
                                      callback="[controller: 'facilityActivity', action: 'upload']"
                                      removeCallback="[controller: 'facilityActivity', action: 'removeImage']"
                                      params="[id: classActivityInstance.id]"/>
        </div>
    </div>
</div>

<r:script>
    $(document).ready(function () {
        $("a[rel=popover]").popover({trigger: 'hover'});
        $("[rel='tooltip']").tooltip();
    });
</r:script>
</body>
</html>
