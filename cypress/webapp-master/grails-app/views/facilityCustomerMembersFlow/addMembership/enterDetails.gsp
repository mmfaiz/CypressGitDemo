<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityCustomerMembers.addMembership.title"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link controller="facilityCustomer" action="index"><g:message code="customer.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityCustomerMembers.addMembership.title"/></li>
</ul>

<g:form>
    <g:render template="/templates/wizard"
            model="[steps: [message(code: 'facilityCustomerMembers.addMembership.wizard.step1'), message(code: 'facilityCustomerMembers.addMembership.wizard.step2'), message(code: 'default.modal.done')], current: 0]"/>

    <g:errorMessage bean="${cmd}"/>

    <h3><g:message code="facilityCustomerMembers.addMembership.heading1"/></h3>

    <p class="lead">
        <g:message code="facilityCustomerMembers.addMembership.description1"/>
    </p>

    <div class="well">
        <fieldset>
            <div class="row">
                <div class="span3">
                    <div class="control-group">
                        <label for="type"><g:message code="membership.type.label"/></label>
                        <div class="controls">
                            <select id="type" name="typeId" class="span2">
                                <g:each in="${membershipTypes}">
                                    <option value="${it.id}" data-start-date="${it.startDate?.format(message(code: 'date.format.dateOnly'))}"
                                            data-end-date="${it.endDate?.format(message(code: 'date.format.dateOnly'))}"
                                            data-grace-period-end-date="${it.gracePeriodEndDate?.format(message(code: 'date.format.dateOnly'))}"
                                            ${cmd?.typeId == it.id ? 'selected' : ''}>${it.name}</option>
                                </g:each>
                            </select>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="span2">
                    <div class="control-group">
                        <label for="startDate"><g:message code="membership.startDate.label.long"/></label>
                        <div class="controls">
                            <g:textField name="startDate" readonly="true" class="span2"
                                         value="${cmd?.startDate?.format(message(code: 'date.format.dateOnly'))}"/>
                        </div>
                    </div>
                </div>
                <div class="span2 left-padding15">
                    <div class="control-group">
                        <label for="endDate"><g:message code="membership.endDate.label.long"/></label>
                        <div class="controls">
                            <g:textField name="endDate" readonly="true" class="span2"
                                         value="${cmd?.endDate?.format(message(code: 'date.format.dateOnly'))}"/>
                        </div>
                    </div>
                </div>
                <div class="span2 left-padding15">
                    <div class="control-group">
                        <label for="gracePeriodEndDate"><g:message code="membership.gracePeriodEndDate.label.long"/></label>
                        <div class="controls">
                            <g:textField name="gracePeriodEndDate" readonly="true" class="span2"
                                         value="${cmd?.gracePeriodEndDate?.format(message(code: 'date.format.dateOnly'))}"/>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="span4">
                    <div class="control-group">
                        <div class="controls">
                            <g:render template="/templates/customer/membershipStartingGracePeriodDays"
                                    model="[membership: cmd]"/>

                            <label class="checkbox" for="paid">
                                <g:checkBox name="paid" value="${cmd?.paid}" class="membership-paid-checkbox"/>
                                <g:message code="membershipCommand.paid.label"/>
                            </label>
                        </div>
                    </div>
                </div>
            </div>
        </fieldset>
    </div>

    <div class="form-actions">
        <g:submitButton class="btn btn-danger" name="cancel"
                value="${message(code: 'button.cancel.label')}"/>
        <g:submitButton class="btn right btn-success" name="next"
                data-toggle="button" show-loader="${message(code: 'default.loader.label')}"
                value="${message(code: 'button.next.label')}"/>
    </div>
</g:form>

<script type="text/javascript">
    $(function() {
        $("#startDate,#endDate,#gracePeriodEndDate").datepicker({
            autoSize: true,
            dateFormat: "${message(code: 'date.format.dateOnly.small')}"
        });

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

        $("#endDate").on("change", function() {
            $("#gracePeriodEndDate").datepicker("option", "minDate", $(this).val());
        });

        $("#startDate").on("change", function() {
            var date = $(this).val();
            var today = new Date();
            $("#endDate").datepicker("option", "minDate",
                    date < today ? today : date);
            $("#endDate").trigger("change");
        }).trigger("change");

        $("#type").trigger("change");
    });
</script>
</body>
</html>
