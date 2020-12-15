<%@ page import="com.matchi.Customer; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityCustomerImport.import.title"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link controller="facilityCustomer" action="index"><g:message code="customer.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityCustomerImport.import.title"/></li>
</ul>

<g:render template="/templates/wizard"
              model="[steps: [message(code: 'facilityCustomerImport.import.wizard.step1'), message(code: 'facilityCustomerImport.import.wizard.step2'), message(code: 'facilityCustomerImport.import.wizard.step3'), message(code: 'default.completed.message')], current: 3]"/>

<h3><g:message code="facilityCustomerImport.import.confirmation.message4"/></h3>

<p class="lead">
    <g:message code="facilityCustomerImport.import.confirmation.message11" args="[importedInfo.imported?.size(), importedInfo.existing?.size()]"/>

    <g:if test="${importedInfo.imported?.size() > 0}">
        <g:message code="facilityCustomerImport.import.confirmation.message5"
                args="[importedInfo.group?.name, createLink(controller: 'facilityGroup', action: 'customers', id: importedInfo.group?.id)]"/>
    </g:if>
</p>
<div class="form-actions">
    <div class="btn-toolbar pull-right">
        <g:link controller="facilityCustomer" action="index" class="btn btn-success"><g:message code="button.done.label"/></g:link>
    </div>
</div>
</body>
</html>
