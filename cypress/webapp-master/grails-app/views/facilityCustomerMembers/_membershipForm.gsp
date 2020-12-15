<%@ page import="com.matchi.membership.Membership" %>

<fieldset>
    <div class="row">
        <div class="span2">
            <div class="control-group">
                <label for="type"><g:message code="membershipType.label"/></label>
                <div class="controls">
                    <select id="type" name="type" class="span2">
                        <option value=""><g:message code="membershipType.multiselect.noneSelectedText"/></option>
                        <g:each in="${types}">
                            <option value="${it.id}" data-start-date="${it.startDate?.format(message(code: 'date.format.dateOnly'))}" data-end-date="${it.endDate?.format(message(code: 'date.format.dateOnly'))}"
                                    data-grace-period-end-date="${it.gracePeriodEndDate?.format(message(code: 'date.format.dateOnly'))}"
                                    ${membership?.type?.id == it.id ? 'selected' : ''}>${it.name}</option>
                        </g:each>
                    </select>
                </div>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="span1">
            <div class="control-group">
                <label for="startDate"><g:message code="membership.startDate.label.long"/></label>
                <div class="controls">
                    <g:textField name="startDate" readonly="true" class="span1"
                        value="${membership?.startDate?.toString(message(code: 'date.format.dateOnly'))}"/>
                </div>
            </div>
        </div>
        <div class="span1 left-padding15">
            <div class="control-group">
                <label for="endDate"><g:message code="membership.endDate.label.long"/></label>
                <div class="controls">
                    <g:textField name="endDate" readonly="true" class="span1"
                        value="${membership?.endDate?.toString(message(code: 'date.format.dateOnly'))}"/>
                </div>
            </div>
        </div>
        <div class="span2 left-padding15">
            <div class="control-group">
                <label for="gracePeriodEndDate"><g:message code="membership.gracePeriodEndDate.label.long"/></label>
                <div class="controls">
                    <g:textField name="gracePeriodEndDate" readonly="true" class="span1"
                        value="${membership?.gracePeriodEndDate?.toString(message(code: 'date.format.dateOnly'))}"/>
                </div>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="span4">
            <div class="control-group">
                <div class="controls">
                    <g:if test="${membership?.startingGracePeriodDays || !customer.hasNonEndedMembership(membership?.id)}">
                        <g:render template="/templates/customer/membershipStartingGracePeriodDays"/>
                    </g:if>

                    <label class="checkbox" for="paid">
                        <input type="checkbox" id="paid" name="paid" value="true" class="membership-paid-checkbox"
                                ${membership?.isPaid() ? 'checked' : ''}
                                ${(membership?.order?.isInvoiced() || membership?.order?.isFree() ||
                                        membership?.order?.isPaidByCreditCard()) ? 'disabled' : ''}/>
                        <g:if test="${membership?.order?.isInvoiced() || membership?.order?.isFree() || membership?.order?.isPaidByCreditCard()}">
                            <span class="muted" rel="tooltip"
                                    title="${message(code: 'membershipCommand.paid.' + (membership?.order?.isInvoiced() ? 'invoiced' : (membership?.order?.isPaidByCreditCard() ? 'creditCard' : 'free')) + '.tooltip')}">
                                <g:message code="membershipCommand.paid.label"/>
                            </span>
                        </g:if>
                        <g:else>
                            <g:message code="membershipCommand.paid.label"/>
                        </g:else>
                    </label>

                    <label class="checkbox" for="cancel">
                        <g:checkBox name="cancel" value="${membership?.cancel}"/>
                        <g:message code="membershipCommand.cancel.label"/>
                    </label>
                </div>
            </div>
        </div>
    </div>
</fieldset>

<script type="text/javascript">
    $(function() {
        $("[rel='tooltip']").tooltip();

        $("#startDate,#endDate,#gracePeriodEndDate").datepicker({
            autoSize: true,
            dateFormat: '${message(code: 'date.format.dateOnly.small')}'
        });

        <g:if test="${!membership}">
            $("#gracePeriodEndDate").datepicker("option", "minDate", new Date());
            $("#endDate").datepicker("option", "minDate", new Date());
            $("#type").on("change", function() {
                if ($(this).val()) {
                    var option = $(this).find("option:selected");
                    $("#gracePeriodEndDate").val(option.attr("data-grace-period-end-date"));
                    $("#endDate").val(option.attr("data-end-date"));
                    $("#startDate").val(option.attr("data-start-date")).trigger("change");
                }
            });
        </g:if>

        $("#endDate").on("change", function() {
            $("#gracePeriodEndDate").datepicker("option", "minDate", $(this).val());
        });

        $("#startDate").on("change", function() {
            var date = $(this).val();
            <g:if test="${membership}">
                $("#endDate").datepicker("option", "minDate", date);
            </g:if>
            <g:else>
                var today = new Date();
                $("#endDate").datepicker("option", "minDate",
                        date < today ? today : date);
            </g:else>
            $("#endDate").trigger("change");
        }).trigger("change");   
    });
</script>