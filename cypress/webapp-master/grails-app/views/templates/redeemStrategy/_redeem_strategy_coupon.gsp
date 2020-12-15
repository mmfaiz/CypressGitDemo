<g:if test="${coupons?.size() < 1}">
    <g:message code="templates.redeemStrategy.redeemstrategycoupon.message1"/>
</g:if>
<g:else>
    <select name="couponId">
        <g:each in="${coupons}">
            <option value="${it.id}" ${currentStrategy?.type?.equals(strategy?.type) && currentStrategy.couponId == it.id ? "selected":""}>
                ${it.name} (${it.nrOfTickets} <g:message code="templates.redeemStrategy.redeemstrategycoupon.message2"/>)
            </option>
        </g:each>
    </select>
</g:else>