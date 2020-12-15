<%@ page import="com.matchi.membership.Membership" %>
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3><g:message code="facilityCustomerFamily.familyAddMembersForm.heading"/></h3>
    <div class="clearfix"></div>
</div>
<form name="memberForm" class="no-margin" action="/facility/customers/family/add">
    <g:hiddenField name="memberId" value="${member.id}"/>
    <g:hiddenField name="familyId" value="${member.family?.id}"/>
    <div class="modal-body">
        <g:if test="${suggestedFamilyMembers.size() > 0}">
            <h4><g:message code="facilityCustomerFamily.familyAddMembersForm.message7"/><br><small><g:message code="facilityCustomerFamily.familyAddMembersForm.message8" args="[suggestedFamilyMembers.size()]"/></small></h4>
            <table width="100%" class="table-striped">
                <thead>
                <th width="40"></th>
                <th class="left-text"><g:message code="default.name.label"/></th>
                <th class="left-text"><g:message code="customer.telephone.label"/></th>
                <th class="left-text"><g:message code="default.address.label"/></th>
                </thead>
                <tbody>
                <g:each in="${suggestedFamilyMembers}" var="sfm">
                    <tr>
                        <td>
                            <g:checkBox name="addMemberId" value="${sfm.id}" checked="false" style="margin: 0 0 4px;"/>
                        </td>
                        <td>${sfm.customer.fullName()}</td>
                        <td>${sfm.customer.telephone}</td>
                        <td>${sfm.customer.address1}</td>
                    </tr>
                </g:each>
                </tbody>
            </table>
            <hr>
        </g:if>

        <h4><g:message code="facilityCustomerFamily.familyAddMembersForm.message5"/></h4>
        <div class="control-group">
            <div class="controls">
                <input type="hidden" id="memberSearch" value=""/>
            </div>
        </div>
        <div id="family-member" class="control-group" style="display: none;">
            <div class="controls well" style="padding: 15px">
            </div>
        </div>

    </div>
    <div class="modal-footer">
        <div class="pull-left">
            <g:submitButton name="submit" id="formSubmit" value="${message(code: 'button.add.label')}"
                            class="btn btn-md btn-success" tabindex="9"/>
            <g:remoteLink controller="facilityCustomerFamily" action="familyEditForm" update="customerModal"
                          onFailure="handleAjaxError()" onSuccess="showLayer('customerModal')" class="btn btn-md btn-danger"
                          params="[ 'customerId': member.customer.id ]" title="${message(code: 'facilityCustomerFamily.familyAddMembersForm.message9')}"><g:message code="button.cancel.label"/></g:remoteLink>
        </div>
    </div>
</form>
<script type="text/javascript">
    var $memberSearch;

    $(document).ready(function() {
        $memberSearch = $("#memberSearch").matchiCustomerSelect({width:'250px', onchange: selectMember, placeholder: "${message(code: 'facilityCustomerFamily.familyAddMembersForm.message10')}", searchUrl: '/autoCompleteSupport/familyMemberSelect?familyId=${g.forJavaScript(data: member.family?.id)}'});
    });

    function selectMember(customer) {
        if(customer && customer.id != "") {
            $.ajax({
                url: '<g:createLink controller="facilityCustomerFamily" action="familyMember"/>?customerId='+ customer.id,
                type: 'GET',
                success: function(data) {
                    $('#family-member').html(data);
                    $("#family-member").slideDown("fast");
                }
            });
        } else {
            $("#customerId").val("");
            $("#family-member").slideUp("fast");
        }
    }
</script>