<%@ page import="org.joda.time.LocalTime; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilitySubscriptionImport.import.upload.message1"/></title>
</head>
<body>
<g:if test="${!facility?.isMasterFacility()}">
    <g:if test="${error}">
        <div class="alert alert-error">
            <a class="close" data-dismiss="alert" href="#">×</a>

            <h4 class="alert-heading">
                ${ new LocalTime().toString("HH:mm:ss") }:  <g:message code="default.error.heading"/>
            </h4>
            ${error}
        </div>
    </g:if>

    <ul class="breadcrumb">
        <li><g:link controller="facilitySubscription" action="index"><g:message code="subscription.label"/></g:link> <span class="divider">/</span></li>
        <li class="active"><g:message code="facilitySubscriptionImport.import.upload.message1"/></li>
    </ul>

    <g:render template="/templates/wizard"
              model="[steps: [message(code: 'facilitySubscriptionImport.import.wizard.step1'), message(code: 'facilitySubscriptionImport.import.wizard.step2'), message(code: 'facilitySubscriptionImport.import.wizard.step3'), message(code: 'default.completed.message')], current: 0]"/>

    <h3><g:message code="facilitySubscriptionImport.import.upload.message4"/></h3>

    <p class="lead">
        <g:message code="facilitySubscriptionImport.import.upload.message5" args="['/download/example_subscription_import.xls']"/></p>

    <g:uploadForm>
        <div class="well">
            <input type="file" name="file" id="file" value="${message(code: 'default.input.file.value')}"/>
        </div>
        <div class="form-actions">
            <g:link event="cancel" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
            <g:submitButton name="submit" type="submit" value="${message(code: 'button.next.label')}" show-loader="${message(code: 'default.loader.label')}"
                            data-toggle="button" class="btn btn-success pull-right"/>
        </div>
    </g:uploadForm>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
</body>
</html>
