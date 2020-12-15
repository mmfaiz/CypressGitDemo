<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="default.member.label.plural"/></title>
</head>

<body>
    <ul class="breadcrumb">
        <li><g:link controller="facilityCustomer" action="index"><g:message code="customer.label.plural"/></g:link> <span class="divider">/</span></li>
        <li><g:link controller="facilityCustomerMembers" action="index"><g:message code="default.member.label.plural"/></g:link> <span class="divider">/</span></li>
        <li class="active"><g:message code="facilityCustomerMembers.setUnpaid.title"/></li>
    </ul>

    <h3><g:message code="facilityCustomerMembers.setUnpaid.heading"/></h3>

    <p class="lead">
        <g:message code="facilityCustomerMembers.setUnpaid.description"/>
    </p>

    <g:if test="${!membersInfo}">
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert"><i class="fa fa-times-circle"></i></button>
            <g:message code="facilityCustomerMembers.setPaidUnpaid.noMemberships"/>
        </div>
    </g:if>

    <g:if test="${pendingMemberships || freeMemberships || unpaidMemberships || nonCashPaidMemberships}">
        <div class="alert">
            <button type="button" class="close" data-dismiss="alert"><i class="fa fa-times-circle"></i></button>

            <g:if test="${pendingMemberships}">
                <div>
                    <g:message code="facilityCustomerMembers.setPaidUnpaid.pendingMemberships"
                            args="[pendingMemberships]"/>
                </div>
            </g:if>
            <g:if test="${freeMemberships}">
                <div>
                    <g:message code="facilityCustomerMembers.setUnpaid.freeMemberships"
                            args="[freeMemberships]"/>
                </div>
            </g:if>
            <g:if test="${unpaidMemberships}">
                <div>
                    <g:message code="facilityCustomerMembers.setUnpaid.unpaidMemberships"
                            args="[unpaidMemberships]"/>
                </div>
            </g:if>
            <g:if test="${nonCashPaidMemberships}">
                <div>
                    <g:message code="facilityCustomerMembers.setUnpaid.nonCashPaidMemberships"
                            args="[nonCashPaidMemberships]"/>
                </div>
            </g:if>
        </div>
    </g:if>

    <g:form>
        <g:if test="${membersInfo}">
            <div class="well">
                <table class="table table-transparent table-condensed table-noborder">
                    <thead>
                    <tr>
                        <th><g:message code="customer.number.label"/></th>
                        <th><g:message code="default.name.label"/></th>
                        <th><g:message code="membership.type.label"/></th>
                        <th><g:message code="membership.status.label"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${membersInfo}" var="info">
                        <tr>
                            <td>${info.customerNr}</td>
                            <td>${info.customerName}</td>
                            <td>${info.membershipType ?: '-'}</td>
                            <td>
                                <span class="label label-success">
                                    <g:message code="membership.status.PAID"/>
                                </span>
                                →
                                <span class="label label-important">
                                    <g:message code="membership.status.UNPAID"/>
                                </span>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </g:if>

        <div class="form-actions">
            <g:link event="cancel" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
            <g:if test="${membersInfo}">
                <g:submitButton name="submit" type="submit" value="${message(code: 'button.confirm.label')}"
                        data-toggle="button" class="btn btn-success pull-right"
                        show-loader="${message(code: 'default.loader.label')}"/>
            </g:if>
        </div>
    </g:form>
</body>
</html>
