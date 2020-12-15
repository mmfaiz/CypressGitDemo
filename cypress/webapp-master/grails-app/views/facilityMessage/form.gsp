<%@ page import="com.matchi.messages.FacilityMessage" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityMessage.form.message1"/></title>
    <r:require modules="jquery-validate,jquery-timepicker"/>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link controller="facilityAdministration" action="index"><g:message code="facilityMessage.form.message2"/></g:link> <span class="divider">/</span></li>
    <li><g:link action="index"><g:message code="facilityMessage.form.message3"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityMessage.form.message4"/></li>
</ul>
<g:errorMessage bean="${group}"/>

<g:form action="save" class="form-horizontal form-well">
    <g:hiddenField name="id" value="${params?.id}" />
    <div class="form-header">
        <g:message code="facilityMessage.form.message15"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
    </div>
    <fieldset>
        <div class="control-group ${hasErrors(bean:message, field:'headline', 'error')}">
            <label class="control-label" for="name"><g:message code="facilityMessage.form.message6"/></label>
            <div class="controls">
                <g:each in="${[com.matchi.messages.FacilityMessage.Channel.BOOKING_CONFIRMED]}">
                    <g:checkBox name="channels" value="${it}"></g:checkBox> - ${it}
                </g:each>
            </div>
        </div>

        <div class="control-group ${hasErrors(bean:message, field:'headline', 'error')}">
            <label class="control-label" for="active"><g:message code="facilityMessage.active.label"/></label>
            <div class="controls">
                <g:checkBox name="active" checked="${cmd.active}"/>
                <span class="help-inline"><g:message code="facilityMessage.form.message8"/></span>
            </div>
        </div>

        <div class="control-group ${hasErrors(bean:message, field:'headline', 'error')}">
            <label class="control-label" for="name"><g:message code="facilityMessage.form.message9"/></label>
            <div class="controls">
                <input class="span2 center-text" type="text" name="validFromDate" id="validFromDate"
                       value="${cmd.validFromDate?.toString("yyyy-MM-dd")}" />

                <g:textField name="validFromTime" class="span1" value="${cmd.validFromTime?.toString("HH:mm")}"/>
                <span> <g:message code="default.to.label"/></span>
                <input class="span2 center-text" type="text" name="validToDate" id="validToDate"
                       value="${cmd.validToDate?.toString("yyyy-MM-dd")}" />

                <g:textField name="validToTime" class="span1" value="${cmd.validToTime?.toString("HH:mm")}"/>
                <span class="help-inline"><g:message code="facilityMessage.form.message11"/></span>
            </div>
        </div>

        <div class="control-group ${hasErrors(bean:message, field:'headline', 'error')}">
            <label class="control-label" for="name"><g:message code="facilityMessage.form.message12"/></label>
            <div class="controls">
                <g:textField name="headline" value="${cmd?.headline}" class="span8"/>
            </div>
        </div>

        <div class="control-group ${hasErrors(bean:message, field:'content', 'error')}">
            <label class="control-label" for="description"><g:message code="facilityMessage.form.message13"/></label>
            <div class="controls">
                <g:textArea name="content" rows="5" cols="30" value="${cmd?.content}" class="span8"/>
            </div>
        </div>

        <div class="form-actions">
            <g:actionSubmit action="save" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
            <g:if test="${params.id}">
                <g:actionSubmit action="delete" value="${message(code: 'button.delete.label')}" class="btn btn-inverse"/>
            </g:if>
            <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
        </div>
    </fieldset>
</g:form>
<r:script>

        $(document).ready(function() {
            $("#name").focus();

            $('#validFromTime').addTimePicker({
                hourText: '${message(code: 'default.timepicker.hour')}',
                minuteText: '${message(code: 'default.timepicker.minute')}'
            });
            $('#validToTime').addTimePicker({
                hourText: '${message(code: 'default.timepicker.hour')}',
                minuteText: '${message(code: 'default.timepicker.minute')}'
            });

            var startDate = new Date();

            $("#validFromDate").datepicker({
                autoSize: true,
                dateFormat: 'yy-mm-dd',
                minDate: new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate() + 1)
            });

            $("#validToDate").datepicker({
                autoSize: true,
                dateFormat: 'yy-mm-dd',
                minDate: new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate() + 1)
            });
        });

</r:script>
</body>
</html>