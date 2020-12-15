<%@ page import="com.matchi.enums.BookingGroupType; org.joda.time.DateTime; com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilitySeasonDeviation.confirm.message1"/></title>
</head>
<body>

<ul class="breadcrumb">
    <li><g:link controller="facilitySeason" action="index"><g:message code="season.label.plural"/></g:link> <span class="divider">/</span></li>
    <li><g:link controller="facilitySeason" action="edit" id="${form.seasonId}"><g:message code="facilitySeason.edit.heading"/></g:link> <span class="divider">/</span></li>
    <li><g:link controller="facilitySeasonDeviation" action="create" params="[seasonId:form.seasonId]"><g:message code="facilitySeasonDeviation.confirm.message4"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="facilitySeasonDeviation.confirm.message1"/></li>
</ul>
<g:flashError/>

<g:set var="notAvailableBooking" value="${!createSeason.open && slots.find { it.slot.booking && it.slot.booking.group?.type == BookingGroupType.NOT_AVAILABLE }}"/>
<g:set var="anotherKindOfBooking" value="${!createSeason.open && slots.find { it.slot.booking && it.slot.booking.group?.type != BookingGroupType.NOT_AVAILABLE }}"/>
<g:set var="bookedOverlappingSlot" value="${createSeason.open && overlaps?.values()?.flatten().find { it.slot.booking && it.slot.booking.group?.type != BookingGroupType.NOT_AVAILABLE }}"/>
<g:if test="${anotherKindOfBooking || bookedOverlappingSlot}">
    <div class="alert alert-danger">
        <a class="close" data-dismiss="alert" href="#"><i class="fas fa-times"></i></a>
        <g:message code="facilitySeasonDeviation.confirm.slots.notDelete"/>
    </div>
</g:if>
<g:elseif test="${notAvailableBooking}">
    <div class="alert">
        <a class="close" data-dismiss="alert" href="#"><i class="fas fa-times"></i></a>
        <g:message code="facilitySeasonDeviation.confirm.slots.delete"/>
    </div>
</g:elseif>

<h4>
    <g:message code="facilitySeasonDeviation.confirm.${createSeason.open ? 'message13' : 'message14'}" args="[slots.size()]"/>
</h4>

<g:if test="${tableSlots}">
    <h4>
        <g:message code="facilitySeasonDeviation.confirm.${createSeason.open ? 'overlapped' : 'booked'}"/>:
    </h4>

    <table class="table table-striped table-bordered">
        <thead>
        <tr>
            <th width="100"><g:message code="default.day.label"/></th>
            <th width="80"><g:message code="default.date.time"/></th>
            <th width="100"><g:message code="court.label"/></th>
            <th width="100"><g:message code="facilitySeasonDeviation.confirm.message9"/></th>
        </tr>
        </thead>

        <g:each in="${tableSlots}" var="slotItem">
            <tr>
                <td>
                    <g:formatDate formatName="date.format.dateOnly" date="${slotItem.slot.startTime}"/> (<g:message code="time.shortWeekDay.${new DateTime(slotItem.slot.startTime).dayOfWeek}"/>)
                </td>
                <td><g:formatDate format="HH:mm" date="${slotItem.slot.startTime}"/> - <g:formatDate format="HH:mm" date="${slotItem.slot.endTime}"/></td>
                <td>${slotItem.slot.court.name}</td>
                <td class="center-text">
                    <g:if test="${createSeason.open}">

                        <g:each in="${overlaps.get(slotItem.slot)}" var="overlappingSlot">
                            <g:if test="${overlappingSlot.slot.booking && overlappingSlot.slot.booking?.group?.type != BookingGroupType.NOT_AVAILABLE}">
                                <span class="label label-warning"><g:slotCourtAndTime slot="${overlappingSlot.slot}"/></span>
                                →
                                <g:remoteLink controller="facilityBooking" action="cancelForm" update="bookingModal"
                                        onFailure="handleAjaxError()" onSuccess="showLayer()"
                                        params="['cancelSlotsData': overlappingSlot.slot.id,
                                              'id': form.seasonId,
                                              'returnUrl': g.createLink(absolute: true, action: 'confirm', id:form.seasonId)
                                        ]"><g:message code="button.unbook.label"/></g:remoteLink>
                            </g:if>
                            <g:else>
                                <span class="label label-important"><g:slotCourtAndTime slot="${overlappingSlot.slot}"/></span>
                                →
                                <g:message code="facilitySeasonDeviation.confirm.message11"/>
                            </g:else>
                            <br>
                        </g:each>
                    </g:if>
                    <g:else>

                        <g:if test="${slotItem.slot.booking && slotItem.slot.booking?.group?.type != BookingGroupType.NOT_AVAILABLE}">
                            <g:remoteLink controller="facilityBooking" action="cancelForm" update="bookingModal"
                                          onFailure="handleAjaxError()" onSuccess="showLayer()"
                                          params="['cancelSlotsData': slotItem.slot.id,
                                                  'id': form.seasonId,
                                                  'returnUrl': g.createLink(absolute: true, action: 'confirm', id:form.seasonId)
                                          ]"><g:message code="button.unbook.label"/></g:remoteLink>
                        </g:if>
                        <g:elseif test="${slotItem.slot.booking && slotItem.slot.booking?.group?.type == BookingGroupType.NOT_AVAILABLE}">
                            <span class="label label-important"><g:message code="facilitySeasonDeviation.confirm.message11"/></span>
                            <span class="label label-warning not-available-warning"><g:message code="bookingGroup.name.NOT_AVAILABLE"/></span>
                        </g:elseif>
                        <g:else>
                            <span class="label label-important"><g:message code="facilitySeasonDeviation.confirm.message11"/></span>
                        </g:else>

                    </g:else>
                </td>
            </tr>
        </g:each>
    </table>
</g:if>

<g:form name="deviationForm" action="applyDeviations">
    <fieldset>
        <div class="form-actions">
            <g:submitButton name="save" value="${message(code: 'button.save.label')}" class="btn btn-success"/>
            <g:link action="edit" controller="facilitySeason" id="${form.seasonId}" class="btn btn-danger"><g:message code="button.cancel.label"/></g:link>
        </div>
    </fieldset>
</g:form>

<div id="bookingModal" class="modal hide fade"></div>
<script type="text/javascript">
    $(document).ready(function() {
        $('#bookingModal').modal({show:false});

        $("#seasons-search-input").focus()

        $(".not-available-warning").tooltip({
            placement: 'right',
            title: '${message(code: 'facilitySeasonDeviation.confirm.message16')}'
        });

        $('#deviationForm').preventDoubleSubmission({})

        <g:if test="${anotherKindOfBooking || bookedOverlappingSlot}">
            $("#save").attr("disabled", "disabled");
        </g:if>
    });
</script>
</body>
</html>
