<label class="checkbox" for="startingGracePeriodDaysCheckbox">
    <g:checkBox name="startingGracePeriodDaysCheckbox" checked="${membership?.startingGracePeriodDays}"/>
    <span class="starting-grace-period-label">
        <g:message code="membershipCommand.startingGracePeriodDays.label"/>
    </span>
</label>

<div style="display: none" class="vertical-margin10 starting-grace-period">
    <g:field type="number" name="startingGracePeriodDays" class="span1"
            value="${membership?.startingGracePeriodDays}" min="1"/>
    <label for="startingGracePeriodDays" class="left-margin5" style="display: inline-block">
        <g:message code="membershipCommand.startingGracePeriodDays.unit"/>
    </label>
    <br/>
</div>

<script type="text/javascript">
    $(function() {
        $("#startingGracePeriodDaysCheckbox").on("change", function() {
            var inputWrapper = $(".starting-grace-period");
            if ($(this).is(":checked")) {
                inputWrapper.show().find("input").focus();
            } else {
                inputWrapper.find("input").val("");
                inputWrapper.hide();
            }
        }).trigger("change");

        $(".membership-paid-checkbox").on("change", function() {
            var checkbox = $("#startingGracePeriodDaysCheckbox");
            if ($(this).is(":checked")) {
                if (checkbox.is(":checked")) {
                    checkbox.click();
                }
                checkbox.prop("disabled", true);
                $(".starting-grace-period-label").addClass("muted");
            } else {
                checkbox.prop("disabled", false);
                $(".starting-grace-period-label").removeClass("muted");
            }
        }).trigger("change");
    });
</script>