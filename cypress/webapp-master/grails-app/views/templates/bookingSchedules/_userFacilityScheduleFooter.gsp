<%@ page import="org.springframework.web.servlet.support.RequestContextUtils" %>

<div id="color-faq" class="schedule-footer">
    <ul class="list-inline no-margin">
        <li>
            <ul class="list-table">
                <li class="no-padding"><i class="white"></i></li>
                <li class="no-padding"><g:message code="templates.bookingSchedules.userFacilityScheduleFooter.message4"/></li>
            </ul>
        </li>
        <li>
            <ul class="list-table">
                <li class="no-padding"><i class="lightGrey"></i></li>
                <li class="no-padding"><g:message code="schedule.slot.not.available"/></li>
            </ul>
        </li>
        <li>
            <ul class="list-table">
                <li class="no-padding"><i class="red"></i></li>
                <li class="no-padding"><g:message code="templates.bookingSchedules.userFacilityScheduleFooter.message3"/></li>
            </ul>
        </li>
        <li>
            <ul class="list-table">
                <li class="no-padding"><i class="green"></i></li>
                <li class="no-padding"><g:message code="booking.paid.faq"/></li>
            </ul>
        </li>
        <li>
            <ul class="list-table">
                <li class="no-padding"><i class="yellow"></i></li>
                <li class="no-padding"><g:message code="templates.bookingSchedules.userFacilityScheduleFooter.message2"/></li>
            </ul>
        </li>
    </ul>
</div>

<r:script>
    var loginBeforeBooking = function(url, linkId) {
        $.jStorage.set('proceedToBooking', linkId);
        $.jStorage.setTTL('proceedToBooking', 1000*60*30);
        window.location.href = url;
    };

    $(document).ajaxStart(function() {
        var tooltips = ($('[rel=tooltip]').get());

        $.each(tooltips, function() {
            $(this).tooltip('destroy');
        });

        $('#schedule-spinner').show();
    });
    $(document).ajaxStop(function() {
        $('#schedule-spinner').hide();

        var $pickerDaily = $('#picker_daily');
        $pickerDaily.datepicker({
            format: "yyyy-mm-dd",
            weekStart: 1,
            todayBtn: "linked",
            language: "${RequestContextUtils.getLocale(request).language}",
            calendarWeeks: true,
            autoclose: true,
            todayHighlight: true,
            startDate: new Date()
        }).off('changeDate').on('changeDate', function(e) {
            var dateFormatted = e.format();
            <%
                def paramsNoDate = params
                params.put("s", 1)
                paramsNoDate.remove("date")
            %>
            $.ajax({
                type:'GET',
                url:'<g:createLink controller="book" action="schedule" params="${paramsNoDate}"/>&date='+dateFormatted,
                success:function(data) {
                    stopBlockBooking();
                    jQuery('#schedule').html(data);
                    startBlockBooking();
                },
                error:function(XMLHttpRequest,textStatus,errorThrown) {
                    // handle error
                }
            });
        });
        $pickerDaily.datepicker('update', '${g.forJavaScript(data: date.toString('yyyy-MM-dd'))}');

        $('table.weekly').floatThead({
            scrollingTop: 60
        });

        var $pickerWeekly = $('#picker_weekly');
        $pickerWeekly.datepicker({
            format: "yyyy-mm-dd",
            weekStart: 1,
            todayBtn: "linked",
            language: "${RequestContextUtils.getLocale(request).language}",
            calendarWeeks: true,
            autoclose: true,
            todayHighlight: true,
            startDate: new Date()
        }).off('changeDate').on('changeDate', function(e) {
            var date = Date.parse(e.date);
            <%
                def paramsNoWeekYear = params
                params.put("s", 1)
                paramsNoWeekYear.remove("week")
                paramsNoWeekYear.remove("year")
            %>
            $.ajax({
                type:'GET',
                url:'<g:createLink controller="book" action="schedule" params="${paramsNoWeekYear   }"/>&week='+date.getISOWeek()+'&year='+date.getUTCFullYear(),
                success:function(data) {
                    jQuery('#schedule').html(data);
                },
                error:function(XMLHttpRequest,textStatus,errorThrown) {
                    // handle error
                }
            });
        });
        $pickerWeekly.datepicker('update', '${g.forJavaScript(data: date.toString('yyyy-MM-dd'))}');
    });

    $(document).ready(function() {
        $("[rel=tooltip]").tooltip();

        var proceedTo = $.jStorage.get('proceedToBooking', null);
        if(proceedTo) {
            $.jStorage.deleteKey('proceedToBooking');
            $('#s'+ proceedTo).click();
        }

        $('table.daily').floatThead({
            scrollingTop: 60
        });

        var $freeSlot = $('.slot.free');
        var bindFreeSlotClickEvent = function() {
            $freeSlot.on('dblclick', function(e){
                e.preventDefault();
            }).one('click', function(e) {
                $freeSlot.unbind('click');
                var slotId = $(this).attr('slotid');
                var $slot = $('#s' + slotId);
                $slot.trigger('click');
            });
        };

        bindFreeSlotClickEvent();

        $('#userBookingModal').on('hidden.bs.modal', function () {
            // Unbind to avoid double order creation
            $freeSlot.unbind('click');
            bindFreeSlotClickEvent();
        });

        $(".block-book-cancel").on("click", function() {
            $freeSlot.unbind('click');
            bindFreeSlotClickEvent();
        });
    });

    var facilityId = "${g.forJavaScript(data: facility.id)}";
    var confirmUrl = "${g.forJavaScript(data: createLink(controller: "bookingPayment", action: "confirm"))}";

    var stopBlockBooking = function() {
        if (typeof blockBooking !== 'undefined')
            blockBooking.stop();
    };
    var startBlockBooking = function() {
        if (typeof blockBooking !== 'undefined')
            blockBooking = $('table.daily').userBlockBook({ facilityId: facilityId, url: confirmUrl, noSlotErrorMessage: "<g:message code="default.multiselect.noneSelectedText" />" })
    };
</r:script>
