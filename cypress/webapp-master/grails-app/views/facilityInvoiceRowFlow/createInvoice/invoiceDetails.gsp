<%@ page contentType="text/html;charset=UTF-8" import="org.joda.time.LocalTime" %>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilityInvoiceRow.createInvoice.invoiceDetails.message1"/></title>
    <r:require modules="jquery-timepicker, matchi-invoice" />
</head>
<body>
<ul class="nav nav-tabs">
    <li><g:link controller="facilityInvoice" action="index"><g:message code="invoice.label.plural"/></g:link></li>
    <li class="active"><g:link controller="facilityInvoiceRow" action="index"><g:message code="facilityInvoiceRow.createInvoice.invoiceDetails.message3"/></g:link></li>
</ul>

<g:if test="${error}">
    <div class="alert alert-error">
        <a class="close" data-dismiss="alert" href="#">×</a>
        <h4 class="alert-heading">
            ${new LocalTime().toString("HH:mm:ss")}
        </h4>
        ${error}
    </div>
</g:if>

<g:render template="/templates/wizard"
          model="[steps: [message(code: 'facilityInvoiceRow.createInvoice.wizard.step1'), message(code: 'facilityInvoiceRow.createInvoice.wizard.step2')], current: 0]"/>

<h1><g:message code="facilityInvoiceRow.createInvoice.invoiceDetails.message4"/></h1>
<p class="lead"></p>

<g:form class="form-inline">

    <div class="well">
        <div class="row">
            <div class="span12">

                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="expirationDate"><g:message code="invoice.invoiceDate.label"/></label>

                        <g:textField class="span2 right-margin10" id="showInvoiceDate" dateFormat1="${message(code: 'date.format.dateOnly.small')}" dateFormat2="${message(code: 'date.format.dateOnly')}"
                                     name="showInvoiceDate" value="${invoiceDetails?.invoiceDate?.toString("${message(code: 'date.format.dateOnly')}")}"/>
                        <g:hiddenField id="invoiceDate" name="invoiceDate" value="${invoiceDetails?.invoiceDate}"/>
                        <g:message code="default.terms.label"/>
                        <g:textField class="span1" name="expirationDays" id="expirationDays"
                                     value="${invoiceDetails?.expirationDays}"/> <g:message code="facilityInvoice.edit.message30"/>
                        <span class="help-inline"><g:message code="invoice.expirationDate.label"/>: <span id="expirationDate"><g:formatDate date="${invoiceDetails?.expirationDate?.toDate()}" formatName="date.format.dateOnly"/></span></span>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="description"><g:message code="facilityInvoiceRow.createInvoice.invoiceDetails.message8"/></label>
                        <div class="controls">
                            <g:textArea name="text" rows="3" cols="15" value="${invoiceDetails?.text}" class="span10" maxlength="${com.matchi.InvoiceService.TEXT_MAX_INPUT_SIZE}"/>
                            <div id="inputCounter">
                                <label><span id="inputCounterCurrent"></span> / <span id="inputCounterMax"></span></label>
                            </div>
                        </div>
                    </div>
                    <g:if test="${facility?.useInvoiceFees}">
                        <div class="control-group">
                            <label class="checkbox">
                                <g:checkBox name="useInvoiceFees" value="${invoiceDetails?.useInvoiceFees}"/>
                                <g:message code="facilityInvoiceRow.createInvoice.invoiceDetails.message9"/>
                                <g:inputHelp title="${message(code: 'facilityInvoiceRow.createInvoice.invoiceDetails.message10')}"/>
                            </label>
                        </div>
                    </g:if>
                    <div class="control-group">
                        <label class="checkbox">
                            <g:checkBox name="createNoCreditInvoices" value="${invoiceDetails?.createNoCreditInvoices ?: true}"/>
                            <g:message code="facilityInvoiceRow.createInvoice.invoiceDetails.message11"/>
                            <g:inputHelp title="${message(code: 'facilityInvoiceRow.createInvoice.invoiceDetails.message12')}"/>
                        </label>
                    </div>
                </fieldset>
            </div>



        </div>
    </div>

    <div class="form-actions">
        <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" />
        <g:submitButton class="btn right btn-success" name="next" value="${message(code: 'button.next.label')}" show-loader="${message(code: 'default.loader.label')}"/>
    </div>

</g:form>
<r:script>
    $(document).ready(function() {
        $("[rel='tooltip']").tooltip();
        $("#invoiceDate").focus();

        var $text = $("#text");
        var textLimit = ${com.matchi.InvoiceService.TEXT_MAX_INPUT_SIZE};

        function textOnChange() {
            var text = $text.val();
            $("#inputCounterCurrent").html(text.length);
            $("#inputCounterMax").html(textLimit);
        }

        $text.on("keyup input", function() {

            textOnChange();
        });

        textOnChange();

    });

</r:script>

</body>
</html>