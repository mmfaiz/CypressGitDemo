<%@ page import="com.matchi.UserRegistrationController;" contentType="text/html;charset=UTF-8" %>
<%@ page pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml" xmlns:fb="http://www.facebook.com/2008/fbml">
<head prefix="og: http://ogp.me/ns# fb: http://ogp.me/ns/fb# matchifb: http://ogp.me/ns/fb/matchifb#">
    <!-- For best instrumentation include New Relic before Google Tag Manager scripts as close as possible to start of head -->
    <g:render template="/templates/newRelicBrowser" />
    <g:render template="/templates/googleTagManager" />
    <g:render template="/templates/heap" />

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title><g:layoutTitle default="MATCHi" /></title>
    <g:set var="href" value="${request.forwardURI}?wl=1"/>
    <g:each in="${grailsApplication.config.i18n.availableLanguages}">
        <link rel="alternate" href="${href}&lang=${it.key}" hreflang="${it.key}" />
    </g:each>
    <r:require modules="b3core"/>

    <r:layoutResources/>
    <g:render template="/templates/facebookLoginAlt"/>
    <script type="text/javascript">
        var _gaq = _gaq || [];
        _gaq.push(['_setAccount', 'UA-33104694-1']);
        _gaq.push(['_trackPageview']);

        (function() {
            var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
            ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
            var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
        })();

    </script>
    <g:render template="/templates/facebookPixelCode" />
    <style type="text/css">

    .content-container {
        border: 0px;
        margin-top: 20px;
    }

        .content-container .header {
            background-color: white;
            border-bottom: white;
        }

        .content-container .block.hero-block {
            background: white;
        }

        .content-container .header {
            display: none;
        }

        .content-container h2 {
            color: #696969;
            font-family: Arial;
        }

        h3 {
            color: #696969;
            font-family: Arial;
        }

        .modal-header {
            background-color: #3294d5;
            background-image: none;
        }

    .modal-header h3 {
        color: #fff;
    }

    </style>
    <g:layoutHead />
</head>
<body>
<div class="whitelabel-wrapper">
<div id="wrapper">
    <div class="container">
        <!-- Messages -->
        <g:flashMessage />
        <g:flashError/>

        <g:layoutBody />
        <div class="clear"></div>
    </div>
</div>
<g:render template="/templates/payments/paymentDialog"/>
<g:if test="${controllerName == 'userRegistration' && actionName == 'index'}">
    <g:termsModal skipCheck="true" overrideBehaviour="true" bootstrap3="true" />
</g:if>
<g:else>
    <g:termsModal bootstrap3="true" existingUserMode="true"/>
</g:else>

<div id="fb-root"></div>
<script>
    window.fbAsyncInit = function() {
        FB.init({
            appId      : ${g.forJavaScript(data: grailsApplication.config.grails.plugins.springsocial.facebook.clientId)},
            status     : true,
            cookie     : true,
            xfbml      : true,
            oauth      : true
        });
    };
    (function(d){
        var js, id = 'facebook-jssdk'; if (d.getElementById(id)) {return;}
        js = d.createElement('script'); js.id = id; js.async = true;
        js.src = "//connect.facebook.net/en_US/all.js";
        d.getElementsByTagName('head')[0].appendChild(js);
    }(document));
</script>
<r:layoutResources/>
</div>
</body>
</html>