<%@ page import="org.joda.time.DateTime" %>
<html>
<head>
    <meta name="layout" content="popupWithCore" />
</head>
<body>
<script type="text/javascript">
        $(document).ready(function() {
            parent.$("#userBookingModal").load("${g.forJavaScript(data: redirectUrl)}");
        });
    </script>
${params.slotId}
</body>
</html>

