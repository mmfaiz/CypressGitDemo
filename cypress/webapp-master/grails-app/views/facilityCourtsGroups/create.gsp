<%@ page import="com.matchi.Sport; com.matchi.Court"%>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="courtGroup.create.title"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="courtGroup.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="courtGroup.create.title"/></li>
</ul>
<g:if test="${!facility?.isMasterFacility()}">
    <g:errorMessage bean="${courtGroupInstance}"/>

    <g:form action="save" class="form-horizontal form-well" name="facilityCourtsGroupsCreateFrm">
        <div class="form-header">
            <g:message code="facilityCourts.create.message8"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
        </div>
        <fieldset>
            <g:render template="form"/>

            <div class="form-actions">
                <g:actionSubmit action="save" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
                <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
            </div>
        </fieldset>
    </g:form>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
<r:script>
    $(document).ready(function () {
        $("#facilityCourtsGroupsCreateFrm").preventDoubleSubmission({});
    });
</r:script>
</body>
</html>
