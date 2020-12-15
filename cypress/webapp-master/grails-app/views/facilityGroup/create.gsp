<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityGroup.create.message1"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="group.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityGroup.create.message3"/></li>
</ul>
<g:if test="${!facility?.isMasterFacility()}">
    <g:errorMessage bean="${group}"/>

    <g:form action="save" class="form-horizontal form-well" name="facilityGroupsCreateFrm">
        <g:hiddenField name="id" value="${group?.id}" />
        <g:hiddenField name="version" value="${group?.version}" />
        <div class="form-header">
            <g:message code="facilityGroup.create.message8"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
        </div>
        <fieldset>
            <div class="control-group ${hasErrors(bean:group, field:'name', 'error')}">
                <label class="control-label" for="name"><g:message code="group.name.label" default="Namn" /></label>
                <div class="controls">
                    <g:textField name="name" value="${group?.name}" class="span8"/>
                </div>
            </div>
            <div class="control-group ${hasErrors(bean:group, field:'description', 'error')}">
                <label class="control-label" for="description"><g:message code="group.description.label" default="Beskrivning" /></label>
                <div class="controls">
                    <g:textArea name="description" rows="10" cols="30" value="${group?.description}" class="span8"/>
                </div>
            </div>
            <g:if test="${categories?.size() > 0}">
                <hr>
                <div class="control-group">
                    <label class="control-label" for="name"><g:message code="facilityGroup.create.message5"/></label>
                    <div class="controls">
                        <g:each in="${categories}" var="category">
                            <label class="checkbox">
                                <g:checkBox name="categoryIds" class="styled" checked="${false}" value="${category.id}" />
                                ${category.name}
                            </label>
                        </g:each>

                        <p class="help-block"><g:message code="facilityGroup.create.message6"/></p>
                    </div>
                </div>
            </g:if>
            <div class="form-actions">
                <g:actionSubmit action="save" value="${g.message(code: 'button.save.label')}" class="btn btn-success"/>
                <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
            </div>
        </fieldset>
    </g:form>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
<r:script>
    $(document).ready(function () {
        $("#facilityGroupsCreateFrm").preventDoubleSubmission({});
    });
    $("#name").focus();
</r:script>
</body>
</html>
