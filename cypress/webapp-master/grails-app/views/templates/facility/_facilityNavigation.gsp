<div class="facility-navigation">
<!-- FACILITY NAVIGATION -->
    <g:if test="${facility.isBookable()}">
        <a href="${createLink(action: 'show',params:[name: facility.shortname])}#bookingNavigation" class="smooth-anchor btn btn-sm btn-primary"><i
                class="fa fa-calendar"></i> <g:message code="default.booking"/></a>
    </g:if>
    <g:if test="${couponsAvailableForPurchase && !params.wl.equals("1")}">
        <a href="${createLink(action: 'show',params:[name: facility.shortname])}#coupons" class="smooth-anchor btn btn-sm btn-primary"><i
                class="fa fa-ticket"></i> <g:message code="offers.label.short"/></a>
    </g:if>
    <g:if test="${!activities.isEmpty()}">
        <a href="${createLink(action: 'show',params:[name: facility.shortname])}#activities" class="smooth-anchor btn btn-sm btn-primary"><i
                class="fa fa-calendar-o"></i> <g:message code="default.activity.plural"/></a>
    </g:if>
    <g:if test="${courses}">
        <a href="${createLink(action: 'show',params:[name: facility.shortname])}#courses" class="smooth-anchor btn btn-sm btn-primary"><i
                class="fa fa-group"></i> <g:message code="course.label.plural"/></a>
    </g:if>
    <g:if test="${events}">
        <a href="${createLink(action: 'show',params:[name: facility.shortname])}#events" class="smooth-anchor btn btn-sm btn-primary"><i
                class="fa fa-group"></i> <g:message code="eventActivity.label.plural"/></a>
    </g:if>
    <g:if test="${trainers}">
        <a href="${createLink(action: 'show',params:[name: facility.shortname])}#trainers" class="smooth-anchor btn btn-sm btn-primary"><i
                class="fas fa-user"></i> <g:message code="default.trainer.plural"/></a>
    </g:if>
    <g:if test="${facility.hasLeagueFromExcel()}">
        <g:link action="leagues" params="[name: facility.shortname]" class="btn btn-sm btn-primary"><i
                class="fas fa-list"></i> <g:message code="default.league.plural"/></g:link>
    </g:if>
<!-- /.facility navigation -->
</div>
