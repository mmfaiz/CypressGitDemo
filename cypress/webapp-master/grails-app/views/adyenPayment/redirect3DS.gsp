<%@ page import="org.joda.time.DateTime" %>
<html>
<head>
    <meta name="layout" content="popup" />
</head>
<body>
<div id="adyen-dialog" class="modal-dialog">
    <div class="modal-content">
        <form id="3dsform" name="3dsform" method="POST" action="${issuerUrl}">
            <input type="hidden" name="PaReq" value="${PaReq}" />
            <input type="hidden" name="MD" value="${MD}" />
            <input type="hidden" name="TermUrl" value="${termUrl}" />
        </form>
    </div>
</div>
<r:script>
    $(document).ready(function() {
        $('#3dsform').submit();
    });
</r:script>
</body>
</html>

