<%@ page import="com.matchi.Sport; com.matchi.Facility"%>
<%@ page import="com.matchi.Court"%>
<%@ page import="org.joda.time.LocalDate"%>
<html>
<head>
    <meta name="layout" content="${params.wl?'whitelabel':'b3main'}" />
    <title><g:message code="userRegistration.activation.message1"/></title>
    <meta name="loginReturnUrl" content="${createLink(controller: 'userProfile', action: 'home')}"/>
</head>
<body>
<g:if test="${success}">
    <div class="block block-grey">
        <div class="page-header text-center">
            <h1><g:message code="userRegistration.activation.message12"/> ${userInstance.firstname}</h1>
        </div>
    </div>


    <div class="block-white">
        <div class="block hero-block">
            <div class="container">
                <div class="row">
                    <div class="col-md-8 col-md-offset-2 text-center">
                        <p class="lead">
                            <g:message code="userRegistration.activation.message13"/>
                            <br>
                            <g:message code="userRegistration.activation.message14"/>
                        </p>
                        <g:if test="${params.returnUrl}">
                            <g:link url="${params.returnUrl}"><g:message code="userRegistration.activation.message3"/></g:link>
                        </g:if>
                        <g:else>
                            <g:link controller="home" action="index" class="btn btn-success btn-large"><g:message code="userRegistration.activation.message3"/></g:link>
                        </g:else>
                    </div>
                </div>
                <div class="space-60"></div>
            </div>
        </div>
    </div>
</g:if>
<g:else>
    <div class="block block-grey">
        <div class="page-header text-center">
            <h1><g:message code="userRegistration.activation.message6"/></h1>
        </div>
    </div>


    <div class="block-white">
        <div class="block hero-block">
            <div class="container">
                <div class="row">
                    <div class="col-md-8 col-md-offset-2 text-center">
                        <p class="lead">
                            <g:message code="userRegistration.activation.message15" args="[createLink(controller: 'login', action: 'auth', params: params)]"/>
                        </p>
                        <g:if test="${params.externalUrl}">
                            <g:link url="${params.externalUrl}"><g:message code="userRegistration.activation.message3"/></g:link>
                        </g:if>
                        <g:elseif test="${params.returnUrl}">
                            <g:link url="${params.returnUrl}"><g:message code="userRegistration.activation.message3"/></g:link>
                        </g:elseif>
                        <g:else>
                            <g:link controller="home" action="index" class="btn btn-success btn-large"><g:message code="userRegistration.activation.message3"/></g:link>
                        </g:else>
                    </div>
                </div>
                <div class="space-60"></div>
            </div>
        </div>
    </div>
</g:else>

</body>
</html>
