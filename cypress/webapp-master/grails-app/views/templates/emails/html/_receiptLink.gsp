<g:if test="${order && order.origin != 'facility' && order.payments.every { it.method != com.matchi.payment.PaymentMethod.GIFT_CARD && it.method != com.matchi.payment.PaymentMethod.COUPON }}">
    <strong><a href="${createLink(controller: 'userProfile', action: 'printReceipt', params: [id: order.id], absolute: true)}" target="_blank"><g:message code="templates.emails.receiptLink" /></a></strong>
</g:if>