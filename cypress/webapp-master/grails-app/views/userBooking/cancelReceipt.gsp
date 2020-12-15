<%@ page import="org.joda.time.DateTime" %>
<html>
<head>
    <meta name="layout" content="popup" />
    <title><g:message code="userBooking.cancelReceipt.message1"/></title>
</head>
<body>

<div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            <h4 class="modal-title" id="cancelConfirmLabel"><g:message code="userBooking.cancelReceipt.message1"/></h4>
        </div>
        <div class="modal-body">
            <g:flashMessage/>
            <div class="row">
                <div class="col-md-12 col-xs-12">
                    <h3><g:message code="userBooking.cancelReceipt.message2"/></h3>
                    <p><g:message code="userBooking.cancelReceipt.message3"/></p>
                </div>
            </div>

        </div>
        <div class="modal-footer">
            <button id="cancelCloseBtn" type="button" class="btn btn-md btn-success" data-dismiss="modal" aria-hidden="true"><g:message code="button.closewindow.label" default="Stäng fönstret"/></button>
        </div>
    </div>
</div>
<script>
    $('#cancelCloseBtn').on('click', function() {
        location.reload();
    });
</script>
</body>
</html>
