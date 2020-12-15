<%@ page import="com.matchi.FacilityProperty; com.matchi.Sport; com.matchi.Court"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="membershipType.label"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="membershipType.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityMembershipType.create.message3"/></li>
</ul>
<g:errorMessage bean="${type}"/>

<g:form action="save" class="form-horizontal form-well" name="facilityMembershipTypeCreateFrm">
    <div class="form-header">
        <g:message code="facilityMembershipType.create.message6"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
    </div>
    <fieldset>
        <g:render template="form"/>

        <div class="form-actions">
            <g:actionSubmit action="save" value="${message(code: 'button.add.label')}" class="btn btn-success"/>
            <g:link action="index" class="btn btn-inverse"><g:message code="button.cancel.label"/></g:link>
        </div>
    </fieldset>
</g:form>
<r:script>
    $(document).ready(function() {
        $("[rel='tooltip']").tooltip();
        $("#facilityMembershipTypeCreateFrm").preventDoubleSubmission({});
    });
</r:script>
</body>
</html>
