<g:applyLayout name="paymentFinishModal" model="${[onclick: 'this.close']}">
    <h1><g:message code="payment.error.message1"/></h1>
    <br/>
        <g:message code="payment.showError.message3"/>
    <g:if test="${message}">
        <h6 style="color:#e74c3c;font-weight:bold;">${message}
    </g:if>
    <g:if test="${membershipType}">
        <p><g:message code="membership.exists.error" args="[membershipType?.name, startDate]"/></p>
    </g:if>

</g:applyLayout>