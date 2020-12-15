<%@ page import="com.matchi.Facility"%>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilityInvoiceRow.createInvoice.invoiceDetails.message1"/></title>
</head>
<body>
<ul class="nav nav-tabs">
    <li><g:link controller="facilityInvoice" action="index"><g:message code="invoice.label.plural"/></g:link></li>
    <li class="active"><g:link controller="facilityInvoiceRow" action="index"><g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.receipt.message3"/></g:link></li>
</ul>

<g:form>
    <g:render template="/templates/wizard"
              model="[steps: [message(code: 'facilityCustomerMembersInvoice.createMembershipInvoice.wizard.step0'), message(code: 'facilityCustomerMembersInvoice.createMembershipInvoice.wizard.step1'), message(code: 'facilityCustomerMembersInvoice.createMembershipInvoice.wizard.step2'), message(code: 'default.modal.done')], current: 3]"/>

    <h1><g:message code="default.modal.done"/></h1>
    <p class="lead"><g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.receipt.message5"/></p>

    <div class="form-actions">
        <g:submitButton class="btn right btn-info right-margin5" name="done" value="${message(code: 'button.back.label')}"/>
    </div>

</g:form>
</body>
</html>