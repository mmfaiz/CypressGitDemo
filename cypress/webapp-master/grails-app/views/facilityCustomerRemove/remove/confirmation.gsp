<%@ page import="com.matchi.Customer; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="customer.remove.label"/></title>
</head>
<body>

<g:render template="/templates/wizard"
          model="[steps: [message(code: 'facilityCustomerRemove.remove.wizard.step1'), message(code: 'default.completed.message')], current: 1]"/>

<h3><g:message code="facilityCustomerRemove.remove.confirmation.message1"/></h3>

<p class="lead">
    <g:message code="facilityCustomerRemove.remove.confirmation.message2" args="[customers.size()]"/>
</p>

<div class="form-actions">
    <div class="btn-toolbar pull-right">
        <g:link url="${returnUrl}" class="btn btn-info"><g:message code="button.back.label"/></g:link>
    </div>
</div>
</body>
</html>
