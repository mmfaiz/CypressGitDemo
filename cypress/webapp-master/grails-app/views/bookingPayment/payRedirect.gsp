<%@ page import="org.joda.time.DateTime" %>
<html>
<head>
    <meta name="layout" content="popup" />
</head>
<body>
<div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <h4 class="modal-title">2. <g:message code="payment.pay.label"/></h4>
        </div>
        <div class="modal-body relative">
            <p>
                <g:message code="payment.pay.message2"/>
            </p>

            <div id="paymentFrameLoading" class="absolute-center vertical-margin30">
                <div class="absolute-center vertical-margin30">
                    <i class="fas fa-spinner fa-spin"></i> <g:message code="default.loader.text"/>
                </div>
            </div>
            <iframe id="paymentFrame" src="${url}" style="width: 570px; height: 480px;border: none"></iframe>
        </div>
    </div>
</div>
<r:script>
    $(document).ready(function() {
        $("#paymentFrame").load(function() {
            $("#paymentFrameLoading").hide();
        });
    });
</r:script>
</body>
</html>

