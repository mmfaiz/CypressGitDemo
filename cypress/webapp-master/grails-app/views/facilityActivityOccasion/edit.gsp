<%@ page import="com.matchi.Sport; com.matchi.Court; com.matchi.excel.ExcelExportManager; com.matchi.orders.Order; com.matchi.FacilityProperty" %>
<g:set var="editOccasionUrl" value="${createLink(action: 'edit', id: occasion.id)}"/>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityActivityOccasion.edit.message1"/></title>
    <r:require modules="matchi-customerselect,jquery-timepicker"/>
</head>

<body>

<g:if test="${!facility?.isMasterFacility()}">
    <g:errorMessage bean="${cmd}"/>

    <ul class="breadcrumb">
        <li><g:link controller="facilityActivity" action="index"><g:message code="default.activity.plural"/></g:link> <span
                class="divider">/</span></li>
        <li><g:link controller="facilityActivity" action="occasions"
                    id="${occasion.activity.id}">${occasion.activity.name}</g:link> <span class="divider">/</span></li>
        <li class="active"><g:message code="facilityActivityOccasion.edit.message3"/></li>
    </ul>

    <h3 style="display: inline-block;">${occasion.activity.name}</h3>

    <p style="display: inline-block;padding-left: 10px">
        <g:formatDate date="${occasion.date.toDate()}" formaName="date.format.dateOnly"/>
        ${occasion.startTime.toString("HH:mm")} - ${occasion.endTime.toString("HH:mm")}
    </p>

    <div class="row">
        <div class="span6">
            <g:form action="update" class="form-horizontal">
                <g:hiddenField name="id" value="${occasion.id}"/>
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="message"><g:message
                                code="activityOccasion.message.label"/> <g:inputHelp
                                title="${message(code: 'facilityActivityOccasion.edit.message22')}"/></label>

                        <div class="controls">
                            <textarea class="span4" rows="5" cols="10" id="message" name="message"
                                      placeholder="${message(code: 'facilityActivityOccasion.edit.message22')}"
                                      id="description"
                                      style="overflow:auto;resize:none"
                                      class="${hasErrors(bean: cmd, field: 'message', 'error')}"
                                      maxlength="255">${occasion.message}</textarea>
                            <span class="help-inline"><a href="#" rel="tooltip" class="privacy"
                                                         data-original-title="${message(code: 'facilityActivityOccasion.edit.message24')}"><i
                                        class="icon-globe"></i></a></span>
                        </div>
                    </div>

                    <div class="control-group ">
                        <label class="control-label" for="firstname"><g:message code="default.date.time"/></label>

                        <div class="controls controls-row">
                            <input type="text" name="startTime"
                                   class="span1 center-text timepicker ${hasErrors(bean: cmd, field: 'startTime', 'error')}"
                                   value="${occasion.startTime.toString("HH:mm")}"
                                   placeholder="${message(code: 'facilityActivityOccasion.edit.message25')}"
                                   id="firstname">
                            <span class="span1" style="width: 5px">-</span>
                            <input type="text" name="endTime"
                                   class="span1 center-text timepicker ${hasErrors(bean: cmd, field: 'endTime', 'error')}"
                                   value="${occasion.endTime.toString("HH:mm")}"
                                   placeholder="${message(code: 'facilityActivityOccasion.edit.message26')}"
                                   id="lastname">
                        </div>
                    </div>

                    <div class="control-group ">
                        <label class="control-label" for="price"><g:message code="default.price.label"/></label>

                        <div class="controls">
                            <input type="text" name="price"
                                   class="${hasErrors(bean: cmd, field: 'price', 'error')}"
                                   value="${occasion.price}" placeholder="${message(code: 'default.price.label')}"
                                   class="span2" id="price">
                        </div>
                    </div>

                    <div class="control-group ">
                        <label class="control-label" for="price"><g:message
                                code="activityOccasion.maxNumParticipants.label"/></label>

                        <div class="controls">
                            <input type="text" name="maxNumParticipants"
                                   class="${hasErrors(bean: cmd, field: 'maxNumParticipants', 'error')}"
                                   value="${occasion.maxNumParticipants}"
                                   placeholder="${message(code: 'facilityActivityOccasion.edit.message27')}" class="span2"
                                   id="maxNumParticipants">
                        </div>
                    </div>

                    <div class="control-group ">
                        <label class="control-label" for="signUpDaysInAdvanceRestriction">
                            <g:message code="activity.signUpDaysInAdvanceRestriction.label"/>
                        </label>

                        <div class="controls">
                            <g:field type="number" name="signUpDaysInAdvanceRestriction"
                                     value="${occasion.signUpDaysInAdvanceRestriction}" min="0" max="${Integer.MAX_VALUE}"
                                     class="span2 ${hasErrors(bean: cmd, field: 'signUpDaysInAdvanceRestriction', 'error')}"/>
                        </div>
                    </div>

                    <div class="control-group ">
                        <label class="control-label" for="signUpDaysUntilRestriction">
                            <g:message code="activity.signUpDaysUntilRestriction.label"/>
                        </label>

                        <div class="controls">
                            <g:field type="number" name="signUpDaysUntilRestriction"
                                     value="${occasion.signUpDaysUntilRestriction}" min="0" max="${Integer.MAX_VALUE}"
                                     class="span2 ${hasErrors(bean: cmd, field: 'signUpDaysUntilRestriction', 'error')}"/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="availableOnline"><g:message
                                code="activityOccasion.availableOnline.label"/></label>

                        <div class="controls">
                            <label class="checkbox">
                                <g:checkBox name="availableOnline" value="${occasion.availableOnline}"/>
                            </label>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="membersOnly"><g:message
                                code="activityOccasion.membersOnly.label"/></label>

                        <div class="controls">
                            <label class="checkbox">
                                <g:checkBox name="membersOnly" value="${occasion.membersOnly ?: false}"/>
                            </label>
                        </div>
                    </div>

                    <div class="control-group ">
                        <label class="control-label" for="cancelHoursInAdvance">
                            <g:message code="classActivity.cancelHoursInAdvance.label"/>
                        </label>

                        <div class="controls">
                            <g:field type="number" name="cancelHoursInAdvance"
                                     value="${occasion.cancelHoursInAdvance}" min="0" max="${Integer.MAX_VALUE}"
                                     class="span2 ${hasErrors(bean: cmd, field: 'cancelHoursInAdvance', 'error')}"/>
                            <p><g:message code="activityOccasion.editCancellationRules.message"/></p>
                        </div>
                    </div>

                    <div class="control-group ">
                        <label class="control-label" for="minNumParticipants">
                            <g:message code="classActivity.minNumParticipants.label"/>
                        </label>

                        <div class="controls">
                            <g:field type="number" name="minNumParticipants"
                                     value="${occasion.minNumParticipants}" min="0" max="${Integer.MAX_VALUE}"
                                     class="span2 ${hasErrors(bean: cmd, field: 'minNumParticipants', 'error')}"/>
                            <p><g:message code="activityOccasion.editCancellationRules.message"/></p>
                        </div>
                    </div>

                    <div class="form-actions">
                        <g:submitButton name="btnSubmit" class="btn btn-success" style="margin-bottom: 10px;"
                                        value="${message(code: 'button.save.label')}"/>
                        <g:actionSubmit action="delete" id="${occasion.id}"
                                        onclick="return confirm('${message(code: 'facilityActivityOccasion.edit.message28')}')"
                                        class="btn btn-danger" style="margin-bottom: 10px;"
                                        value="${message(code: 'button.delete.label')}"/>
                        <g:link class="btn btn-primary" style="margin-bottom: 10px;" action="occasions"
                                controller="facilityActivity" id="${occasion.activity.id}"><g:message
                                code="button.back.label"/></g:link>
                        <g:actionSubmit action="deleteAndRefund" id="${occasion.id}"
                                        onclick="return confirm('${message(code: 'facilityActivityOccasion.edit.message29')}')"
                                        class="btn btn-inverse" style="margin-bottom: 10px;" value="Remove and refund"/>
                    </div>

                </fieldset>
            </g:form>

            <table class="table" style="width:100%">
                <thead>
                <tr>
                    <th colspan="3"><g:message code="default.booking.plural"/> (${occasion.bookings.size()}<g:message
                            code="unit.st"/>)</th>
                </tr>
                </thead>
                <tbody>
                <g:if test="${!occasion?.bookings?.isEmpty()}">
                    <g:each in="${occasion?.bookings}" var="booking">
                        <tr>
                            <td width="100">${booking.slot.court.name}</td>
                            <td width="100">
                                <g:formatDate date="${booking.slot.startTime}" format="HH:mm"/> -
                                <g:formatDate date="${booking.slot.endTime}" format="HH:mm"/>
                            </td>

                            <td>
                                <g:if test="${occasion.bookings.size() > 1}">
                                    <g:remoteLink controller="facilityBooking" action="cancelForm" update="bookingModal"
                                                  class="pull-right"
                                                  onFailure="handleAjaxError()" onSuccess="showLayer('bookingModal')"
                                                  title="Avboka"
                                                  params="['cancelSlotsData': booking.slot.id,
                                                           'returnUrl'      : editOccasionUrl]"><i
                                            class="icon-remove"></i></g:remoteLink>
                                </g:if>
                                <g:else>
                                    <small class="pull-right"><g:message
                                            code="facilityActivityOccasion.edit.message8"/></small>
                                </g:else>
                            </td>

                        </tr>
                    </g:each>
                </g:if>
                <g:else>
                    <tr>
                        <td colspan="2" align="center"><p><i><g:message code="facilityActivityOccasion.edit.message9"/></i>
                        </p></td>
                    </tr>
                </g:else>
                </tbody>
            </table>

        </div>

        <div class="offset1 span5">
            <g:form action="addParticipant" id="${occasion.id}">

                <table class="table" style="width:100%">
                    <thead>
                    <tr>
                        <th colspan="2"><g:message
                                code="facilityActivityOccasion.edit.message30"/> (${occasion.participations.size()}<g:message
                                code="unit.st"/>)</th>
                        <th>
                            <a id="exportCustomers" href="javascript: void(0)"
                               class="btn btn-small btn-inverse pull-right">
                                <g:message code="button.export.label"/>
                            </a>
                        </th>
                    </tr>
                    </thead>
                    <tr>

                        <td colspan="3">
                            <g:if test="${!occasion.isFull() && !occasion.isPast()}">
                                <input type="hidden" name="customerId" id="customerSearch"/>
                                <g:link controller="facilityCustomer" action="create"
                                        class="btn btn-small right left-margin10"
                                        params="[returnUrl: g.createLink(absolute: true, action: 'edit', id: occasion.id)]"><g:message
                                        code="facilityActivityOccasion.edit.message10"/></g:link>
                                <g:submitButton id="btnAddParticipant" name="btnAddParticipant"
                                                value="${message(code: 'button.add.label')}" class="btn btn-small right"/>
                            </g:if>
                            <g:elseif test="${occasion.isPast()}">
                                <h6><g:message code="facilityActivityOccasion.edit.message11"/></h6>
                            </g:elseif>
                            <g:else>
                                <h6><g:message code="facilityActivityOccasion.edit.message12"/></h6>
                            </g:else>
                        </td>
                    </tr>
                    <g:if test="${!participants.isEmpty()}">
                        <g:each in="${participants}" var="participant">
                            <g:hiddenField name="customerId" value="${participant.customer.id}"
                                           class="participantCustomer"/>
                            <tr>
                                <td width="1">
                                    <span class="span1" style="margin-left: 0;">
                                        <g:fileArchiveUserImage size="small" id="${participant.customer.user?.id}"/>
                                    </span>
                                </td>

                                <td>
                                    <b>
                                        <g:link controller="facilityCustomer" action="show" id="${participant.customer.id}">
                                            ${participant.customer.number} - ${participant.customer.fullName()}
                                        </g:link>
                                    </b>
                                    <g:if test="${participant.order?.metadata?.get(Order.META_USER_MESSAGE)}">
                                        <span class="help-inline"><a href="#" rel="tooltip" class="privacy"
                                                                     title="${participant.order.metadata[Order.META_USER_MESSAGE]}"><i
                                                    class="icon-comment"></i></a></span>
                                    </g:if>
                                    <br>
                                    <g:set var="payment" value="${paymentStatus[participant]}"/>
                                    <g:if test="${payment}">
                                        <span class="label label-success" rel="tooltip"
                                              title="${message(code: 'payment.paid.label')} <g:formatDate
                                                      date="${payment.dateCreated}" format="yyyy-MM-dd HH:mm"/>"><g:message
                                                code="payment.method.${payment.method}"/></span>
                                    </g:if>
                                    <g:elseif
                                            test="${participant?.order?.isFinalPaid() && !participant?.order?.isAnnulled()}">
                                        <span class="label label-success" rel="tooltip"
                                              title="${message(code: 'payment.paid.label')} <g:formatDate
                                                      date="${participant.order.dateCreated}" format="yyyy-MM-dd HH:mm"/>">
                                            <g:if test="${participant.order.isPaidByCreditCard()}">
                                                <g:message code="payment.method.CREDIT_CARD"/>
                                            </g:if>
                                            <g:elseif test="${participant.order.isPaidByCoupon()}">
                                                <g:message code="payment.method.COUPON"/>
                                            </g:elseif>
                                            <g:else>
                                                <g:message code="payment.paid.label"/>
                                            </g:else>

                                        </span>
                                    </g:elseif>
                                    <g:else>
                                        <span class="label label-important"><g:message
                                                code="facilityActivityOccasion.edit.message16"/></span>

                                    </g:else>
                                    ${participant.customer.email}
                                </td>

                                <td align="right">
                                    <div class="btn-group pull-right">
                                        <button class="btn btn-small dropdown-toggle" data-toggle="dropdown"><g:message
                                                code="facilityActivityOccasion.edit.message32"/> <span class="caret"></span>
                                        </button>
                                        <ul class="dropdown-menu">
                                            <g:if test="${participant?.order?.hasRefundablePayment() && participant.order?.isStillRefundable()}">
                                                <li><g:link action="removeParticipantPayment"
                                                            params="[participant: participant.id]"><g:message
                                                            code="facilityActivityOccasion.edit.refund"/></g:link></li>
                                                <li><g:link action="removeParticipantAndRefund"
                                                            params="[participant: participant.id, occasion: occasion.id]"><g:message
                                                            code="facilityActivityOccasion.edit.removeAndRefund"/></g:link></li>
                                                <li><g:link action="removeParticipantWithoutRefund"
                                                            params="[participant: participant.id, occasion: occasion.id]"><g:message
                                                            code="facilityActivityOccasion.edit.removeWithoutRefund"/></g:link></li>
                                            </g:if>
                                            <g:elseif
                                                    test="${participant?.order?.isFinalPaid() && !participant?.order?.isAnnulled()}">
                                                <g:if test="${!participant?.order.isPaidByCreditCard()}">
                                                    <li><g:link action="removeParticipantPayment"
                                                                params="[participant: participant.id]"><g:message
                                                                code="facilityActivityOccasion.edit.message17"/></g:link></li>
                                                </g:if>
                                                <li><g:link action="removeParticipantWithoutRefund"
                                                            params="[participant: participant.id, occasion: occasion.id]"><g:message
                                                            code="facilityActivityOccasion.edit.remove"/></g:link></li>
                                            </g:elseif>
                                            <g:else>
                                                <li><g:link action="addParticipantPayment"
                                                            params="[participant: participant.id]"><g:message
                                                            code="facilityActivityOccasion.edit.message18"/></g:link></li>
                                                <li><g:link action="removeParticipantWithoutRefund"
                                                            params="[participant: participant.id, occasion: occasion.id]"><g:message
                                                            code="facilityActivityOccasion.edit.remove"/></g:link></li>
                                            </g:else>

                                        </ul>
                                    </div>
                                </td>
                            </tr>
                        </g:each>
                    </g:if>
                    <g:else>
                        <tr>
                            <td colspan="2" align="center"><p><i><g:message
                                    code="facilityActivityOccasion.edit.message20"/></i></p></td>
                        </tr>
                    </g:else>

                </table>
            </g:form>
        </div>

        <g:if test="${!occasion.isPast()}">
            <g:ifFacilityPropertyEnabled facility="${occasion.activity.facility}"
                                         name="${FacilityProperty.FacilityPropertyKey.FEATURE_QUEUE.name()}">
                <g:render template="waitingList"/>
            </g:ifFacilityPropertyEnabled>
        </g:if>
    </div>

    <div id="bookingModal" class="modal hide fade"></div>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
    <r:script>
        $(document).ready(function() {
            $('#bookingModal').modal({ show:false, dynamic: true });

            $("#customerSearch").matchiCustomerSelect({
                width: "210px",
                placeholder: "${message(code: 'facilityActivityOccasion.edit.message33')}",
                onchange: function() { $('#btnAddParticipant').focus() }
            });
            $("a[rel=popover]").popover();
            $('.timepicker').addTimePicker({
                hourText: '${message(code: 'default.timepicker.hour')}',
                minuteText: '${message(code: 'default.timepicker.minute')}'
            });
            $("[rel='tooltip']").tooltip({html: false});

            $("#exportCustomers").click(function() {
                var customerIds = $(":hidden.participantCustomer");

                if (!customerIds.length) {
                    alert("${message(code: 'facilityCourseParticipant.index.noneSelected', encodeAs: 'JavaScript')}");
                    return;
                }

                window.location.href = "${g.forJavaScript(data: createLink(controller: 'facilityCustomer', action: 'export', params: [exportType: ExcelExportManager.ExportType.COMPLETE, returnUrl: createLink(action: 'edit', id: occasion.id)]))}&" + customerIds.serialize();
            });
        });
</r:script>
</body>
</html>
