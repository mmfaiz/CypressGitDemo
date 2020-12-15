<%@ page import="org.springframework.web.servlet.support.RequestContextUtils" %>
<g:set var="activePage" value="${params.controller}-${params.action}"/>
<nav id="main-navigation" class="navbar navbar-default navbar-fixed-top dark-bg" role="navigation">
    <div class="container">
        <!-- Brand and toggle get grouped for better mobile display -->
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#navbar-collapse">
                <span class="sr-only"><g:message code="default.navigation.toggle"/></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <g:link class="navbar-brand" controller="home" action="index">MATCHi</g:link>
        </div>

        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse" id="navbar-collapse">
            <ul class="nav navbar-nav navbar-left">
                <li class="${activePage.contains('book-index') ? "active":""}">
                    <g:link controller="book" action="index">
                        <span><g:message code="button.book.label"/></span>
                    </g:link>
                </li>
                <li class="${activePage.contains('activity') ? "active":""}">
                    <g:link controller="activities" action="index"><span><g:message code="default.activity.plural"/></span></g:link>
                </li>
                <li class="${activePage.contains('facility') ? "active":""}">
                    <g:link controller="facilities" action="index"><span><g:message code="facility.label.plural"/></span></g:link>
                </li>
                <li class="${activePage == 'home-getmatchi' ? "active":""}">
                    <g:link controller="home" action="getmatchi"><span><g:message code="default.navigation.getmatchi"/></span></g:link>
                </li>
            </ul>
            <ul class="nav navbar-nav navbar-right">
                <li>
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <span>${RequestContextUtils?.getLocale(request)?.language?.toUpperCase()}
                        <b class="caret"></b></span>
                    </a>
                    <ul class="dropdown-menu">
                        <g:each in="${grailsApplication.config.i18n.availableLanguages}">
                            <li><a href="javascript:void(0)" onclick="location.href = '${request.forwardURI}?lang=${it.key}'"><span>${it.value?.toUpperCase()}</span></a></li>
                        </g:each>
                    </ul>
                </li>
                <li class="login ${activePage.contains('login') ? "active":""}">
                    <g:link controller="login" action="auth" params="[returnUrl: pageProperty(name: 'meta.loginReturnUrl') ?: request.forwardURI]"><span><g:message code="default.navigation.login"/></span></g:link>
                </li>
                <li class="login ${activePage.contains('userRegistration') ? "active":""}">
                    <g:link controller="userRegistration" action="index" params="[returnUrl: pageProperty(name: 'meta.loginReturnUrl') ?: request.forwardURI]"><span><g:message code="default.navigation.register"/></span></g:link>
                </li>
            </ul>
        </div><!-- /.navbar-collapse -->
    </div>
</nav>
