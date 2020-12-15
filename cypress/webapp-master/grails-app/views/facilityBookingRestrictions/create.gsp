<%@ page import="com.matchi.facility.FacilityBookingRestrictionsController" %>
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>
    <h3><g:message code="facilityBookingRestriction.modal.create.header"/></h3>
    <div class="clearfix"></div>
</div>
<g:form controller="facilityBookingRestrictions" action="save" name="bookingForm" class="no-margin">
    <div class="modal-body">
        <g:flashError/>
        <g:hiddenField name="restrictionSlots" value="${restrictionSlotData}"/>
        <g:hiddenField name="date" value="${date}"/>

        <div class="well well-small">
            <g:if test="${requirementProfiles?.size() > 0}">
                <div class="control-group">
                    <label for="validUntilBeforeStart">
                        <g:message code="facilityBookingRestriction.modal.validUntil.label" />
                        <g:inputHelp title="${message(code: "facilityBookingRestriction.modal.validUntil.help")}"/>
                    </label>
                    <g:textField name="validUntilBeforeStart" value="0" style="width: 50px"/>
                    <select name="validUntilUnit" id="validUntilUnit" style="width: 80px;">
                        <g:each in="${com.matchi.facility.FacilityBookingRestrictionsController.ValidUntilUnit.toList()}">
                            <option value="${it.name()}"><g:message code="facilityBookingRestriction.modal.${it.name()}" /></option>
                        </g:each>
                    </select>
                </div>

                <div class="control-group">
                    <strong><g:message code="requirementProfile.label.plural"/></strong><br/>
                    <g:each in="${requirementProfiles}">
                        <label class="checkbox">
                            <g:checkBox id="${it.id}_requirementProfile" name="requirementProfiles" value="${it.id}"
                                checked="false"/>
                            ${it.name}
                        </label>
                    </g:each>
                </div>
            </g:if>
            <g:else>
                <strong><g:message code="facilityBookingRestriction.modal.no.requirement.profiles"/></strong>
            </g:else>
        </div>

        <div id="recurrence">
            <g:bookingFormRecurrence slot="${slots[0]}"/>
        </div>
    </div>
    <div class="modal-footer">
        <div class="pull-left">
            <g:if test="${requirementProfiles?.size() > 0}">
                <button name="book" class="btn btn-md btn-success"><g:message code="button.save.label"/></button>
            </g:if>
            <g:else>
                <button name="book" class="btn btn-md btn-success" disabled="disabled"><g:message code="button.save.label"/></button>
            </g:else>
            <a  id="cancelBtn" href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-danger"><g:message code="button.cancel.label" /></a>
        </div>
    </div>
</g:form>
<script type="text/javascript">
    $(document).ready(function() {
        $("#bookingForm").preventDoubleSubmission({});
        $("[rel=tooltip]").tooltip();
    });
</script>