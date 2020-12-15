<r:script>
    $(function() {
        Intercom("trackEvent", "purchased-${g.forJavaScript(data: order.article.name())}",
                {
                    description: "${g.forJavaScript(data: order.description.encodeAsJavaScript())}",
                    price: {
                        currency: "${g.forJavaScript(data: g.currentFacilityCurrency(facility: order?.facility))}",
                        amount: ${g.forJavaScript(data: order.price * 100)}
                    }
                }
        );
    });
</r:script>
