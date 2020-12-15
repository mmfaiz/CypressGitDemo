<html>
<head>
    <meta name="layout" content="${params.wl?'whitelabel':'b3noFooter'}" />
    <title><g:message code="userRegistration.registrationComplete.message1"/></title>
</head>
<body>

<div class="block">
    <div class="block vertical-padding40">
        <div class="container">

            <div class="row">

                <div class="col-md-6 col-md-offset-3">

                    <div class="page-header">
                        <h1 class="h2 text-success"><i class="fas fa-check"></i> <g:message code="userRegistration.registrationComplete.message2"/></h1>
                    </div>

                    <p class="lead">
                      <g:message code="userRegistration.registrationComplete.message7"/><br>
                    </p>

                    <g:if test="${!params.wl}">
                      <g:if test="${params.returnUrl}">
                        <g:link url="${params.returnUrl}" class="btn btn-success btn-large"><g:message code="userRegistration.registrationComplete.message6"/></g:link>
                      </g:if>

                      <g:else>
                        <g:link controller="home" action="index" class="btn btn-success btn-large"><g:message code="userRegistration.registrationComplete.message6"/></g:link>
                      </g:else>
                    </g:if>

                </div><!-- /.col-md-6 col-md-offset-3 text-center -->

            </div><!-- /.row -->

        </div><!-- /.container -->
    </div><!-- /.block vertical-padding40 -->
</div><!-- /.block top-margin50 -->

</body>
</html>
