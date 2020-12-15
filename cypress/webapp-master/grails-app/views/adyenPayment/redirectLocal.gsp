<%@ page import="org.joda.time.DateTime" %>
<html>
<head>
    <meta name="layout" content="popup" />
</head>
<body>
<div id="adyen-dialog" class="modal-dialog">
    <div class="modal-content">
        <form id="localForm" name="localForm" method="post" action="${model.url}" target="_parent">
            <input type="hidden" name="merchantSig" value="${model.merchantSig}" />
            <input type="hidden" name="sessionValidity" value="${model.sessionValidity}" />
            <input type="hidden" name="shopperLocale" value="${model.shopperLocale}" />
            <input type="hidden" name="merchantAccount" value="${model.merchantAccount}" />
            <input type="hidden" name="paymentAmount" value="${model.paymentAmount}" />
            <input type="hidden" name="currencyCode" value="${model.currencyCode}" />
            <input type="hidden" name="countryCode" value="${model.countryCode}" />
            <input type="hidden" name="skinCode" value="${model.skinCode}" />
            <input type="hidden" name="merchantReference" value="${model.merchantReference}" />
            <input type="hidden" name="brandCode" value="${model.brandCode}" />
            <input type="hidden" name="issuerId" value="${model.issuerId}" />
            <input type="hidden" name="resURL" value="${model.resURL}" />
            <input type="hidden" name="shipBeforeDate" value="${model.shipBeforeDate}" />
        </form>
    </div>
</div>
<r:script>
    $(document).ready(function() {
        $('#localForm').submit();
    });
</r:script>
</body>
</html>

