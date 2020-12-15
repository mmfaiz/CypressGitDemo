<html>
<head>
    <meta name="layout" content="b3noFooter">
    <title><g:message code="userBooking.cancelByTicket.title"/></title>
</head>

<body>
<div class="block top-margin50 vertical-padding40">
    <div class="container">

        <div class="row">
            <div class="col-md-6 col-md-offset-3 col-xs-12 text-center">
                <div class="page-header">
                    <h1 class="h2"><g:message code="userBooking.cancelByTicket.title"/></h1>
                </div>

                <h3 class="vertical-padding10"><g:message code="userBooking.cancelByTicket.heading"/></h3>

                <div class="vertical-padding20">
                    <g:message code="userBooking.cancelByTicket.description" encodeAs="HTML"
                               args="[humanDateFormat(date: new org.joda.time.DateTime(date)), court.name]"/>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
