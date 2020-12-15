<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page pageEncoding="UTF-8"%>
<%@ page import="org.springframework.web.servlet.support.RequestContextUtils" %>
<!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml" xmlns:fb="http://www.facebook.com/2008/fbml">
<head prefix="og: http://ogp.me/ns# fb: http://ogp.me/ns/fb# matchifb: http://ogp.me/ns/fb/matchifb#">
    <!-- For best instrumentation include New Relic before Google Tag Manager scripts as close as possible to start of head -->
    <g:render template="/templates/newRelicBrowser" />
    <g:render template="/templates/googleTagManager" model="[facility: facility]" />
    <g:render template="/templates/heap" />

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title><g:layoutTitle default="MATCHi" /></title>
    <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.png')}" type="image/x-icon" />

    <r:require modules="coreadmin,jquery-ui-i18n,mousetrap"/>

    <r:layoutResources/>
    <asset:i18n locale="${RequestContextUtils.getLocale(request).language}"/>
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
        <div id="data-loader" class="alert alert-info">
            <r:img uri="/images/spinner.gif" style="margin-right: 10px;"/><span id="move-info-label" class="text"><g:message code="default.loader.label"/></span>
        </div>

        <div id="container" class="container">

            <g:render template="/templates/facility/scheduledTasks"/>

            <!-- Messages -->
            <g:flashMessage />
            <g:flashError/>
            <g:errorMessage bean="bean" />

            <!-- end Messages -->

            <g:layoutBody />
            <div class="clear"></div>
            <div class="space100"></div>
        </div>
    </div>
    <div class="push"></div>
</div>
<g:termsModal existingUserMode="true" />
<g:render template="/templates/footer" model="[facility: true]"/>
<div id="helpModal" class="modal hide fade"></div>
<r:layoutResources/>
<g:render template="/templates/intercom"/>
<g:render template="/templates/elevio" />
</body>
</html>