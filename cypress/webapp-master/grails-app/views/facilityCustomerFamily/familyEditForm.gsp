<%@ page import="com.matchi.membership.Membership" %>
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3><g:message code="facilityCustomerFamily.familyEditForm.message1"/></h3>
    <div class="clearfix"></div>
</div>
<form name="memberForm" class="no-margin" action="/facility/customers/family/edit">
    <g:hiddenField name="memberId" value="${member.id}"/>
    <g:hiddenField name="familyId" value="${member?.family?.id}"/>
    <div class="modal-body">
        <g:if test="${member?.family?.members?.size() > 0}">
            <p class="lead">
                <g:message code="facilityCustomerFamily.familyEditForm.message2"/>
            </p>
            <table width="100%" class="table-striped">
                <thead>
                <th class="left-text" width="60"><g:message code="membershipFamily.contact.label"/></th>
                <th class="left-text"><g:message code="default.name.label"/></th>
                <th class="left-text"><g:message code="customer.telephone.label"/></th>
                <th class="left-text"><g:message code="default.address.label"/></th>
                </thead>
                <tbody>
                <g:each in="${member?.family?.members}" var="m" status="i">
                    <tr>
                        <td>
                            <input type="radio" name="contactId" value="${m.customer.id}" ${member.family.contact.id == m.customer.id ? "checked" : ""} style="margin: 0 0 4px;"/>
                        </td>
                        <td>${m.customer.fullName()}</td>
                        <td>${m.customer.telephone}</td>
                        <td>${m.customer.address1}</td>
                        <td>
                            <g:if test="${!m.customer.equals(member?.family?.contact)}">
                                <g:link controller="facilityCustomerFamily" action="removeFromFamily" title="${message(code: 'facilityCustomerFamily.familyEditForm.message9')}"
                                        onclick="return confirm('${message(code: 'facilityCustomerFamily.familyEditForm.message10')}')"
                                        params="[customerId: member.customer?.id, memberId: m.id]">
                                    <i class="icon-remove"></i>
                                </g:link>
                            </g:if>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </g:if>
    </div>
    <div class="modal-footer">
        <div class="pull-left">
            <g:submitButton name="submit" id="formSubmit" value="${message(code: 'button.save.label')}" class="btn btn-md btn-success" tabindex="9"/>
            <g:link class="btn btn-inverse" onclick="return confirm('${message(code: 'facilityCustomerFamily.familyEditForm.message11')}')"
                    controller="facilityCustomerFamily" action="remove" params="[familyId: member.family.id, customerId: member.customer.id]"><g:message code="facilityCustomerFamily.familyEditForm.message7"/></g:link>
            <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-danger"><g:message code="button.cancel.label" default="Avbryt"/></a>
        </div>
        <div class="pull-right">
            <g:remoteLink controller="facilityCustomerFamily" action="familyAddMembersForm" update="customerModal"
                          onFailure="handleAjaxError()" onSuccess="showLayer('customerModal')"
                          params="[ 'customerId': member.customer.id ]" title="${message(code: 'facilityCustomerFamily.familyAddMembersForm.heading')}" class="btn btn-md btn-info"><g:message code="facilityCustomerFamily.familyEditForm.message8"/></g:remoteLink>
        </div>
    </div>
</form>