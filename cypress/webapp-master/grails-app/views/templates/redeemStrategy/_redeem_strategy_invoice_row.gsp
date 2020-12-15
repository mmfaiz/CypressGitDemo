<%@ page import="com.matchi.subscriptionredeem.redeemstrategy.InvoiceRowRedeemStrategy" %>
<g:if test="${facility.hasApplicationInvoice()}">
    <g:render template="/templates/facility/listOrganizations"
              model="${[facility: facility, name: 'organizationId', organizations: organizations, currentOrganizationId: currentStrategy?.organizationId]}"/>
    <ul class="inline no-bottom-margin">
        <li>
            <select name="externalArticleId">
                <option value=""><g:message code="default.article.multiselect.noneSelectedText"/></option>
                <g:each in="${articles}">
                    <option value="${it.id}" ${currentStrategy?.externalArticleId == it.id ? "selected":""}>${it.descr}</option>
                </g:each>
            </select>
        </li>
        <li>
            <g:textField name="description" class="span3" value="${currentStrategy?.description}" placeholder="${message(code: 'default.description.label')}"/>
        </li>
        <li>
            <g:select class="span1"
                      name="vat" from="${[0: message(code: 'default.vat.none'), 6:'6%', 12:'12%', 25:'25%'].entrySet()}"
                      value="${currentStrategy?.vat}" optionValue="value" optionKey="key"/>
        </li>
        <li>
            <g:textField name="amount" class="span1" value="${currentStrategy?.amount ?: 80 }"/>
        </li>
        <li>
            <select name="redeemAmountType" class="span1">
                <g:each in="${InvoiceRowRedeemStrategy.RedeemAmountType.list()}">
                    <option value="${it}" ${currentStrategy?.redeemAmountType?.equals(it) ? "selected":""}><g:message code="redeem.amount.type.${it}" args="[facility.currency]"/></option>
                </g:each>
            </select>
        </li>
    </ul>
    <p class="help-block">
        <g:message code="templates.redeemStrategy.redeemstrategyinvoicerow.message3" args="[facility.currency]"/>
    </p>
    <div class="space5"></div>
</g:if>