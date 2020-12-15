<%@ page import="org.joda.time.LocalTime" %>
<g:if test="${messages.size() > 0}">
    <div id="notification-global" class="alert global-notification" role="alert" style="display: none;">
        <button id="close-gn" type="button" class="close" onclick="hideNotification('hideGlobalNotification')"
                data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <g:each in="${messages}">
            <strong class="text-success text-md right-margin10"><i class="fas fa-flag"></i></strong>
            <div style="display: inline-block"><g:toRichHTML text="${it.notificationText.toString()}" /></div>
        </g:each>
    </div>

    <r:script>
        $(document).ready(function() {
            if(!getCookie("hideGlobalNotification")) {
                 $("#notification-global").show();
            }
        });
    </r:script>
</g:if>
