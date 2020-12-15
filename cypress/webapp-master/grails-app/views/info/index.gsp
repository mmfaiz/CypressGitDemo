<!doctype html>
<html>
<head>
    <meta name="layout" content="${!params.wl.equals("1") ? "b3noFooter":"whitelabel"}" />
    <title>MATCHi</title>
</head>
<body>

<div class="block top-margin50 vertical-padding40">
    <div class="container">

        <div class="row">
            <div class="col-md-6 col-md-offset-3 col-xs-12">
                <g:if test="${header}">
                    <div class="page-header">
                        <h1 class="h2">${header}</h1>
                    </div>
                </g:if>

                <p class="lead">${message}</p>
                <g:if test="${params.returnUrl}">
                    <g:link url="${params.returnUrl}" class="btn btn-success btn-large"><g:message code="info.index.message1"/></g:link>
                </g:if>
                <g:else>
                    <div class="top-margin50">
                        <g:link controller="userProfile" action="home" class="btn btn-success btn-large"><g:message code="info.index.message1"/></g:link>
                    </div>
                </g:else>

            </div><!-- /.col-md-6 -->
        </div><!-- /.row -->
    </div><!-- /.container -->

    <div class="space-60"></div>
</div>
</body>
</html>
