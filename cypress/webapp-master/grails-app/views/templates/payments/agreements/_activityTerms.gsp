<div class="row bottom-margin20">
    <div class="col-sm-12 col-xs-12">
        <div class="checkbox checkbox-success" id="acceptTermsWrap">
            <input type="checkbox" id="acceptTerms" name="acceptTerms" value="true"/>
            <label for="acceptTerms">
                <span class="weight400"><g:message code="payment.confirm.activityTerms.label" args="[occasion.activity.name]"/></span>
            </label>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function() {
        $("#acceptTerms").on("change", function() {
            $("#btnSubmit").prop("disabled", !$(this).is(":checked"));
        }).trigger("change");

        $("#activity-terms-link").popover({
            container: "#acceptTermsWrap",
            content: "${forJavaScript(data: occasion.activity.terms)}",
            title: "${message(code: 'payment.confirm.activityTerms.title')}",
            html: true,
            placement: "top",
            template: '<div class="popover modal-popover" role="tooltip"><div class="arrow"></div><h3 class="popover-title"></h3><div class="popover-content"></div></div>'
        });
    });
</script>