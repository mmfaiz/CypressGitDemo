<g:applyLayout name="${showPage ? 'paymentFinishPage' : 'paymentFinishModal'}"
               model="${[onclick: "parent.location.href = '${createLink(controller: 'userProfile', action: 'pastBookings', params: [name: recordingPurchase.customer.facility.shortname])}';"]}">

    <!-- NOTE! This is modal dialog that is loaded with Ajax and only body will be included so scripts in head will not be included, only body content. -->
    <g:render template="/templates/googleTagManager" model="[facility: facility, orders: [order]]" />

    <h1 class="h3"><g:message code="default.modal.thankYou"/></h1>
    <p><g:message code="recording.buy.receipt.message" args="[order?.user?.email]"/></p>
</g:applyLayout>
