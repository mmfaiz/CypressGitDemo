<script type="text/javascript">
    $(document).ready(function() {

        moment.locale('<g:locale/>');

        $.datepicker.setDefaults($.datepicker.regional["<g:locale i18n="true"/>"]);

        // IE console
        if ( ! window.console ) console = { log: function(){} };

        // store url for current page as global variable
        var current_page = document.location.href;
        $("ul#nav li").removeClass('current');

        // apply selected states depending on current page
        var activeLink = $(".navbar").find("ul.nav").find("a[href]").filter(function() {
            var href = $(this).attr("href");
            if (href.length > 1) {
                if ((href.match(/\//g) || []).length > 2) {
                    href = href.substr(0, href.lastIndexOf("/"));
                }
                if(current_page.match("offers") && href.includes('customers')) {
                    return true;
                }
                if (current_page.indexOf(href) != -1) {
                    return true;
                }
            }
            return false;
        });

        if (activeLink.length) {
            $(".navbar").find("ul.nav").children("li").has(activeLink[0]).addClass('current');
        }
    });
</script>

<div id="header-wrapper">
    <div id="header">
        <sec:ifLoggedIn>
            <g:if test="${user && !facility}">
                <sec:ifAllGranted roles="ROLE_USER">
                    <g:render template="/templates/navigation/menuUser" />
                </sec:ifAllGranted>
            </g:if>
            <g:if test="${facility}">
                <g:ifFacilityAccessible>
                    <g:render template="/templates/navigation/menuFacility" />
                </g:ifFacilityAccessible>
            </g:if>
        </sec:ifLoggedIn>
        <sec:ifNotLoggedIn>
            <g:render template="/templates/navigation/menuDefault" />
        </sec:ifNotLoggedIn>
    </div> <!--  #header END -->
</div> <!--  #header-wrapper END -->