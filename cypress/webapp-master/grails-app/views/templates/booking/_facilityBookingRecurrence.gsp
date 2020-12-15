<%@ page import="org.joda.time.DateTime; com.matchi.Season" %>
<div class="controls well" style="padding: 15px">
    <g:hiddenField id="useRecurrence" name="useRecurrence" value="false" />

    <a href="javascript:void(0)" onclick="toggleRecurrence();">
        <h6>
            <i class="icon-repeat"></i>
            <g:message code="templates.booking.facilityBookingRecurrence.message1"/>
            <i id="toggleMarker" class="icon-chevron-right"></i>
        </h6>
    </a>
    <div id="recurrenceForms" style="display: none;">
        <div class="space10"></div>
        <div class="control-group">
            <div class="controls controls-row">
                <!--<div class="control-label inline">Start</div>-->
                <input type="text" class="span1 center-text" name="showRecurrenceStart" readonly="true"
                       value="<g:formatDate formatName="date.format.dateOnly" date="${date}" />"/>
                <g:hiddenField name="recurrenceStart" value="${new DateTime(date).toString("yyyy-MM-dd")}" />
                <div class="span1 control-label" style="width: 5px;">-</div>
                <input type="text" class="span1 center-text" name="showRecurrenceEnd" id="showRecurrenceEnd"
                       value="<g:formatDate formatName="date.format.dateOnly" date="${season?.endTime}" />"/>
                <g:hiddenField name="recurrenceEnd" value="${new DateTime(season?.endTime).toString("yyyy-MM-dd")}" />
            </div>
            <div class="controls">
                <g:each in="${(1..7)}">
                    <label class="checkbox inline" for="weekDays_${it}">
                        <g:checkBox name="weekDays" id="weekDays_${it}" class="no_float"
                                    checked="${it == weekDay}" value="${it}"
                                    readonly="${it == weekDay}"/>
                        <g:message code="time.shortWeekDay.${it}"/>
                    </label>
                </g:each>
            </div>
            <div class="controls">

                <label class="pull-left">
                    <% // WEEKLY=1, always %>
                    <g:hiddenField name="frequency" value="1"/>
                    ${message(code: "bookingGroupFrequency.message")}
                    <select name="interval" style="width: 45px;height:auto; margin-top: 5px;">
                        <g:each in="${1..10}">
                            <option value="${it}">${it}</option>
                        </g:each>
                    </select>
                    <span id="intervalName">${message(code: "bookingGroupFrequency.interval.${frequencies[0].type}")}</span>
                </label>
            </div>
        </div>
    </div>
    <div class="clearfix"></div>
</div>
<script type="text/javascript">
    $(document).ready(function() {
        $("#showRecurrenceEnd").datepicker({
            firstDay: 1,
            autoSize: true,
            dateFormat: '<g:message code="date.format.dateOnly.small"/>',
            minDate: new Date("${g.forJavaScript(data: new DateTime(date).plusDays(1).toString('yyyy-MM-dd'))}"),
            maxDate: new Date("${g.forJavaScript(data: new DateTime(Season.findByFacility(season.facility, [sort: 'endTime', order: 'desc'])?.endTime).toString('yyyy-MM-dd'))}"),
            altField: '#recurrenceEnd',
            altFormat: 'yy-mm-dd'
        });

        $("#frequency").on("change", function() {
            $("#intervalName").html($(this).find(":selected").attr("interval"));
        });
    });

    function toggleRecurrence() {
        var $toggleMarker = $("#toggleMarker");
        var $useRecurrence = $("#useRecurrence");
        var $showActivated = $("#showActivated");
        var $recurrenceForms = $("#recurrenceForms");

        var locked = $recurrenceForms.attr("locked");
        if (locked != "true") {
            $recurrenceForms.toggle();
        }

        if($toggleMarker.hasClass("icon-chevron-right")) {
            $toggleMarker.removeClass("icon-chevron-right");
            $toggleMarker.addClass("icon-chevron-down");

            $showActivated.html("${message(code: 'templates.booking.facilityBookingRecurrence.message2')}");
            $useRecurrence.val(true);

            $("#formSubmit").hide();
            $("#recurrenceSubmit").show();
        } else {
            if (locked != "true") {
                $toggleMarker.removeClass("icon-chevron-down");
                $toggleMarker.addClass("icon-chevron-right");

                $showActivated.html("${message(code: 'templates.booking.facilityBookingRecurrence.message3')}");
                $useRecurrence.val(false);

                $("#formSubmit").show();
                $("#recurrenceSubmit").hide();
            } else {
                alert("${message(code: 'templates.booking.facilityBookingRecurrence.message4')}");
            }
        }
    }
</script>