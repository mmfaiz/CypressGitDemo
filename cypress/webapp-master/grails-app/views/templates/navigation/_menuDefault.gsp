<div class="navbar">
    <div class="navbar-inner">
        <div class="container">
            <g:link class="brand" controller="home" action="index"><r:img uri="/images/logo_clean_negative.png"/></g:link>
            <ul class="nav pull-right">
                <li><g:link controller="home" action="index"><g:message code="default.home.label"/></g:link></li>
                <li><g:link controller="facilities" action="index"><g:message code="facility.label.plural"/></g:link></li>
                <li><g:link controller="home" action="about"><g:message code="default.navigation.about"/></g:link></li>
                <li><g:link controller="home" action="getmatchi"><g:message code="default.navigation.getmatchi"/></g:link></li>
                <li class="login"><g:link class="login_btn btn btn-primary" controller="login" action="auth"
                        params="[returnUrl: pageProperty(name: 'meta.loginReturnUrl') ?: request.forwardURI]"><g:message code="default.navigation.login"/></g:link></li>
            </ul>
        </div>
    </div>
</div>



