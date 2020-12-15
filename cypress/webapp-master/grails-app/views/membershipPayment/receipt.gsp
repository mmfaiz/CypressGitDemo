<g:applyLayout name="${showPage ? 'paymentFinishPage' : 'paymentFinishModal'}"
               model="${[onclick: "parent.location.href = '${finishUrl ?: createLink(controller: 'facility', action: 'show', params: [name: membership.customer.facility.shortname])}';"]}">
    <h1 class="h3"><g:message code="default.modal.thankYou"/></h1>
    <p><g:message code="membership.buy.receipt.message" args="[order?.user?.email]"/></p>
</g:applyLayout>
