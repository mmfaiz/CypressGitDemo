<%@ page import="com.matchi.payment.PaymentMethod" %>
<div class="modal-content">
    <script type="text/javascript" src="${grailsApplication.config.adyen.library}"></script>
    <g:form name="confirmForm" class="no-margin">
        <g:hiddenField name="orderId" value="${order.id}"/>
        <g:hiddenField name="id" value="${form.id}"/>

        <div class="modal-header flex-center separate">
                <h4 class="modal-title"><g:message code="formPayment.buy.confirm"/></h4>
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times-circle"></i></button>
            </div>

        <div class="modal-body">

            <div class="row">
                <div class="col-sm-12" style="text-align: left">
                    <g:flashError/>
                    <g:errorMessage bean="${command}"/>

                    <h4 class="weight400">
                        ${form.facility.name}
                    </h4>
                </div>
            </div>

            <div class="well vertical-margin20">
                <table class="table no-bottom-margin">
                    <thead>
                        <th><i class="fa fa-group"></i> <g:message code="formPayment.confirm.form"/></th>
                        <th><i class="fa fa-money"></i> <g:message code="default.price.label"/></th>
                    </thead>
                    <tbody>
                    <tr>
                        <td>${form.name.encodeAsHTML()}</td>
                        <td><g:formatMoney value="${order.price}" facility="${order.facility}"/></td>
                    </tr>
                    </tbody>
                </table>
            </div>

            <g:render template="/templates/payments/paymentMethod" model="${paymentMethodsModel}"/>
        </div>

        <div class="modal-footer">
            <g:render template="/templates/payments/submitBtnAndHandler"
                    model="[targetController: 'formPayment', facility: form.facility,
                            btnText: message(code: 'button.confirm.label')]"/>
            <button class="btn btn-md btn-danger" data-dismiss="modal" aria-hidden="true">
                <g:message code="button.cancel.label"/>
            </button>
        </div>
    </g:form>
</div>

<script type="text/javascript">
    $(document).ready(function() {
        $("[rel='tooltip']").tooltip();

        $("#btnSubmit").focus();
    });

    $(".modal-body").css( "max-height", "2024px" );

    var onLoading = function() {
        $("#btnSubmit").attr("disabled", "disabled");
    };
</script>