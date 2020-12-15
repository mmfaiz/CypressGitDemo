<%@ page import="com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilityInvoiceRow.createInvoiceRow.confirmRows.message1"/></title>
</head>
<body>

<g:render template="/facilityInvoiceRowFlow/createInvoiceRow/invoiceRowBreadcrumb"/>

<ul class="nav nav-tabs">
    <li><g:link controller="facilityInvoice" action="index"><g:message code="invoice.label.plural"/></g:link></li>
    <li class="active"><g:link controller="facilityInvoiceRow" action="index"><g:message code="facilityInvoiceRow.createInvoiceRow.confirmRows.message1"/></g:link></li>
</ul>

<g:render template="/templates/wizard" model="[steps: [message(code: 'facilityInvoiceRow.createInvoiceRow.wizard.step1'), message(code: 'facilityInvoiceRow.createInvoiceRow.wizard.step2'), message(code: 'facilityInvoiceRow.createInvoiceRow.wizard.step3')], current: 2]"/>

<g:form>

    <h1><g:message code="facilityInvoiceRow.createInvoiceRow.confirmRows.message4"/></h1>
    <p class="lead"><g:message code="facilityInvoiceRow.createInvoiceRow.confirmRows.message5"/></p>

    <div class="well">
    <table class="table table-transparent">
        <thead>
            <tr>
                <th><g:message code="customer.label"/></th>
                <th><g:message code="default.article.label"/></th>
                <th><g:message code="invoiceRow.description.label"/></th>
                <th><g:message code="default.quantity.label"/></th>
                <th><g:message code="default.price.label"/></th>
                <th><g:message code="default.discount.label"/></th>
                <th><g:message code="default.vat.label"/></th>
                <th><g:message code="default.total.label"/></th>
            </tr>
        </thead>
        <g:each in="${invoices}" var="invoice">
        <tr>
            <td width="150">${invoice.customer.fullName()}</td>
            <td width="50">${invoice.externalArticleId}</td>
            <td>${invoice.description}</td>
            <td width="50">${invoice.amount}${invoice.unit}</td>
            <td width="50"><g:formatMoney value="${invoice.price}"/></td>
            <td width="50"><g:formatDiscount invoiceRow="${invoice}"/></td>
            <td width="50">${invoice.vat}%</td>

            <td width="50"><g:formatMoney value="${invoice.getTotalIncludingVAT()}"/></td>
        </tr>
        </g:each>

    </table>
    </div>

    <div class="form-actions">
        <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" />
        <g:submitButton class="btn right btn-success" name="next" value="${message(code: 'facilityInvoiceRow.createInvoiceRow')}" id="btnNext" show-loader="${message(code: 'default.loader.label')}" />
        <g:submitButton class="btn right btn-info right-margin5" name="back" value="${message(code: 'button.back.label')}"  />
    </div>

</g:form>

<r:script>
    $(document).ready(function() {
        $("#btnNext").focus();
    });

</r:script>
</body>
</html>