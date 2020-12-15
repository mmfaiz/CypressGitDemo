<%@ page import="com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilityInvoiceRow.createInvoice.invoiceDetails.message1"/></title>
</head>
<body>

<ul class="breadcrumb">
    <li><g:link controller="facilityCustomerMembers" action="index"><g:message code="default.member.label.plural"/></g:link> <span class="divider">/</span></li>
        <li class="active"><g:message code="facilityInvoiceRow.createInvoiceRow"/></li>
</ul>

<g:form>
    <g:render template="/templates/wizard"
              model="[steps: [message(code: 'facilityCustomerMembersInvoice.createMembershipInvoice.wizard.step0'), message(code: 'facilityCustomerMembersInvoice.createMembershipInvoice.wizard.step1'), message(code: 'facilityCustomerMembersInvoice.createMembershipInvoice.wizard.step2'), message(code: 'default.modal.done')], current: 2]"/>
    <h1><g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.confirm.message4"/></h1>
    <p class="lead"><g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.confirm.message5"/></p>

    <hr>
    <div class="row">
        <div class="span3">
            <strong><g:message code="default.description.label"/></strong><br>
            ${invoiceMembershipCommand.text}
        </div>

        <div class="span2">
            <strong><g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.confirm.message7"/></strong><br>
             ${confirmInformation.size()}<g:message code="unit.st"/>
        </div>

        <div class="span2">
            <strong><g:message code="default.total.label"/></strong><br>
            <g:formatMoney value="${confirmInformation.sum(0) { it.price }}"/>
        </div>
    </div>

    <hr>

    <table class="table table-transparent">
        <thead>
            <th width="80"><g:message code="customer.number.label"/></th>
            <th><g:message code="default.member.label"/></th>
            <g:if test="${!invoiceMembershipCommand.articleId}"><th><g:message code="default.account.label"/></th></g:if>
            <th class="center-text"><g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.confirm.message11"/></th>
            <th width="100"><g:message code="membershipType.label"/></th>
            <th width="80"><g:message code="default.discount.label"/></th>
            <th width="150"><g:message code="default.price.label"/></th>
            <th width="100">
                <span class="right"><g:message code="default.total.label"/></span>
            </th>
        </thead>
        <g:each in="${confirmInformation}" var="confirmInfo">
            <tr>
                <td>${confirmInfo.customerNr}</td>
                <td>${confirmInfo.customerName}</td>
                <g:if test="${!invoiceMembershipCommand.articleId}"><td>${confirmInfo.account}</td></g:if>
                <g:if test="${confirmInfo.isFamilyContact}">
                    <td class="center-text">
                        <strong><span id="${confirmInfo.membershipId}_family" class="popover-hint">${confirmInfo.familyMembers.size()}<g:message code="unit.st"/></span></strong>
                    </td>
                </g:if>
                <g:else>
                    <td class="center-text">-</td>
                </g:else>
                <td>${confirmInfo.typeName}</td>
                <td><g:formatDiscount invoiceRow="${confirmInfo}" zeroValue="-"/></td>
                <td>
                    <g:formatMoney value="${confirmInfo.price}"/>
                    <g:if test="${confirmInfo?.vatPercentage > 0}"><small><g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.confirm.message18" args="[confirmInfo.vatPercentage]"/></small></g:if>
                    <g:else><small>(<g:message code="default.vat.excluded"/>)</small></g:else>
                </td>
                <td>
                    <span class="right">
                        <g:formatMoney value="${confirmInfo.total}"/>
                    </span>
                </td>
            </tr>
        </g:each>

        <tfoot>
            <tr>
                <g:if test="${!invoiceMembershipCommand.articleId}"><td colspan="6"></td></g:if>
                <g:else><td colspan="5"></td></g:else>
                <td><strong><g:message code="default.total.label"/></strong></td>
                <td>
                    <span class="right">
                        <strong><g:formatMoney value="${confirmInformation.sum(0) { it.total }}"/></strong>
                    </span>
                </td>
            </tr>
        </tfoot>
    </table>

    <div class="form-actions">
        <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}" />
        <g:submitButton class="btn right btn-success" name="next" id="btnNext" value="${message(code: 'facilityInvoiceRow.createInvoiceRow')}" show-loader="${message(code: 'default.loader.label')}" />
        <g:submitButton class="btn right btn-info right-margin5" name="back" value="${message(code: 'button.back.label')}" />
    </div>
</g:form>
<r:script>
    $(document).ready(function() {
        $("#btnNext").focus();

        <g:each in="${confirmInformation}" var="confirmInfo">
            <g:if test="${confirmInfo.isFamilyContact}">
                $("#${g.forJavaScript(data: confirmInfo.membershipId)}_family").popover({
                    title: "${message(code: 'facilityCustomerMembersInvoice.createMembershipInvoice.confirm.message22')}",
                    content: "" +
                        "<table class='table no-margin-padding' style='font-size: 12px; color: #2c2c2c;'>" +
                        <g:each in="${confirmInfo.familyMembers}" var="member">
                            "<tr><td nowrap>${g.forJavaScript(data: member.fullName)}</td><td nowrap>${g.forJavaScript(data: member.birthyear)}</td><td nowrap>${g.forJavaScript(data: member.type)}</td><td nowrap><g:formatMoney value="${member.price}"/></td></tr>" +
                        </g:each>
                        "</table>",
                    trigger: "hover",
                    placement: "top"
                });
            </g:if>
        </g:each>
    });
</r:script>
</body>
</html>