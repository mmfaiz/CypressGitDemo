<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml" xmlns:fb="http://www.facebook.com/2008/fbml">
<head prefix="og: http://ogp.me/ns# fb: http://ogp.me/ns/fb# matchifb: http://ogp.me/ns/fb/matchifb#">
    <!-- For best instrumentation include New Relic before Google Tag Manager scripts as close as possible to start of head -->
    <g:render template="/templates/newRelicBrowser" />
    <g:render template="/templates/googleTagManager" />
    <g:render template="/templates/heap" />

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title><g:layoutTitle default="MATCHi" /></title>
    <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.png')}" type="image/x-icon" />

    <r:require modules="bootstrap-responsive, core"/>

    <r:layoutResources/>
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
    <g:render template="/templates/facebookPixelCode" />
</head>
<body class="backgroundImg">
<div id="wrapper">
    <g:headerLayoutResponsive user="${true}" home="${true}"/>

    <g:layoutBody />

    <g:render template="/templates/footerResponsive"/>
</div>
<r:layoutResources/>
</body>
</html>