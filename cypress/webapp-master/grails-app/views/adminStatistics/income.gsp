<%@ page import="com.matchi.Facility" %>
<html>
<head>
    <meta name="layout" content="adminLayout"/>
    <title>MATCHi <g:message code="adminHome.index.title"/></title>
    <r:require modules="select2,jquery-multiselect-widget,daterangepicker,highcharts3"/>
</head>
<body>


<div id="container">
    <div class="content">
        <ul id="statistics-tab" class="nav nav-tabs">
            <li><g:link action="index"><g:message code="facility.invoicing.label"/></g:link></li>
            <li class="active"><g:link action="income" ><g:message code="adminStatistics.index.income"/></g:link></li>
        </ul>

        <form method="GET" id="filterForm" class="form-inline well" style="padding:12px 10px 4px 10px;">
            <fieldset>
                <div class="control-group">
                    <div tabindex="2" id="daterange" class="daterange pull-left" style="background-color: #fff;margin: 0 10px 0 0px;>
                    <i class="icon-calendar icon-large"></i>
                    <span ><g:formatDate format="MMMM d, yyyy"  date="${start.toDate()}" locale="sv"/> - <g:formatDate format="MMMM d, yyyy"  date="${end.toDate()}" locale="sv"/></span> <b style="margin-top:6px" class="caret"></b>
                </div>
                <div class="form-group col-sm-3 no-margin">
                    <g:select from="${grails.util.Holders.config.matchi.settings.currency.keySet()}" name="currency" value="${params.currency ?: 'SEK'}"
                              title="${message(code: 'adminStatistics.index.facilityIds.title')}"/>
                </div>

                <input name="start" id="rangestart" class="span8" type="hidden" value="<g:formatDate format="yyyy-MM-dd"  date="${start.toDate()}" locale="sv"/>">
                <input name="end" id="rangeend" class="span8" type="hidden" value="<g:formatDate format="yyyy-MM-dd"  date="${end.toDate()}" locale="sv"/>">

                <button id="filterSubmit" tabindex="3" class="btn" style="margin-right: 20px" type="submit"><g:message code="button.update.label"/></button>



            </fieldset>
        </form>

        <div class="row">
            <div class="span12 stats">
                <div class="row">
                    <div class="kpi span3">
                        <label><g:message code="adminStatistics.income.netaxeptTotal"/></label>
                        <h1 rel="tooltip" title="${message(code: 'adminStatistics.income.numberOfBookings')}">
                            ${data.sum { it.num_netaxept }}<g:message code="unit.st"/>
                        </h1>
                    </div>
                    <div class="kpi span3">
                        <label><g:message code="adminStatistics.income.adyenTotal"/></label>
                        <h1 rel="tooltip" title="${message(code: 'adminStatistics.income.numberOfBookings')}">
                            ${data.sum { it.num_adyen }}<g:message code="unit.st"/>
                        </h1>
                    </div>
                    <div class="kpi span3">
                        <label><g:message code="adminStatistics.income.couponTotal"/></label>
                        <h1>${data.sum { it.num_coupon }}<g:message code="unit.st"/></h1>
                        <label class="footer"><g:message code="adminStatistics.income.online"/> ${data.sum { it.num_coupon }}<g:message code="unit.st"/></label>
                    </div>
                    <div class="kpi span3">
                        <label><g:message code="default.total.label"/></label>
                        <h1>${data.sum { it.cnt }}<g:message code="unit.st"/></h1>
                    </div>
                    <div class="kpi span3">
                        <label><g:message code="adminStatistics.income.mobile"/></label>
                        <g:set var="num_api" value="${data.sum { it.num_api }}"/>
                        <g:set var="cnt" value="${data.sum { it.cnt }}"/>
                        <h1>${num_api ?: 0}<g:message code="unit.st"/></h1>
                        <label class="footer"><g:formatNumber number="${(num_api && cnt ? num_api / cnt : 0)*100}"/>% <g:message code="adminStatistics.income.avTotalt"/></label>
                    </div>
                </div>
                <div class="row">
                    <div class="kpi span3">
                        <label><g:message code="adminStatistics.income.payServiceOms"/></label>
                        <h1><g:formatNumber number="${data.sum { it.total }}"/> </h1>
                        <label class="footer"><g:message code="adminStatistics.income.credited"/> <g:formatNumber number="${data.sum { it.total_credited }}"/></label>
                    </div>
                    <!-- Cancellation Fees -->
                    <div class="kpi span3">
                        <label><g:message code="adminStatistics.income.cancellationFees"/></label>
                        <h1><g:formatNumber number="${cancellationFees}" maxFractionDigits="2"/></h1>
                    </div>
                    <!-- Fixed fees -->
                    <div class="kpi span3">
                        <label>Fixed fees</label>
                        <h1><g:formatNumber number="${fixedFees}" maxFractionDigits="2"/></h1>
                    </div>
                    <!-- Monthly fees -->
                    <div class="kpi span3">
                        <label>Monthly fees</label>
                        <h1><g:formatNumber number="${monthlyFees}" maxFractionDigits="2"/></h1>
                    </div>
                </div>
                <div class="row">
                    <!-- Yearly fees -->
                    <div class="kpi span3">
                        <label>Yearly fees</label>
                        <h1><g:formatNumber number="${yearlyFees}" maxFractionDigits="2"/></h1>
                    </div>
                    <!-- One time fees -->
                    <div class="kpi span3">
                        <label>One time fees</label>
                        <h1><g:formatNumber number="${oneTimeFees}" maxFractionDigits="2"/></h1>
                    </div>
                    <!-- Total fixed and recurring fees -->
                    <div class="kpi span3">
                        <label>Total fixed, montly, yearly</label>
                        <h1><g:formatNumber number="${fixedFees+monthlyFees+yearlyFees}" maxFractionDigits="2"/></h1>
                    </div>
                    <!-- Variable fees -->
                    <div class="kpi span3">
                        <label>Variable fees</label>
                        <h1><g:formatNumber number="${variableFees}" maxFractionDigits="2"/></h1>
                    </div>
                </div>
                <div class="row">
                    <!-- Variable fees (percentage) -->
                    <!-- Offer fees -->
                    <div class="kpi span3">
                        <label>Offer fees</label>
                        <h1><g:formatNumber number="${offerFees}" maxFractionDigits="2"/></h1>
                    </div>
                    <!-- SMS fees -->
                    <div class="kpi span3">
                        <label>Total fees</label>
                        <h1><g:formatNumber number="${totalFees}" maxFractionDigits="2"/></h1>
                    </div>
                    <div class="kpi span3">
                        <label>Avarage fees / contract</label>
                        <h1><g:formatNumber number="${contractFacilities ? totalFees/contractFacilities?.size() : 0}" maxFractionDigits="2"/></h1>
                        <g:if test="${contractFacilities}">
                            <label class="footer">${totalFees} / ${contractFacilities?.size()}</label>
                        </g:if>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="span12">

            </div>
        </div>
        <br>
        <div class="row">
            <div class="span12">
                <div id="chart1" style=""></div>
            </div>
        </div>


        <!--
        <div class="row">
            <div class="span12">
                <div id="chart2" style=""></div>
            </div>
        </div>
        <br>
        <div class="row">
            <div class="span12">
                <div id="chart2" style=""></div>
            </div>
        </div>
            -->

    </div>


</div>

<r:script>
    $(document).ready(function () {
        $('#chart1').highcharts({
            chart: {
                type: 'area',
                zoomType: 'x',
                spacingRight: 20
            },
            title: {
                text: "${message(code: 'adminStatistics.income.chartTitle')}"
            },
            subtitle: {

            },
            xAxis: {
                type: 'datetime',
                maxZoom: 14 * 24 * 3600000, // fourteen days
                title: {
                    text: null
                }
            },
            yAxis: {
                title: {
                    text: "${message(code: 'adminStatistics.income.chartY')}"
                }
            },
            tooltip: {
                shared: true
            },
            legend: {
                enabled: true
            },
            plotOptions: {
                area: {
                    stacking: 'normal',
                    lineColor: '#666666',
                    lineWidth: 1,
                    marker: {
                        lineWidth: 1,
                        lineColor: '#666666'
                    }
                }
            },

            series: [{
                type: 'area',
                name: "${message(code: 'payment.method.CREDIT_CARD')}",
                pointInterval: 24 * 3600 * 1000,
                pointStart: Date.UTC(${g.forJavaScript(data: start.getYear())}, ${g.forJavaScript(data: start.getMonthOfYear()-1)}, ${g.forJavaScript(data: start.getDayOfMonth())}),
                data: [
    <g:each in="${data}">${g.forJavaScript(data: it.num_netaxept+it.num_adyen)},</g:each>
    ]
},
{
    type: 'area',
    name: "${message(code: 'payment.method.COUPON')}",
                pointInterval: 24 * 3600 * 1000,
                pointStart: Date.UTC(${g.forJavaScript(data: start.getYear())}, ${g.forJavaScript(data: start.getMonthOfYear()-1)}, ${g.forJavaScript(data: start.getDayOfMonth())}),
                data: [
    <g:each in="${data}">${g.forJavaScript(data: it.num_coupon)},</g:each>
    ]
}]
});




$('#daterange').daterangepicker(
{
ranges: {
'${message(code: 'default.dateRangePicker.currentWeek')}': [(Date.today().getDay()==1?Date.today():Date.today().moveToDayOfWeek(1, -1)), Date.today().moveToDayOfWeek(0)],
'${message(code: 'default.dateRangePicker.lastWeek')}': [(Date.today().getDay()==1?Date.today():Date.today().add({ weeks: -1 }).moveToDayOfWeek(1, -1)), Date.today().add({ weeks: -1 }).moveToDayOfWeek(0)],
'${message(code: 'default.dateRangePicker.currentMonth')}': [Date.today().moveToFirstDayOfMonth(), Date.today().moveToLastDayOfMonth()],
'${message(code: 'default.dateRangePicker.lastMonth')}': [Date.today().moveToFirstDayOfMonth().add({ months: -1 }), Date.today().moveToFirstDayOfMonth().add({ days: -1 })]
},

format: "yyyy-MM-dd",
startDate: "<g:formatDate format="yyyy-MM-dd"  date="${start.toDate()}" locale="sv"/>",
                endDate: "<g:formatDate format="yyyy-MM-dd"  date="${end.toDate()}" locale="sv"/>",
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
                $('#filterForm').submit();
            }
    );

    });


</r:script>


</body>
</html>
