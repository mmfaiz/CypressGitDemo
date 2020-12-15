<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityCustomerMembers.addMembership.title"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link controller="facilityCustomer" action="index"><g:message code="customer.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityCustomerMembers.addMembership.title"/></li>
</ul>

<g:form>
    <g:render template="/templates/wizard"
            model="[steps: [message(code: 'facilityCustomerMembers.addMembership.wizard.step1'), message(code: 'facilityCustomerMembers.addMembership.wizard.step2'), message(code: 'default.modal.done')], current: 1]"/>

    <h3><g:message code="facilityCustomerMembers.addMembership.heading2"/></h3>

    <p class="lead">
        <g:message code="facilityCustomerMembers.addMembership.description2"
                args="[membershipTypes.find {it.id == cmd.typeId}.name, cmd.startDate.toDate(), cmd.endDate.toDate()]"/>
    </p>

    <g:if test="${membersCount}">
        <div class="alert">
            <button type="button" class="close" data-dismiss="alert"><i class="fa fa-times-circle"></i></button>
            <g:message code="facilityCustomerMembers.addMembership.customersWithMembership" args="[membersCount]"/>
        </div>
    </g:if>

    <div class="well">
        <table class="table table-transparent table-condensed table-noborder">
            <thead>
            <tr>
                <th>#</th>
                <th><g:message code="default.name.label"/></th>
                <th><g:message code="customer.email.label"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${customersInfo}">
                <tr>
                    <td>${it.nr}</td>
                    <td>${it.name}</td>
                    <td>${it.email}</td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="form-actions">
        <g:submitButton class="btn btn-danger" name="cancel"
                value="${message(code: 'button.cancel.label')}"/>
        <g:if test="${customersInfo}">
            <g:submitButton class="btn right btn-success" name="submit"
                    data-toggle="button" show-loader="${message(code: 'default.loader.label')}"
                    value="${message(code: 'button.confirm.label')}"/>
        </g:if>
        <g:submitButton class="btn right btn-info right-margin5"
                name="back" value="${message(code: 'button.back.label')}" />
    </div>
</g:form>
</body>
</html>
