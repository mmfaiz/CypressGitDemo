<%@ page import="com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilityInvoiceRow.createInvoice.invoiceDetails.message1"/></title>
</head>
<body>
<ul class="nav nav-tabs">
    <li><g:link controller="facilityInvoice" action="index"><g:message code="invoice.label.plural"/></g:link></li>
    <li class="active"><g:link controller="facilityInvoiceRow" action="index"><g:message code="facilitySubscriptionInvoice.createSubscriptionInvoice.errorNoPriceLists.message3"/></g:link></li>
</ul>

<g:if test="${!facility?.isMasterFacility()}">
    <g:form>
        <g:render template="/templates/wizard"
                  model="[steps: [message(code: 'facilitySubscriptionInvoice.createSubscriptionInvoice.wizard.step1'), message(code: 'facilitySubscriptionInvoice.createSubscriptionInvoice.wizard.step2')], current: 0]"/>

        <g:if test="${exception}">
            <h1><g:message code="facilitySubscriptionInvoice.createSubscriptionInvoice.errorNoPriceLists.message4"/></h1>
            <p class="lead"><g:message code="facilitySubscriptionInvoice.createSubscriptionInvoice.errorNoPriceLists.message5"/></p>
        </g:if>
        <g:else>
            <h1><g:message code="facilitySubscriptionInvoice.createSubscriptionInvoice.errorNoPriceLists.message6"/></h1>
            <p class="lead"><g:message code="facilitySubscriptionInvoice.createSubscriptionInvoice.errorNoPriceLists.message7"/></p>

            <h2><g:message code="facilitySubscriptionInvoice.createSubscriptionInvoice.errorNoPriceLists.message8"/></h2>
            <p class="lead">
                <g:each in="${missingSportPriceLists}">
                    ${it.name}<br>
                </g:each>
            </p>
        </g:else>

        <hr>
        <div class="form-actions">
            <g:submitButton class="btn left btn-danger" name="cancel" value="${message(code: 'button.back.label')}" />
            <g:if test="${exception}">
                <g:submitButton class="btn right btn-success" name="cancelToPriceLists" value="${message(code: 'facilitySubscriptionInvoice.createSubscriptionInvoice.errorNoPriceLists.message9')}" />
            </g:if>
            <g:else>
                <g:submitButton class="btn right btn-success" name="cancelToPriceListsCreate" value="${message(code: 'facilitySubscriptionInvoice.createSubscriptionInvoice.errorNoPriceLists.message10')}" />
            </g:else>

        </div>

    </g:form>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
</body>
</html>