<%@ page import="org.springframework.web.servlet.support.RequestContextUtils" %>
<div id="footer">
    <div class="container">
        <div class="row">
            <div class="col-md-3">
                <ul class="list-unstyled">
                    <li><h4 class="h2 weight100"><g:message code="templates.general.footer.message1"/></h4></li>
                    <li>
                        <a href="#" onclick='javascript:$("#_elev_io button").length > 0 ? $("#_elev_io button").click() : window.open("<g:message code="default.helpSection.url"/>","_self");'><i class="fas fa-question-circle fa-fw"></i> <g:message code="default.helpsection.label"/></a>
                    </li>
                    <li>
                        <a href="http://status.matchi.se" target="_blank">
                            <i class="fas fa-signal fa-fw"></i> <g:message code="default.status.page.label"/>
                        </a>
                    </li>
                </ul>
                <div class="space-10 visible-xs"></div>
            </div>
            <div class="col-md-3">
                <ul class="list-unstyled">
                    <li><h4 class="h2 weight100"><g:message code="templates.general.footer.message3"/></h4></li>
                    <li><g:link controller="home" action="about"><i class="fas fa-info-circle fa-fw"></i> <g:message code="default.navigation.about"/></g:link></li>
                    <li><g:link target="_new" controller="home" action="privacypolicy"><i class="fa fa-file-text fa-fw"></i> <g:message code="home.integritypolicy.title"/></g:link></li>
                    <li><g:link target="_new" controller="home" action="useragreement"><i class="fa fa-file-text fa-fw"></i> <g:message code="home.useragreement.title"/></g:link></li>
                    <li><g:link url="http://jobs.matchi.se"><i class="fas fa-suitcase fa-fw"></i> <g:message code="default.navigation.jobs"/></g:link></li>
                </ul>
            <div class="space-10 visible-xs"></div>
            </div>
            <div class="col-md-3">
                <h4 class="h2 weight100"><g:message code="templates.general.footer.message6"/></h4>
                <div class="vcard">
                    <div class="adr">
                        <div class="street-address"><g:message code="default.matchi.address.street"/></div>
                        <div class="postal-code inline"><g:message code="default.matchi.address.zip"/></div>
                        <div class="locality inline"><g:message code="default.matchi.address.city"/></div>,
                        <div class="country-name inline"><g:message code="default.matchi.address.country"/></div>
                    </div>
                </div>
                <div class="space-10 visible-xs"></div>
            </div>
            <div class="col-md-3">
                <h4 class="h2 weight100"><g:message code="templates.general.footer.message7"/></h4>
                <ul class="list-unstyled">
                    <li class="icon">
                        <a href="http://twitter.com/matchisports" target="_blank">
                            <i class="fab fa-twitter"></i>
                            @matchisports
                        </a>
                    </li>
                    <li class="icon">
                        <a href="https://www.facebook.com/matchisports/" target="_blank">
                            <i class="fab fa-facebook-square"></i>
                            facebook.com/matchisports
                        </a>
                    </li>
                    <li class="icon">
                        <a href="https://www.instagram.com/matchisports/" target="_blank">
                            <i class="fab fa-instagram"></i>
                            instagram.com/matchisports
                        </a>
                    </li>
                    <li class="icon">
                        <a href="http://blog.matchi.se" target="_blank">
                            <i class="fas fa-rss-square"></i>
                            <g:message code="default.navigation.blog"/>
                        </a>
                    </li>
                </ul>
                <div class="space-10 visible-xs"></div>
            </div>
        </div>
        <div class="space10">&nbsp;</div>
        <div class="row">
            <div class="col-md-12 text-muted text-center">
                <ul class="list-inline">
                    <li>&copy; <g:thisYear /> matchi.se</li>
                    <li class="hidden-xs">|</li>
                    <li>version <g:meta name="app.version"/></li>
                    <li class="visible-xs">
                        <a href="javascript: void(0)" class="viewport-toggle">
                            <g:message code="templates.general.footer.desktopVersion"/>
                        </a>
                    </li>
                    <sec:ifAnyGranted roles="ROLE_ADMIN">
                        <li class="hidden-xs">|</li>
                        <li><g:link controller="adminHome"><g:message code="adminHome.index.title"/></g:link></li>
                    </sec:ifAnyGranted>
                    <sec:ifAnyGranted roles="ROLE_ADMIN">
                        <li class="hidden-xs">|</li>
                        <li><g:switchFacility/></li>
                    </sec:ifAnyGranted>
                    <li>
                        <g:select name="lang" from="${grailsApplication.config.i18n.availableLanguages}"
                                optionKey="key" optionValue="key"
                                value="${RequestContextUtils.getLocale(request).language}"
                                onchange="location.href = '${request.forwardURI}?lang=' + this.value"/>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</div> <!--  #footer END -->

<script type="text/javascript">

    $(document).ready(function() {
        moment.locale('<g:locale/>');
        $.datepicker.setDefaults($.datepicker.regional["<g:locale i18n="true"/>"]);
    });

    $(function() {
        $(".viewport-toggle").on("click", function() {
            $("meta[name=viewport]").attr("content", "width=1200");
        });
    });
</script>