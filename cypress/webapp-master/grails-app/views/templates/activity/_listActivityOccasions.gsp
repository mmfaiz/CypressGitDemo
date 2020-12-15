<%@ page import="com.matchi.FacilityProperty; com.matchi.watch.ClassActivityWatch" %>

<table class="table no-top-margin">
    <g:each in="${activityOccasions}" var="occasion" status="idx">
        <tr class="activity-occasion ${nOccasions && idx >= nOccasions ? 'hidden' : ''}">
            <td width="100" nowrap="" class="vertical-padding10"><g:formatDate date="${occasion.date.toDate()}" formatName="date.format.dateOnly"/>, ${occasion.startTime.toString("HH:mm")}</td>
            <td width="70" class="vertical-padding10" valign="middle">${occasion.lengthInMinutes()} <g:message code="unit.min"></g:message></td>
            <td width="70" class="vertical-padding10">
                <div  rel="tooltip" valign="middle" data-placement="top" data-animation="false" title="${message(code: "templates.booking.facilityBookingInfo.nParticipations")}">
                    <i class="fas fa-user"></i> ${occasion.numParticipations()} / ${occasion.maxNumParticipants}
                </div>
            </td>
            <td width="100" class="vertical-padding10 text-right">
                <g:if test="${user}">
                    <g:if test="${occasion.isParticipating(user)}">
                        <g:if test="${occasion.activity.cancelByUser}">
                            <g:remoteLink
                                    class="btn btn-xs book-activity"
                                    controller="activityPayment" params="[id: occasion.id]" action="confirm"
                                    update="userBookingModal"
                                    onFailure="handleAjaxError(XMLHttpRequest, textStatus, errorThrown)"
                                    onSuccess="showLayer('userBookingModal')">${message(code: 'facility.show.message31')}</g:remoteLink>
                        </g:if>
                    </g:if>
                    <g:elseif test="${occasion.isFull()}">
                        <i><g:message code="facility.show.message5"/></i>
                    </g:elseif>
                    <g:elseif test="${occasion.membersOnly && !user.hasActiveMembershipIn(facility) && !user.getMembershipIn(facility)?.inStartingGracePeriod}">
                        <i>
                            <g:message code="facility.show.message32"/>
                            <g:if test="${facility.recieveMembershipRequests}">
                                <g:link controller="membershipRequest"
                                        action="index" params="[name: facility.shortname]"><g:message code="facility.show.message33"/></g:link>
                            </g:if>
                        </i>
                    </g:elseif>
                    <g:else>
                        <g:remoteLink
                                class="btn btn-xs ${occasion.isParticipating(user) ? "" : "btn-success"} book-activity"
                                controller="activityPayment" params="[id: occasion.id]" action="confirm"
                                update="userBookingModal"
                                onFailure="handleAjaxError(XMLHttpRequest, textStatus, errorThrown)"
                                onSuccess="showLayer('userBookingModal')">${occasion.isParticipating(user) ? message(code: 'facility.show.message31') : message(code: 'button.book.label')}
                        </g:remoteLink>
                    </g:else>
                </g:if>
                <g:elseif test="${occasion.isFull()}">
                    <i><g:message code="facility.show.message5"/></i>
                </g:elseif>
                <g:elseif test="${!user && occasion.membersOnly}">
                    <sec:ifNotLoggedIn>
                        <i>
                            <g:message code="facility.show.message32"/>
                            <g:message code="facility.show.message35"/>
                        </i>
                    </sec:ifNotLoggedIn>
                </g:elseif>
                <g:else>
                    <g:link
                            class="btn btn-xs btn-success"
                            controller="login" action="auth"
                            params="${[returnUrl:createLink(controller: 'activity', action: 'index', params: [ id: occasion.id, comeback:true])]}" onclick="loginBeforeBooking(this.href, '${occasion.id}');">
                        <g:message code="button.book.label"/>
                    </g:link>
                </g:else>
            </td>
        </tr>

        <g:if test="${!user && occasion.isFull()}">
            <g:ifFacilityPropertyEnabled facility="${facility}" name="${FacilityProperty.FacilityPropertyKey.FEATURE_QUEUE.name()}">
                <sec:ifNotLoggedIn>
                    <tr class="${nOccasions && idx >= nOccasions ? 'hidden' : ''}">
                        <td colspan="5" class="text-right text-sm no-border no-top-padding">
                            <g:link
                                    class="btn btn-xs btn-link"
                                    controller="login" action="auth"
                                    params="${[returnUrl:request.forwardURI]}" onclick="loginBeforeBooking(this.href, '${occasion.id}');">
                                <g:message code="facility.show.message34"/>
                            </g:link>
                        </td>
                    </tr>
                </sec:ifNotLoggedIn>
            </g:ifFacilityPropertyEnabled>
        </g:if>

        <g:if test="${user && !occasion.isParticipating(user) && occasion.isFull()}">
            <g:ifFacilityPropertyEnabled facility="${facility}" name="${FacilityProperty.FacilityPropertyKey.FEATURE_QUEUE.name()}">
                <tr class="${nOccasions && idx >= nOccasions ? 'hidden' : ''}">
                    <td colspan="5" class="text-right text-sm no-border no-top-padding">
                        <g:set var="existingActivityWatch"
                               value="${ClassActivityWatch.findByUserAndClassActivityAndFromDate(user, occasion.activity, occasion.date.toDateTime(occasion.startTime).toDate())}"/>
                        <g:link controller="classActivityWatch" action="confirm"
                                data-occasion-id="${occasion.id}"
                                elementId="occasion-watch_${occasion.id}" style="${existingActivityWatch ? 'display: none' : ''}"
                                class="add-activity-watch">
                            <g:message code="activityQueueForm.link.add"/>
                        </g:link>
                        <g:link controller="classActivityWatch" method="DELETE" data-occasion-id="${occasion.id}"
                                data-confirm-message="${message(code: 'default.confirm')}"
                                data-watch-id="${existingActivityWatch?.id}" class="remove-activity-watch"
                                elementId="occasion-unwatch_${occasion.id}" style="${existingActivityWatch ? '' : 'display: none'}">
                            <g:message code="activityQueueForm.link.remove"/>
                        </g:link>
                    </td>
                </tr>
            </g:ifFacilityPropertyEnabled>
        </g:if>
    </g:each>
</table>