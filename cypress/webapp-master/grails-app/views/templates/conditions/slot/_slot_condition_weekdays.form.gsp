<div class="pricelist condition">
        <div class="control-group">
            <label class="checkbox inline" for="checkall-weekdays" style="margin-left: 10px;">
                <g:checkBox name="checkall-weekdays" class="checkall-weekdays" id="checkall-weekdays" checked="false" value=""/>
                <g:message code="templates.conditions.slot.slotconditionweekdays.form.message1"/>
            </label>
            <br>
            <g:each in="${(1..7)}">
                <label class="checkbox" for="weekDays_${it}" style="">
                    <g:checkBox name="weekdays" id="weekDays_${it}" style="margin-left: 10px;"
                                            checked="false" value="${it}"/>
                    <g:message code="time.shortWeekDay.${it}"/>
                </label>
            </g:each>


        </div>
</div>

<r:script>
    $(function () {
        $('.checkall-weekdays').click(function () {
            $("input[name=weekdays]").attr('checked', this.checked);
        });
    });
</r:script>

