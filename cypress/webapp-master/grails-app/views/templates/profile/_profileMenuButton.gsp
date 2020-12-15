<a href="#" class="dropdown-toggle" data-toggle="dropdown">
    <div class="navbar-user-image avatar-circle-xs"><g:fileArchiveUserImage size="small" id="${user.id}"/></div>
    <span>${user.firstname}
    <b class="caret"></b></span>
</a>
<ul class="dropdown-menu">
    <g:if test="${user.facility}">
        <g:ifFacilityAccessible>
            <li class="${activePage?.contains('login') ? "active":""}">
                <g:link controller="${defaultFacilityController()}">
                    <i class="fa fa-user-o fa-fw"></i> <g:message code="templates.navigation.myClub"/>
                </g:link>
            </li>
            <li class="divider"></li>
        </g:ifFacilityAccessible>
    </g:if>
    <li><g:link controller="userProfile" action="home"><i class="fa fa-dashboard fa-fw"></i> <g:message code="default.home.label"/></g:link></li>
    <li><g:link controller="userProfile" action="index"><i class="fas fa-user fa-fw"></i> <g:message code="templates.navigation.myProfile"/></g:link></li>

    <li><g:link controller="userProfile" action="payments"><i class="fas fa-list fa-fw"></i> <g:message code="templates.navigation.myActivity"/></g:link></li>
    <li><g:link controller="userProfile" action="remotePayments"><i class="fa fa-handshake-o fa-fw"></i> <g:message code="templates.navigation.remotePayments"/></g:link></li>
    <li><g:link controller="userProfile" action="account"><i class="fas fa-cog fa-fw"></i> <g:message code="templates.profile.profileMenuButton.message4"/></g:link></li>
    <li class="divider"></li>
    <li><g:link controller="home" base="${message(code: "default.helpSection.url")}"><i class="fas fa-question-circle fa-fw"></i> <g:message code="default.helpsection.label"/></g:link></li>
    <li class="divider"></li>
    <li><g:link controller="logout" action="index"><i class="fa fa-sign-out fa-fw"></i> <g:message code="default.navigation.logout"/></g:link></li>
</ul>
