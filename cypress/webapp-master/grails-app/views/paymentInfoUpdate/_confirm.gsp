<div class="modal-content">
    <g:formRemote name="confirmForm" url="[action: 'pay']" update="userBookingModal">
        <g:hiddenField name="orderId" value="${order.id}"/>
        <g:hiddenField name="savePaymentInformation" value="${Boolean.TRUE}"/>
        <g:hiddenField name="ignoreAssert" value="${Boolean.TRUE}"/>
    </g:formRemote>
</div>

<script type="text/javascript">
    $(document).ready(function() {
        $.getScript("${g.forJavaScript(data: grailsApplication.config.adyen.library)}", function() {
            $('#confirmForm').submit();
        });
    });
</script>
