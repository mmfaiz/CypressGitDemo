<%@ page import="com.matchi.Sport; com.matchi.Court"%>
<html>
<head>
    <meta name="layout" content="b3facility"/>
    <title><g:message code="facilityActivity.create.title"/></title>
</head>
<body>

<div class="container vertical-padding20">
    <ul class="breadcrumb">
        <li><i class=" ti-write"></i> <g:link controller="facilityActivity" action="index"><g:message code="default.activity.plural"/></g:link> <span class="divider"></span></li>
        <li class="active"><g:message code="facilityActivity.create.message3"/></li>
    </ul>

    <g:if test="${!facility?.isMasterFacility()}">
        <g:b3StaticErrorMessage bean="${classActivityInstance}"/>

        <div class="panel panel-default">
            <div class="panel-heading">
                <span class="block text-muted top-margin5"><g:message code="default.form.create.instructions"/></span>
            </div>

            <g:form action="save" class="form panel-body" name="facilityActivityCreateFrm">
                <g:hiddenField name="id" value="${classActivityInstance?.id}" />
                <g:hiddenField name="version" value="${classActivityInstance?.version}" />
                <g:render template="form"/>

                <div class="col-sm-12 vertical-margin40 text-right">
                    <g:link action="index" class="btn btn-default"><g:message code="button.cancel.label"/></g:link>
                    <g:actionSubmit action="save" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
                </div>
            </g:form>
        </div>
    </g:if>
    <g:else><g:message code="facility.onlyLocal"/></g:else>
</div>
<r:script>
    $(document).ready(function() {
        $("[rel='tooltip']").tooltip();

        $("#facilityActivityCreateFrm").preventDoubleSubmission({});
    });
</r:script>
</body>
</html>