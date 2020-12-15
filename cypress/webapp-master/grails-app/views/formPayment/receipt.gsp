<g:applyLayout name="${showPage ? 'paymentFinishPage' : 'paymentFinishModal'}"
               model="${[onclick: onclick="parent.location.href = '${returnUrl}';"]}">
    <div class="row">
        <div class="col-md-12 col-xs-12">
            <h2><g:message code="default.modal.thankYou"/></h2>
            <p><g:message code="formPayment.receipt.message" args="[order ? order.user?.email : adminEmail]"/></p>
        </div>
    </div>
</g:applyLayout>

