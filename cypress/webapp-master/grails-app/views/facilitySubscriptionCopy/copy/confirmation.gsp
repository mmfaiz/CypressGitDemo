<%@ page import="com.matchi.Customer; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilitySubscriptionCopy.copy.title"/></title>
</head>
<body>

<g:if test="${!facility?.isMasterFacility()}">
    <g:render template="/templates/wizard"
                  model="[steps: [message(code: 'facilitySubscriptionCopy.copy.wizard.step1'), message(code: 'facilitySubscriptionCopy.copy.wizard.step2'), message(code: 'default.completed.message')], current: 2]"/>

    <h3><g:message code="facilitySubscriptionCopy.copy.confirmation.message2"/></h3>

    <p class="lead">
        <g:message code="facilitySubscriptionCopy.copy.confirmation.message4"
                args="[copySubscriptionResult.fromDate.toString('yyyy-MM-dd'), copySubscriptionResult.toDate.toString('yyyy-MM-dd')]"/>
    </p>

    <div class="form-actions">
        <div class="btn-toolbar pull-right">
            <g:link controller="facilitySubscription" action="index" class="btn btn-info"><g:message code="button.quit.label"/></g:link>
        </div>
    </div>

</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
</body>
</html>
