<%@ page import="org.joda.time.LocalTime" %>
<g:if test="${messages.size() > 0}">
        <g:each in="${messages}" var="message">
            <div class="front-end-message message-container" id="${message.baseId}" style="display: none;">
                <div class="fem-container">
                    <div class="content">${htmlOutputs[message.id].encodeAsRaw()}</div>
                </div>
                <button type="button" class="fem-close close" onclick="hideNotification('hideFrontEndMessage_${message.baseId}'); $('#${message.baseId}').hide();" data-dismiss="alert" aria-label="Close"><i class="fa fa-times-circle"></i></button>
                <style>
                    ${message.cssCode.encodeAsRaw()}
                </style>
                <r:script>
                $(document).ready(function() {
                    if(!getCookie("hideFrontEndMessage_${message.baseId}")) {
                         $("#${message.baseId}").show();
                    }
                });
                </r:script>
            </div>
        </g:each>
        <r:script>
            $(document).ready(function() {
                $('.fem-toggle-collapse').click(function() {
                    console.log($(this))
                    $(this).closest(".fem-container").toggleClass("open")
                });
            });
        </r:script>
</g:if>