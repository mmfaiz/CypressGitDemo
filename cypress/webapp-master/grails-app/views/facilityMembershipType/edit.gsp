<%@ page import="com.matchi.FacilityProperty; com.matchi.Sport; com.matchi.Court"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="membershipType.label.plural"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="membershipType.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityMembershipType.edit.message3"/></li>
</ul>
<g:errorMessage bean="${type}"/>

<g:form action="update" class="form-horizontal form-well" name="facilityMembershipTypeEditFrm">
    <g:hiddenField name="id" value="${type?.id}" />
    <div class="form-header">
        <g:message code="facilityMembershipType.edit.message7"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
    </div>
    <fieldset>
        <g:render template="form"/>

        <div class="form-actions">
            <g:actionSubmit action="update" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
            <g:link action="cancel" id="${type.id}" class="btn btn-danger"
                onclick="return confirm('${message(code: 'facilityMembershipType.edit.message9')}')"><g:message code="button.delete.label"/></g:link>
            <g:link action="index" class="btn btn-inverse"><g:message code="button.cancel.label"/></g:link>
        </div>
    </fieldset>
</g:form>
<r:script>
    $(document).ready(function() {
        $("[rel='tooltip']").tooltip();
        $("#facilityMembershipTypeEditFrm").preventDoubleSubmission({});
    });
</r:script>
</body>
</html>
