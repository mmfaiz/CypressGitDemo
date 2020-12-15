<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3><g:message code="membership.label"/></h3>
    <div class="clearfix"></div>
</div>

<g:form name="memberForm" class="no-margin" controller="facilityCustomerMembers" action="saveMembership">
    <g:hiddenField name="customerId" value="${customer.id}"/>

    <div class="modal-body">
        <h3><g:message code="facilityCustomerMembers.membershipForm.message3"/></h3>

        <g:render template="membershipForm"/>
    </div>

    <div class="modal-footer">
        <div class="pull-left">
            <g:submitButton name="submit" id="formSubmit" value="${message(code: 'button.add.label')}"
                    class="btn btn-md btn-success"/>
            <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-danger">
                <g:message code="button.cancel.label"/>
            </a>
        </div>
    </div>
</g:form>
