<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml">
<head>
    <!-- For best instrumentation include New Relic before Google Tag Manager scripts as close as possible to start of head -->
    <g:render template="/templates/newRelicBrowser" />
    <g:render template="/templates/googleTagManager" model="[facility: facility]" />
    <g:render template="/templates/heap" />

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title><g:layoutTitle default="MATCHi" /></title>
    <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.png')}" type="image/x-icon" />
    <r:require modules="coreadmin,mousetrap,jquery-ui-i18n"/>
    <r:layoutResources/>
    <asset:i18n locale="${g.locale()}"/>
    <script type="text/javascript">
        var _gaq = _gaq || [];
        _gaq.push(['_setAccount', 'UA-33104694-1']);
        _gaq.push(['_trackPageview']);

        (function() {
            var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
            ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
            var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
        })();

        $(document).ready(function() {
            $('#helpModal').modal({ show:false, dynamic: true });
        });
    </script>
</head>
<body>
<g:headerLayout facility="${true}" home="${false}"/>
<div id="wrapper">
    <div id="container-wrapper">
        <g:layoutBody />
    </div>
    <div class="push"></div>
</div>
<g:termsModal existingUserMode="true" />
<g:render template="/templates/footer" model="[facility: true]"/>
<div id="helpModal" class="modal hide fade"></div>
<r:layoutResources/>
<g:render template="/templates/intercom"/>
<g:render template="/templates/elevio" />
<script src="/js/select2-3.5.2/select2_locale_${g.locale()}.js"></script>
</body>
</html>
