<div class="pricelist condition">
    <div class="control-group">
        <div style="width: 360px;display: inline-block">
            <label class="checkbox inline" for="activities_" style="margin-left: 10px; width: 130px">
                <g:checkBox name="courtsCheckAll" class="checkall-activities" id="activities_" checked="false" value=""/>
                <g:message code="templates.conditions.slot.slotconditionactivities.form.message1"/>
            </label>
            <label class="checkbox inline" for="activities_" style="margin-left: 10px; width: 130px">
                <g:checkBox name="noActivities" class="check-no-activities" id="activities_" checked="false" value="true"/>
                <g:message code="templates.conditions.slot.slotconditionactivities.form.message2"/>
            </label><br>
            <g:each in="${activities}" var="activity">
                <label class="checkbox inline court" for="activity_${activity.id}" style="margin-left: 10px; width: 130px">
                    <g:checkBox name="activities" id="activity_${activity.id}" checked="false" value="${ activity.id }"/>
                    ${activity.name}
                </label>
            </g:each>

        </div>
    </div>
</div>

<r:script>
    $(function () {
        $('.checkall-activities').click(function () {
            $("input[name=activities]").attr('checked', this.checked);
            if(this.checked) {
                $('.check-no-activities').attr('checked', false);
            }

        });

        $("input[name=activities]").click(function () {
            $('.check-no-activities').attr('checked', false);
        });

        $('.check-no-activities').click(function () {
            $('.checkall-activities').attr('checked', false);
            $("input[name=activities]").attr('checked', false);
        });
    });
</r:script>
