
<div class="navbar navbar-fixed-top admin">
    <div class="navbar-inner">
        <div class="container">
            <g:link controller="adminHome" class="brand">
                <g:message code="templates.navigation.menuAdmin.message2"/>
            </g:link>
            <div class="nav-collapse collapse right">
                <ul class="nav">
                    <li>
                        <g:link controller="userProfile" action="home">
                            <g:message code="default.home.label"/>
                        </g:link>
                    </li>
                    <li>
                        <g:link controller="facilityBooking" action="index">
                            <g:message code="templates.navigation.myClub"/>
                        </g:link>
                    </li>
                    <li><g:link controller="adminUser"><g:message code="user.label.plural"/></g:link></li>
                    <li><g:link controller="adminFacility"><g:message code="facility.label.plural"/></g:link></li>

                    <li class="dropdown">
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
            </div>
        </div>
    </div>
</div>