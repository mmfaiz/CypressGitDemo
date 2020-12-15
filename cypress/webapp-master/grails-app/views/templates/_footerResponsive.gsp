<div id="footer-wrapper">
    <div id="footer" style="width: auto;">
        <!-- Desktop -->
        <div class="container-fluid visible-desktop">
            <div class="row-fluid" style="max-width: 960px; margin: 0 auto;">
                <div class="span3">
                    <h4><g:message code="templates.footer.question"/></h4>
                    <p><a href="mailto:info@matchi.se">info@matchi.se</a>

                        <br>Eller läs i vår
                        <g:link controller="home" base="${message(code: "default.helpSection.url")}">
                            <g:message code="default.faq.label"/>
                        </g:link></p>
                    <p><a href="http://status.matchi.se" target="_blank">
                        <g:message code="default.status.page.label"/>
                    </a></p>
                </div>
                <div class="span3">
                    <h4><g:message code="templates.footerResponsive.message4"/></h4>
                    <p><g:message code="default.matchi.address.street"/>, <g:message code="default.matchi.address.zip"/> <g:message code="default.matchi.address.city"/></p>
                </div>
                <div class="span3">
                    <h4><g:message code="templates.footer.followUs"/></h4>
                    <ul id="social">
                        <li><a class="twitter" href="http://twitter.com/matchisports" target="_blank">Twitter</a></li>
                        <li><a class="facebook" href="https://www.facebook.com/matchisports/" target="_blank">Facebook</a></li>
                    </ul>
                </div>
                <div class="span3">
                    <h4><g:message code="templates.footerResponsive.message9"/></h4>
                    <p><g:message code="templates.footer.message11"/></p>
                </div>
            </div>
        </div>
        <div class="container-fluid visible-desktop">
            <div class="row-fluid">
                <div class="span12 center-text">
                    &copy; <g:thisYear /> | version <g:meta name="app.version"/>
                    <sec:ifAnyGranted roles="ROLE_ADMIN">
                        | <g:link controller="adminHome"><g:message code="adminHome.index.title"/></g:link>
                    </sec:ifAnyGranted> | <g:link target="_new" controller="home" action="privacypolicy"><g:message code="home.integritypolicy.title"/></g:link> | <g:link target="_new" controller="home" action="useragreement"><g:message code="home.useragreement.title"/></g:link>
                </div>
            </div>
        </div><!-- /Desktop -->

        <!-- Tablet & phone -->
        <div class="container-fluid hidden-desktop">
            <div class="row-fluid">
                <div class="span6 center-text">
                    <h4><g:message code="templates.footerResponsive.message13"/></h4>
                    <p><a href="mailto:info@matchi.se">info@matchi.se</a>
                </div>
                <div class="span6 center-text">
                    <h4><g:message code="templates.footerResponsive.message9"/></h4>
                    <p><g:message code="templates.footer.message11"/></p>
                </div>
            </div>
        </div>
        <div class="container-fluid hidden-desktop">
            <div class="row-fluid">
                <div class="span12 center-text">
                    &copy; <g:thisYear /> matchi.se
                </div>
            </div>
        </div><!-- /Tablet & phone -->
    </div> <!--  #footer END -->
</div> <!--  #footer-wrapper END -->

