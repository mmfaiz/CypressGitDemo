<%@ page import="org.joda.time.LocalTime" %>
<g:if test="${messages.size() > 0}">
    <div id="notification-global" class="alert global-notification alert-dismissible message-container" role="alert" style="display: none;">
        <g:each in="${messages}">
            <strong class="text-success text-md right-margin10"><i class="fas fa-flag"></i></strong> <div style="display: inline-block"><g:toRichHTML text="${it.notificationText.toString()}" /></div>
        </g:each>
        <button id="close-gn" type="button" class="close" onclick="hideNotification('hideGlobalNotification')" data-dismiss="alert" aria-label="Close"><i class="fa fa-times-circle"></i></button>
    </div>

    <r:script>
        $(document).ready(function() {
            if(!getCookie("hideGlobalNotification")) {
                 $("#notification-global").show();
            }
        });
    </r:script>
</g:if>
