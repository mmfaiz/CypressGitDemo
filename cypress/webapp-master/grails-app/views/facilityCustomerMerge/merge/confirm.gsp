<%@ page import="com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityCustomerImport.import.title"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link controller="facilityCustomer" action="index"><g:message code="customer.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityCustomerMerge.merge.heading"/></li>
</ul>

<g:render template="/templates/wizard"
              model="[steps: [message(code: 'facilityCustomerMerge.merge.wizard.step1'), message(code: 'facilityCustomerMerge.merge.wizard.step2'), message(code: 'default.completed.message')], current: 1]"/>

<h3><g:message code="facilityCustomerMerge.merge.confirm.message4"/></h3>

<p class="lead">
    <g:message code="facilityCustomerMerge.merge.confirm.message26" args="[primary.fullName()]"/>
    <g:each in="${merges}" status="i" var="merge">
        <strong>${merge.fullName()}</strong><g:if test="${i != (merges.size() - 1)}">,</g:if>
    </g:each>.<br>
    <g:message code="facilityCustomerMerge.merge.confirm.message27"/></p>

<g:form>
    <div class="well">
        <table class="table table-transparent table-condensed table-noborder">
            <tbody>
            <tr>
                <td width="120"><strong><g:message code="customer.number.label"/></strong></td><td width="250">${result.number}</td>
                <td width="120"><strong><g:message code="facilityCustomerMerge.merge.confirm.message6"/></strong></td><td width="250">${result.invoiceAddress1 ?: "-"}</td>
            </tr>
            <tr>
                <td><strong><g:message code="default.name.label"/></strong></td><td>${result.fullName()}</td>
                <td width="120"><strong><g:message code="facilityCustomerMerge.merge.confirm.message8"/></strong></td><td width="250">${result.invoiceAddress2 ?: "-"}</td>
            </tr>
            <tr>
                <td><strong><g:message code="customer.email.label"/></strong></td><td>${result.email ?: "-"}</td>
                <td><strong><g:message code="facilityCustomerMerge.merge.confirm.message10"/></strong></td><td>${result.invoiceZipcode ?: "-"}</td>

            </tr>
            <tr>
                <td><strong><g:message code="customer.address1.label"/></strong></td><td>${result.address1 ?: "-"}</td>
                <td><strong><g:message code="facilityCustomerMerge.merge.confirm.message12"/></strong></td><td>${result.invoiceCity ?: "-"}</td>
            </tr>
            <tr>
                <td><strong><g:message code="customer.address2.label"/></strong></td><td>${result.address2 ?: "-"}</td>
                <td><strong><g:message code="facilityCustomerMerge.merge.confirm.message14"/></strong></td><td>${result.invoiceContact ?: "-"}</td>
            </tr>
            <tr>
                <td><strong><g:message code="customer.zipcode.label"/></strong></td><td>${result.zipcode ?: "-"}</td>
                <td><strong><g:message code="facilityCustomerMerge.merge.confirm.message16"/></strong></td><td>${result.invoiceEmail ?: "-"}</td>
            </tr>
            <tr>
                <td><strong><g:message code="customer.city.label"/></strong></td><td>${result.city ?: "-"}</td>
                <td><strong><g:message code="facilityCustomerMerge.merge.confirm.message18"/></strong></td><td>${result.invoiceTelephone ?: "-"}</td>
            </tr>
            <tr>
                <td><strong><g:message code="customer.telephone.label"/></strong></td><td colspan="3">${result.telephone ?: "-"}</td>
            </tr>
            <tr>
                <td><strong><g:message code="customer.cellphone.label"/></strong></td><td colspan="3">${result.cellphone ?: "-"}</td>
            </tr>
            <tr>
                <td><strong><g:message code="customer.web.label"/></strong></td><td colspan="3">${result.web ?: "-"}</td>
            </tr>
            <g:if test="${result.contact}">
                <tr>
                    <td><strong><g:message code="customer.contact.label"/></strong></td><td colspan="3">${result.contact ?: "-"}</td>
                </tr>
            </g:if>
            <tr>
                <td><strong><g:message code="facilityCustomerMerge.merge.confirm.notes"/></strong></td><td colspan="3">${result.notes ?: "-"}</td>
            </tr>
            </tbody>
        </table>
    </div>
    <div class="form-actions">
        <g:link event="cancel" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>

        <g:submitButton name="submit" type="submit" value="${message(code: 'button.confirm.label')}" data-toggle="button" class="btn btn-success pull-right" show-loader="${message(code: 'default.loader.label')}"/>
        <g:link event="previous" class="btn btn-info pull-right right-margin5"><g:message code="button.back.label"/></g:link>
    </div>
</g:form>
</body>
</html>
