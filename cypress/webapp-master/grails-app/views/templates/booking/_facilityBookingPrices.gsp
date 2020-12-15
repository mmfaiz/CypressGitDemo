<div class="span1" id="bookingPriceTotal">
    <g:hiddenField name="bookingPrice" value="${bookingPrices.total}"/>
    <h6><g:message code="templates.booking.facilityBookingPrices.message1"/></h6><h3 class="popover-hint">${formatMoneyShort(value: bookingPrices.total, forceZero: true)}</h3>
</div>

<script type="text/javascript">
    $(document).ready(function() {
    $("#bookingPriceTotal").popover({title: "${message(code: 'default.amount.label')}", content: "" +
        "<table class=table style='font-size: 12px; color: #2c2c2c;'>" +
        <g:each in="${bookingPrices.rows}" var="row">
        <g:set var="price" value="${row.key}"/>
        <g:set var="isSubscription" value="${row.value?.any{ it.isSubscription }}"/>
        <g:if test="${isSubscription && !price.price}">
        "<tr><td colspan=4>${message(code:'booking.price.withoutPriceList')}</td><tr>" +
        </g:if>
        <g:else>
        "<tr><td>${g.forJavaScript(data: price.name() == 'Unknown' ? '' : price.name())}</td><td>${g.forJavaScript(data: row.value?.size())}st </td><td width=1>${message(code: 'templates.booking.facilityBookingPrices.message3')}</td><td width=100>${formatMoney(value: price.price, forceZero: true)}</td></tr>" +
        </g:else>
        </g:each>
        "<tr><td colspan=3><b>${message(code: "default.amount.label")}</b></td><td><b>${formatMoney(value: bookingPrices.total, forceZero: true)}</b></td></tr>" +
        "</table>",
            trigger: "hover"
        });
    });
</script>