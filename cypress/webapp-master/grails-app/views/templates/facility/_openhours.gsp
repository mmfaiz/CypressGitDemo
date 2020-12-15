<%@ page import="org.joda.time.DateTimeFieldType" %>

<label><g:message code="facility.openhours.label"/></label>
<g:each in="${(1..7)}">
    <div class="facility select-availability disabled" day="${it}">
        <ul class="list-inline">
            <li style="width: 100px;"><g:message code="time.weekDay.plural.${it}"/></li>
            <li>
                <div class="checkbox">
                    <g:checkBox class="openHoursControl" name="active_${it}" day="${it}" data-title="Checked means open" rel="tooltip"/>
                    <label for="active_${it}"></label>
                </div>
            </li>
            <li><span class="timeLabel" day="${it}">00:00 - 23:00</span></li>
            <li style="width: 600px"><div class="timeFilter" day="${it}" style="display: block"></div></li>
        </ul>

        <input type="hidden" class="fromMinute" name="fromMinute_${it}" />
        <input type="hidden" class="toMinute" name="toMinute_${it}" />
        <input type="hidden" class="active" name="active_${it}" />
    </div>
</g:each>

<r:script>
    function updateAvailabilityLabel(start, end, timeLabelEl) {
        var startHours = Math.floor(start / 60);
        var startMinutes = start - startHours * 60;
        var endHours = Math.floor(end / 60);
        var endMinutes = end - endHours * 60;
        timeLabelEl.html(formatHour(startHours) + ":" + formatHour(startMinutes) + " - "
                        + formatHour(endHours) + ":" + formatHour(endMinutes));
    }

    function formatHour(hour) {
        return (hour > 9 ? hour : "0"+hour)
    }

    $(document).ready(function() {
        $(".openHoursControl").tooltip({ delay: { show: 1000, hide: 100 } });

        $(".openHoursControl").on("click", function() {
            var $this = $(this);

            if($this.is(":checked")) {
                $this.closest(".select-availability").removeClass("disabled");
            } else {
                $this.closest(".select-availability").addClass("disabled");
            }
        });

        $( ".timeFilter" ).slider({
            range: true,
            min: 0,
            max: 1440,
            step: 30,
            values: [0, 1380],
            slide: function( event, ui ) {
                var weekDay   = $(this).attr("day");
                var $this      = $(".select-availability[day=" + weekDay + "]");
                var $timeLabel = $this.find(".timeLabel");
                var $fromMinute  = $this.find(".fromMinute");
                var $toMinute    = $this.find(".toMinute");

                $fromMinute.val(ui.values[ 0 ]);
                $toMinute.val(ui.values[ 1 ]);

                updateAvailabilityLabel(ui.values[0], ui.values[1], $timeLabel);
            },
            create: function(event, ui) {
                var weekDay   = $(this).attr("day");
                var $this      = $(".select-availability[day=" + weekDay + "]");
                var $fromMinute  = $this.find(".fromMinute");
                var $toMinute    = $this.find(".toMinute");

                $fromMinute.val("0");
                $toMinute.val("1380");
            }
        });

        <g:each in="${facility.availabilities}">
            var $this = $(".select-availability[day=${g.forJavaScript(data: it.weekday)}]");

            var $timeFilter = $this.find(".timeFilter");
            var $timeLabel  = $this.find(".timeLabel");
            var $active     = $this.find(".openHoursControl");
            var $fromMinute  = $this.find(".fromMinute");
            var $toMinute    = $this.find(".toMinute");

            $timeFilter.slider("values", 0, "${g.forJavaScript(data: it.begin.get(DateTimeFieldType.minuteOfDay()))}");
            $timeFilter.slider("values", 1, "${g.forJavaScript(data: it.end.plusSeconds(1).get(DateTimeFieldType.minuteOfDay()) == 0 ? 1440 : it.end.get(DateTimeFieldType.minuteOfDay()))}");

            $fromMinute.val($timeFilter.slider("values", 0));
            $toMinute.val($timeFilter.slider("values", 1));

            updateAvailabilityLabel($timeFilter.slider("values", 0), $timeFilter.slider("values", 1), $timeLabel);

            <g:if test="${it.active}">
                $active.attr("checked", true);
                $active.attr("value", true);
                $this.removeClass("disabled");
            </g:if>
        </g:each>
    });
</r:script>
