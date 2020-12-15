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
              model="[steps: [message(code: 'facilityCustomerMerge.merge.wizard.step1'), message(code: 'facilityCustomerMerge.merge.wizard.step2'), message(code: 'default.completed.message')], current: 2]"/>

<h3><g:message code="default.modal.done"/></h3>

<p class="lead">
    <g:message code="facilityCustomerMerge.merge.confirmation.message7"/><br>
    <g:message code="facilityCustomerMerge.merge.confirmation.message8"
            args="[createLink(controller: 'facilityCustomer', action: 'show', id: primary.id), primary.fullName()]"/>
    <g:each in="${merges}" var="merge" status="i">
        ${merge.fullName()}<g:if test="${i != (merges.size() - 1)}">,</g:if>
    </g:each>
    <g:message code="facilityCustomerMerge.merge.confirmation.message5"
            args="[createLink(controller: 'facilityCustomerArchive', action: 'index')]"/></p>

<g:uploadForm>
    <div class="form-actions">
        <div class="btn-toolbar pull-right">
            <g:link controller="facilityCustomer" action="index" class="btn btn-info"><g:message code="button.quit.label"/></g:link>
        </div>
    </div>
</g:uploadForm>
</body>
</html>
