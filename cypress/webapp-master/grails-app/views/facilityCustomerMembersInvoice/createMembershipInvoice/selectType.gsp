<html xmlns="http://www.w3.org/1999/html">
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
              model="[steps: [message(code: 'facilityCustomerMembersInvoice.createMembershipInvoice.wizard.step0'), message(code: 'facilityCustomerMembersInvoice.createMembershipInvoice.wizard.step1'), message(code: 'facilityCustomerMembersInvoice.createMembershipInvoice.wizard.step2'), message(code: 'default.modal.done')], current: 0]"/>

    <h1><g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.enterDetails.message4"/></h1>
    <p class="lead"><g:message code="facilityCustomerMembersInvoice.createMembershipInvoice.selectType.description"/></p>

    <div class="well">
        <div class="row">
            <div class="span12">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="createNewMembership">
                            <g:message code="facilityInvoiceRow.createInvoiceRow.selectType.createNewMembership.label"/>
                        </label>
                        <div class="controls">
                            <label class="radio">
                                <g:radio name="createNewMembership" value="false" checked="${!createNewMembership}"/>
                                <g:message code="facilityInvoiceRow.createInvoiceRow.selectType.createNewMembership.false"/>
                            </label>
                            <label class="radio">
                                <g:radio name="createNewMembership" value="true" checked="${createNewMembership}"/>
                                <g:message code="facilityInvoiceRow.createInvoiceRow.selectType.createNewMembership.true"/>
                                <i class="fas fa-info-circle text-primary" rel="tooltip"
                                        title="${message(code: 'facilityInvoiceRow.createInvoiceRow.selectType.createNewMembership.true.tooltip')}"></i>
                            </label>
                        </div>
                    </div>
                </fieldset>
            </div>
        </div>
    </div>

    <div class="form-actions">
        <g:submitButton class="btn btn-danger" name="cancel" value="${message(code: 'button.cancel.label')}"/>
        <g:submitButton class="btn right btn-success" name="next" value="${message(code: 'button.next.label')}"/>
    </div>
</g:form>

<r:script>
    $(function() {
        $("[rel='tooltip']").tooltip();
    });
</r:script>
</body>
</html>