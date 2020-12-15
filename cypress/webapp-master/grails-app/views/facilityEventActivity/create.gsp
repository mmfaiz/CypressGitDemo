<!doctype html>
<html>
<head>
    <meta name="layout" content="b3facility">
    <g:set var="entityName" value="${message(code: 'eventActivity.label')}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
    <r:require modules="bootstrap3-wysiwyg, select2, bootstrap-switch"/>
</head>

<body>
<div class="container content-container">
    <ol class="breadcrumb">
        <li><i class=" ti-write"></i> <g:link action="index"><g:message code="eventActivity.label.plural"/></g:link></li>
        <li class="active"><g:message code="default.create.label" args="[entityName]"/></li>
    </ol>

    <g:if test="${!facility?.isMasterFacility()}">
        <g:b3StaticErrorMessage bean="${eventActivityInstance}"/>

        <div class="panel panel-default">
            <g:form action="save" class="form panel-body vertical-padding20" name="facilityEventActivityCreateFrm">

                <div class="vertical-padding20">
                    <span class="block text-muted top-margin5"><g:message code="default.form.create.instructions"/></span>
                    <hr/>
                </div>

                <g:render template="form"/>

                <div class="top-margin20 text-right">
                    <g:submitButton name="save" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
                    <g:actionSubmit action="preview" class="btn btn-info btn-preview"
                            value="${message(code: 'button.preview.label')}" formtarget="_blank"/>
                    <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
                </div>
            </g:form>
        </div>
    </g:if>
    <g:else><g:message code="facility.onlyLocal"/></g:else>
</div>
<r:script>
    $(document).ready(function() {
        $("#facilityEventActivityCreateFrm").preventDoubleSubmission({});
        $(".btn-preview").allowDoubleSubmission());
    });
</r:script>
</body>
</html>
