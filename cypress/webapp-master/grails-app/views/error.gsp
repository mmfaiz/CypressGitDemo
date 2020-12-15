<g:if env="production">
    <!doctype html>
    <html>
    <head>
        <title><g:message code="error.title"/></title>
        <meta name="layout" content="b3noFooter">
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
                        <h1 class="page-header top-margin20"><g:message code="error.heading"/></h1>
                        <p class="lead no-margin">
                            <g:message code="error.subheading"/>
                        </p>
                        <p class="bottom-margin20">
                            <g:message code="error.description"/>
                        </p>
                        <a href="javascript:history.go(-1)" class="btn btn-success btn-lg vertical-margin20"><g:message code="button.back.label"/></a>
                    </div>

                </div>

            </div>
        </div>
    </div>

    </body>
    </html>
</g:if>
<g:else>
    <!doctype html>
    <html>
    <head>
        <title><g:message code="error.title"/></title>
        <meta name="layout" content="main">
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'errors.css')}" type="text/css">
    </head>
    <body>

    <g:renderException exception="${exception}" />

    </body>
    </html>
</g:else>
