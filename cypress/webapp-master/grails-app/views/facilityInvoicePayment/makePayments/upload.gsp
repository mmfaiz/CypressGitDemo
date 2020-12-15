<%@ page import="org.joda.time.LocalTime; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityInvoicePayment.makePayments.title"/></title>
</head>
<body>
<g:if test="${error}">
    <div class="alert alert-error">
        <a class="close" data-dismiss="alert" href="#">×</a>

        <h4 class="alert-heading">
            ${ new LocalTime().toString("HH:mm:ss") }:  <g:message code="default.error.heading"/>
        </h4>
        ${error}
    </div>
</g:if>

<ul class="breadcrumb">
    <li><g:link controller="facilityInvoice" action="index"><g:message code="invoice.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityInvoicePayment.makePayments.title"/></li>
</ul>

<g:render template="/templates/wizard"
              model="[steps: [message(code: 'facilityInvoicePayment.makePayments.wizard.step1'), message(code: 'facilityInvoicePayment.makePayments.wizard.step2'), message(code: 'default.completed.message')], current: 0]"/>

<h3><g:message code="facilityInvoicePayment.makePayments.upload.message4"/></h3>

<p class="lead">
    <g:message code="facilityInvoicePayment.makePayments.upload.message5"/>
</p>

<g:uploadForm>
    <div class="well">
        <input type="file" name="file" id="file" value="${message(code: 'default.input.file.value')}"/>
    </div>
    <div class="form-actions">
        <g:link event="cancel" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
        <g:submitButton name="submit" type="submit" value="${message(code: 'button.next.label')}" show-loader="${message(code: 'default.loader.label')}"
                        data-toggle="button" class="btn btn-success pull-right"/>
    </div>
</g:uploadForm>
</body>
</html>
