<g:set var="activeSubPage" value="${params.controller}-${params.action}"/>

<nav class="navbar navbar-submenu navbar-fixed-top hidden-xs" role="navigation">
    <div class="container">
        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse" id="navbar-collapse">
            <ul class="nav navbar-nav navbar-left">
                <li class="${activeSubPage.contains('userProfile-index') ? "active":""}">
                    <g:link action="index"><g:message code="templates.navigation.myProfile"/></g:link>
                <li class="${activeSubPage.contains('userProfile-bookings') ? "active":""}">
                    <g:link action="bookings"><g:message code="default.booking.plural"/></g:link>
                </li>
                <!--<li class="${activeSubPage.contains('userProfile-activity') ? "active":""}">
                    <g:link action="activity">Aktivitet/Transaktioner</g:link>
                </li>-->
                <li class="${activeSubPage.contains('userProfile-account') ? "active":""}">
                    <g:link action="account"><g:message code="templates.navigation.menuUser.message7"/></g:link>
                </li>
            </ul>
        </div><!-- /.navbar-collapse -->
    </div>
</nav>