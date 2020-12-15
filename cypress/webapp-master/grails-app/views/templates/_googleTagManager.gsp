<%
    Facility facility = facility
    BigDecimal totalPrice = orders?.sum{Order order -> order.price}
%>
<%@ page import="com.matchi.Facility; com.matchi.orders.Order; com.matchi.googletagmanager.GoogleTagManagerService" %>

<g:each in="${GoogleTagManagerService.getGoogleTagManagerContainers(facility)}" var="googleTagManagerContainerId">

    <!-- Google Tag Manager -->
    <script>(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':
            new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],
        j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
        'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
    })(window,document,'script','dataLayer','<%=googleTagManagerContainerId%>');</script>
    <!-- End Google Tag Manager -->

</g:each>

<!-- Google Tag Manager trigger custom event -->
<!-- Needed to support tracking in modal dialogs that are loaded through AJAX only with body content. -->
<!-- Events are named after request url for example "/bookingPayment/confirm", "activityPayment/confirm" etc. -->
<script>
    dataLayer.push({"event": "<%= request.forwardURI %>", "facility": "<%= facility?.shortname %>", "price": "<%= totalPrice %>"});
</script>