<%@page import="org.joda.time.DateTime" %>
<html>
<head>
    <meta name="layout" content="popupWithCore" />
</head>
<body>
<script type="text/javascript">
    $(document).ready(function() {
        parent.$("#userBookingModal").load("<g:createLink controller="bookingPayment" action="showError" params="${errParams}"/>");
    });
</script>
</body>
</html>

