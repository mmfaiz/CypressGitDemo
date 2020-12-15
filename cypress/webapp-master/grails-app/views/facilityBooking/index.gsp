<%@ page import="org.joda.time.DateTime; com.matchi.Booking; com.matchi.Facility; com.matchi.DateUtil; com.matchi.facility.FacilityBookingController"%>
<%@ page import="com.matchi.schedule.Schedule"%>
<%@ page import="com.matchi.schedule.Schedule.Status"%>
<%@ page import="org.joda.time.LocalDate"%>
<g:set var="dateParamString" value="${formatDate(format: 'yyyy-MM-dd', date: date.toDate())}"/>
<html>
<head>
    <meta name="layout" content="facilityLayoutBooking" />
    <title><g:message code="facilityBooking.index.message1"/></title>
    <r:require modules="jquery-validate,matchi-blockbooking,matchi-schedule,mousetrap,select2,jquery-periodicalUpdater,matchi-customerselect,jstorage,jquery-fullscreen,daterangepicker" />
</head>
<body>
<div id="move-info" class="alert alert-info" style="display: none;">
    <a class="close" href="javascript:void(0)" onclick="moveBooking.stopMove()">×</a>
    <span id="move-info-label"></span>
</div>

<g:if test="${!facility?.isMasterFacility()}">
<div id="facility-schedule" class="schedule">

    <div style="margin-left: 100px;">
        <g:flashMessage />
        <g:flashError/>
        <g:errorMessage bean="bean" />
    </div>

    <div style="min-width: 960px">
        <div class="row-fluid">
            <div class="span4">
                <g:form action="list" method="get" class="form-inline hidden-fullscreen" style="margin: 9px 0 0 100px">
                    <g:hiddenField name="date" value="${dateParamString}"/>
                    <div class="input-append noprint pull-left" id="searchBookingContainer">
                        <input class="span10" placeholder="${message(code: 'facilityBooking.index.message7')}" id="bookingSearch" name="q" type="text"><button class="btn" type="submit"><i class="icon-search"></i></button>
                    </div>

                    <!-- <a class="btn" href="#">Idag</a>-->
                </g:form>
            </div>
            <div class="span4 date-navigation">
                <div>
                    <div class="week noprint current-week"><g:message code="facilityBooking.index.message8"/> <g:formatDate format="w" date="${date.toDate()}" locale="${user.language}" /></div>
                    <span class="prev hidden-fullscreen">
                        <a href="javascript: void(0)" onclick="prevDate()"><i class="icon-chevron-left"></i></a>
                        <span class="loading noprint"><r:img uri="/images/spinner.gif"/></span>
                    </span>
                    <span class="date hidden-fullscreen"><a href="#" id="picker" class="current-date"><g:formatDate format="EEEE d MMMM "  date="${date.toDate()}" locale="${user.language}"/></a></span>
                    <span class="date visible-fullscreen current-date"><g:formatDate format="EEEE d MMMM "  date="${date.toDate()}" locale="${user.language}"/></span>
                    <span class="next hidden-fullscreen">
                        <a href="javascript: void(0)" onclick="nextDate()"><i class="icon-chevron-right"></i></a>
                        <span class="loading noprint"><r:img uri="/images/spinner.gif"/></span>
                    </span>
                </div>
            </div>

            <div class="span4 pull-right schedule-control">
                <g:form name="selectedSlotsForm" method="post" class="form-inline" style="margin-right: 5px;">
                    <div class="btn-toolbar pull-right noprint" style="margin-left: 10px">
                        <div>
                            <g:scheduleViewControl date="${date}" fullscreen="${true}"/>
                            <a id="block-control" href="javascript:void(0);" onclick="blockBookingStart(this)" class="btn pull-right hidden-fullscreen"><g:message code="facilityBooking.index.message3"/></a>
                        </div>

                        <g:hiddenField id="addRestrictionSlotData" name="addRestrictionSlotData" value=""/>
                        <g:hiddenField id="delRestrictionSlotData" name="delRestrictionSlotData" value=""/>
                        <g:hiddenField id="markedSlotData" name="slotId" value=""/>
                        <g:hiddenField id="paymentSlotsData" name="alterSlotsData" value=""/>
                        <g:hiddenField id="cancelSlotsData" name="cancelSlotsData" value=""/>
                        <g:hiddenField name="exportSlotsData" value=""/>
                        <g:hiddenField name="date" value="${dateParamString}"/>

                        <input type="button" id="blockBookingBookBtn" value="${message(code: 'button.book.label')}"
                                class="btn disabled blockbooking-control"/>
                        <input type="button" id="blockBookingCancelBookingBtn" value="${message(code: 'button.unbook.label')}"
                                class="btn disabled blockbooking-control">

                        <g:render template="/templates/facility/bookingMenu"
                                  model="[menuClass: 'blockbooking-control']"/>

                        <a href="#" onclick="blockBookingStop()" class="btn btn-danger blockbooking-control" style="display: none"><g:message code="button.cancel.label"/></a>
                    </div>
                    <div class="loading right noprint"><r:img uri="/images/spinner.gif" style="margin-right:7px"/><g:message code="default.loader.label"/></div>
                </g:form>
            </div>
        </div>
    </div>

    <div id="daily-schedule-wrap">
        <g:render template="/templates/bookingSchedules/facilityDailyScheduleWrap"/>
    </div>

    <div id="color-faq" class="noprint">
        <ul class="inline">
            <li><i class="green"></i><g:message code="facilityBooking.index.fullyPaid"/></li>
            <g:if test="${facility.boxnet}">
                <li><i class="lightGreen"></i><g:message code="facilityBooking.index.partlyPayment"/></li>
            </g:if>
            <li><i class="yellow"></i><g:message code="facilityBooking.index.unpaid"/></li>
            <li><i class="blue"></i><g:message code="subscription.label"/></li>
            <li><i class="lightBlue"></i><g:message code="facilityBooking.index.cancelledSubscription"/></li>
            <li><i class="purple"></i><g:message code="default.activity"/></li>
            <li><i class="red"></i><g:message code="facilityBooking.index.internalBooking"/></li>
            <li><i class="lightGrey"></i><g:message code="schedule.slot.not.available"/></li>
            <g:if test="${facility.hasBookingRestrictions()}">
                <li><i class="restricted"></i><g:message code="schedule.slot.restricted"/></li>
            </g:if>
        </ul>
    </div>
</div>
</g:if>
<g:else>
    <div class="container">
        <h2>Master facility: ${facility.name}</h2>
        <p>Schedule is not activated for Master facilities</p>
        <p>Master facility for these venues:</p>
        <ul>
            <g:each in="${facility.memberFacilities}" var="memberFacility">
                <li>${memberFacility.name}</li>
            </g:each>
        </ul>
    </div>
</g:else>


    <div id="bookingModal" class="modal hide fade"></div>

    <r:script>
    var dateParamString = "${dateParamString}";
    var RESULT = "";

    var BTN_BOOKING_LABEL = "${message(code: 'button.book.label')}";
    var BTN_BOOKING_SELECTOR = "#blockBookingBookBtn";

    var BTN_PAYMENT_LABEL = "${message(code: 'payment.pay.label')}";
    var BTN_PAYMENT_SELECTOR = "#blockBookingPaymentBtn";

    var BTN_CANCELBOOKING_LABEL = "${message(code: 'button.unbook.label')}";
    var BTN_CANCELBOOKING_SELECTOR = "#blockBookingCancelBookingBtn";

    var BTN_EXPORT_BOOKINGS_LABEL = "${message(code: 'facilityBooking.index.menu.export.bookings')}";
    var BTN_EXPORT_BOOKINGS_SELECTOR = "#blockBookingExportBookingsBtn";

    var BTN_EXPORT_CUSTOMERS_LABEL = "${message(code: 'facilityBooking.index.menu.export.customers')}";
    var BTN_EXPORT_CUSTOMERS_SELECTOR = "#blockBookingExportCustomersBtn";

    var BTN_SEND_EMAIL_LABEL = "${message(code: 'facilityBooking.index.menu.send.email')}";
    var BTN_SEND_EMAIL_SELECTOR = "#sendEmailBtn";

        <g:if test="${facility?.hasBookingRestrictions()}">
            var BTN_ADD_BOOKING_RESTRICTIONS_LABEL = "${message(code: 'button.add.label')}";
        var BTN_ADD_BOOKING_RESTRICTIONS_SELECTOR = "#addBookingRestrictionBtn";

        var BTN_DEL_BOOKING_RESTRICTIONS_LABEL = "${message(code: 'button.delete.label')}";
        var BTN_DEL_BOOKING_RESTRICTIONS_SELECTOR = "#delBookingRestrictionBtn";
        </g:if>

        <g:if test="${facility.hasSMS()}">
            var BTN_SEND_SMS_LABEL = "${message(code: 'facilityBooking.index.menu.send.sms')}";
        var BTN_SEND_SMS_SELECTOR = "#sendSmsBtn";
        </g:if>

        <g:if test="${facility.hasApplicationInvoice()}">
            var BTN_INVOICE_LABEL = "${message(code: 'facilityBooking.index.menu.invoice')}";
        var BTN_INVOICE_SELECTOR = "#invoiceBtn";
        </g:if>
        var $blockControl = $("#block-control");

        var schedules;
        var blockBooking;
        var moveBooking;

        var blockBookingStart = function(origin) {
            if(!moveBooking.isMoveStarted()) {
                blockBooking.start(origin);
            }
        };

        var blockBookingStop = function() {
            blockBooking.stop();
            disableBtn(BTN_BOOKING_SELECTOR, BTN_BOOKING_LABEL);
            disableBtn(BTN_PAYMENT_SELECTOR, BTN_PAYMENT_LABEL);
            disableBtn(BTN_CANCELBOOKING_SELECTOR, BTN_CANCELBOOKING_LABEL);
            disableBtn(BTN_EXPORT_BOOKINGS_SELECTOR, BTN_EXPORT_BOOKINGS_LABEL);
            disableBtn(BTN_EXPORT_CUSTOMERS_SELECTOR, BTN_EXPORT_CUSTOMERS_LABEL);
            disableBtn(BTN_SEND_EMAIL_SELECTOR, BTN_SEND_EMAIL_LABEL);
        <g:if test="${facility?.hasBookingRestrictions()}">
            disableBtn(BTN_ADD_BOOKING_RESTRICTIONS_SELECTOR, BTN_ADD_BOOKING_RESTRICTIONS_LABEL);
            disableBtn(BTN_DEL_BOOKING_RESTRICTIONS_SELECTOR, BTN_DEL_BOOKING_RESTRICTIONS_LABEL);
        </g:if>
        <g:if test="${facility.hasSMS()}">disableBtn(BTN_SEND_SMS_SELECTOR, BTN_SEND_SMS_LABEL);</g:if>
        <g:if test="${facility.hasApplicationInvoice()}">disableBtn(BTN_INVOICE_SELECTOR, BTN_INVOICE_LABEL);</g:if>
        $("#markedSlotData").attr('value', '');
        $("#paymentSlotsData").attr('value', '');
        $("#cancelSlotsData").attr('value', '');
        $("#exportSlotsData").attr('value', '');
        $("#addRestrictionSlotData").attr('value', '');
        $("#delRestrictionSlotData").attr('value', '');
    };

    var disableBtn = function(id, label) {
        if ($(id).get(0).tagName == "LI") {
            $(id).find("a").text(label);
        } else {
            $(id).val(label);
        }
        $(id).addClass("disabled");
    };

    var enableBtn = function(id, label) {
        if ($(id).get(0).tagName == "LI") {
            $(id).find("a").text(label);
        } else {
            $(id).val(label);
        }
        $(id).removeClass("disabled");
    };

    var hasSelectedSlots = function() {
        return $("#markedSlotData").attr('value').length > 0;
    };

    var hasUnRestrictionSlots = function() {
        return $("#addRestrictionSlotData").attr('value').length > 0;
    };

    var hasRestrictionSlots = function() {
        return $("#delRestrictionSlotData").attr('value').length > 0;
    };

    var hasSelectedNonPaidBookings = function() {
        return $("#paymentSlotsData").attr('value').length > 0;
    };

    var hasSelectedBookings = function() {
        return $("#cancelSlotsData").attr('value').length > 0;
    };

    var blockBookingOnChange = function() {
        numFreeSlots = blockBooking.numSelectedFreeSlots();
        numNonPaidBookedSlots = blockBooking.numSelectedNonPaidBookedSlots();
        numBookedSlots = blockBooking.numSelectedBookedSlots();
        numCancelledSlots = blockBooking.numSelectedCancelledSlots();
        numRestrictedSlots = blockBooking.numSelectedRestrictedSlots();
        numUnRestrictedSlots = blockBooking.numSelectedUnRestrictedSlots();
        freeSlots = blockBooking.getSelectedFreeSlots();
        nonPaidBookedSlots = blockBooking.getSelectedNonPaidBookedSlots();
        bookedSlots = blockBooking.getSelectedBookedSlots();
        cancelledSlots = blockBooking.getSelectedCancelledSlots();
        restrictedSlots = blockBooking.getSelectedRestrictedSlots();
        unRestrictedSlots = blockBooking.getSelectedUnRestrictedSlots();


        if(numFreeSlots > 0) {
            enableBtn(BTN_BOOKING_SELECTOR, BTN_BOOKING_LABEL +" (" + numFreeSlots + ")");

            var markedSlotData = "";
            for (slotId in freeSlots) {
                markedSlotData += slotId + ",";
            }

            $("#markedSlotData").attr('value', markedSlotData);

        } else {
            disableBtn(BTN_BOOKING_SELECTOR, BTN_BOOKING_LABEL);
            $("#markedSlotData").attr('value', '');
        }

        if(numNonPaidBookedSlots > 0) {
            enableBtn(BTN_PAYMENT_SELECTOR, BTN_PAYMENT_LABEL +" (" + numNonPaidBookedSlots + ")");
        <g:if test="${facility.hasApplicationInvoice()}">enableBtn(BTN_INVOICE_SELECTOR, BTN_INVOICE_LABEL +" (" + numBookedSlots + ")");</g:if>

        var markedNonPaidSlotData = "";
        for (slotId in nonPaidBookedSlots) {
            markedNonPaidSlotData += slotId + ",";
        }

        $("#paymentSlotsData").attr('value', markedNonPaidSlotData);

    } else {
        disableBtn(BTN_PAYMENT_SELECTOR, BTN_PAYMENT_LABEL);
        <g:if test="${facility.hasApplicationInvoice()}">disableBtn(BTN_INVOICE_SELECTOR, BTN_INVOICE_LABEL);</g:if>
        $("#paymentSlotsData").attr('value', '');
    }

    if(numBookedSlots > 0) {
        enableBtn(BTN_CANCELBOOKING_SELECTOR, BTN_CANCELBOOKING_LABEL +" (" + numBookedSlots + ")");

        var markedCancelSlotData = "";
        for (slotId in bookedSlots) {
            markedCancelSlotData += slotId + ",";
        }

        $("#cancelSlotsData").attr('value', markedCancelSlotData);

    } else {
        disableBtn(BTN_CANCELBOOKING_SELECTOR, BTN_CANCELBOOKING_LABEL);
        $("#cancelSlotsData").attr('value', '');
    }

    if (numBookedSlots || numCancelledSlots) {
        var sum = numBookedSlots + numCancelledSlots;
        enableBtn(BTN_EXPORT_BOOKINGS_SELECTOR, BTN_EXPORT_BOOKINGS_LABEL +" (" + sum + ")");
        enableBtn(BTN_EXPORT_CUSTOMERS_SELECTOR, BTN_EXPORT_CUSTOMERS_LABEL +" (" + sum + ")");
        enableBtn(BTN_SEND_EMAIL_SELECTOR, BTN_SEND_EMAIL_LABEL +" (" + sum + ")");
        <g:if test="${facility.hasSMS()}">enableBtn(BTN_SEND_SMS_SELECTOR, BTN_SEND_SMS_LABEL +" (" + sum + ")");</g:if>

        var slotData = [];
        for (slotId in bookedSlots) {
            slotData.push(slotId);
        }
        for (slotId in cancelledSlots) {
            slotData.push(slotId);
        }
        $("#exportSlotsData").attr('value', slotData.join(","));
    } else {
        disableBtn(BTN_EXPORT_BOOKINGS_SELECTOR, BTN_EXPORT_BOOKINGS_LABEL);
        disableBtn(BTN_EXPORT_CUSTOMERS_SELECTOR, BTN_EXPORT_CUSTOMERS_LABEL);
        disableBtn(BTN_SEND_EMAIL_SELECTOR, BTN_SEND_EMAIL_LABEL);
        <g:if test="${facility.hasSMS()}">disableBtn(BTN_SEND_SMS_SELECTOR, BTN_SEND_SMS_LABEL);</g:if>
        $("#exportSlotsData").attr('value', '');
    }


        <g:if test="${facility?.hasBookingRestrictions()}">
            if (numRestrictedSlots > 0) {
                enableBtn(BTN_DEL_BOOKING_RESTRICTIONS_SELECTOR, BTN_DEL_BOOKING_RESTRICTIONS_LABEL +" (" + numRestrictedSlots + ")");

                var slotData = [];
                for (slotId in restrictedSlots) {
                    slotData.push(slotId);
                }

                $("#delRestrictionSlotData").attr('value', slotData.join(","));
            } else {
                disableBtn(BTN_DEL_BOOKING_RESTRICTIONS_SELECTOR, BTN_DEL_BOOKING_RESTRICTIONS_LABEL);
                $("#delRestrictionSlotData").attr('value', '');
            }

            if (numUnRestrictedSlots > 0) {
                enableBtn(BTN_ADD_BOOKING_RESTRICTIONS_SELECTOR, BTN_ADD_BOOKING_RESTRICTIONS_LABEL +" (" + numUnRestrictedSlots + ")");

                var slotData = [];
                for (slotId in unRestrictedSlots) {
                    slotData.push(slotId);
                }

                $("#addRestrictionSlotData").attr('value', slotData.join(","));
            } else {
                disableBtn(BTN_ADD_BOOKING_RESTRICTIONS_SELECTOR, BTN_ADD_BOOKING_RESTRICTIONS_LABEL);
                $("#addRestrictionSlotData").attr('value', '');
            }
        </g:if>
        };

        $(document).ready(function() {

            $("[rel=tooltip]").tooltip();

            $facilitySchedule = $("#facility-schedule");
            initSchedules();
            blockBooking = $facilitySchedule.bb({onchange: blockBookingOnChange});
            moveBooking = $facilitySchedule.moveBookings();

            var $dp = $("<input type='text' style='position:absolute;top:-100px;'/>").datepicker({
            firstDay: 1,
            autoSize: true,
            dateFormat: 'yy-mm-dd',
            defaultDate: '<g:formatDate date="${date.toDate()}" format="yy-MM-dd"/>',
            showWeek: true,
            onSelect: function(dateText, inst) {
                window.location.href = "?date=" + dateText + getDateTabParams(true);
            }
        }).appendTo('body');

        $("a#picker").button().click(function(e) {
            if ($dp.datepicker('widget').is(':hidden')) {
                $dp.datepicker("show").datepicker("widget").show().position({
                    my: "left top",
                    at: "center bottom",
                    of: this
                });
            } else {
                $dp.hide();
            }
            e.preventDefault();
        });

        $('#bookingModal').modal({ show:false, dynamic: true });

        $('#bookingModal').on('shown', function () {
            if($customerSearch) {
                $('[rel=tooltip]').tooltip('hide');
                if(!$customerSearch.val()) {
                    $customerSearch.open();
                }
            }
        });

        /**
            Timebooking KeyBoard shourt cuts
         */
        Mousetrap.bind(['ctrl+left', 'command+left'], function(e) {
            prevDate();
        });

        Mousetrap.bind(['ctrl+right', 'command+right'], function(e) {
            nextDate();
        });

        Mousetrap.bind(['ctrl+shift+d', 'command+shift+d'], function(e) {
            $("#picker").click();
        });

        Mousetrap.bind(['ctrl+shift+s', 'command+shift+s'], function(e) {
            $("#bookingSearch").focus();
        });

        Mousetrap.bind('m', function(e) {
            if(blockBooking.isStarted()) {
                blockBookingStop();
            } else if(!moveBooking.isMoveStarted()) {
                $blockControl.trigger("click");
            }
        });

        /**
            Auto update the booking view
         */
        var scheduleInfo = "";
        $facilitySchedule.find("table").PeriodicalUpdater('${g.forJavaScript(data: createLink(action: "checkUpdate"))}', {
            method: 'post',
            minTimeout: 32000,
            maxTimeout: 300000,
            multiplier: 2,
            type: 'json',
            maxCalls: 0,
            autoStop: 0,
            autoStopCallback: function() { },
            verbose: 0,
            error: function (xhr) {
                if (xhr.status === 401) {
                    location.href = "${g.forJavaScript(data: createLink(controller: 'login', action: 'auth'))}";
                }
            },
            data: function() {
                <g:if test="${!params.date || params.date == new Date().format('yyyy-MM-dd')}">
                    dateParamString = $.datepicker.formatDate("yy-mm-dd", new Date());
                    $(":hidden[name=date]").val(dateParamString);
                </g:if>
                return {date: dateParamString, scheduleInfo: scheduleInfo};
            }
        }, function(remoteData, success, xhr, handle) {
            if(xhr.status === 200){
                scheduleInfo = remoteData.result;
                updateSchedule();
            }
        });

        $facilitySchedule.on('shown.bs.tab', 'a[data-toggle="tab"]', function (e) {
            var ds = $($(e.target).attr("href")).find(".daily-schedule");
            schedules[$(".daily-schedule").index(ds)].redraw();
            blockBookingStop();
            if (history.pushState) {
                var taburl = "${g.forJavaScript(data: createLink(action: 'index'))}?" + getDateTabParams();
                window.history.pushState({path: taburl}, '', taburl);
            }
        }).on('click', '#blockBookingBookBtn', function(e) {
            if(!hasSelectedSlots()) {
                return false
            }
            $.ajax({
                type: 'POST',
                data: $(this).parents('form:first').serialize()
                        + "&returnUrl="+ encodeURIComponent("${g.forJavaScript(data: createLink(action: 'index'))}?" + getDateTabParams()),
                url: "${g.forJavaScript(data: createLink(action: 'bookingForm'))}",
                success: function(data,textStatus){
                    jQuery('#bookingModal').html(data);
                    showLayer();
                }
            });
            return false;
        }).on('click', '#blockBookingCancelBookingBtn', function(e) {
            if(!hasSelectedBookings()) {
                return false
            }
            $.ajax({
                type: 'POST',
                data: $(this).parents('form:first').serialize()
                        + "&returnUrl="+ encodeURIComponent("${g.forJavaScript(data: createLink(action: 'index'))}?" + getDateTabParams()),
                url: "${g.forJavaScript(data: createLink(action: 'cancelForm'))}",
                success: function(data,textStatus){
                    jQuery('#bookingModal').html(data);
                    showLayer();
                }
            });
            return false;
        });
    });

    function initSchedules() {
        schedules = [];
        $(".daily-schedule").each(function() {
            schedules.push($(this).matchiSchedule({marginTop: 200, maxSlotHeight: 70}));
        });
    }

    function updateSchedule() {
        $.ajax({
            url: '${g.forJavaScript(data: createLink(action: "index", params: [update: true]))}' + getDateTabParams(),
            type: 'POST',
            success: function(data, textStatus, jqXHR) {
                if ( !blockBooking.isStarted() ) {
                    $('[rel=tooltip]').tooltip('hide');
                    $facilitySchedule.find("#daily-schedule-wrap").html(data);
                    initSchedules();
                    blockBooking = $facilitySchedule.bb({onchange: blockBookingOnChange});
                    $(".current-date").text(jqXHR.getResponseHeader("${FacilityBookingController.CURRENT_DATE_HEADER}"));
                    $(".current-week").text(jqXHR.getResponseHeader("${FacilityBookingController.CURRENT_WEEK_HEADER}"));
                }
            }
        });
    }

    function prevDate() {
        $facilitySchedule.find('.date-navigation .prev .loading').show();
        $facilitySchedule.find('.date-navigation .next a').attr("onclick", null);
        $facilitySchedule.find('.date-navigation .prev a').hide();
        var d = new Date(dateParamString);
        d.setDate(d.getDate() - 1);
        window.location.href = "${g.forJavaScript(data: createLink(action: 'index', params: [dateChange: true]))}&date=" +
                $.datepicker.formatDate("yy-mm-dd", d) + getDateTabParams(true);
    }

    function nextDate() {
        $facilitySchedule.find('.date-navigation .next .loading').show();
        $facilitySchedule.find('.date-navigation .next a').hide();
        $facilitySchedule.find('.date-navigation .prev a').attr("onclick", null);
        var d = new Date(dateParamString);
        d.setDate(d.getDate() + 1);
        window.location.href = "${g.forJavaScript(data: createLink(action: 'index', params: [dateChange: true]))}&date=" +
                $.datepicker.formatDate("yy-mm-dd", d) + getDateTabParams(true);
    }

    function extendSubmitSlotsFormAction(action) {
        action = action.replace(/returnUrl=[^&]*&?/, "");
        var _returnUrl = "${g.forJavaScript(data: createLink(absolute: true, action: 'index', params: params.subMap(params.keySet() - 'error' - 'message' - 'returnUrl' - 'tab' - 'date')))}";
        return action + (action.indexOf('?') == -1 ? "?" : "&") + "returnUrl=" +
                encodeURIComponent(_returnUrl + (_returnUrl.indexOf('?') == -1 ? "?" : "") + getDateTabParams());
    }

    function getDateTabParams(skipDate) {
        return (skipDate ? "" : "&date=" + dateParamString) +
                ($(".tab-pane.active").length ? "&tab=" + $(".tab-pane.active").attr("id") : "");
    }

    (function($){
        $.fn.moveBookings = function() {
            //private vars
            var startedMove = false;
            var MOVE_KEY = "slodIds";
            var KEY_TIMEOUT = 180000; //Timeout set to 3min

            var $container = $("#move-info");
            var $label = $("#move-info-label");

            var mouseMoveDownEvent = "mousedown.moveBooking";

            var moveClicked = function(slotCell) {
                var slotId = slotCell.attr("slotid");
                var bookingSlotId = getMoveBooking();

                $.ajax({
                    url: "${g.forJavaScript(data: createLink(action: 'moveBookingForm'))}",
                    data: {slotId: slotId, bookingSlotId: bookingSlotId, date: dateParamString,
                            returnUrl: "${g.forJavaScript(data: createLink(action: 'index'))}?" + getDateTabParams()},
                    type: 'POST',
                    dataType: 'html',
                    success: function(html) {
                        $("#bookingModal").html(html);
                        $('#bookingModal').modal('show');
                    }
                });
            };

            var showInfo = function(/*Object*/customerInfo) {
                var text = "<strong>${message(code: 'facilityBooking.index.message6')}</strong> ";
                text += customerInfo.court + " ";
                text += customerInfo.date + " ";
                text += customerInfo.start + "-" + customerInfo.end + ", ";
                text += customerInfo.customer;

                $label.html(text);
                $container.slideDown();
            };

            var getMoveBooking = function() {
                return $.jStorage.get(MOVE_KEY);
            };

            var getInfo = function(slotId) {
                $.ajax({
                    url: '<g:createLink action="getBookingInfo"/>?slotId='+slotId,
                    type: 'POST',
                    success: function(data) {
                        if ( !blockBooking.isStarted() ) {
                            showInfo(data);
                        }
                    }
                });
            };

            var clearMove = function() {
                $container.slideUp();
                $.jStorage.flush();
                $(document).off(mouseMoveDownEvent, "td.slot");
            };

            this.init = function() {
                var status = getMoveBooking();

                if(status != null) {
                    this.startMove(status);

                    $.jStorage.listenKeyChange(MOVE_KEY, function(key, action){
                        if (action == "deleted") {
                            if(startedMove) {
                                startedMove = false;
                                clearMove();
                            }
                        }
                    });
                }

                return this;
            };

            this.startMove = function(slotId) {
                if(!startedMove) {
                    startedMove = true;

                    $.jStorage.set(MOVE_KEY, slotId);
                    $.jStorage.setTTL(MOVE_KEY, KEY_TIMEOUT);
                }

                $(document).on(mouseMoveDownEvent, "td.slot", function(e) {
                    moveClicked($(this));
                    e.preventDefault();
                });

                getInfo(slotId);
                $blockControl.addClass("disabled");
            };

            this.isMoveStarted = function() {
                return startedMove;
            };

            this.stopMove = function() {
                if(startedMove) {
                    startedMove = false;
                    clearMove();
                    $blockControl.removeClass("disabled");
                }
            };

            return this.init();
        }
    })(jQuery);

    var showForm = function(slotId) {
        if(blockBooking.isStarted() || moveBooking.isMoveStarted()) {
            return false;
        }

        jQuery.ajax({
            type: 'POST',
            data: {'slotId': slotId, 'date': dateParamString,
                    returnUrl: "${g.forJavaScript(data:  createLink(action: 'index'))}?" + getDateTabParams()},
            url: "${g.forJavaScript(data: createLink(action: 'bookingForm'))}",
            success: function(data,textStatus) {
                $('#bookingModal').html(data);showLayer();
            },
            error: function(XMLHttpRequest,textStatus,errorThrown) {
                handleAjaxError(XMLHttpRequest,textStatus,errorThrown);
            }
        });
        return false;
    };
    </r:script>
</body>
</html>
