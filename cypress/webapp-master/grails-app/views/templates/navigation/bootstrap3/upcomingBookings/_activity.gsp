<%@ page import="com.matchi.activities.Participation" %>
<g:set var="participation" value="${reservation as Participation}" />
<li>
    <g:remoteLink class="userCancelBooking" action="confirm" controller="activityPayment"
                  update="userBookingModal"
                  onSuccess="showLayer('userBookingModal')"
                  params="[id:participation.occasion.id]">
        <div class="media">
            <div class="media-left">
                <div class="avatar-square-xs text-lg text-center">
                    <i class="fa fa-calendar fa-lg"></i>
                </div>
            </div>
            <div class="media-body">
                <h5 class="media-heading">${participation.occasion.activity.facility.encodeAsHTML()}</h5>
                ${participation.occasion.activity.name.encodeAsHTML()}
            </div>
            <div class="media-right">
                <g:formatDate format="dd/MM HH:mm" date="${participation.occasion.getStartDateTime().toDate()}" />
            </div>
        </div>
    </g:remoteLink>
</li>