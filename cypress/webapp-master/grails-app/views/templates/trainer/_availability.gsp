<g:if test="${trainer?.hasAvailability()}">
    <div id="trainer-availabilities_${trainer.id}">
        <h4><g:message code="trainer.availability.label"/></h4>
        <p class="help-block"><g:message code="trainer.availability.label.help"/></p>

        <div class="row">
            <div class="col-sm-12">
                <a href="javascript:void(0)" class="btn btn-info" onclick="addAvailability('${trainer.id}')"><i class="ti-plus"></i> <g:message code="button.add.label"/></a>
            </div>
        </div>
        <g:set var="availabilities" value="${trainer.getCurrentAndFutureAvailabilities(new Date())}" />
        <g:if test="${availabilities.size() > 0}">
            <g:set var="idx" value="${0}"/>
            <g:each in="${1..7}" var="day" status="i">
                <g:set var="av" value="${availabilities.findAll { it.weekday == day }}" />
                <g:each in="${av}" var="a">
                    <g:set var="idx" value="${idx + 1}"/>
                    <div class="form-group select-availability top-margin10 row">
                        <div class="col-sm-2">
                            <input id="fromTime_${idx}" type="hidden" class="fromTime" name="fromTime_${idx}" value="${a.begin}"/>
                            <input id="toTime_${idx}" type="hidden" class="toTime" name="toTime_${idx}" value="${a.end}"/>

                            <g:select class="selectPicker" name="weekDay_${idx}" from="${1..7}" value="${a.weekday}"
                                      valueMessagePrefix="time.weekDay.plural"/>
                        </div>

                        <div class="col-sm-2">
                            <div class="input-group">
                                <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
                                <input class="form-control showStartDate" readonly="true" type="text" name="showStartDate_${idx}" id="showStartDate_${idx}"
                                       placeholder="${message(code: "default.startDate.label")}" data-index="${idx}"
                                       value="${formatDate(date: a.validStart?.toDate(), formatName: 'date.format.dateOnly')}"/>
                                <g:hiddenField name="startDate_${idx}" id="startDate_${idx}" value="${formatDate(date: a.validStart?.toDate(), formatName: 'date.format.dateOnly')}"/>
                            </div>
                        </div>

                        <div class="col-sm-2">
                            <div class="input-group">
                                <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
                                <input class="form-control" readonly="true" type="text" name="showEndDate_${idx}" id="showEndDate_${idx}"
                                       placeholder="${message(code: "default.endDate.label")}" data-index="${idx}"
                                       value="${formatDate(date: a.validEnd?.toDate(), formatName: 'date.format.dateOnly')}"/>
                                <g:hiddenField name="endDate_${idx}" id="endDate_${idx}" value="${formatDate(date: a.validEnd?.toDate(), formatName: 'date.format.dateOnly')}"/>
                            </div>
                        </div>

                        <div class="col-sm-2 text-center">
                            <span class="small text-muted right-margin5"><g:message code="userProfile.index.message14"/> </span>
                            <p id="timeLabel_${i}" class="timeLabel" style="margin-top: 0px;">
                                ${a.begin.toString("HH:mm")} - ${a.end.toString("HH:mm")}
                            </p>
                        </div>

                        <div class="col-sm-3">
                            <div id="timeFilter_${i}" class="timeFilter slider-input" style="margin-top: 18px;"></div>
                        </div>

                        <div class="col-sm-1 text-center">
                            <a href="javascript:void(0)" onclick="$(this).closest('.select-availability').remove()" class=""><i class="fa fa-remove text-danger" style="margin-top: 4px; font-size: 2em;"></i></a>
                        </div>
                    </div>
                </g:each>
            </g:each>
        </g:if>
    </div>

    <div id="a-template" class="form-group select-availability top-margin10 row" style="display: none;">
        <div class="col-sm-2">
            <input id="fromTime" type="hidden" class="fromTime" name="fromTime_${it}" />
            <input id="toTime" type="hidden" class="toTime" name="toTime_${it}" />

            <g:select name="weekDay" class="selectPicker" from="${1..7}" valueMessagePrefix="time.weekDay.plural"/>
        </div>

        <div class="col-sm-2">
            <div class="input-group">
                <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
                <input class="form-control " readonly="true" type="text" id="showStartDate"
                       placeholder="${message(code: "default.startDate.label")}"/>
                <g:hiddenField name="startDate" id="startDate"/>
            </div>
        </div>

        <div class="col-sm-2">
            <div class="input-group">
                <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
                <input class="form-control" readonly="true" type="text" id="showEndDate"
                       placeholder="${message(code: "default.endDate.label")}"/>
                <g:hiddenField name="endDate" id="endDate"/>
            </div>
        </div>

        <div class="col-sm-2 text-center">
            <span class="small text-muted right-margin5"><g:message code="userProfile.index.message14"/> </span>
            <p class="timeLabel" style="margin-top: 0px;">
                00:00 - 23:30
            </p>
        </div>

        <div class="col-sm-3">
            <div id="timeFilter" class="timeFilter slider-input" style="margin-top: 18px;"></div>
        </div>

        <div class="col-sm-1 text-center">
            <a href="javascript:void(0)" onclick="$(this).closest('.select-availability').remove()" class=""><i class="fa fa-remove text-danger" style="margin-top: 4px; font-size: 2em;"></i></a>
        </div>
    </div>

    <r:script>
        var initDatePickers = function ($picker) {
            var index = $picker.data('index');

            $picker.datepicker({
                autoSize: true,
                dateFormat: '<g:message code="date.format.dateOnly.small"/>',
                altField: '#startDate_' + index,
                altFormat: 'yy-mm-dd',
                minDate: new Date()
            });

            var $endPicker = $('#showEndDate_' + index);

            $endPicker.datepicker({
                autoSize: true,
                dateFormat: '<g:message code="date.format.dateOnly.small"/>',
                altField: '#endDate_' + $picker.data('index'),
                altFormat: 'yy-mm-dd',
                minDate: new Date()
            });

            $picker.on('change', function() {
                var i = $(this).data('index');
                $('#showEndDate_' + i).datepicker( "option", "minDate", new Date($('#startDate_' + i).val()));
            });
        }

        $(document).ready(function() {
            $('.selectPicker:visible').selectpicker();

            $(".showStartDate").each(function () {
                initDatePickers($(this));
            });

            $( ".timeFilter" ).slider({
                range: true,
                min: 0,
                max: 1410,
                step: 30,
                slide: function( event, ui ) {
                    var $this      = $(this).closest('.select-availability');
                    var $timeLabel = $this.find('.timeLabel');
                    var $fromTime  = $this.find('.fromTime');
                    var $toTime    = $this.find('.toTime');

                    var fromTime = getTime(ui.values[0]);
                    var toTime = getTime(ui.values[1]);

                    $fromTime.val(fromTime);
                    $toTime.val(toTime);

                    $timeLabel.html($fromTime.val() + ' - ' + $toTime.val());
                },
                create: function() {
                    var $this      = $(this).closest('.select-availability');
                    var $fromTime  = $this.find('.fromTime');
                    var $toTime    = $this.find('.toTime');


                    $(this).slider('values', [getMinutes($fromTime.val().substr(0,5)),
                                        getMinutes($toTime.val().substr(0,5))]);
                }
            });
        });

        var addAvailability = function(trainerId) {

            var idx  = $('.select-availability').length;

            // To avoid id collision
            $('.select-availability').each(function () {
                var $self = $(this);
                var $fromTime = $($self.find('.fromTime')[0]);
                if($fromTime.attr('id').indexOf('_') > -1) {
                    var i = parseInt($fromTime.attr('id').split('_')[1]);
                    if(i >= idx) {
                        idx = i+1;
                    }
                }
            });


            var $new = $('#a-template').clone();
            $new.appendTo('#trainer-availabilities_' + trainerId);
            $new.css('display', 'block');
            $new.removeAttr('id');

            $new.find('#weekDay').prop('name', 'weekDay_'+idx).selectpicker();
            $new.find('#toTime').prop('name', 'toTime_'+idx).prop('id', 'toTime_'+idx);
            $new.find('#fromTime').prop('name', 'fromTime_'+idx).prop('id', 'fromTime_'+idx);;
            $new.find('#timeFilter').prop('id', 'timeFilter_'+idx);
            $new.find('#showStartDate').prop('id', 'showStartDate_'+idx).prop('name', 'showStartDate_'+idx);
            $new.find('#startDate').prop('id', 'startDate_'+idx).prop('name', 'startDate_'+idx);
            $new.find('#showEndDate').prop('id', 'showEndDate_'+idx).prop('name', 'showEndDate_'+idx);
            $new.find('#endDate').prop('id', 'endDate_'+idx).prop('name', 'endDate_'+idx);

            $new.find('#showStartDate_' + idx).attr('data-index', idx);
            $new.find('#showEndDate_' + idx).attr('data-index', idx);

            $new.find('#timeFilter_'+idx).slider({
                range: true,
                min: 0,
                max: 1410,
                step: 30,
                values: [ 0, 1410 ],
                slide: function( event, ui ) {
                    var $timeLabel = $new.find('.timeLabel');
                    var $fromTime  = $new.find('.fromTime');
                    var $toTime    = $new.find('.toTime');

                    var fromTime = getTime(ui.values[0]);
                    var toTime = getTime(ui.values[1]);

                    $fromTime.val(fromTime);
                    $toTime.val(toTime);

                    $timeLabel.html(fromTime + ' - ' + toTime);
                },
                create: function() {
                    var $fromTime  = $new.find('.fromTime');
                    var $toTime    = $new.find('.toTime');

                    $fromTime.val('00:00');
                    $toTime.val('23:30');
                }
            });

            initDatePickers($new.find('#showStartDate_' + idx));
        };
    </r:script>
</g:if>