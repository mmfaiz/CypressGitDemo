<%@ page import="com.matchi.Sport; com.matchi.Court" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityActivityOccasion.create.message1"/></title>
    <r:require modules="matchi-customerselect,jquery-timepicker"/>
</head>

<body>

<g:if test="${!facility?.isMasterFacility()}">
    <ul class="breadcrumb">
        <li><g:message code="facilityActivityOccasion.create.message2"/></li>
    </ul>
    <g:errorMessage bean="${command}"/>

    <g:form action="save" class="form-horizontal form-well">
        <g:hiddenField name="id" value="${activity?.id}"/>
        <g:hiddenField name="version" value="${activity?.version}"/>
        <g:hiddenField name="comment" value="${comments?.comment}"/>
        <g:hiddenField name="showComment" value="${comments?.showComment}"/>
        <div class="form-header">
            <g:message code="facilityActivityOccasion.create.message3"/>
        </div>
        <fieldset>
            <div class="control-group ${hasErrors(bean: command, field: 'customerId', 'error')}">
        <label class="control-label" for="customerSearch"><g:message
                code="facilityActivityOccasion.create.message4"/></label>

        <div class="controls">
            <input type="hidden" id="customerSearch" name="customerId" value="${command.customerId}"/>
        </div>
        <br>

        <div class="control-group ${hasErrors(bean: command, field: 'activityId', 'error')}">
            <label class="control-label" for="activityId"><g:message code="default.activity"/></label>

            <div class="controls">
                <select id="activityId" name="activityId" style="width: 200px">
                    <option value=""><g:message code="facilityActivityOccasion.create.message16"/></option>
                    <g:each in="${activities}" var="activity">
                        <option value="${activity.id}" data-online="${activity.onlineByDefault}"
                                data-membersOnly="${activity.membersOnly ?: false}"
                                data-price="${activity.price}" data-participants="${activity.maxNumParticipants}"
                                data-daysInAdvance="${activity.signUpDaysInAdvanceRestriction}"
                                data-daysUntil="${activity.signUpDaysUntilRestriction}"
                                data-minNumParticipants="${activity.minNumParticipants}"
                                data-cancelHoursInAdvance="${activity.cancelHoursInAdvance}">
                            ${activity.name?.encodeAsHTML()}
                        </option>
                    </g:each>
                </select>
            </div>
        </div>

        </div>
        <h4><g:message code="facilityActivityOccasion.create.message6"/></h4>
        <table class="table">
            <thead>
            <tr>
                <th><g:message code="default.date.label"/></th>
                <th><g:message code="default.date.time"/></th>
                <th><g:message code="default.price.label"/> (<g:currentFacilityCurrency
                        facility="${activity?.facility}"/>)</th>
                <th><g:message code="activityOccasion.maxNumParticipants.label"/></th>
                <th><g:message code="activityOccasion.availableOnline.label"/></th>
                <th><g:message code="activityOccasion.membersOnly.label"/></th>
                <th><g:message code="activity.signUpDaysInAdvanceRestriction.label"/></th>
                <th><g:message code="activity.signUpDaysUntilRestriction.label"/></th>
                <th><g:message code="user.activity.label"/></th>
                <th><g:message code="facilityActivityOccasion.create.message12"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${command.occasions}" var="occasion" status="stat">
                <g:hiddenField name="occasions[${stat}].slotIds" value="${occasion.slotIds}"/>
                <g:hiddenField name="occasions[${stat}].date" value="${occasion.date}"/>
                <g:hiddenField name="occasions[${stat}].minNumParticipants" value="${activity?.minNumParticipants}"/>
                <g:hiddenField name="occasions[${stat}].cancelHoursInAdvance" value="${activity?.cancelHoursInAdvance}"/>

                <tr>
                    <g:errorMessage bean="${occasion}"/>
                    <td><p><g:formatDate date="${occasion.date.toDate()}" formatName="date.format.dateOnly"/></p></td>
                    <td nowrap>
                        <input type="text"
                               class="timepicker center-text ${hasErrors(bean: occasion, field: 'startTime', 'error')}"
                               name="occasions[${stat}].startTime"
                               value="${occasion.startTime?.toString("HH:mm")}"
                               style="width:40px"/> -

                        <input type="text"
                               class="timepicker center-text ${hasErrors(bean: occasion, field: 'endTime', 'error')}"
                               name="occasions[${stat}].endTime"
                               value="${occasion.endTime?.toString("HH:mm")}"
                               style="width:40px"/>
                        <span class="help-block"><g:message code="facilityActivityOccasion.create.message13"/></span>
                    </td>
                    <td>
                        <g:textField class="${hasErrors(bean: occasion, field: 'price', 'error')}"
                                     name="occasions[${stat}].price" value="${occasion.price}" style="width:30px"/>
                    </td>
                    <td><g:textField class="${hasErrors(bean: occasion, field: 'numParticipants', 'error')}"
                                     name="occasions[${stat}].numParticipants" value="${occasion.numParticipants}"
                                     style="width:30px"/>
                    </td>
                    <td class="center-text">
                        <g:checkBox name="occasions[${stat}].availableOnline" value="${occasion.availableOnline}"/>
                    </td>
                    <td class="center-text">
                        <g:checkBox name="occasions[${stat}].membersOnly" value="${occasion.membersOnly}"/>
                    </td>
                    <td><g:textField class="${hasErrors(bean: occasion, field: 'signUpDaysInAdvanceRestriction', 'error')}"
                                     name="occasions[${stat}].signUpDaysInAdvanceRestriction"
                                     value="${occasion.signUpDaysInAdvanceRestriction}" style="width:30px"/>
                    <td><g:textField class="${hasErrors(bean: occasion, field: 'signUpDaysUntilRestriction', 'error')}"
                                     name="occasions[${stat}].signUpDaysUntilRestriction"
                                     value="${occasion.signUpDaysUntilRestriction}" style="width:30px"/>
                    </td>
                    <td>
                        <p>
                            <a href="javascript:void(0)" rel="popover" data-content="<g:each in="${occasion.slots}">
                                <g:slotCourtAndTime slot="${it}"/><br>
                            </g:each>"
                               data-original-title="${message(code: 'facilityActivityOccasion.create.message17')}">${occasion.slots.size()}<g:message
                                    code="unit.st"/></a>
                        </p>
                    </td>
                    <td><g:textArea class="${hasErrors(bean: occasion, field: 'message', 'error')}" name="occasions[${stat}].message" rows="1" cols="10" value="${occasion.message}"/>
                        <span class="help-block"><g:message code="facilityActivityOccasion.create.message14"/></span>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>

        <div class="form-actions">
            <g:actionSubmit action="save" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
            <g:link action="index" class="btn btn-danger" controller="facilityBooking"><g:message
                    code="button.cancel.label"/></g:link>
        </div>
        </li>

    </ul>
    </fieldset>

    </g:form>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
<r:script>
    $(document).ready(function() {
        $("#customerSearch").matchiCustomerSelect({ width: "200px" });
        $("#activityId").select2().change(function() {
            var selectedActivity = $(this).find("option:selected");
            $(":checkbox[name$=availableOnline]").prop(
                    "checked", selectedActivity.attr("data-online") == "true");
            $(":checkbox[name$=membersOnly]").prop(
                    "checked", selectedActivity.attr("data-membersOnly") == "true");
            $(":input[name$=price]").val(selectedActivity.attr("data-price") || "0");
            $(":input[name$=numParticipants]").val(selectedActivity.attr("data-participants") || "0");
            $(":input[name$=signUpDaysInAdvanceRestriction]").val(selectedActivity.attr("data-daysInAdvance") || "");
            $(":input[name$=signUpDaysUntilRestriction]").val(selectedActivity.attr("data-daysUntil") || "");
            $(":input[name$=minNumParticipants]").val(selectedActivity.attr("data-minNumParticipants") || "");
            $(":input[name$=cancelHoursInAdvance]").val(selectedActivity.attr("data-cancelHoursInAdvance") || "");
        });
        $('.timepicker').addTimePicker({
            hourText: '${message(code: 'default.timepicker.hour')}',
            minuteText: '${message(code: 'default.timepicker.minute')}'
        });
        $("a[rel=popover]").popover({trigger:'hover'});
    });

</r:script>
</body>
</html>
