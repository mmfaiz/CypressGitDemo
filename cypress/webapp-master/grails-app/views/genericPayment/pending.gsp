<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="whitelabel" />
    <title><g:message code="default.loader.text"/></title>
</head>

<body>
    <div class="container">
        <div class="row">
            <div class="col-xs-12 col-sm-6 col-sm-offset-3" style="margin-top: 25%;">
                <p class="text-center" style="font-size: 2em;"><g:message code="default.loader.text"/>...</p>
                <p class="text-center" style="margin-top: 25px; margin-bottom: 25px;">
                    <i class="fa fa-spin fa-spinner" style="font-size: 10em;"></i>
                </p>
                <p class="text-center" style="font-size: 1.1em;"><g:message code="adyen.pending.waitingText" args="${[(timeout / 1000) as Integer]}"/></p>
            </div>
        </div>
    </div>
</body>

<g:render template="/templates/payments/pendingCheck" model="${[interval: interval, timeout: timeout, orderId: order.id]}" />

</html>