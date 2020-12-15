<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3><g:message code="membership.label"/></h3>
    <div class="clearfix"></div>
</div>

<g:form name="memberForm" class="no-margin" controller="facilityCustomerMembers" action="updateMembership">
    <g:hiddenField name="id" value="${membership.id}"/>

    <div class="modal-body">
        <h3><g:message code="facilityCustomerMembers.membershipForm.message2"/></h3>

        <g:render template="membershipForm"/>
    </div>

    <div class="modal-footer">
        <div class="pull-left">
            <g:submitButton name="submit" id="formSubmit" value="${message(code: 'button.update.label')}"
                    class="btn btn-md btn-success"/>
            <g:if test="${membership.customer.facility.membershipRequiresApproval || !membership.activated}">
                <g:if test="${membership.activated}">
                    <g:link class="btn btn-md btn-info" controller="facilityCustomerMembers" action="toggleMembershipActivation" id="${membership.id}"
                            onclick="return confirm('${message(code: 'facilityCustomerMembers.membershipForm.deactivate.confirm', encodeAs: 'JavaScript')}')">
                        <g:message code="facilityCustomerMembers.membershipForm.deactivate.label"/>
                    </g:link>
                </g:if>
                <g:else>
                    <g:link class="btn btn-md btn-info" controller="facilityCustomerMembers" action="toggleMembershipActivation" id="${membership.id}">
                        <g:message code="facilityCustomerMembers.membershipForm.activate.label"/>
                    </g:link>
                </g:else>
            </g:if>
            <g:if test="${membership.order.isPaidByCreditCard() && membership.order.isStillRefundable()}">
                <div class="btn-group">
                    <g:link class="btn btn-md btn-inverse" controller="facilityCustomerMembers" action="removeMembership" id="${membership.id}"
                            onclick="return confirm('${message(code: 'facilityCustomerMembers.membershipForm.remove.confirm', encodeAs: 'JavaScript')}')">
                        <g:message code="button.delete.label"/>
                    </g:link>
                    <button class="btn btn-md btn-inverse dropdown-toggle" data-toggle="dropdown">
                        <span class="caret"></span>
                    </button>
                    <ul class="dropdown-menu text-left">
                        <li>
                            <g:link controller="facilityCustomerMembers" action="removeMembership"
                                    id="${membership.id}" params="[refund: true]"
                                    onclick="return confirm('${message(code: 'facilityCustomerMembers.membershipForm.removeAndRefund.confirm', encodeAs: 'JavaScript')}')">
                                <g:message code="facilityCustomerMembers.membershipForm.removeAndRefund.button"/>
                            </g:link>
                        </li>
                    </ul>
                </div>
            </g:if>
            <g:else>
                <g:link class="btn btn-md btn-inverse" controller="facilityCustomerMembers" action="removeMembership" id="${membership.id}"
                        onclick="return confirm('${message(code: 'facilityCustomerMembers.membershipForm.remove.confirm', encodeAs: 'JavaScript')}')">
                    <g:message code="button.delete.label"/>
                </g:link>
            </g:else>
            <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-danger">
                <g:message code="button.cancel.label"/>
            </a>
        </div>
    </div>
</g:form>
