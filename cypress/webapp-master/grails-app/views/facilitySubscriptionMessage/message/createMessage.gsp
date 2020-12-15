<%@ page import="com.matchi.FacilityProperty; org.joda.time.LocalTime; org.joda.time.DateTime; com.matchi.Facility"%>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilitySubscriptionMessage.message.createMessage.message1"/></title>
    <r:require modules="bootstrap-wysihtml5"/>
</head>
<body>

<g:if test="${!facility?.isMasterFacility()}">
    <g:form class="form-inline">
        <g:render template="/templates/wizard"
                  model="[steps: [message(code: 'facilitySubscriptionMessage.message.wizard.step1'), message(code: 'default.completed.message')], current: 0]"/>

        <g:if test="${error}">
            <div class="alert alert-error">
                <a class="close" data-dismiss="alert" href="#">×</a>

                <h4 class="alert-heading">
                    ${ new LocalTime().toString("HH:mm:ss") }:  <g:message code="default.error.heading"/>
                </h4>
                ${error}
            </div>
        </g:if>

        <h1><g:message code="facilitySubscriptionMessage.message.createMessage.message2"/></h1>
        <p class="lead">
            <g:message code="facilitySubscriptionMessage.message.createMessage.message5" args="[subscriptionIds.size()]"/>
        </p>
        <div class="well">
            <div class="row">
                <div class="span12">
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label" for="fromMail"><g:message code="facilitySubscriptionMessage.message.createMessage.message3"/></label>
                            <select id="fromMail" name="fromMail">
                                <option value="${user.email}">${user.email}</option>
                                <option value="${facility.email}">${facility.email}</option>
                            </select>&nbsp;&nbsp;&nbsp;
                            <label class="control-label" for="bccMail"><g:message code="facilitySubscriptionMessage.message.createMessage.message4"/></label>
                            <select id="bccMail" name="bccMail">
                                <option value="${user.email}">${user.email}</option>
                                <option value="${facility.email}">${facility.email}</option>
                            </select>
                        </div>
                    </fieldset>
                </div>
            </div>
        </div>

        <g:textArea id="message" name="message" rows="15" cols="100" class="span12" placeholder="${message(code: 'facilityCustomerMessage.message.createMessage.placeholder')}" value="${facilityMessage?.value}"/>

        <div class="form-actions">
            <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" tabindex="1"/>
            <g:submitButton class="btn right btn-success" name="next" value="${message(code: 'button.submit.label')}" show-loader="${message(code: 'default.loader.label')}" tabindex="0"/>
        </div>
    </g:form>
    <r:script>
        $('#message').wysihtml5({
            parserRules: wysihtml5ParserRules,
            stylesheets: ["${resource(dir: 'js/bootstrap/bootstrap-wysihtml5', file: 'wysiwyg-color.css')}"]
        });
        $("#message").focus();
    </r:script>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
</body>
</html>