<%@ page import="com.matchi.Sport; com.matchi.Court; com.matchi.FacilityProperty"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="default.activity.plural"/></title>
</head>
<body>

<g:if test="${!facility?.isMasterFacility()}">
    <ul class="breadcrumb">
        <li><g:link controller="facilityActivity" action="index"><g:message code="default.activity.plural"/></g:link> <span class="divider">/</span></li>
        <li class="active">${activity.name}</li>
    </ul>

    <g:errorMessage bean="${activity}"/>

    <ul class="nav nav-tabs">
        <li class="active"><g:link action="occasions" id="${activity.id}"><g:message code="facilityActivity.occasions.message3"/></g:link></li>
        <li>
            <g:link controller="facilityActivity" action="edit" id="${activity.id}"><g:message code="button.edit.label"/></g:link>
        </li>
    </ul>

    <h4><g:message code="facilityActivity.occasions.message5"/></h4>
    <table class="table" data-provides="rowlink">
        <thead>
        <tr>
            <th><g:message code="facilityActivity.occasions.message6"/></th>
            <th><g:message code="facilityActivity.occasions.message7"/></th>
            <th><g:message code="default.price.label"/> (<g:currentFacilityCurrency facility="${activity?.facility}"/>)</th>
            <th><g:message code="user.activity.label"/></th>
            <g:ifFacilityPropertyEnabled facility="${activity.facility}" name="${FacilityProperty.FacilityPropertyKey.FEATURE_QUEUE.name()}">
                <th><g:message code="facilityActivityOccasion.edit.watchQueue"/></th>
            </g:ifFacilityPropertyEnabled>
        </tr>
        </thead>
        <tbody>
        <g:each in="${futureOccasions}">
            <tr>
                <td>
                    <g:link controller="facilityActivityOccasion" action="edit" params="[id: it.id]">
                        <strong><g:formatDate date="${it.date.toDate()}" formatName="date.format.dateOnly"/></strong>
                        ${it.startTime.toString("HH:mm")}-${it.endTime.toString("HH:mm")}
                    </g:link>
                </td>
                <td>
                    <span class="label label-${(it.participations.size() >= it.maxNumParticipants) ? 'important':'success'}">${it.participations.size()} / ${it.maxNumParticipants}</span>
                </td>
                <td>
                    <g:formatMoney value="${it.price}" facility="${it?.activity?.facility}"/>
                </td>
                <td>
                    <a href="javascript:void(0)" rel="popover" data-content="<g:each in="${it.bookings}" var="booking">
                        <g:slotCourtAndTime slot="${booking.slot}"/><br>
                    </g:each>" data-original-title="${message(code: 'facilityActivity.occasions.message11')}">${it.bookings.size()}<g:message code="unit.st"/></a>

                </td>
                <g:ifFacilityPropertyEnabled facility="${activity.facility}" name="${FacilityProperty.FacilityPropertyKey.FEATURE_QUEUE.name()}">
                    <td>${it.watchQueueSize}</td>
                </g:ifFacilityPropertyEnabled>
            </tr>


        </g:each>

        </tbody>
    </table>
    <br>
    <h4><g:message code="facilityActivity.occasions.message10"/></h4>
    <table class="table">
        <thead>
        <tr>
            <th><g:message code="facilityActivity.occasions.message6"/></th>
            <th><g:message code="facilityActivity.occasions.message7"/></th>
            <th><g:message code="default.price.label"/> (<g:currentFacilityCurrency facility="${activity?.facility}"/>)</th>
            <th><g:message code="user.activity.label"/></th>
            <th>&nbsp;</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${pastOccasions}">
            <tr>
                <td>
                    <strong><g:formatDate date="${it.date.toDate()}" formatName="date.format.dateOnly"/></strong>
                    ${it.startTime.toString("HH:mm")}-${it.endTime.toString("HH:mm")}
                </td>
                <td>
                    <span class="label label-${(it.participations.size() >= it.maxNumParticipants) ? 'important':'success'}">${it.participations.size()} / ${it.maxNumParticipants}</span>
                </td>
                <td>
                  <g:formatMoney value="${it.price}" facility="${it?.activity?.facility}"/>
                </td>
                <td>
                    <a href="javascript:void(0)" rel="popover" data-content="<g:each in="${it.bookings}" var="booking">
                        <g:slotCourtAndTime slot="${booking.slot}"/><br>
                    </g:each>" data-original-title="${message(code: 'facilityActivity.occasions.message11')}">${it.bookings.size()}<g:message code="unit.st"/></a>
                </td>
                <td>
                    <g:link controller="facilityActivityOccasion" action="edit" params="[id: it.id]"><img src="${resource(dir:'images', file:'edit_btn.png')}"/></g:link>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
<r:script>
    $(document).ready(function() {
        $("a[rel=popover]").popover({trigger:'hover'});
        $("[rel='tooltip']").tooltip();
    });
</r:script>
</body>
</html>
