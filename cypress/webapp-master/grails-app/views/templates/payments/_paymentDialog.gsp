<div class="modal fade" id="userBookingModal" tabindex="-1" role="dialog" aria-labelledby="userBookingModal" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header flex-center separate">
                <h4 class="modal-title"><g:message code="default.loader.label"/></h4>
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times-circle"></i></button>
            </div>
            <div class="modal-body relative">
                <div class="absolute-center">
                    <i class="fas fa-spinner fa-spin"></i> <g:message code="default.loader.text"/>
                </div>
            </div>
            <div class="clearfix"></div>
            <div class="modal-footer">
                <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-danger"><g:message code="button.cancel.label"/></a>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="modalLoading" tabindex="-1" role="dialog" aria-labelledby="modalLoading" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header flex-center separate">
                <h4 class="modal-title"><g:message code="default.loader.label"/></h4>
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times-circle"></i></button>
            </div>
            <div class="modal-body relative">
                <div class="absolute-center vertical-margin30">
                    <i class="fas fa-spinner fa-spin"></i> <g:message code="default.loader.text"/>
                </div>
            </div>
            <div class="clearfix"></div>
            <div class="modal-footer">
                <a href="javascript:void(0)" data-dismiss="modal" class="btn btn-md btn-danger"><g:message code="button.cancel.label"/></a>
            </div>
        </div>
    </div>
</div>

<r:script>
    function openPaymentDialog() {
        $("#userBookingModal").html($('#modalLoading').html());
        $("#userBookingModal").modal("toggle");
    }

    $(function() {
        $("#userBookingModal").modal({ show: false, backdrop: 'static', keyboard: false });
        $("#modalLoading").modal({ show: false, backdrop: 'static', keyboard: false });
    });
</r:script>