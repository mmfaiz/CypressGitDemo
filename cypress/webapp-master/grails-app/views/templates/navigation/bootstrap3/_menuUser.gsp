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
            <g:link class="navbar-brand" controller="userProfile" action="home">MATCHi</g:link>
        </div>

        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse" id="navbar-collapse">
            <ul class="nav navbar-nav navbar-left">
                <li class="user-menu ${activePage.contains('userProfile') && !activePage.contains('userProfile-bookings') ? "active":""}">
                    <g:profileMenuButton />
                </li>
                <li class="${activePage.contains('book-index') ? "active":""}">
                    <g:link controller="book" action="index">
                        <span><g:message code="button.book.label"/></span>
                    </g:link>
                </li>
                <li class="${activePage.contains('activity') ? "active":""}">
                    <g:link controller="activities" action="index">
                        <span><g:message code="default.activity.plural"/></span>
                    </g:link>
                </li>
                <li class="${activePage.contains('facility') ? "active":""}">
                    <g:link controller="facilities" action="index">
                        <span><g:message code="facility.label.plural"/></span>
                    </g:link>
                </li>
                <li class="${activePage.contains('matching') ? "active":""}">
                    <g:link controller="matching" action="index">
                        <span><g:message code="templates.navigation.menuUser.message3"/></span>
                    </g:link>
                </li>
            </ul>
            <ul class="nav navbar-nav navbar-right">
                <g:ifBookableTrainer>
                    <li class="${activePage.contains('userTrainer') ? "active":""}">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <span><g:message code="default.trainer.label"/>
                            <b class="caret"></b></span>
                        </a>
                        <ul class="dropdown-menu">
                            <li>
                                <g:link controller="userTrainer" action="index">
                                    <span><g:message code="default.availability.label"/></span>
                                </g:link>
                            </li>
                            <li>
                                <g:link controller="userTrainer" action="requests">
                                    <span><g:message code="default.request.plural.label"/></span>
                                </g:link>
                            </li>
                        </ul>
                    </li>
                </g:ifBookableTrainer>
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
                <li>
                    <g:inboxMenuButton />
                </li>
                <li>
                    <g:bookingsMenuButton />
                </li>
                <li class="user-menu">
                    <g:profileMenuButton />
                </li>
            </ul>
        </div><!-- /.navbar-collapse -->
    </div>
</nav>

<g:if test="${!pageProperty(name: 'meta.paymentDialogIncluded')}">
    <g:render template="/templates/payments/paymentDialog"/>
</g:if>
