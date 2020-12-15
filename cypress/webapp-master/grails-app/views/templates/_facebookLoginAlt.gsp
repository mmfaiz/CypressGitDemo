<script type="text/javascript">
    function facebookLogin(ticket) {
        FB.login(function(response) {

            if (response.status == "connected") {
                $.ajax({
                    url: "${g.forJavaScript(data: createLink(controller:'userRegistration', action:'checkFbUser'))}?token="  + response.authResponse.accessToken,
                    statusCode: {
                        // Existing user
                        200: function () {
                            window.location="${g.forJavaScript(data: createLink(controller:'userRegistration', action:'fbConnect'))}" + "?token=" + response.authResponse.accessToken+"&ticket="+ticket;
                        },
                        // New user
                        404: function () {
                            $('#acceptConsentModalSubmit').click(function(){
                                var acceptTerms = $('#acceptConsentModalCheckBoxTerms').is(":checked");
                                var receiveNewsletters = $('#acceptConsentModalCheckBoxReceiveNewsletter').is(":checked");
                                var receiveCustomerSurveys = $('#acceptConsentModalCheckBoxReceiveCustomerSurveys').is(":checked");

                                window.location="${g.forJavaScript(data: createLink(controller:'userRegistration', action:'fbConnect'))}" + "?token=" + response.authResponse.accessToken+"&ticket="+ticket + "&receiveNewsletters="+receiveNewsletters + "&receiveCustomerSurveys="+receiveCustomerSurveys + "&terms="+acceptTerms;
                            });
                            $('#acceptConsentModal').modal('show');
                        }
                    }

                });

            } else {
                // user cancelled login, do nothing
            }
        }, { scope: '${controllerName == "userRegistration" ? grailsApplication.config.grails.plugins.springsocial.facebook.signupPermissions : grailsApplication.config.grails.plugins.springsocial.facebook.signinPermissions}' });
    }
</script>