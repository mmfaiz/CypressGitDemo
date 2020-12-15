<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="default.member.label.plural"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link controller="facilityCustomer" action="index"><g:message code="customer.label.plural"/></g:link> <span class="divider">/</span></li>
    <li><g:link controller="facilityCustomerMembers" action="index"><g:message code="default.member.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityCustomerMembers.cancel.confirm.message4"/></li>
</ul>

<h3><g:message code="facilityCustomerMembers.cancel.confirm.message5"/></h3>

<p class="lead">
    <g:message code="facilityCustomerMembers.cancel.confirm.message8"/>
</p>

<g:form>
    <div class="well">
        <table class="table table-transparent table-condensed table-noborder">
            <thead>
            <tr>
                <th><g:message code="customer.number.label"/></th>
                <th><g:message code="default.name.label"/></th>
                <th><g:message code="membership.type.label"/></th>
                <th><g:message code="membership.endDate.label.long"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${membersInfo}" var="info">
                <tr>
                    <td>${info.customerNr}</td>
                    <td>${info.customerName}</td>
                    <td>${info.membershipType ?: '-'}</td>
                    <td><g:formatDate date="${info.membershipEndDate}" formatName="date.format.dateOnly"/></td>
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

</body>
</html>
