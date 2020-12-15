<%@ page import="com.matchi.facility.FilterBookingsCommand; com.matchi.payment.PaymentMethod; com.matchi.Booking; com.matchi.Facility; com.matchi.DateUtil;"%>
<%@ page import="com.matchi.schedule.Schedule"%>
<%@ page import="com.matchi.schedule.Schedule.Status"%>
<%@ page import="org.joda.time.LocalDate"%>
<html>
<head>
    <meta name="layout" content="facilityLayoutBooking" />
    <title>${ facility } - <g:message code="facilityBooking.list.message7"/></title>
    <r:require modules="jquery-validate,daterangepicker,mousetrap,jquery-PrintArea,jquery-multiselect-widget,matchi-customerselect" />
</head>
<body>

<div id="facility-schedule" class="schedule">

    <div style="margin-left: 100px;">
        <g:flashMessage />
        <g:flashError/>
        <g:errorMessage bean="bean" />
    </div>

    <div style="min-width: 960px">
        <div class="row-fluid">
            <div class="span9 pull-left">
                <g:form action="list" name="searchForm" method="get" class="form-inline no-bottom-margin" style="margin-left:100px">
                    <ul class="inline filter-list">
                        <li>
                            <input tabindex="1" name="q" value="${cmd?.q}" placeholder="${message(code: 'facilityBooking.list.message8')}" id="search" type="text" style="height: 18px; padding: 8px; width: 198px">
                        </li>
                        <li>
                            <div tabindex="2" onkeydown="test(this)" id="daterange" class="daterange">
                                <i class="icon-calendar icon-large"></i>
                                <span ><g:formatDate format="MMMM d, yyyy" date="${start.toDate()}"/> - <g:formatDate format="MMMM d, yyyy"  date="${end.toDate()}"/></span> <b style="margin-top:6px" class="caret"></b>
                            </div>
                            <input name="start" id="rangestart" class="span8" type="hidden" value="<g:formatDate format="yyyy-MM-dd"  date="${start.toDate()}"/>">
                            <input name="end" id="rangeend" class="span8" type="hidden" value="<g:formatDate format="yyyy-MM-dd"  date="${end.toDate()}"/>">
                        </li>
                        <li>
                            <select id="markpaid" name="markpaid" multiple="true">
                                <option value="${true}" ${cmd?.markpaid ? "selected":""}><g:message code="facilityBooking.list.message1"/></option>
                                <option value="${false}" ${cmd?.markpaid != null && !cmd?.markpaid ? "selected":""}><g:message code="facilityBooking.list.message2"/></option>
                            </select>
                        </li>
                        <li>
                            <g:select name="paymentMethods" from="${FilterBookingsCommand.BookingPaymentMethod.list()}" multiple="true"
                                    valueMessagePrefix="facilityBooking.list.payment.method" value="${cmd.paymentMethods}"/>
                        </li>
                        <li>
                            <g:select name="courtIds" from="${com.matchi.Court.available(facility).list()}" multiple="true"
                                    optionKey="id" optionValue="name" value="${cmd.courtIds}"/>
                        </li>
                        <li>
                            <select id="bookingTypes" name="bookingTypes" multiple="true">
                                <g:each var="type" in="${bookingTypes}">
                                    <option value="${type}" ${cmd?.bookingTypes?.contains(type) ? 'selected' : ''}><g:message code="bookingGroup.name.${type}"/></option>
                                </g:each>
                            </select>
                        </li>
                        <g:if test="${facilityGroups}">
                            <li>
                                <select id="groups" name="groups" multiple="true">
                                    <option value="0" ${cmd?.groups?.contains(0L) ? "selected" : ""}>
                                        <g:message code="facilityCustomer.index.noGroup"/>
                                    </option>
                                    <g:each in="${facilityGroups}">
                                        <option value="${it.id}" ${cmd?.groups?.contains(it.id) ? "selected" : ""}>${it.name}</option>
                                    </g:each>
                                </select>
                            </li>
                        </g:if>
                        <li>
                            <button tabindex="3" class="btn" type="submit"><g:message code="button.search.label"/></button>
                        </li>
                    </ul>
                </g:form>

            </div>

            <div class="span3 pull-right schedule-control">
                <g:scheduleViewControl date="${start}" print="true"/>

                <g:render template="/templates/facility/bookingMenu"
                        model="[menuClass: 'right', returnUrl: createLink(absolute: true, action: 'list', params: params.subMap(params.keySet() - 'error' - 'message'))]"/>
                <div class="btn-toolbar pull-right no-margin">
                    <div class="btn-group hidden-fullscreen right-margin10" style="white-space:nowrap;">
                        <input type="button" id="blockBookingCancelBookingBtn"
                                value="${message(code: 'button.unbook.label')}" class="btn disabled">
                    </div>
                </div>
            </div>
        </div>
    </div>
    <table width="100%" border="0" cellpadding="0" cellspacing="0" class="booking-list" style="margin-top:10px;">
        <thead>
        <tr>
            <th style="width:93px;border:0"></th>
            <th width="180"><g:message code="default.booking.plural"/></th>
            <th style="padding-left: 20px">
                <label class="checkbox no-margin">
                    <input type="checkbox" name="selectAll" id="selectAll"/>
                    <g:message code="facilityBooking.list.message20"/>
                </label>
            </th>
            <th class="right-text">
                <div class="loading"><r:img uri="/images/spinner.gif" style="margin-right:7px"/><g:message code="default.loader.label"/></div>
                <g:if test="${!bookings.isEmpty()}">
                    <div class="">
                            <g:message code="facilityBooking.list.message10" args="[bookings.size()]"/>
                            <g:if test="${totalPrice}">
                                <span class="left-padding10">
                                    <g:message code="facilityBooking.list.totalPrice" args="[totalPrice]"/>
                                </span>
                            </g:if>
                    </div>
                </g:if>
            </th>
        </tr>
        </thead>
        <tbody>
        <%
            def currentDate = null
        %>
        <g:if test="${!bookings.isEmpty()}">
            <g:each in="${bookings}" var="booking" status="idx">
                <%
                    def bookingDate = new org.joda.time.LocalDate(booking.slot.timeSpan.start)
                    def newDate = currentDate == null || currentDate != bookingDate
                    def odd = idx % 2 == 0;
                %>
                <tr class="${newDate?"date":""} ${!odd?:"odd"}">
                    <td style="width:93px;border:0"></td>
                    <g:if test="${newDate}">
                        <td class="date" width="180">
                            <strong><g:formatDate format="EEEE d MMMM "  date="${booking.slot.timeSpan.start.toDate()}" locale="${user.language}"/></strong>
                        </td>
                    </g:if>
                    <g:else>
                        <td class="nodate" width="180"></td>
                    </g:else>
                    <td colspan="2">
                        <g:scheduleListBooking booking="${booking}"/>
                    </td>

                    <g:remoteLink style="position: absolute;top:-100px;" id="s${booking.slot.id}" controller="facilityBooking" action="bookingForm" update="bookingModal" onFailure="handleAjaxError()" onSuccess="showLayer()"
                                  params="[slotId: booking.slot.id, returnUrl: g.createLink(absolute: true, action: 'list', params: [start: start, end: end, q: cmd?.q, markpaid: cmd?.markpaid])]">
                        &nbsp;
                    </g:remoteLink>
                </tr>
                <%
                    currentDate = bookingDate
                %>
            </g:each>
        </g:if>
        <g:else>
            <tr class="date">
                <td style="width:100px;border:0" ></td>
                <td style="text-align: left" colspan="3">
                    <div class="padding-top: 10px;">
                        <br>
                        <h3><g:message code="facilityBooking.list.message5"/></h3>
                        <p><g:message code="facilityBooking.list.message6"/></p>
                        <br>
                    </div>

                </td>
            </tr>
        </g:else>

        </tbody>
    </table>
</div>


<g:if test="${!bookings.isEmpty()}">
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
            <li><i class="lightGrey"></i><g:message code="facilityBooking.index.cancelledSubscription"/></li>
        </ul>
    </div>
</g:if>

<div id="bookingModal" class="modal hide fade"></div>

<g:form name="selectedSlotsForm" style="display: none">
    <g:hiddenField name="exportSlotsData" value=""/>
    <g:hiddenField name="alterSlotsData" value=""/>
</g:form>

<r:script>

$(document).ready(function() {
    $("[rel=tooltip]").tooltip();

    $("#markpaid").multiselect({
        create: function() {$(this).next().width(210);},
        classes: "multi",
        minWidth: 115,
        checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
        uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
        noneSelectedText: "${message(code: 'facilityBooking.list.multiselect.noneSelectedText')}",
        selectedText: "${message(code: 'facilityBooking.list.multiselect.selectedText')}"
    });

    $("#paymentMethods").multiselect({
        create: function() {$(this).next().width(210);},
        classes: "multi",
        minWidth: 145,
        checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
        uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
        noneSelectedText: "${message(code: 'paymentMethod.multiselect.noneSelectedText')}",
        selectedText: "${message(code: 'paymentMethod.multiselect.selectedText')}"
    });

    $("#courtIds").multiselect({
        create: function() {$(this).next().width(210);},
        classes: "multi",
        minWidth: 145,
        checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
        uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
        noneSelectedText: "${message(code: 'court.multiselect.noneSelectedText')}",
        selectedText: "${message(code: 'court.multiselect.selectedText')}"
    });

    $("#bookingTypes").multiselect({
       create: function() {$(this).next().width(210);},
        classes: "multi",
        minWidth: 145,
        checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
        uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
        noneSelectedText: "${message(code: 'bookingGroup.multiselect.noneSelectedText')}",
        selectedText: "${message(code: 'bookingGroup.multiselect.selectedText')}"
    });

    <g:if test="${facilityGroups}">
    $("#groups").multiselect({
        create: function() {$(this).next().width(210);},
        classes: "multi",
        minWidth: 145,
        checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
        uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
        noneSelectedText: "${message(code: 'group.multiselect.noneSelectedText')}",
        selectedText: "${message(code: 'group.multiselect.selectedText')}"
    });
    </g:if>

    $('#daterange').daterangepicker(
            {
                ranges: {
                    '${message(code: 'default.dateRangePicker.today')}': ['today', 'today'],
                    '${message(code: 'default.dateRangePicker.tomorrow')}': ['tomorrow', 'tomorrow'],
                    '${message(code: 'default.dateRangePicker.currentWeek')}': [(Date.today().getDay()==1?Date.today():Date.today().moveToDayOfWeek(1, -1)), Date.today().moveToDayOfWeek(1)],
                    '${message(code: 'default.dateRangePicker.currentMonth')}': [Date.today().moveToFirstDayOfMonth(), Date.today().moveToLastDayOfMonth()],
                    '${message(code: 'default.dateRangePicker.lastMonth')}': [Date.today().moveToFirstDayOfMonth().add({ months: -1 }), Date.today().moveToFirstDayOfMonth().add({ days: -1 })]
                },
                format: "${message(code: 'date.format.dateOnly')}",
                startDate: "<g:formatDate format="yyyy-MM-dd"  date="${start.toDate()}"/>",
                endDate: "<g:formatDate format="yyyy-MM-dd"  date="${end.toDate()}"/>",
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
                $('#daterange span').html(start.toString('MMMM d, yyyy') + ' - ' + end.toString('MMMM d, yyyy'));
                $('.loading').show();
                $('#searchForm').submit();
            }
    );

    $(":checkbox[name=slotId]").click(function() {
        handleSlotSelection();
    });

    $("#selectAll").click(function() {
        $(":checkbox[name=slotId]").prop("checked", this.checked);
        handleSlotSelection();
    });

    $("#blockBookingCancelBookingBtn").on("click", function() {
        var slotIds = $("#exportSlotsData").val();
        if (slotIds) {
            if (slotIds.split(",").length > ${grailsApplication.config.matchi.booking.cancel.maxBatchSize}) {
                alert("${message(code: 'facilityBooking.list.cancelLimit', args: [grailsApplication.config.matchi.booking.cancel.maxBatchSize])}");
            } else {
                $.ajax({
                    type: 'POST',
                    data: "cancelSlotsData=" + slotIds + "&returnUrl=" +
                            encodeURIComponent("${g.forJavaScript(data: createLink(absolute: true, action: 'list', params: params.subMap(params.keySet() - 'error' - 'message')))}"),
                    url: "${g.forJavaScript(data: createLink(action: 'cancelForm'))}",
                    success: function(data,textStatus){
                        jQuery('#bookingModal').html(data);
                        showLayer();
                    }
                });
            }
        }
        return false;
    });
});

function handleSlotSelection() {
    var slotIds = [];
    var unpaidSlotIds = [];
    $(":checkbox[name=slotId]:checked").each(function() {
        slotIds.push($(this).val());
        if ($(this).attr("data-paid") != "true") {
            unpaidSlotIds.push($(this).val());
        }
    });

    $("#exportSlotsData").val(slotIds.join(","));
    $("#alterSlotsData").val(unpaidSlotIds.join(","));

    if (slotIds.length) {
        $("#sendEmailBtn, #sendSmsBtn, #blockBookingExportBookingsBtn, #blockBookingExportCustomersBtn, #blockBookingCancelBookingBtn").removeClass("disabled");
    } else {
        $("#sendEmailBtn, #sendSmsBtn, #blockBookingExportBookingsBtn, #blockBookingExportCustomersBtn, #blockBookingCancelBookingBtn").addClass("disabled");
    }
    if (unpaidSlotIds.length) {
        $("#blockBookingPaymentBtn, #invoiceBtn").removeClass("disabled");
    } else {
        $("#blockBookingPaymentBtn, #invoiceBtn").addClass("disabled");
    }
}
</r:script>
</body>
</html>
