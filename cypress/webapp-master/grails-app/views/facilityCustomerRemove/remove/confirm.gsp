<%@ page import="org.joda.time.LocalTime; org.joda.time.DateTime; com.matchi.Facility"%>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="customer.remove.label"/></title>
</head>
<body>

<g:form class="form-inline">
    <g:render template="/templates/wizard"
              model="[steps: [message(code: 'facilityCustomerRemove.remove.wizard.step1'), message(code: 'default.completed.message')], current: 0]"/>

    <g:if test="${error}">
        <div class="alert alert-error">
            <a class="close" data-dismiss="alert" href="#">×</a>

            <h4 class="alert-heading">
                ${ new LocalTime().toString("HH:mm:ss") }:  <g:message code="default.error.heading"/>
            </h4>
            ${error}
        </div>
    </g:if>

    <h1><g:message code="facilityCustomerRemove.remove.confirm.message1"/></h1>
    <p class="lead"><g:message code="facilityCustomerRemove.remove.confirm.message2"/></p>

    <table class="table table-transparent">
        <thead>
        <th width="40"></th>
        <th><g:message code="facilityCustomerInvite.invite.confirm.message4"/></th>
        <th><g:message code="customer.label"/></th>
        <th><g:message code="customer.email.label"/></th>
        <th><g:message code="customer.telephone.label"/></th>
        <th><g:message code="customer.type.label"/></th>
        </thead>
        <tbody>
        <g:each in="${customers}" var="customer">
            <tr>
                <td class="center-text">
                    <g:checkBox name="customerId" value="${customer.id}" checked="${true}"/>
                </td>
                <td>${customer.number}</td>
                <td>${customer.fullName()}</td>
                <td>${customer.email}</td>
                <td>${customer.telephone ?: customer.cellphone}</td>
                <td><g:if test="${customer.type}"><g:message code="customer.type.${customer.type}" /></g:if></td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="form-actions">
        <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" />
        <g:submitButton class="btn right btn-success" name="next" value="${message(code: 'button.remove.label')}" show-loader="${message(code: 'default.loader.label')}"/>
    </div>
</g:form>
<r:script>
    $("[rel=tooltip]").tooltip();
</r:script>
</body>
</html>