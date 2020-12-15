<%@ page import="com.matchi.orders.OrderPayment" %>
<g:set var="payments" value="${membership.order.payments.sort{it.dateCreated}}"/>
<g:set var="lastPayment" value="${payments.size() ? payments.last() : null}"/>

<div class="more-info">
    <g:if test="${(membership.paid && lastPayment || lastPayment?.type == "Invoice") || lastPayment?.status == OrderPayment.Status.CREDITED || lastPayment?.status == OrderPayment.Status.ANNULLED}">
        <div class="more-info-box">
            <g:set var="labelText" value="membership.paymentHistory.moreInfo"/>
            <g:if test="${lastPayment?.type == "Adyen"}">
                <g:set var="labelText" value="facilityBooking.list.payment.method.CREDIT_CARD_SIMPLIFIED"/>
                <p><g:message code="membership.paymentHistory.paidByCreditCardOn" args="${[lastPayment.dateCreated.dateString]}" /></p>
            </g:if>
            <g:if test="${lastPayment?.type == "Cash"}">
                <g:set var="labelText" value="payment.method.ADMIN"/>
                <p><g:message code="membership.paymentHistory.markedAsPaidOn" args="${[lastPayment.dateCreated.dateString]}" /></p>
                <p><g:message code="user.account.savedcard.issuer" />: ${membership.order?.getActivePayment()?.issuer?.fullName()?: membership.order.issuer.fullName()}</p>
            </g:if>
            <g:if test="${lastPayment?.type == "Invoice"}">
                <g:set var="labelText" value="facilityBooking.list.payment.method.INVOICE"/>

                <g:if test="${lastPayment?.invoiceRow?.invoice}">
                    <g:if test="${membership.paid}">
                        <p><g:message code="membership.paymentHistory.paidByInvoiceOn" args="${[lastPayment.invoiceRow.invoice.paidDate]}" /></p>
                    </g:if>
                    <g:else>
                        <p><g:message code="membership.paymentHistory.invoiceCreateNotPaid" args="${[lastPayment.invoiceRow.invoice.invoiceDate]}" /></p>
                    </g:else>
                    <span class='nolink'>
                        <p>
                            <a href='<g:createLink controller="facilityInvoice" action="edit" params="${[id: lastPayment.invoiceRow.invoice.id]}"/>'><g:message code="default.invoice.label" /></a>
                        </p>
                    </span>
                </g:if>
                <g:else>
                    <p><g:message code="membership.paymentHistory.invoiceRowNotAddedToInvoice" /></p>
                </g:else>
            </g:if>

            <g:if test="${lastPayment?.status == OrderPayment.Status.CREDITED || lastPayment?.status == OrderPayment.Status.ANNULLED}">
                <p>credited on ${lastPayment.lastUpdated.dateString}</p>
            </g:if>
        </div>
        <span class="label label-default"><g:message code="${labelText}" /></span>
    </g:if>
</div>