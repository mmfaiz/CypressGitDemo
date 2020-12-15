<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="popup" />
</head>
<body>

<div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            <h4 id="modal-title"><g:message code="activityPayment.cancel.confirm.cancelled"/></h4>
        </div>

        <div class="modal-body">
            <g:flashMessage/>
            <div class="row">
                <div class="col-sm-12 col-xs-12">
                    <h1 class="h3"><g:message code="activityPayment.cancel.confirm.unsubscribed"/></h1>
                    <p><g:message code="activityPayment.cancel.confirm.unsubscribed.desc" args="[occasion.activity.name]"/></p>
                </div>
            </div>
        </div>

        <div class="modal-footer">
            <button class="btn btn-md btn-success" data-dismiss="modal" onclick="parent.location.reload();" aria-hidden="true"><g:message code="button.close.label"/></button>
        </div>
    </div>
</div>

</body>
</html>
