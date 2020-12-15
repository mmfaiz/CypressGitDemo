<%@ page import="com.matchi.membership.Membership" %>
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3><g:message code="facilityCustomerFamily.familyForm.message1"/></h3>
    <div class="clearfix"></div>
</div>
<form name="memberForm" class="no-margin" action="/facility/customers/family/create">
    <g:hiddenField name="memberId" value="${member.id}"/>
    <div class="modal-body">
        <p class="lead">
            <g:message code="facilityCustomerFamily.familyForm.message8" args="[member.customer]"/>
        </p>
        <p><g:message code="facilityCustomerFamily.familyForm.message2"/></p>
        <p><g:message code="facilityCustomerFamily.familyForm.message3"/></p>
        <g:if test="${suggestedFamilies.size() > 0}">
            <hr>
            <h5><g:message code="facilityCustomerFamily.familyForm.message9"/><br><small><g:message code="facilityCustomerFamily.familyForm.message4"/></small></h5>
            <table id="famTable" width="100%" class="table-striped">
                <thead>
                <th width="40"></th>
                <th class="left-text"><g:message code="default.name.label"/></th>
                <th class="left-text"><g:message code="default.phone.label"/></th>
                <th class="left-text"><g:message code="default.address.label"/></th>
                </thead>
                <tbody>
                <g:each in="${suggestedFamilies}" var="f">
                    <tr>
                        <td>
                            <g:radio name="familyId" value="${f.id}" style="margin: 0 0 4px;"/>
                        </td>
                        <td>${f.contact.fullName()}</td>
                        <td>${f.contact.telephone ?: "-"}</td>
                        <td>${f.contact.address1 ?: "-"}</td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </g:if>
    </div>
    <div class="modal-footer">
        <div class="pull-left">
            <g:submitButton name="submit" id="formSubmit" value="${message(code: 'facilityCustomerFamily.familyForm.message10')}"
                            class="btn btn-md btn-success" tabindex="9"/>
            <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-danger"><g:message code="button.cancel.label" default="Avbryt"/></a>
        </div>
    </div>
</form>
<script type="text/javascript">
    $(document).ready(function() {
        $("#famTable").find("[type=radio]").on("change", function() {
            if($(this).is(":checked")) {
                $("#formSubmit").val("${message(code: 'facilityCustomerFamily.familyForm.message11')}");
            }
        });
    });
</script>