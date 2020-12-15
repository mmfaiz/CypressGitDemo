<%@ page import="com.matchi.facility.EditMembershipsCommand.PeriodType"%>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="default.member.label.plural"/></title>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link controller="facilityCustomer" action="index"><g:message code="customer.label.plural"/></g:link> <span class="divider">/</span></li>
    <li><g:link controller="facilityCustomerMembers" action="index"><g:message code="default.member.label.plural"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilityCustomerMembers.editMemberships.title"/></li>
</ul>

<g:form>
    <g:render template="/templates/wizard"
            model="[steps: [message(code: 'facilityCustomerMembers.editMemberships.wizard.step1'), message(code: 'facilityCustomerMembers.editMemberships.wizard.step2'), message(code: 'default.modal.done')], current: 0]"/>

    <g:errorMessage bean="${cmd}"/>

    <g:if test="${!membersInfo}">
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert"><i class="fa fa-times-circle"></i></button>
            <g:message code="facilityCustomerMembers.editMemberships.enterDetails.noMemberships"/>
        </div>
    </g:if>

    <h3><g:message code="facilityCustomerMembers.editMemberships.heading1"/></h3>

    <p class="lead">
        <g:message code="facilityCustomerMembers.editMemberships.description1"/>
    </p>

    <div class="well">
        <fieldset>
            <div class="control-group">
                <div class="controls">
                    <label class="control-label checkbox">
                        <g:checkBox name="updateStartingGracePeriodDays" value="${cmd?.updateStartingGracePeriodDays}" class="update-option"/>
                        <g:message code="editMembershipsCommand.updateStartingGracePeriodDays.label"/>
                        <i class="fas fa-info-circle text-primary" rel="tooltip"
                                title="${message(code: 'editMembershipsCommand.updateStartingGracePeriodDays.tooltip')}"></i>
                    </label>
                </div>
            </div>
            <div class="control-group left-margin20" style="${cmd?.updateStartingGracePeriodDays ? '' : 'display: none'}">
                <div class="controls left-margin20">
                    <label for="startingGracePeriodDays">
                        <g:message code="editMembershipsCommand.startingGracePeriodDays.label"/>:
                    </label>
                    <g:field type="number" name="startingGracePeriodDays" class="span2 ${hasErrors(bean: cmd, field: 'startingGracePeriodDays', 'error')}"
                            value="${cmd?.startingGracePeriodDays}" min="0"/>
                </div>
            </div>

            <div class="control-group">
                <div class="controls">
                    <label class="control-label checkbox">
                        <g:checkBox name="updateStartDate" value="${cmd?.updateStartDate}" class="update-option"/>
                        <g:message code="editMembershipsCommand.updateStartDate.label"/>
                    </label>
                </div>
            </div>
            <div class="control-group left-margin20" style="${cmd?.updateStartDate ? '' : 'display: none'}">
                <div class="controls row">
                    <div class="span3">
                        <label class="radio">
                            <g:radio name="startDateType" value="${PeriodType.DATE}" class="update-type"
                                    checked="${!cmd || cmd.startDateType == PeriodType.DATE}"/>
                            <g:message code="editMembershipsCommand.type.DATE.label"/>:
                        </label>
                        <g:textField name="startDate" class="span2 center-text left-margin20 date-value ${hasErrors(bean: cmd, field: 'startDate', 'error')}"
                                     value="${cmd?.startDate?.format(message(code: 'date.format.dateOnly'))}"/>
                    </div>
                    <div class="span7">
                        <label class="radio">
                            <g:radio name="startDateType" value="${PeriodType.DAYS}" class="update-type"
                                    checked="${cmd?.startDateType == PeriodType.DAYS}"/>
                            <g:message code="editMembershipsCommand.type.DAYS.label"/>:
                        </label>
                        <g:field name="startDays" type="number" min="1" class="span2 left-margin20 days-value ${hasErrors(bean: cmd, field: 'startDays', 'error')}" value="${cmd?.startDays}"/>
                    </div>
                </div>
            </div>

            <div class="control-group">
                <div class="controls">
                    <label class="control-label checkbox">
                        <g:checkBox name="updateEndDate" value="${cmd?.updateEndDate}" class="update-option"/>
                        <g:message code="editMembershipsCommand.updateEndDate.label"/>
                    </label>
                </div>
            </div>
            <div class="control-group left-margin20" style="${cmd?.updateEndDate ? '' : 'display: none'}">
                <div class="controls row">
                    <div class="span3">
                        <label class="radio">
                            <g:radio name="endDateType" value="${PeriodType.DATE}" class="update-type"
                                    checked="${!cmd || cmd.endDateType == PeriodType.DATE}"/>
                            <g:message code="editMembershipsCommand.type.DATE.label"/>:
                        </label>
                        <g:textField name="endDate" class="span2 center-text left-margin20 date-value ${hasErrors(bean: cmd, field: 'endDate', 'error')}"
                                     value="${cmd?.endDate?.format(message(code: 'date.format.dateOnly'))}"/>
                    </div>
                    <div class="span7">
                        <label class="radio">
                            <g:radio name="endDateType" value="${PeriodType.DAYS}" class="update-type"
                                    checked="${cmd?.endDateType == PeriodType.DAYS}"/>
                            <g:message code="editMembershipsCommand.type.DAYS.label"/>:
                        </label>
                        <g:field name="endDays" type="number" min="1" class="span2 left-margin20 days-value ${hasErrors(bean: cmd, field: 'endDays', 'error')}" value="${cmd?.endDays}"/>
                    </div>
                </div>
            </div>

            <div class="control-group">
                <div class="controls">
                    <label class="control-label checkbox">
                        <g:checkBox name="updateGracePeriodEndDate" value="${cmd?.updateGracePeriodEndDate}" class="update-option"/>
                        <g:message code="editMembershipsCommand.updateGracePeriodEndDate.label"/>
                    </label>
                </div>
            </div>
            <div class="control-group left-margin20" style="${cmd?.updateGracePeriodEndDate ? '' : 'display: none'}">
                <div class="controls row">
                    <div class="span3">
                        <label class="radio">
                            <g:radio name="gracePeriodEndDateType" value="${PeriodType.DATE}" class="update-type"
                                    checked="${!cmd || cmd.gracePeriodEndDateType == PeriodType.DATE}"/>
                            <g:message code="editMembershipsCommand.type.DATE.label"/>:
                        </label>
                        <g:textField name="gracePeriodEndDate" class="span2 center-text left-margin20 date-value ${hasErrors(bean: cmd, field: 'gracePeriodEndDate', 'error')}"
                                     value="${cmd?.gracePeriodEndDate?.format(message(code: 'date.format.dateOnly'))}"/>
                    </div>
                    <div class="span7">
                        <label class="radio">
                            <g:radio name="gracePeriodEndDateType" value="${PeriodType.DAYS}" class="update-type"
                                    checked="${cmd?.gracePeriodEndDateType == PeriodType.DAYS}"/>
                            <g:message code="editMembershipsCommand.type.DAYS.label"/>:
                        </label>
                        <g:field name="gracePeriodEndDays" type="number" min="1" class="span2 left-margin20 days-value ${hasErrors(bean: cmd, field: 'gracePeriodEndDays', 'error')}" value="${cmd?.gracePeriodEndDays}"/>
                    </div>
                </div>
            </div>

            <div class="control-group">
                <div class="controls">
                    <label class="control-label checkbox">
                        <g:checkBox name="updateMembershipType" value="${cmd?.updateMembershipType}" class="update-option"/>
                        <g:message code="editMembershipsCommand.updateMembershipType.label"/>
                    </label>
                </div>
            </div>
            <div class="control-group left-margin20" style="${cmd?.updateMembershipType ? '' : 'display: none'}">
                <div class="controls left-margin20">
                    <label for="membershipTypeId">
                        <g:message code="editMembershipsCommand.membershipTypeId.label"/>:
                    </label>
                    <g:select name="membershipTypeId" from="${membershipTypes}"
                            optionKey="id" optionValue="name" value="${cmd?.membershipTypeId}"
                             class="span2 ${hasErrors(bean: cmd, field: 'membershipTypeId', 'error')}"/>
                </div>
            </div>
        </fieldset>
    </div>

    <table class="table table-transparent">
        <thead>
        <tr>
            <th><g:message code="customer.number.label"/></th>
            <th><g:message code="default.name.label"/></th>
            <th><g:message code="membership.label"/></th>
            <th><g:message code="membership.startingGracePeriodDays.label"/></th>
            <th><g:message code="membership.startDate.label.long"/></th>
            <th><g:message code="membership.endDate.label.long"/></th>
            <th><g:message code="membership.gracePeriodEndDate.label.long"/></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${membersInfo}" var="info">
            <tr>
                <td>${info.customerNr}</td>
                <td>${info.customerName}</td>
                <td>${info.membershipType ?: '-'}</td>
                <td>
                    <g:if test="${info.membershipStartingGracePeriodAllowed}">
                        ${info.membershipStartingGracePeriod}
                    </g:if>
                </td>
                <td><g:formatDate date="${info.membershipStartDate.toDate()}" formatName="date.format.dateOnly"/></td>
                <td><g:formatDate date="${info.membershipEndDate.toDate()}" formatName="date.format.dateOnly"/></td>
                <td><g:formatDate date="${info.membershipGracePeriodEndDate.toDate()}" formatName="date.format.dateOnly"/></td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="form-actions">
        <g:submitButton class="btn btn-danger" name="cancel"
                value="${message(code: 'button.cancel.label')}"/>
        <g:if test="${membersInfo}">
            <g:submitButton class="btn right btn-success" name="next"
                    data-toggle="button" show-loader="${message(code: 'default.loader.label')}"
                    value="${message(code: 'button.next.label')}"/>
        </g:if>
    </div>
</g:form>

<r:script>
    $(function() {
        $(".date-value").datepicker({
            autoSize: true,
            dateFormat: "${message(code: 'date.format.dateOnly.small')}"
        });
        $("#gracePeriodEndDate").datepicker( "option", "minDate", new Date());

        $(".update-option").on("change", function() {
            $(this).closest(".control-group").next().toggle($(this).is(":checked"));
        });

        $(".update-type").on("change", function() {
            var dateTypeSelected = $(this).val() == "${PeriodType.DATE}";
            var parentEl = $(this).closest(".control-group");
            parentEl.find(".date-value").prop("disabled", !dateTypeSelected);
            parentEl.find(".days-value").prop("disabled", dateTypeSelected);
        });
        $(".update-type:checked").trigger("change");

        $("[rel='tooltip']").tooltip();
    });
</r:script>

</body>
</html>
