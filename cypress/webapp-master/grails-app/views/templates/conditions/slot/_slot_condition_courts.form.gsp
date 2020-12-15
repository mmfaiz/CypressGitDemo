<div class="pricelist condition">
    <div class="control-group">
        <div style="margin-left: 10px;">
            <label class="checkbox block" for="courts_">
                <g:checkBox name="courtsCheckAll" class="checkall-courts" id="courts_" checked="false" value=""/>
                <g:message code="templates.conditions.slot.slotconditioncourts.form.message1"/>
            </label><br>
            <g:each in="${courts}" var="court">
                <label class="checkbox block court" for="courts_${court.id}">
                    <g:checkBox name="courts" id="courts_${court.id}" checked="false" value="${ court.id }"/>
                    ${court.name} (${court.facility.name})
                </label>
            </g:each>

        </div>
    </div>
</div>

<r:script>
    $(function () {
        $('.checkall-courts').click(function () {
            $("input[name=courts]").attr('checked', this.checked);
        });
    });
</r:script>
