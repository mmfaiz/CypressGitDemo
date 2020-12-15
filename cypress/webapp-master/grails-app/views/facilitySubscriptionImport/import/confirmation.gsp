<%@ page import="com.matchi.Customer; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilitySubscriptionImport.import.title"/></title>
</head>
<body>

<g:if test="${!facility?.isMasterFacility()}">
    <g:render template="/templates/wizard"
              model="[steps: [message(code: 'facilitySubscriptionImport.import.wizard.step1'), message(code: 'facilitySubscriptionImport.import.wizard.step2'), message(code: 'facilitySubscriptionImport.import.wizard.step3'), message(code: 'default.completed.message')], current: 3]"/>

    <h3><g:message code="facilitySubscriptionImport.import.confirmation.message2"/></h3>

    <p class="lead">
        <g:message code="facilitySubscriptionImport.import.confirmation.message4"
                args="[importedInfo.imported.size(), importedInfo.failed.size()]"/>
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
