<div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only"><g:message code="button.close.label"/></span></button>
            <h4 class="modal-title" id="messageModalLabel"><g:message code="message.index.message1" args="[user.fullName()]" encodeAs="HTML"/></h4>
        </div>
        <g:form name="messageForm" action="sendMessage" class="no-margin">
            <div class="modal-body">
                <g:hiddenField name="id" value="${user.id}" />
                <g:hiddenField name="returnUrl" value="${returnUrl}" />

                <div class="form-group">
                    <g:textArea name="message" class="form-control" rows="10" cols="30" placeholder="${message(code: 'message.index.message2')}" />
                </div>

            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-md btn-default" data-dismiss="modal"><g:message code="button.cancel.label"/></button>
                <button type="submit" class="btn btn-md btn-success"><g:message code="button.submit.label"/></button>
            </div>
        </g:form>
    </div>
</div>
