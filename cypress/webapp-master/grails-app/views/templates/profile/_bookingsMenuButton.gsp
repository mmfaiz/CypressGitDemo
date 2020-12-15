<a href="#" class="dropdown-toggle" data-toggle="dropdown">
    <span><i class="fa fa-calendar"></i>
    <g:if test="${reservations}">
        <span class="badge">${reservations.size()}</span>
    </g:if>
    <b class="caret"></b></span>
</a>
<ul class="dropdown-menu dropdown-bookings">
    <g:if test="${reservations}">
        <g:each in="${reservations}" var="reservation" status="i">
            <g:if test="${i < 6}">
                <g:render template="/templates/navigation/bootstrap3/upcomingBookings/${reservation.getArticleType().name().toLowerCase()}" model="[reservation: reservation]"/>
            </g:if>
        </g:each>
    </g:if>
    <g:else>
        <li><a><g:message code="default.no.upcoming.bookings"/></a></li>
    </g:else>
    <li class="divider"></li>
    <li><g:link controller="userProfile" action="bookings"><g:message code="templates.profile.bookingsMenuButton.message2"/></g:link></li>
    <li class="divider"></li>
    <li><g:link controller="userProfile" action="recordings"><g:message code="templates.profile.bookingsMenuButton.message3"/></g:link></li>
</ul>
