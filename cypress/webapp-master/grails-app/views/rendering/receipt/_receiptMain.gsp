<%@ page import="com.matchi.HtmlUtil" contentType="text/html;charset=UTF-8" %>
<tr>
    <td colspan="2">
        <div class="main">
            <table border="0" width="100%">
                <thead>
                <tr>
                    <th width="60%"><g:message code="invoiceRow.description.label"/></th>
                    <th align="center"><g:message code="default.article.label"/></th>
                    <th align="center"><g:message code="default.quantity.label"/></th>
                    <th align="right"><g:message code="default.price.label"/></th>
                </tr>
                <tr>
                    <th colspan="4"><hr size="1"/></th>
                </tr>
                </thead>

                <g:each in="${order.payments}" var="payment">
                    <tr>
                        <td>
                            ${HtmlUtil.escapeAmpersands(order.description)}
                            <br/>
                            <small><g:message code="adminStatistics.index.paymentMethod"/>:
                                <g:if test="${payment.method}">
                                    <strong>${message(code:"payment.method.${payment.method}")}</strong>

                                </g:if>
                                <g:else>
                                    <strong>${message(code:"payment.type.${payment.type}")}</strong>
                                </g:else>
                            </small>
                            <g:if test="${activity}">
                                <small><g:message code="default.activity.label"/>: <strong>${activity}</strong></small>
                            </g:if>
                        </td>
                        <td align="center">${order.article}</td>
                        <td align="center">${1}</td>
                        <td align="right"><g:formatMoneyShort value="${payment.total()}"/> ${currency}</td>
                    </tr>
                </g:each>

                <tfoot>
                <tr>
                    <th colspan="4"><hr size="1"/></th>
                </tr>
                <tr>
                    <th colspan="4">&nbsp;</th>
                </tr>
                <tr>

                    <th align="right"><g:message code="rendering.invoice.invoice.message10"/></th>
                    <th colspan="3" align="right"><g:formatMoneyShort value="${order.getTotalAmountPaid()}"/> ${currency} (<g:message code="default.varav.label"/> ${order.payments.sum { it.vat() }} ${currency} <g:message code="default.vat.label"/>)</th>
                </tr>
                <tr>
                    <th colspan="4">&nbsp;</th>
                </tr>

                </tfoot>

            </table>
        </div>

    </td>
</tr>