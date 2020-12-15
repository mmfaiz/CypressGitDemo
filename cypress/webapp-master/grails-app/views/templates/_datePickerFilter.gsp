<r:require modules="daterangepicker"/>
<r:script>
    $(function() {
        $('#daterange').daterangepicker(
                {
                    ranges: {
                        '${message(code: 'default.dateRangePicker.currentWeek')}': [(Date.today().getDay()==1?Date.today():Date.today().moveToDayOfWeek(1, -1)), Date.today().moveToDayOfWeek(0)],
                        '${message(code: 'default.dateRangePicker.lastWeek')}': [(Date.today().getDay()==1?Date.today().add({ weeks: -1 }):Date.today().add({ weeks: -1 }).moveToDayOfWeek(1, -1)), Date.today().add({ weeks: -1 }).moveToDayOfWeek(0)],
                        '${message(code: 'default.dateRangePicker.currentMonth')}': [Date.today().moveToFirstDayOfMonth(), Date.today().moveToLastDayOfMonth()],
                        '${message(code: 'default.dateRangePicker.lastMonth')}': [Date.today().moveToFirstDayOfMonth().add({ months: -1 }), Date.today().moveToFirstDayOfMonth().add({ days: -1 })]
                    },

                    format: "${message(code: 'date.format.dateOnly')}",
                    startDate: "<g:formatDate formatName="date.format.dateOnly"  date="${start.toDate()}"/>",
                    endDate: "<g:formatDate formatName="date.format.dateOnly"  date="${end.toDate()}"/>",
                    locale: {
                        applyLabel:"${message(code: 'default.dateRangePicker.applyLabel')}",
                        fromLabel:"${message(code: 'default.dateRangePicker.fromLabel')}",
                        toLabel:"${message(code: 'default.dateRangePicker.toLabel')}",
                        customRangeLabel:"${message(code: 'default.dateRangePicker.customRangeLabel')}",
                        daysOfWeek:['${message(code: 'default.dateRangePicker.daysOfWeek.sun')}', '${message(code: 'default.dateRangePicker.daysOfWeek.mon')}', '${message(code: 'default.dateRangePicker.daysOfWeek.tue')}', '${message(code: 'default.dateRangePicker.daysOfWeek.wed')}', '${message(code: 'default.dateRangePicker.daysOfWeek.thu')}', '${message(code: 'default.dateRangePicker.daysOfWeek.fri')}','${message(code: 'default.dateRangePicker.daysOfWeek.sat')}'],
                        monthNames:['${message(code: 'default.dateRangePicker.monthNames.january')}', '${message(code: 'default.dateRangePicker.monthNames.february')}', '${message(code: 'default.dateRangePicker.monthNames.march')}', '${message(code: 'default.dateRangePicker.monthNames.april')}', '${message(code: 'default.dateRangePicker.monthNames.may')}', '${message(code: 'default.dateRangePicker.monthNames.june')}', '${message(code: 'default.dateRangePicker.monthNames.july')}', '${message(code: 'default.dateRangePicker.monthNames.august')}', '${message(code: 'default.dateRangePicker.monthNames.september')}', '${message(code: 'default.dateRangePicker.monthNames.october')}', '${message(code: 'default.dateRangePicker.monthNames.november')}', '${message(code: 'default.dateRangePicker.monthNames.december')}'],
                        firstDay:0
                    }
                },
                function(start, end) {
                    $('#rangestart').val(start.toString('yyyy-MM-dd'));
                    $('#rangeend').val(end.toString('yyyy-MM-dd'));
                    $('#daterange span').html(moment(start).format('MMM D, YYYY') + ' - ' + moment(end).format('MMM D, YYYY'));
                    $('#filterForm').submit();
                }
        );
    });
</r:script>

<div tabindex="2" id="daterange" class="daterange" style="background-color: #fff; margin: 0;">
    <i class="icon-calendar icon-large"></i>
    <span><g:formatDate format="MMM d, yyyy" date="${start.toDate()}"/> - <g:formatDate format="MMM d, yyyy" date="${end.toDate()}"/></span> <b style="margin-top:6px" class="caret"></b>
</div>

<input name="start" id="rangestart" class="span8" type="hidden" value="<g:formatDate format="yyyy-MM-dd"  date="${start.toDate()}"/>">
<input name="end" id="rangeend" class="span8" type="hidden" value="<g:formatDate format="yyyy-MM-dd"  date="${end.toDate()}"/>">