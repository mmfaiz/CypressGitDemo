<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page pageEncoding="UTF-8"%>
<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<g:set var="locale" value="${RequestContextUtils.getLocale(request).language}"/>
<!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml" xmlns:fb="http://www.facebook.com/2008/fbml">
<head prefix="og: http://ogp.me/ns# fb: http://ogp.me/ns/fb# matchifb: http://ogp.me/ns/fb/matchifb#">
    <!-- For best instrumentation include New Relic before Google Tag Manager scripts as close as possible to start of head -->
    <g:render template="/templates/newRelicBrowser" />
    <g:render template="/templates/googleTagManager" />
    <g:render template="/templates/heap" />

    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    %{--<meta name="apple-itunes-app" content="app-id=720782039">--}%
    <title><g:layoutTitle default="MATCHi" /></title>
    <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.png')}" type="image/x-icon" />

    <r:require modules="b3core,jquery-ui-i18n,mousetrap"/>

    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->

    <r:layoutResources/>
    <asset:i18n locale="${locale}"/>
</head>
<body class="facility-admin">
<div id="wrap" class="wrap-less">
    <g:b3FacilityHeader />
    <div id="wrap-content">
        <g:render template="/templates/facility/boostrap3/scheduledTasks" model="[errorClass: 'alert-danger']"/>

        <div id="message-container">
            <!-- Messages -->
            <g:b3StaticFlashMessage />
            <g:b3StaticFlashError />
        </div>

        <g:layoutBody />
    </div>
</div>
<g:termsModal existingUserMode="true" bootstrap3="true" />
<g:render template="/templates/general/footer"/>
<r:layoutResources/>
<g:render template="/templates/intercom"/>
<g:render template="/templates/elevio" />
</body>
</html>
