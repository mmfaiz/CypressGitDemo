<r:script>
    var lastCall, now;
    var checkFunction = function() {
        lastCall = +new Date();

        $.ajax({
            url: "${g.forJavaScript(data: createLink(controller: "adyenPayment", action: "checkPendingPayment"))}",
            method: "GET",
            dataType: "json",
            data: { orderId: ${g.forJavaScript(data: orderId)} },
            success: function(res) {
                if(!res.url) {
                    now = +new Date();

                    // Rerun check function...
                    // Wait to avoid more than 1 call per second
                    setTimeout(checkFunction, ${g.forJavaScript(data: interval)} - (now - lastCall));
                } else {
                    window.location.href = res.url;
                }
            }
        });
    };

    checkFunction();
</r:script>