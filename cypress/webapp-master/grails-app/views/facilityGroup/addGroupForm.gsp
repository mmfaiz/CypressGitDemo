<%@ page import="org.joda.time.DateTime; com.matchi.membership.Membership" %>
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3><g:message code="facilityGroup.addGroupForm.message1"/></h3>
    <div class="clearfix"></div>
</div>
<g:form name="groupForm" class="no-margin" controller="facilityGroup" action="addCustomer">
    <g:hiddenField name="customerId" value="${customer.id}"/>
    <g:hiddenField name="returnUrl" value="${g.createLink(controller:'facilityCustomer', action:'show', absolute: true, id: customer.id)}"/>
    <div class="modal-body">
        <div class="alert alert-error" style="display: none;">
            <a class="close" data-dismiss="alert" href="#">×</a>
        </div>

        <fieldset>
            <div class="control-group">
                <div class="controls">
                    <select id="id" name="id" class="span3 required" tabindex="0">
                        <option value=""><g:message code="facilityGroup.addGroupForm.message2"/></option>
                        <g:each in="${groups}">
                            <option value="${it.id}">
                                ${it.name}
                            </option>
                        </g:each>
                    </select>
                </div>
            </div>
        </fieldset>
    </div>
    <div class="modal-footer">
        <div class="pull-left">
            <g:submitButton name="submit" id="formSubmit" value="${message(code: 'button.add.label')}" class="btn btn-md btn-success"/>
            <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-danger"><g:message code="button.cancel.label" default="Avbryt"/></a>
        </div>
    </div>
</g:form>
<script type="text/javascript">
    $(document).ready(function() {
        $("#groupForm").validate({
            errorLabelContainer: ".alert-error",
            errorPlacement: function(error, element) {},
            highlight: function (element, errorClass) {
                $(element).addClass("invalid-input");
            },
            unhighlight: function (element, errorClass) {
                $(element).removeClass("invalid-input");
            },
            messages: {
                id: "${message(code: 'facilityGroup.addGroupForm.message3')}"
           }
        });

        $('#customerModal').on('shown', function () {
            $('#id').focus();
        });
    });
</script>