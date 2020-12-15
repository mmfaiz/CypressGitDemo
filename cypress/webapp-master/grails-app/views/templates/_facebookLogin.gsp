<r:script>
    function facebookLogin(ticket) {
        FB.login(function(response) {
            if (response.status == "connected") {
                // user successfully logged in
                window.location = "${g.forJavaScript(data: createLink(controller:'userRegistration', action:'fbConnect', params: [returnUrl: pageProperty(name: 'meta.facebookReturnUrl')]))}" +
                        "&token=" + response.authResponse.accessToken +
                        "&ticket=" + (ticket || "");
            } else {
                // user cancelled login, do nothing
            }
        }, { scope: '${grailsApplication.config.grails.plugins.springsocial.facebook.signupPermissions}' });
    }
</r:script>