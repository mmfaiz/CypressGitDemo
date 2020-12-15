<div class="span1" id="bookingPaymentTotal">
    <h6><g:message code="templates.booking.facilityBookingPayments.message1"/></h6><h3 class="popover-hint"><g:formatMoneyShort value="${bookingPayments?.total?:0}" forceZero="true" /></h3>
</div>

<script type="text/javascript">
    $(document).ready(function() {
        $("#bookingPaymentTotal").popover({title: "${message(code: 'payment.label.plural')}", content: "" +
            "<table class=table style='font-size: 12px; color: #2c2c2c;'>" +
                <g:each in="${bookingPayments.rows}" var="row">
                    <g:if test="${bookingPayments.rows.size() > 1}">
                        "<tr><td colspan=4><strong>${g.forJavaScript(data: row.slot.court)} <g:formatDate date="${row.slot.startTime}" format="HH:mm" locale="sv"/>-<g:formatDate date="${row.slot.endTime}" format="HH:mm" locale="sv"/></strong></td></tr>" +
                    </g:if>
                    <g:each in="${row.payments}" var="payment">
                        "<tr><td colspan=3><g:formatDate date="${payment.dateCreated}" format="yyyy-MM-dd HH:mm" locale="sv"/></td><td>${formatMoney(value: payment.amount, forceZero: true)}</td></tr>" +
                    </g:each>
                </g:each>
                "<tr><td colspan=3><strong>${message(code: "default.amount.label")}</strong></td><td><strong>${g.formatMoney(value: bookingPayments.total)}</strong></td></tr>" +
            "</table>",
            trigger: "hover"
        });
    });
</script>
