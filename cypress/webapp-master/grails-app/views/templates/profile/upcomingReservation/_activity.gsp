<%@ page import="com.matchi.activities.Participation" %>
<g:set var="participation" value="${upcomingReservation as Participation}" />
<div class="col-xs-6 top-padding10">
    <h6 class="media-heading">
        ${participation.occasion.activity.facility.name}
    </h6>
    <small class="block top-margin5">
        ${participation.occasion.activity.name}
    </small>
    <div class="top-margin5">
        <ul class="list-inline text-sm">
            <!-- VIEW BOOKING -->
            <!-- CANCEL BOOKING -->
            <li class="text-xs">
                <g:remoteLink action="confirm" controller="activityPayment"
                              update="userBookingModal"
                              onSuccess="showLayer('userBookingModal')"
                              params="[id:participation.occasion.id,
                                       returnUrl: g.createLink(absolute: false, action: 'home')]">
                    <i class="fas fa-times"></i> <g:message code="button.unbook.label"/>
                </g:remoteLink>
            </li>
            <!-- SHARE BOOKING -->
        </ul>
    </div>
</div><!-- /.col-xs-6 -->
<div class="col-xs-4 text-right">
    <h6 class="top-margin10 bottom-margin5 text-right">
        <g:humanDateFormat date="${participation.occasion.getStartDateTime()}"/>
    </h6>
    <small class="block">
        <g:formatDate format="HH:mm" date="${participation.occasion.getStartDateTime().toDate()}" />
    </small>
</div><!-- /.col-xs-4 -->