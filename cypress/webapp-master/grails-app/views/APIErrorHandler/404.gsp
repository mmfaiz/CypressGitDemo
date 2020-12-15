<html>
<head>
    <meta name="layout" content="b3noFooter"/>
    <title><g:message code="APIErrorHandler.404.title"/></title>
</head>
<body>
<div class="overlay">
    <div class="content">
        <div class="container">

            <div class="row">

                <div class="col-sm-3">
                    <div class="icon-xxlg">
                        <span class="fa-stack fa-lg">
                            <i class="fas fa-circle fa-stack-2x"></i>
                            <i class="fas fa-bug fa-stack-1x fa-inverse"></i>
                        </span>
                    </div>
                </div>

                <div class="col-sm-9">
                    <h1 class="page-header top-margin20">404</h1>
                    <p class="lead no-margin">
                        <g:message code="APIErrorHandler.404.description"/>
                    </p>
                    <p class="bottom-margin20">
                        <g:message code="APIErrorHandler.404.contact"/>
                    </p>
                    <g:link controller="home" action="index" class="btn btn-success btn-lg vertical-margin20"><g:message code="APIErrorHandler.404.back"/></g:link>
                </div>

            </div>

        </div>
    </div>
</div>
</body>
</html>
