<%@ page import="org.joda.time.DateTime" %>
<g:applyLayout name="${showPage ? 'paymentFinishPage' : 'paymentFinishModal'}"
               model="${[onclick: "parent.location.href = removeParam('comeback', parent.location.href);"]}">
    <h1 class="h3"><g:message code="default.modal.thankYou"/></h1>
    <p><g:message code="remotePayment.receipt.description"/></p>

    <div class="row">
        <div class="col-sm-12">
            <p> <g:message code="remotePayment.receipt.message1"/> <a href="${createLink(controller: 'userProfile', action: 'payments')}" target="_blank"><g:message code="templates.navigation.myActivity" /></a> <g:message code="remotePayment.receipt.message2"/> <a href="${createLink(controller: 'userProfile', action: 'printReceipt', params: [id: order.id])}" target="_blank"><g:message code="remotePayment.receipt.message3"/></a>.</p>
        </div>
        <div class="col-sm-4">
            <h6><g:message code="default.price.label"/></h6>
            <p class="ellipsis">
                <g:formatMoney value="${order.total()}" facility="${order.facility}" />
            </p>
        </div>
    </div>
</g:applyLayout>