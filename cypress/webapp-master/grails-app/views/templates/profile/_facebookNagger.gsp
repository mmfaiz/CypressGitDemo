<g:if test="${user?.facebookUID == null}">
    <div id="fbConnect" class="profile-notification message-container" style="display: none;">
            <div>
                <p>
                    <g:message code="default.useFacebook.message"/>
                    <button class="btn btn-facebook btn-sm" onclick="facebookLogin()"><i class="fab fa-facebook"></i> | <g:message code="auth.connect.with.facebook"/></button>
                </p>
            </div>
            <a class="close" href="javascript:void(0)" onclick="removeFacebookNagger();$('.profile-notification').hide();$('body').removeClass('user-profile-popup');"><i class="fa fa-times-circle"></i></a>
        <div id="fb-root"></div>
        <script>
          window.fbAsyncInit = function() {
            FB.init({
              appId      : ${g.forJavaScript(data: grailsApplication.config.grails.plugins.springsocial.facebook.clientId)},
              status     : true,
              cookie     : true,
              xfbml      : true,
              oauth      : true
            });
          };
          (function(d){
            var js, id = 'facebook-jssdk'; if (d.getElementById(id)) {return;}
            js = d.createElement('script'); js.id = id; js.async = true;
            js.src = "//connect.facebook.net/en_US/all.js";
            d.getElementsByTagName('head')[0].appendChild(js);
          }(document));

          $( document ).ready(function() {
            if (!getCookie("hideFacebookNagger")) {
              $("#fbConnect").show();
              $('body').addClass("user-profile-popup");
            }
          });
        </script>
    </div>
</g:if>