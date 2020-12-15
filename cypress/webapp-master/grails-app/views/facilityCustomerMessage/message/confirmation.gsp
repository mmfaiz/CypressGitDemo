<%@ page import="com.matchi.Customer; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityCustomerMessage.message.createMessage"/></title>
</head>
<body>

<g:render template="/templates/wizard"
              model="[steps: [message(code: 'facilityCustomerMessage.message.wizard.step1'), message(code: 'default.completed.message')], current: 1]"/>

<h3><g:message code="facilityCustomerMessage.message.confirmation.message2"/></h3>

<p class="lead">
    <g:message code="facilityCustomerMessage.message.confirmation.message4" args="[customerIds?.size()]"/>
</p>

<div class="form-actions">
    <div class="btn-toolbar pull-right">
        <g:link url="${returnUrl}" class="btn btn-info"><g:message code="facilityCustomerMessage.message.confirmation.message3" args="${[originTitle]}"/></g:link>
    </div>
</div>
</body>
</html>
