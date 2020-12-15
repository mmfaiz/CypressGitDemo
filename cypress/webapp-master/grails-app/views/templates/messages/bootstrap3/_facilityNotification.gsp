<g:if test="${messages.size() > 0}">
    <div class="alert alert-warning alert-dismissible notification-facility ${cssClass}" role="alert" style="display: none;">
        <button id="close-gn" type="button" class="close" onclick="hideNotification('hideFacilityNotification_${facility.id}')" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <g:each in="${messages}">
            <div style="display: inline-block">
                <strong class="text-success text-md right-margin10"><i class="fas fa-flag"></i></strong>
                ${g.toRichHTML(text: it.content)}
            </div>
        </g:each>
    </div>

    <r:script>
        $(document).ready(function() {
            if(!getCookie("hideFacilityNotification_${g.forJavaScript(data: facility.id)}")) {
                 $(".notification-facility").show();
            }
        });
    </r:script>
</g:if>
