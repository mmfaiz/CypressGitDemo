<g:set var="activePage" value="${params.controller}-${params.action}"/>
<nav class="navbar navbar-default navbar-fixed-top admin" role="navigation">
    <div class="container">
        <!-- Brand and toggle get grouped for better mobile display -->
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#navbar-collapse">
                <span class="sr-only"><g:message code="default.navigation.toggle"/></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
        <g:link controller="adminHome" class="navbar-brand navbar-brand-facility">
                <g:message code="templates.navigation.menuAdmin.message2"/>
            </g:link>
        </div>

        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse" id="navbar-collapse">
            <ul class="nav navbar-nav navbar-right">
                <li>
                    <g:link controller="userProfile" action="home"><g:message code="templates.navigation.menuAdmin.message13"/></g:link>
                </li>
                <li>
                    <g:link controller="facilityBooking" action="index"><g:message code="templates.navigation.myClub"/></g:link>
                </li>
                <li class="${activePage.contains('adminUser') ? "active":""}">
                    <g:link controller="adminUser"><g:message code="user.label.plural"/></g:link>
                </li>
                <li class="${activePage.contains('adminFacility') ? "active":""}">
                    <g:link controller="adminFacility"><g:message code="facility.label.plural"/></g:link>
                </li>


                <li class="dropdown ${activePage.contains('adminOrder') ||
                        activePage.contains('adminRegion') ||
                        activePage.contains('adminStatistics') ||
                        activePage.contains('adminContactMe') ||
                        activePage.contains('adminJob') ||
                        activePage.contains('adminMail') ||
                        activePage.contains('adminSystem') ||
                        activePage.contains('adminGlobalNotification') ||
                        activePage.contains('adminFormTemplate') ? "active":""}">
                    <a href="#"
                       class="dropdown-toggle"
                       data-toggle="dropdown">
                        <g:message code="templates.navigation.menuAdmin.message12"/>
                        <b class="caret"></b>
                    </a>
                    <ul class="dropdown-menu">
                        <li><g:link controller="adminFormTemplate" action="index"><g:message code="formTemplate.label.plural"/></g:link></li>
                        <li><g:link controller="adminGlobalNotification" action="index"><g:message code="globalNotification.label.plural"/></g:link></li>
                        <li><g:link controller="adminFrontEndMessage" action="index"><g:message code="frontEndMessage.label.plural"/></g:link></li>
                        <li><g:link controller="adminJob"><g:message code="templates.navigation.menuAdmin.message8"/></g:link></li>
                        <li><g:link controller="adminMail"><g:message code="templates.navigation.menuAdmin.message14"/></g:link></li>
                        <li><g:link controller="adminSystem" action="mpc">MPC</g:link></li>
                        <li><g:link controller="adminOrder"><g:message code="order.label.plural"/></g:link></li>
                        <li><g:link controller="adminRegion"><g:message code="region.label.plural"/></g:link></li>
                        <li><g:link controller="adminContactMe"><g:message code="templates.navigation.menuAdmin.message7"/></g:link></li>
                        <li><g:link controller="adminStatistics"><g:message code="templates.navigation.menuAdmin.message6"/></g:link></li>
                        <li><g:link controller="adminSystem"><g:message code="templates.navigation.menuAdmin.message9"/></g:link></li>
                        <li><g:link controller="adminMatchiConfig"><g:message code="templates.navigation.menuAdmin.message15"/></g:link></li>
                    </ul>
                </li>

                <sec:ifLoggedIn>
                    <li class="dropdown">
                        <g:profileMenuButton />
                    </li>
                </sec:ifLoggedIn>
            </ul>
        </div><!-- /.navbar-collapse -->
    </div>
</nav>