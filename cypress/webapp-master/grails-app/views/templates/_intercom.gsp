<%@ page import="com.matchi.intercom.IntercomSecureMode" %>
<sec:ifLoggedIn>
    <g:set var="currentUser" value="${applicationContext.springSecurityService.getCurrentUser()}"/>
    <g:set var="currentFacility" value="${applicationContext.userService.userFacility}"/>
    <script>
        window.intercomSettings = {
            name: "${g.forJavaScript(data: currentUser.fullName())}",
            email: "${g.forJavaScript(data: currentUser.email)}",
            created_at: ${g.forJavaScript(data: currentUser.dateCreated?.getTime()?.intdiv(1000))},
            app_id: "${g.forJavaScript(data: grailsApplication.config.matchi.intercom.appId)}",
            user_hash: "${g.forJavaScript(data: IntercomSecureMode.generateUserHash(grailsApplication.config.matchi.intercom.appSecretKey, currentUser.email))}",
            tag: "${g.forJavaScript(data: hasFacilityFullRights(ignoreAdminRole: true) ? 'facilityadmin' : '')}"
            <g:if test="${currentFacility}">
                ,company: {
                    id: '${g.forJavaScript(data: currentFacility.id)}',
                    name: '${g.forJavaScript(data: currentFacility.name)}'
                }
            </g:if>
        };
    </script>
    <script>(function(){var w=window;var ic=w.Intercom;if(typeof ic==="function"){ic('reattach_activator');ic('update',intercomSettings);}else{var d=document;var i=function(){i.c(arguments)};i.q=[];i.c=function(args){i.q.push(args)};w.Intercom=i;function l(){var s=d.createElement('script');s.type='text/javascript';s.async=true;s.src='https://widget.intercom.io/widget/${g.forJavaScript(data: grailsApplication.config.matchi.intercom.appId)}';var x=d.getElementsByTagName('script')[0];x.parentNode.insertBefore(s,x);}if(w.attachEvent){w.attachEvent('onload',l);}else{w.addEventListener('load',l,false);}}})()</script>
</sec:ifLoggedIn>