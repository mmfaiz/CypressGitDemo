<%@ page import="com.matchi.Customer; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityInvoicePayment.makePayments.title"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link controller="facilityCustomer" action="index"><g:message code="customer.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityInvoicePayment.makePayments.title"/></li>
</ul>

<g:render template="/templates/wizard"
              model="[steps: [message(code: 'facilityInvoicePayment.makePayments.wizard.step1'), message(code: 'facilityInvoicePayment.makePayments.wizard.step2'), message(code: 'default.completed.message')], current: 2]"/>

<h3><g:message code="facilityInvoicePayment.makePayments.confirmation.message4"/></h3>

<p class="lead">
    <g:message code="facilityInvoicePayment.makePayments.confirmation.message7" args="[successful]"/>

    <g:if test="${errors > 0}">
        <br><g:message code="facilityInvoicePayment.makePayments.confirmation.message5"/>
    </g:if>
</p>
<div class="form-actions">
    <div class="btn-toolbar pull-right">
        <g:link controller="facilityInvoice" action="index" class="btn btn-info"><g:message code="button.quit.label"/></g:link>
    </div>
</div>
</body>
</html>
