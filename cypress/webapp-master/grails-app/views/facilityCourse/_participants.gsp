<%@ page import="com.matchi.activities.Participant" %>
<g:if test="${participants?.size() > 0}">
    <g:each in="${participants}">
        <li id="${it.extendedId}" customerId="${it.customer?.id}" class="draggable-item course-${it.activity.hintColor}" rel="${it.activity.id}">
            <div class="media">
                <div class="media-left right-padding10">
                    <div class="avatar-circle-xxs">
                        <g:fileArchiveUserImage id="${it.customer.user?.id}" size="small"/>
                    </div>
                </div>
                <div class="media-body weight400">
                    <g:link controller="facilityCustomer" action="show" id="${it.customer.id}"
                            class="draggable-handle text-sm ellipsis ${it.status == com.matchi.activities.Participant.Status.CANCELLED ? 'text-strikethrough' : ''}"
                            target="_blank">
                        ${it.customer.fullName}

                        <g:set var="nrOfOccasionsFromFormSubmission" value="${it.numberOfOccasionsFromSubmission.toInteger()}"/>
                        <g:set var="nrOfOccasionsPlanned" value="${it.nOccasions}"/>
                        <g:set var="badgeColor" value="${nrOfOccasionsPlanned == nrOfOccasionsFromFormSubmission ? 'success':'warning'}"/>
                        <span id="${it.extendedId}_nrOccasions" class="badge badge-${badgeColor} pull-right">
                            <span class="plannedOccasions">${nrOfOccasionsPlanned}</span> /
                            <span class="wantedOccasions">${nrOfOccasionsFromFormSubmission}</span>
                        </span>
                    </g:link>
                    <g:if test="${it.customer.birthyear}">
                        <div class="text-sm text-right pull-right" style="width: 8%; margin-right: 5px;">
                            -${it.customer.birthyear.toString()[-2..-1]}
                        </div>
                    </g:if>
                    <g:if test="${it.submission}">
                        <g:link controller="facilityForm" action="showSubmission" id="${it.submission.id}" class="submission pull-right" target="_blank">
                            <span class="ti ti-search"></span>
                        </g:link>
                    </g:if>
                </div>
            </div>
        </li>
    </g:each>
</g:if>
<g:else>
    <li><p><g:message code="facilityCourse.planning.selectCourseToFilter" /></p></li>
</g:else>