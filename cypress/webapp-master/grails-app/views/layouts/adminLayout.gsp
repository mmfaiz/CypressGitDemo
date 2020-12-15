<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml">
<head>
    <!-- For best instrumentation include New Relic before Google Tag Manager scripts as close as possible to start of head -->
    <g:render template="/templates/newRelicBrowser" />
    <g:render template="/templates/googleTagManager" />
    <g:render template="/templates/heap" />

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title><g:layoutTitle default="MATCHi" /></title>
    <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.png')}" type="image/x-icon" />

    <r:require modules="coreadmin,select2"/>

    <r:layoutResources/>

    <r:script>
        $.datepicker.setDefaults($.datepicker.regional["<g:locale i18n="true"/>"]);
        $(document).ready(function() {
            // store url for current page as global variable
            var current_page = document.location.href;
            $("ul#nav li").removeClass('current');

            if (current_page.match("/admin/users/")) {
                $(".navbar .nav > li:eq(1)").addClass('current');
            } else if (current_page.match("/admin/facility/") || current_page.match("/admin/customer/"))  {
                $(".navbar .nav > li:eq(2)").addClass('current');
            } else {
                $(".navbar .nav > li:eq(0)").addClass('current');
            }
        });

        var _gaq = _gaq || [];
        _gaq.push(['_setAccount', 'UA-33104694-1']);
        _gaq.push(['_trackPageview']);

        (function() {
            var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
            ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
            var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
        })();
    </r:script>
</head>
<body>
<g:render template="/templates/navigation/menuAdmin" />
<div id="wrapper">
    <div id="container-wrapper">
        <div id="container" class="container">

            <!-- Messages -->
            <g:flashMessage />
            <g:flashError/>

            <g:errorMessage bean="bean" />
            <!-- end Messages -->

            <g:layoutBody />
            <div class="clear"></div>
            <div class="space100">&nbsp;</div>
        </div>
    </div>
    <div class="push"></div>
</div>
<g:render template="/templates/footer"/>
<r:layoutResources/>
</body>
</html>