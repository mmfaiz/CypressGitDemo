<%@ page import="com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityCustomerArchive.unarchive.confirm.message1"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link controller="facilityCustomer" action="index"><g:message code="customer.label.plural"/></g:link> <span class="divider">/</span></li>
    <li><g:link controller="facilityCustomerArchive" action="index"><g:message code="facilityCustomerArchive.unarchive.confirm.message3"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityCustomerArchive.unarchive.confirm.message4"/></li>
</ul>

<h3><g:message code="facilityCustomerArchive.unarchive.confirm.message5"/></h3>
<g:if test="${!customers}">
    <p class="lead">
        <g:message code="facilityCustomerArchive.unarchive.confirm.message6"/></p>

    <div class="form-actions">
        <g:link event="cancel" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
    </div>
</g:if>
<g:else>
    <p class="lead">
        <g:message code="facilityCustomerArchive.unarchive.confirm.message13"/></p>

    <g:form>
        <div class="well">
            <table class="table table-transparent table-condensed table-noborder">
                <thead>
                    <tr>
                        <th><g:message code="customer.number.label"/></th>
                        <th><g:message code="default.name.label"/></th>
                        <th><g:message code="customer.email.label"/></th>
                        <th><g:message code="customer.telephone.label"/></th>
                    </tr>
                </thead>
                <tbody>
                <g:each in="${customers}" var="customer">
                    <tr>
                        <td>${customer.number}</td>
                        <td>${customer.fullName()}</td>
                        <td>${customer.email}</td>
                        <td>${customer.telephone}</td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
        <div class="form-actions">
            <g:link event="cancel" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>

            <g:submitButton name="submit" type="submit" value="${message(code: 'button.confirm.label')}" data-toggle="button" class="btn btn-success pull-right" show-loader="${message(code: 'default.loader.label')}"/>
        </div>
    </g:form>
</g:else>
</body>
</html>
