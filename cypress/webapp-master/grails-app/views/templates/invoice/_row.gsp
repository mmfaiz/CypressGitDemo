<%@ page import="com.matchi.invoice.InvoiceRow.DiscountType;" contentType="text/html;charset=UTF-8" defaultCodec="html"%>
<g:each in="${invoice.rows}" var="row">
    <tr>
        <td>${row.description}</td>
        <td align="center">${row.amount}</td>
        <td align="right"><g:formatMoneyShort value="${row.getPrice()}"/></td>
        <g:if test="${discount}">
            <td align="right"><g:formatMoneyShort value="${row.getDiscount()}"/>${row.discountType == DiscountType.PERCENT ? "%" : ""}</td>
        </g:if>
        <td align="right"><g:formatMoneyShort value="${row.getTotalIncludingVAT()}"/></td>
    </tr>
</g:each>