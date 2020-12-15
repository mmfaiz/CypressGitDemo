<script type="text/javascript">
    $(document).ready(function() {
        $(".modal.fade.in").css( "height", "" );
    });
</script>
<div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <h4><g:message code="netaxeptPayment.error.message1"/></h4>
        </div>
        <div class="modal-body">
            <h2><g:message code="netaxeptPayment.error.message2"/></h2>

            <g:if test="${message}">
                <p>${message}</p>
            </g:if>
        </div>

        <div class="modal-footer">
            <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-danger"><g:message code="button.close.label"/></a>
        </div>
    </div>
</div>