<%@ page import="org.springframework.web.servlet.support.RequestContextUtils;" %>
<div id="footer-wrapper">
    <div id="footer">
        <div class="container">
            <div class="row">
                <div class="span3">
                    <div class="padding10">
                        <h4><g:message code="templates.general.footer.message1"/></h4>
                        <ul class="unstyled" style="font-size: 14px">
                            <li>
                                <a href="#" onclick='javascript:$("#_elev_io button").click()'><g:message code="default.helpsection.label"/></a>
                            </li>
                            <li>
                                <a href="http://status.matchi.se" target="_blank">
                                    <g:message code="default.status.page.label"/>
                                </a>
                            </li>
                        </ul>
                    </div>
                </div>
                <div class="span3">
                    <div class="padding10">
                        <h4><g:message code="templates.general.footer.message3"/></h4>
                        <ul class="unstyled" style="font-size: 14px">
                            <li><g:link controller="home" action="about"><i class="fas fa-info-circle fa-fw"></i> <g:message code="default.navigation.about"/></g:link></li>
                            <li><g:link target="_new" controller="home" action="privacypolicy"><i class="fa fa-file-text fa-fw"></i> <g:message code="home.integritypolicy.title"/></g:link></li>
                            <li><g:link target="_new" controller="home" action="useragreement"><i class="fa fa-file-text fa-fw"></i> <g:message code="home.useragreement.title"/></g:link></li>
                        </ul>
                    </div>
                </div>

                <div class="span3">
                    <div class="padding10">
                        <h4><g:message code="templates.general.footer.message6"/></h4>
                        <p><g:message code="default.matchi.address.street"/>, <g:message code="default.matchi.address.zip"/> <g:message code="default.matchi.address.city"/></p>
                    </div>
                </div>
                <div class="span3">
                    <div class="padding10">
                        <h4><g:message code="templates.general.footer.message7"/></h4>
                        <ul id="social" class="unstyled">
                            <li><a href="http://twitter.com/matchisports" target="_blank"><i class="fab fa-twitter fa-2x fa-fw" aria-hidden="true"></i></a></li>
                            <li><a href="https://www.facebook.com/matchisports/" target="_blank"><i class="fab fa-facebook fa-2x fa-fw" aria-hidden="true"></i></a></li>
                            <li><a href="https://www.instagram.com/matchisports/" target="_blank"><i class="fab fa-instagram fa-2x fa-fw" aria-hidden="true"></i></a></li>
                            <li><a href="http://blog.matchi.se" target="_blank"><i class="fas fa-rss-square fa-2x fa-fw" aria-hidden="true"></i></a></li>
                        </ul>
                    </div>
                </div>
            </div>
            <div class="space10">&nbsp;</div>
            <div class="row">
                <div class="span12 center-text">
                &copy; <g:thisYear /> matchi.se | version <g:meta name="app.version"/>
                <sec:ifAnyGranted roles="ROLE_ADMIN">
                    | <g:link controller="adminHome"><g:message code="adminHome.index.title"/></g:link>
                </sec:ifAnyGranted>
                <sec:ifAnyGranted roles="ROLE_ADMIN">
                    | <g:switchFacility/>
                </sec:ifAnyGranted>
                <g:ifFacilityAccessible>
                    <g:switchUserFacility/>
                </g:ifFacilityAccessible>
                    | <g:select name="lang" from="${grailsApplication.config.i18n.availableLanguages}"
                                optionKey="key" optionValue="value"
                                style="height: auto"
                                value="${g.locale()}"
                                onchange="location.href = '${createLink(controller: defaultFacilityController())}?lang=' + this.value"/>
                </div>
            </div>
        </div>
    </div> <!--  #footer END -->
</div> <!--  #footer_wrapper END -->
