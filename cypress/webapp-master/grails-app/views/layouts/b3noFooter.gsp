<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page pageEncoding="UTF-8"%>
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
    <g:set var="href" value="${request.forwardURI}"/>
    <link rel="alternate" href="${href}" hreflang="x-default" />
    <g:each in="${grailsApplication.config.i18n.availableLanguages}">
        <link rel="alternate" href="${href}?lang=${it.key}" hreflang="${it.key}" />
    </g:each>

    <r:require modules="b3core"/>

    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->

    <r:layoutResources/>
    <g:render template="/templates/facebookPixelCode" />
    <g:render template="/templates/facebookLoginAlt"/>
    <r:script>
        var _gaq = _gaq || [];
        _gaq.push(['_setAccount', 'UA-33104694-1']);
        _gaq.push(['_trackPageview']);

        (function() {
            var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
            ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
            var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
        })();

    </r:script>
    <r:script>
        $(document).ready(function(){
            if ($("#notification-global").length > 0 && !getCookie("hideGlobalNotification"))
            {
                $('#main-navigation').addClass("move-content");
            } else {
                $('#main-navigation').removeClass("move-content");
            }
            $( "#close-gn" ).click(function() {
                $('#main-navigation').removeClass("move-content");
            });
        });
    </r:script>
</head>
<body>
<div id="wrap" class="wrap-less">
    <g:b3Header />
    <div id="wrap-content">
        <div id="message-container">
            <!-- Messages -->
            <g:b3StaticFlashMessage />
            <g:b3StaticFlashError />
        </div>

        <g:layoutBody />
    </div>
</div>

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

<g:render template="/templates/elevio" />
<r:layoutResources/>
</body>
</html>
