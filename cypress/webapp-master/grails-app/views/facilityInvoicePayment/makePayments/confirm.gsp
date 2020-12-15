<%@ page import="com.matchi.Customer; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityInvoicePayment.makePayments.title"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link controller="facilityInvoice" action="index"><g:message code="customer.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityCustomerImport.import.title"/></li>
</ul>

<g:render template="/templates/wizard"
          model="[steps: [message(code: 'facilityInvoicePayment.makePayments.wizard.step1'), message(code: 'facilityInvoicePayment.makePayments.wizard.step2'), message(code: 'default.completed.message')], current: 1]"/>

<h3><g:message code="facilityInvoicePayment.makePayments.wizard.step2"/></h3>

<p class="lead">
    <g:message code="facilityInvoicePayment.makePayments.confirm.message16"/>
</p>

<g:form>
    <div class="well">

        <div class="accordion" id="accordion1">
            <div class="accordion-group">
                <div class="accordion-heading" style="background-color: white">
                    <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#rows-success">
                        <strong><g:message code="facilityInvoicePayment.makePayments.confirm.message17" args="[invoicePaymentsInfo.success.size()]"/></strong>
                    </a>
                </div>
                <div id="rows-success" class="accordion-body collapse">
                    <div class="accordion-inner">
                        <g:if test="${invoicePaymentsInfo?.success?.size() > 0}">
                            <table class="table table-transparent">
                                <thead>
                                <tr>
                                    <th width="100"><g:message code="invoice.number.label"/></th>
                                    <th width="300"><g:message code="customer.label"/></th>
                                    <th width="100"><g:message code="facilityInvoicePayment.makePayments.confirm.message8"/></th>
                                    <th width="100"><g:message code="default.date.label"/></th>
                                    <th width="100"><span class="pull-right"><g:message code="invoicePayment.amount.label"/></span></th>
                                </tr>
                                </thead>
                                <g:each in="${invoicePaymentsInfo.success}">
                                    <tr>
                                        <td>${it.invoice.number}</td>
                                        <td>${it.customer}</td>
                                        <td>${it.ocr}</td>
                                        <td>${it.date}</td>
                                        <td><span class="pull-right"><g:formatMoney value="${it.amount}"/></span></td>
                                    </tr>
                                </g:each>
                            </table>
                        </g:if>
                        <g:else>
                            <g:message code="facilityInvoicePayment.makePayments.confirm.message11"/>
                        </g:else>
                    </div>
                </div>
            </div>
        </div>

        <div class="accordion" id="accordion2">
            <div class="accordion-group">
                <div class="accordion-heading" style="background-color: white">
                    <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#rows-error">
                        <strong><g:message code="facilityInvoicePayment.makePayments.confirm.message18" args="[invoicePaymentsInfo.error.size()]"/></strong>
                    </a>
                </div>
                <div id="rows-error" class="accordion-body collapse">
                    <div class="accordion-inner">
                        <g:if test="${invoicePaymentsInfo?.error?.size() > 0}">
                            <table class="table table-transparent">
                                <thead>
                                <tr>
                                    <th width="100"><g:message code="facilityInvoicePayment.makePayments.confirm.message8"/></th>
                                    <th width="100"><g:message code="default.date.label"/></th>
                                    <th width="100"><span class="pull-right"><g:message code="invoicePayment.amount.label"/></span></th>
                                </tr>
                                </thead>
                                <g:each in="${invoicePaymentsInfo.error}">
                                    <tr>
                                        <td>${it.ocr}</td>
                                        <td>${it.date}</td>
                                        <td><span class="pull-right"><g:formatMoney value="${it.amount}"/></span></td>
                                    </tr>
                                </g:each>
                            </table>
                        </g:if>
                        <g:else>
                            <g:message code="facilityInvoicePayment.makePayments.confirm.message11"/>
                        </g:else>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="span12">
            <p class="lead pull-right"><g:message code="facilityInvoicePayment.makePayments.confirm.message19"/>: <b><g:formatMoney value="${invoicePaymentsInfo.total}"/></b></p>
        </div>
    </div>


    <div class="form-actions">

        <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" />

        <g:submitButton id="next" class="btn right btn-success" name="submit" value="${message(code: 'facilityInvoicePayment.makePayments.title')}" show-loader="${message(code: 'default.loader.label')}" />
        <g:submitButton class="btn right btn-info right-margin5" name="back" value="${message(code: 'button.back.label')}" />
    </div>
</g:form>
<r:script>
    $(function() {
        $("#next").focus();
    });
</r:script>
</body>
</html>
