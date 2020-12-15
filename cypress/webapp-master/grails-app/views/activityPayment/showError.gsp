<g:applyLayout name="${showPage ? "paymentFinishPage" : "paymentFinishModal"}"
               model="${[onclick: "this.close"]}">
    <h1><g:message code="payment.error.message1"/></h1>
    <br/>
    <p>
    <g:message code="payment.showError.message3"/>
    <g:if test="${message}">
        <h6 style="color:#e74c3c;font-weight:bold;">${message}</h6>
    </g:if>
    </p>
</g:applyLayout>