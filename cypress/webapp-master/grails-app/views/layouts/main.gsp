<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml" xmlns:fb="http://www.facebook.com/2008/fbml">
<head prefix="og: http://ogp.me/ns# fb: http://ogp.me/ns/fb# matchifb: http://ogp.me/ns/fb/matchifb#">
    <!-- For best instrumentation include New Relic before Google Tag Manager scripts as close as possible to start of head -->
    <g:render template="/templates/newRelicBrowser" />
    <g:render template="/templates/googleTagManager" />
    <g:render template="/templates/heap" />

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    %{--<meta name="apple-itunes-app" content="app-id=720782039">--}%
    <title><g:layoutTitle default="MATCHi" /></title>
    <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.png')}" type="image/x-icon" />

    <r:require modules="core, jquery-hover"/>

    <r:layoutResources/>
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
    <g:layoutHead />
</head>
<body>
<div id="wrapper">
    <g:headerLayout facility="${false}" user="${true}" home="${false}"/>

    <div id="container-wrapper">
        <div class="container">

            <!-- Messages -->
            <g:flashMessage />
            <g:flashError/>

            <g:layoutBody />
            <div class="clear"></div>
            <div class="space100">&nbsp;</div>
        </div>
    </div>
    <g:render template="/templates/topFooter"/>
    <g:render template="/templates/footer"/>
</div>
<r:layoutResources/>
</body>
</html>