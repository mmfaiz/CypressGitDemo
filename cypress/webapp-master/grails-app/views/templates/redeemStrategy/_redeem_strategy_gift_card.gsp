<%@ page import="com.matchi.subscriptionredeem.redeemstrategy.InvoiceRowRedeemStrategy" %>

<g:if test="${!giftCards}">
    <g:message code="templates.redeemStrategy.redeemstrategygiftcards.emptyList"/>
</g:if>
<g:else>
    <ul class="inline no-bottom-margin">
        <li>
            <select name="giftCardId">
                <g:each in="${giftCards}">
                    <option value="${it.id}" ${currentStrategy?.type?.equals(strategy?.type) && currentStrategy.giftCardId == it.id ? "selected":""}>
                        ${it.name}
                    </option>
                </g:each>
            </select>
        </li>
        <li>
            <g:textField name="amount" class="span1" value="${currentStrategy?.amount ?: 80}"/>
        </li>
        <li>
            <g:select name="redeemAmountType" from="${InvoiceRowRedeemStrategy.RedeemAmountType.list()}"
                    valueMessagePrefix="giftCardRedeemStrategy.redeemAmountType" class="span1"
                    value="${currentStrategy?.redeemAmountType}"/>
        </li>
    </li>
</g:else>