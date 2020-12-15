<%@ page import="com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilityInvoiceRow.createInvoice.invoiceDetails.message1"/></title>
</head>
<body>
<ul class="nav nav-tabs">
    <li><g:link controller="facilityInvoice" action="index"><g:message code="invoice.label.plural"/></g:link></li>
    <li class="active"><g:link controller="facilityInvoiceRow" action="index"><g:message code="facilityInvoiceRow.createInvoice.selectInvoices.message3"/></g:link></li>
</ul>

<g:form>
    <g:render template="/templates/wizard"
              model="[steps: [message(code: 'facilityInvoiceRow.createInvoice.wizard.step1'), message(code: 'facilityInvoiceRow.createInvoice.wizard.step2')], current: 1]"/>
    <h1><g:message code="facilityInvoiceRow.createInvoice.selectInvoices.message18" args="[invoices.size()]"/></h1>
    <p class="lead"><g:message code="facilityInvoiceRow.createInvoice.selectInvoices.message4"/></p>
    <hr>

    <div class="row">
        <div class="span2">
            <strong><g:message code="invoice.invoiceDate.label"/></strong><br>
            ${invoiceDetails?.invoiceDate?.toString("${message(code:"date.format.dateOnly")}")}
        </div>

        <div class="span2">
            <strong><g:message code="invoice.expirationDate.label"/></strong><br>
            ${invoiceDetails?.getExpirationDate()?.toString("${message(code:"date.format.dateOnly")}")}
        </div>
        <div class="span4">
            <strong><g:message code="facilityInvoiceRow.createInvoice.selectInvoices.message7"/></strong><br>
            <p>${invoiceDetails.text}</p>
        </div>
        <div class="span4">
            <strong><g:message code="facilityInvoiceRow.createInvoice.selectInvoices.message8"/></strong><br>
            <p><g:formatMoney value="${total}" facility="${facility}"/></p>
        </div>
    </div>

    <hr>

    <div class="well">

        <div class="accordion" id="accordion2">

            <g:each in="${invoices}">
                <div class="accordion-group">
                    <div class="accordion-heading" style="background-color: white">
                        <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#rows-${it.customer.id}-${it.organization?.id}">
                            <b>${it.customer.fullName()}</b> ${it.customer.email}
                            <span class="pull-right"><g:formatMoney value="${it.getTotalIncludingVAT()}" facility="${facility}"/></span>
                        </a>
                    </div>
                    <div id="rows-${it.customer.id}-${it.organization?.id}" class="accordion-body collapse">
                        <div class="accordion-inner">
                            <table class="table table-transparent">
                                <thead>
                                <tr>
                                    <th width="400"><g:message code="invoiceRow.description.label"/></th>
                                    <th width="25"><g:message code="default.quantity.label"/></th>
                                    <th width="55"><g:message code="default.price.label"/></th>
                                    <th width="35"><g:message code="default.discount.label"/></th>
                                    <th width="70"><g:message code="default.vat.label"/></th>
                                    <th width="35"><span class="pull-right"><g:message code="default.total.label"/></span></th>
                                </tr>
                                </thead>
                                <g:each in="${it.rows}" var="row">
                                    <tr>
                                        <td>${row.description}</td>
                                        <td>${row.amount}${row.unit}</td>
                                        <td><g:formatMoney value="${row.price}" facility="${facility}"/></td>
                                        <td><g:formatDiscount invoiceRow="${row}" facility="${facility}"/></td>
                                        <td><g:formatMoney value="${row.getTotalVAT()}" facility="${facility}"/> (${row.vat?.intValue()}%)</td>
                                        <td><span class="pull-right"><g:formatMoney value="${row.getTotalIncludingVAT()}" facility="${facility}"/></span></td>
                                    </tr>
                                </g:each>
                            </table>

                            <div class="row">
                                <div class="pull-right" style="width:230px;padding-right: 10px">
                                    <dl class="dl-horizontal">
                                        <dt><g:message code="facilityInvoiceRow.createInvoice.selectInvoices.message15"/></dt>
                                        <dd><g:formatMoney value="${it.getTotalExcludingVAT()}" facility="${facility}"/></dd>
                                        <dt><g:message code="default.vat.label"/></dt>
                                        <dd><g:formatMoney value="${it.getTotalVAT()}" facility="${facility}"/>&nbsp;</dd>
                                        <dt>&nbsp;</dt>
                                        <dd>&nbsp;</dd>
                                        <dt><big><g:message code="default.total.label"/></big></dt>
                                        <dd><big><g:formatMoney value="${it.getTotalIncludingVAT()}" facility="${facility}"/></big></dd>
                                    </dl>
                                </div>
                            </div>

                        </div>

                    </div>


                </div>
            </g:each>
        </div>

    </div>

    <div class="row">
        <div class="span12">
            <p class="lead pull-right">
                <g:message code="facilityInvoiceRow.createInvoice.selectInvoices.message19"/>: <b><g:formatMoney value="${total}" facility="${facility}"/></b>
            </p>
        </div>
    </div>

    <div class="form-actions">

        <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" />

        <g:submitButton id="submit" class="btn right btn-success" name="next" value="${message(code: 'adminStatistics.index.createInvoices')}" show-loader="${message(code: 'default.loader.label')}" />
        <g:submitButton class="btn right btn-info right-margin5" name="back" value="${message(code: 'button.back.label')}" />

    </div>

    <g:if test="${facility.hasFortnox() && invoices.size > 100}">
        <div class="row">
            <div class="span12">
                <p class="lead pull-right">
                    ${message(code: 'facilityInvoiceRow.createInvoice.selectInvoices.fortnoxLongRunningOperationInformation')}
                </p>
            </div>
        </div>
    </g:if>

</g:form>
</body>
<r:script>
    $(document).ready(function() {
        $("#submit").focus();
    });
</r:script>
</html>