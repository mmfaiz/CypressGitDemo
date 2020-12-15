<html>
<head>
    <meta name="layout" content="popup" />
</head>
<body>
<div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <h4 class="modal-title"><g:message code="payment.error.messageHeader"/></h4>
        </div>
        <div class="modal-body">
            <p>
                ${errorMessage}
            </p>
        </div>
        <div class="modal-footer">
            <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-danger"><g:message code="button.close.label"/></a>
        </div>
    </div>
</div>
</body>
</html>
