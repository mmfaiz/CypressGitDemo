<div id="header_wrapper">
    <div id="header">
        <div class="navbar">
            <div class="navbar-inner">
                <div class="container">
                    <g:link controller="home" action="index" class="brand">
                        <r:img uri="/images/logo_clean_negative.png"/>
                    </g:link>
                    <div class="visible-phone">
                        <ul class="nav pull-right">
                            <li><g:link controller="home" action="index"><g:message code="default.home.label"/></g:link></li>
                            <li><g:link controller="home" action="about"><g:message code="default.navigation.about"/></g:link></li>
                        </ul>
                    </div>
                    <div class="hidden-phone">
                        <ul class="nav pull-right">
                            <li><g:link controller="home" action="index"><g:message code="default.home.label"/></g:link></li>
                            <li><g:link controller="facilities" action="index"><g:message code="facility.label.plural"/></g:link></li>
                            <li><g:link controller="home" action="about"><g:message code="default.navigation.about"/></g:link></li>
                            <sec:ifLoggedIn>
                                <li>
                                    <g:link controller="userProfile" action="home"><g:message code="templates.navigation.myProfile"/></g:link>
                                </li>
                            </sec:ifLoggedIn>
                            <sec:ifNotLoggedIn>
                                <li class="login"><g:link class="login_btn btn btn-primary" controller="login" action="auth"><g:message code="default.navigation.login"/></g:link></li>
                            </sec:ifNotLoggedIn>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div> <!--  #header END -->
</div> <!--  #header_wrapper END -->