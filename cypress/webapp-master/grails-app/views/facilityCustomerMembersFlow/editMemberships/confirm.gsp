<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="default.member.label.plural"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link controller="facilityCustomer" action="index"><g:message code="customer.label.plural"/></g:link> <span class="divider">/</span></li>
    <li><g:link controller="facilityCustomerMembers" action="index"><g:message code="default.member.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityCustomerMembers.editMemberships.title"/></li>
</ul>

<g:form>
    <g:render template="/templates/wizard"
            model="[steps: [message(code: 'facilityCustomerMembers.editMemberships.wizard.step1'), message(code: 'facilityCustomerMembers.editMemberships.wizard.step2'), message(code: 'default.modal.done')], current: 1]"/>

    <h3><g:message code="facilityCustomerMembers.editMemberships.heading2"/></h3>

    <p class="lead">
        <g:message code="facilityCustomerMembers.editMemberships.description2"/>
    </p>

    <div class="well">
        <table class="table table-transparent table-condensed table-noborder">
            <thead>
            <tr>
                <th width="24"></th>
                <th>#</th>
                <th><g:message code="default.name.label"/></th>
                <th><g:message code="membership.label"/></th>
                <th><g:message code="membership.startingGracePeriodDays.label"/></th>
                <th><g:message code="membership.startDate.label.long"/></th>
                <th><g:message code="membership.endDate.label.long"/></th>
                <th><g:message code="membership.gracePeriodEndDate.label.long"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${membersInfo}" var="info">
                <tr>
                    <td>
                        <g:if test="${info.errors}">
                            <span class="label label-warning" rel="tooltip"
                                    title="<div class='text-left'>${message(code: 'facilityCustomerMembers.editMemberships.confirm.errors.title')}:<ul><li>${info.errors.join('</li><li>')}</li></ul></div>"><i class="icon-exclamation-sign"></i></span>
                        </g:if>
                    </td>
                    <td>${info.customerNr}</td>
                    <td>${info.customerName}</td>
                    <td>
                        ${info.membershipType ?: '-'}
                        <g:if test="${info.newMembershipTypeId}">
                            →
                            <strong>${membershipTypes.find {it.id == info.newMembershipTypeId}.name}</strong>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${info.membershipStartingGracePeriodAllowed}">
                            ${info.membershipStartingGracePeriod ?: 0}
                            <g:if test="${info.newStartingGracePeriod}">
                                →
                                <strong>${info.newStartingGracePeriod}</strong>
                            </g:if>
                        </g:if>
                    </td>
                    <td>
                        <g:formatDate date="${info.membershipStartDate.toDate()}" formatName="date.format.dateOnly"/>
                        <g:if test="${info.newStartDate}">
                            →
                            <strong><g:formatDate date="${info.newStartDate.toDate()}" formatName="date.format.dateOnly"/></strong>
                        </g:if>
                    </td>
                    <td>
                        <g:formatDate date="${info.membershipEndDate.toDate()}" formatName="date.format.dateOnly"/>
                        <g:if test="${info.newEndDate}">
                            →
                            <strong><g:formatDate date="${info.newEndDate.toDate()}" formatName="date.format.dateOnly"/></strong>
                        </g:if>
                    </td>
                    <td>
                        <g:formatDate date="${info.membershipGracePeriodEndDate.toDate()}"
                                formatName="date.format.dateOnly"/>
                        <g:if test="${info.newGracePeriodEndDate}">
                            →
                            <strong><g:formatDate date="${info.newGracePeriodEndDate.toDate()}" formatName="date.format.dateOnly"/></strong>
                        </g:if>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="form-actions">
        <g:submitButton class="btn btn-danger" name="cancel"
                value="${message(code: 'button.cancel.label')}"/>
        <g:submitButton class="btn right btn-success" name="submit"
                data-toggle="button" show-loader="${message(code: 'default.loader.label')}"
                value="${message(code: 'button.confirm.label')}"/>
        <g:submitButton class="btn right btn-info right-margin5"
                name="back" value="${message(code: 'button.back.label')}" />
    </div>
</g:form>

<r:script>
    $(function() {
        $("[rel='tooltip']").tooltip();
    });
</r:script>
</body>
</html>
