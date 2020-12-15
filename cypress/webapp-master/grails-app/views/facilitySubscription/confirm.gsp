<%@ page import="com.matchi.enums.BookingGroupType; org.joda.time.DateTime; java.text.SimpleDateFormat; com.matchi.Court; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title>${ facility } - <g:message code="subscription.label"/></title>
    <jqval:resources/>
</head>
<body>
<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="subscription.label"/></g:link> <span class="divider">/</span></li>
    <li>
        <g:if test="${cmd.id}">
            <g:link action="edit"><g:message code="facilitySubscription.edit.heading"/></g:link> <span class="divider">/</span>
        </g:if>
        <g:else>
            <g:link action="create"><g:message code="facilitySubscription.confirm.message3"/></g:link> <span class="divider">/</span>
        </g:else>
    </li>
    <li class="active"><g:message code="facilitySubscription.confirm.message4"/></li>
</ul>

<g:if test="${!facility?.isMasterFacility()}">
    <g:form name="subscriptionForm" action="save" method="post" class="form-horizontal form-well">
        <g:hiddenField name="id" value="${cmd.id}"/>
        <g:hiddenField name="customerId" value="${cmd.customerId}"/>
        <g:hiddenField name="description" value="${cmd.description}"/>
        <g:hiddenField name="courtId" value="${cmd.courtId}"/>
        <g:hiddenField name="dateFrom" value="${cmd.dateFrom}"/>
        <g:hiddenField name="dateTo" value="${cmd.dateTo}"/>
        <g:hiddenField name="time" value="${cmd.time}"/>
        <g:hiddenField name="season" value="${cmd.season}"/>
        <g:hiddenField name="showComment" value="${cmd.showComment}"/>
        <g:hiddenField name="interval" value="${cmd.interval}"/>
        <g:hiddenField name="returnUrl" value="${params?.returnUrl}" />
        <g:hiddenField name="accessCode" value="${cmd.accessCode}" />

        <div class="form-header">
            ${cmd.id ? message(code: 'facilitySubscription.confirm.message30') : message(code: 'facilitySubscription.confirm.message30') }<span class="ingress"><g:message code="facilitySubscription.confirm.message5"/></span>
        </div>
        <fieldset>
            <div class="control-group form-inline">
                <label class="control-label"><g:message code="customer.label"/>:</label>
                <div class="controls strong-inline">
                    <strong>${customer.number} - ${customer.fullName()}</strong>
                </div>
            </div>
            <div class="control-group form-inline">
                <label class="control-label" for="dateFrom"><g:message code="createSubscriptionCommand.dateFrom.label"/>*</label>
                <div class="controls strong-inline">
                    <strong><g:message code="time.weekDay.plural.${new DateTime(cmd.dateFrom).dayOfWeek}"/>, </strong>
                    <strong><g:formatDate date="${new DateTime(cmd.dateFrom).toDate()}" formatName="date.format.dateOnly"/></strong>
                    &nbsp;&nbsp;<g:message code="createSubscriptionCommand.dateTo.label"/>*&nbsp;&nbsp;
                    <strong><g:formatDate date="${new DateTime(cmd.dateTo).toDate()}" formatName="date.format.dateOnly"/></strong>&nbsp;
                </div>
            </div>
            <div class="control-group form-inline">
                <label class="control-label"><g:message code="court.label"/></label>
                <div class="controls strong-inline">
                    <strong>${Court.get(cmd.courtId)}</strong>
                </div>
            </div>
            <div class="control-group form-inline">
                <label class="control-label"><g:message code="facilitySubscription.confirm.message9"/>*</label>
                <div class="controls strong-inline">
                    <strong>${cmd.time}</strong>
                </div>
            </div>
            <div class="control-group form-inline">
                <label class="control-label"><g:message code="facilitySubscription.confirm.message10"/></label>
                <div class="controls strong-inline">
                    <strong>${cmd.interval}</strong>&nbsp;
                <g:message code="bookingGroupFrequency.interval.WEEKLY"/>
                </div>
            </div>
            <g:if test="${cmd.description}">
                <div class="control-group">
                    <label class="control-label" for="description"><g:message code="subscription.description.label"/></label>
                    <div class="controls strong-inline">
                        <small>${cmd.description}</small>
                    </div>
                </div>
            </g:if>
            <g:if test="${cmd.accessCode}">
                <div class="control-group">
                    <label class="control-label" for="description"><g:message code="subscription.accessCode.label"/></label>
                    <div class="controls strong-inline">
                        <strong>${cmd.accessCode}</strong>
                    </div>
                </div>
            </g:if>
            <div class="form-actions">
                <g:if test="${slots}">
                    <g:submitButton name="submit" value="${cmd.id ? message(code: 'button.update.label') : message(code: 'button.create.label') }" class="btn btn-success" show-loader="${message(code: 'default.loader.label')}"/>
                </g:if>
                <g:actionSubmit action="${cmd.id ? 'edit' : 'create'}" name="submit" value="${message(code: 'button.back.label')}" class="btn btn-inverse"/>
                <g:if test="${params.returnUrl}">
                    <g:link url="${params.returnUrl}" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
                </g:if>
                <g:else>
                    <g:link action="index" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
                </g:else>
            </div>
            <g:if test="${sortedSlots.notAvailableSlots.size() > 0}">
                <div class="alert alert-block" style="margin: 0 20px;">
                    <a class="close" data-dismiss="alert" href="#">×</a>

                    <h4 class="alert-heading"><g:message code="facilitySubscription.confirm.message34" args="[sortedSlots.notAvailableSlots.size()]"/></h4>
                </div>
            </g:if>
        </fieldset>
    </g:form>
    <g:if test="${slots}">
        <h4><g:message code="facilitySubscription.confirm.message35" args="[sortedSlots.freeSlots.size() + sortedSlots.subscriptionSlots.size()]"/></h4>
        <p><g:message code="facilitySubscription.confirm.message14"/></p>

        <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th class="center-text"><g:message code="default.date.label"/></th>
                <th class="center-text"><g:message code="default.date.time"/></th>
                <th class="center-text"><g:message code="court.label"/></th>
                <th class="center-text"><g:message code="subscription.status.label"/></th>
                <th class="center-text"><g:message code="facilitySubscription.confirm.message19"/></th>
            </tr>
            </thead>

            <g:if test="${sortedSlots.willBeRemoved.size() > 0}">
                <g:each in="${sortedSlots.willBeRemoved}" var="slot">
                    <tr>
                        <td class="center-text"><g:formatDate date="${slot.startTime}" formatName="date.format.dateOnly"/></td>
                        <td class="center-text"><g:formatDate date="${slot.startTime}" format="HH:mm"/>-<g:formatDate date="${slot.endTime}" format="HH:mm"/></td>
                        <td class="center-text">${slot.court.name}</td>

                        <td class="center-text">
                            <span class="label label-important"><g:message code="facilitySubscription.confirm.message20"/></span>
                        </td>
                        <td class="center-text">
                            <g:message code="facilitySubscription.confirm.message21"/>
                        </td>
                    </tr>
                </g:each>
            </g:if>

            <g:each in="${slots}" var="slot">
                <tr>
                    <td class="center-text"><g:formatDate date="${slot.startTime}" formatName="date.format.dateOnly"/></td>
                    <td class="center-text"><g:formatDate date="${slot.startTime}" format="HH:mm"/>-<g:formatDate date="${slot.endTime}" format="HH:mm"/></td>
                    <td class="center-text">${slot.court.name}</td>

                    <g:if test="${!slot.booking && !slot.subscription}">
                        <td class="center-text">
                            <span class="label label-success">${cmd.id ? message(code: 'facilitySubscription.confirm.message36') : message(code: 'facilitySubscription.confirm.message37')}</span>
                        </td>
                        <td class="center-text">
                            <g:message code="facilitySubscription.confirm.message21"/>
                        </td>
                    </g:if>
                    <g:else>
                        <g:if test="${(slot.subscription != null) && (slot.subscription?.id == cmd.id)}">
                            <g:if test="${slot.booking && slot.subscription?.customer == slot.booking?.customer}">
                                <td class="center-text">
                                    <span class="label label-success"><g:message code="facilitySubscription.confirm.message23"/></span>
                                </td>
                                <td class="center-text">
                                    <g:message code="facilitySubscription.confirm.message21"/>
                                </td>
                            </g:if>
                            <g:elseif test="${slot.booking && slot.subscription?.customer != slot.booking?.customer}">
                                <td class="center-text">
                                    <span class="label label-danger"><g:message code="facilitySubscription.confirm.message38"/> ${slot.booking.customer.number}-${slot.booking.customer.fullName()}</span>
                                </td>
                                <td class="center-text">
                                    <g:message code="facilitySubscription.confirm.message21"/>
                                </td>
                            </g:elseif>
                            <g:else>
                                <td class="center-text">
                                    <span class="label label-yellow"><g:message code="facilitySubscription.confirm.message26"/></span>
                                </td>
                                <td class="center-text">
                                    <g:message code="facilitySubscription.confirm.message21"/>
                                </td>
                            </g:else>
                        </g:if>
                        <g:else>
                            <td class="center-text">
                                <g:if test="${slot.booking != null}">
                                    <span class="label label-important"><g:message code="facilitySubscription.confirm.message38"/> ${slot?.booking?.customer?.number}-${slot?.booking?.customer?.fullName()}</span>
                                </g:if>
                                <g:else>
                                    <span class="label label-yellow"><g:message code="facilitySubscription.confirm.message39"/></span>
                                </g:else>
                            </td>
                            <td class="center-text">
                                <g:if test="${slot.booking != null}">
                                    <g:remoteLink controller="facilityBooking" action="cancelForm" update="bookingModal"
                                                  onFailure="handleAjaxError()" onSuccess="showLayer()"
                                                  params="['cancelSlotsData': slot.id,
                                                          'returnUrl': g.createLink(absolute: true, action: 'confirm', params: [
                                                          id:cmd.id,
                                                          customerId: cmd.customerId,
                                                          description: cmd.description,
                                                          courtId: cmd.courtId,
                                                          dateFrom: cmd.dateFrom,
                                                          dateTo: cmd.dateTo,
                                                          time: cmd.time,
                                                          season: cmd.season,
                                                          showComment: cmd.showComment,
                                                          interval: cmd.interval,
                                                          customer: customer])]"><g:message code="button.unbook.label"/></g:remoteLink>
                                </g:if>
                            </td>
                        </g:else>
                    </g:else>
                </tr>
            </g:each>
        </table>
    </g:if>
    <g:else>
        <h3><g:message code="facilitySubscription.confirm.message29"/></h3>
    </g:else>

    <div id="bookingModal" class="modal hide fade"></div>
    <g:javascript>
        $(document).ready(function() {
            $('#bookingModal').modal({show:false});
        });
    </g:javascript>

</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
</body>
</html>
