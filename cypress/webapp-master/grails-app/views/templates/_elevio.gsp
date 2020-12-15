<%@ page import="com.matchi.intercom.IntercomSecureMode" %>
<script>
    !function(e,l,v,i,o,n){e[i]||(e[i]={}),e[i].account_id=n;var g,h;g=l.createElement(v),g.type="text/javascript",g.async=1,g.src=o+n,h=l.getElementsByTagName(v)[0],h.parentNode.insertBefore(g,h);e[i].q=[];e[i].on=function(z,y){e[i].q.push([z,y])}}(window,document,"script","_elev","https://cdn.elev.io/sdk/bootloader/v4/elevio-bootloader.js?cid=","5b39e736246df");
    /*
     * For passing user information, please see
     * https://api-docs.elevio.help/en/articles/24
     */

    var elevioGroups = [];

    <sec:ifNotLoggedIn>
        elevioGroups.push('nonAdmin');
        window._elev.on('load', function(_elev) {
            _elev.setUser({
                groups: elevioGroups
            });
        });
    </sec:ifNotLoggedIn>

    <sec:ifLoggedIn>
        <g:set var="currentUser" value="${applicationContext.springSecurityService.getCurrentUser()}"/>
        <g:set var="currentFacility" value="${controllerName}"/>

        <g:if test="${controllerName && controllerName.startsWith("facility") && !controllerName.equals("facility")}">
            elevioGroups.push('facilityAdmin');
        </g:if>
        <g:else>
            elevioGroups.push('nonAdmin');
        </g:else>

        window._elev.on('load', function(_elev) {
            _elev.setUser({
                first_name: "${g.forJavaScript(data: currentUser.firstname)}",
                last_name: "${g.forJavaScript(data: currentUser.lastname)}",
                email: "${g.forJavaScript(data: currentUser.email)}",
                user_hash: "${g.forJavaScript(data: IntercomSecureMode.generateUserHash(grailsApplication.config.matchi.elevio.clientSecret, currentUser.email))}",
                groups: elevioGroups
            });

            _elev.setLanguage("${g.forJavaScript(data: currentUser.language)}");
        });
    </sec:ifLoggedIn>
</script>