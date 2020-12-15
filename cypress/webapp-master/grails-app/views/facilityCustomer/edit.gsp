<%@ page import="com.matchi.Customer; com.matchi.Sport; com.matchi.Court; com.matchi.facility.FacilityCustomerController"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilityCustomer.edit.message1"/></title>
</head>
<body>

<g:errorMessage bean="${cmd}"/>
<g:errorMessage bean="${customer}"/>

<ul class="breadcrumb">
    <li>
        <g:set var="targetCustomerController" value="${session[FacilityCustomerController.CUSTOMER_LIST_CONTROLLER_KEY] ?: 'facilityCustomer'}"/>
        <g:link controller="${targetCustomerController}" action="index"><g:message code="facilityCustomer.show.list.${targetCustomerController}"/></g:link><span class="divider">/</span>
    </li>
    <li class="active"><g:message code="facilityCustomer.edit.message1"/></li>
</ul>

<g:form action="update" class="form-horizontal form-well">
    <g:hiddenField name="id" value="${cmd?.id ?: customer.id}"/>
    <g:hiddenField name="facilityId" value="${cmd?.facilityId ?: customer.facility.id}"/>
    <g:hiddenField name="returnUrl" value="${returnUrl}"/>

    <div class="form-header">
        <g:message code="facilityCustomer.edit.message1"/><span class="ingress"><g:message code="default.form.create.instructions"/></span>
    </div>
    <fieldset>
        <g:render template="form"/>

        <div class="form-actions">
            <g:actionSubmit action="update" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
            <g:link action="show" params="${params}" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
        </div>
    </fieldset>
</g:form>
</body>
</html>
