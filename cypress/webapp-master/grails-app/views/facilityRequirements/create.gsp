<%@ page import="com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilityRequirements.create.title"/></title>
    <r:require modules="jquery-timepicker, datejs, jquery-multiselect-widget"/>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link controller="facilityRequirements" action="index"><g:message code="requirementProfile.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityRequirements.create.title"/></li>
</ul>

<g:if test="${!facility?.isMasterFacility()}">
    <g:form action="save" class="form-horizontal form-well" name="facilityRequirementsCreateFrm">
        <g:hiddenField name="id" value="${cmd?.id}" />

        <div class="form-header">
            <g:message code="facilityRequirements.create.title"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
        </div>

        <fieldset>
            <div class="control-group">
                <label class="control-label" for="name"><g:message code="default.name.label"/> <g:inputHelp title="${message(code: 'facilityRequirements.create.name.tooltip')}"/></label>
                <div class="controls">
                    <g:textField name="name" value="" class="span8"/>
                </div>
            </div>
            <g:each in="${cmd?.requirements}" var="req" status="index">
                <g:hiddenField name="requirements[${req.key}].requirementClassName" value="${req.value.requirementClassName}" />
                <g:render template="${req.value.requirementClassName}" model="[req: req, index: index, facility: facility]"/>
            </g:each>
            <div class="form-actions">
                <g:actionSubmit action="save" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
                <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
            </div>
        </fieldset>
    </g:form>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
<r:script>
    $(document).ready(function() {
        $("[rel='tooltip']").tooltip();
        $("#facilityRequirementsCreateFrm").preventDoubleSubmission({});
    });

    function toggleDisabled(key) {
        $('.' + key + '-element').prop('disabled', function (i,v) { return !v; });
    }
</r:script>
</body>
</html>
