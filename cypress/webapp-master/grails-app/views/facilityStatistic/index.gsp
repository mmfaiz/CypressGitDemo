<%@ page import="org.joda.time.DateTime; com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title>${ facility } - <g:message code="facilityStatistic.index.message13"/></title>
    <r:require modules="select2,highcharts,jquery-multiselect-widget,daterangepicker,jquery-timepicker"/>
</head>
<body>

<div id="container">
    <div class="content">
        <g:if test="${!facility?.isMasterFacility()}">
            <ul id="statistics-tab" class="nav nav-tabs">
                <li class="active"><g:link action="index" params="[start: start.toString('yyyy-MM-dd'), end: end.toString('yyyy-MM-dd')]"><g:message code="default.booking.plural"/></g:link></li>
                <li><g:link action="payment" params="[start: start.toString('yyyy-MM-dd'), end: end.toString('yyyy-MM-dd')]"><g:message code="adminStatistics.index.income"/></g:link></li>
                <li><g:link action="customer" params="[start: start.toString('yyyy-MM-dd'), end: end.toString('yyyy-MM-dd')]"><g:message code="adminStatistics.index.customer"/></g:link></li>
            </ul>

            <form method="GET" id="filterForm" class="form-inline well" style="padding:12px 10px 4px 10px;">
                <fieldset>
                    <div class="control-group">
                        <ul class="inline filter-list">
                            <li>
                                <g:render template="/templates/datePickerFilter"/>
                            </li>
                            <li>
                                <select id="courtIds" name="courtIds" multiple="true">
                                    <g:each in="${courts}">
                                        <option value="${it.id}" ${cmd.courtIds.contains(it.id)?"selected":""}>${it.name}</option>
                                    </g:each>
                                </select>
                            </li>
                            <li>
                                <input class="span1 filter-time" type="text" name="startTime" id="timeConditionFrom" value="${cmd.startTime}" placeholder="${message(code: 'facilityStatistic.index.message29')}"/>
                            </li>
                            <li>
                                <input class="span1 filter-time" type="text" name="endTime" id="timeConditionTo" value="${cmd.endTime}" placeholder="${message(code: 'facilityStatistic.index.message30')}" />
                            </li>
                            <li>
                                <g:select name="weekdays" from="${1..7}" value="${cmd.weekdays}" multiple="multiple"
                                          valueMessagePrefix="time.weekDay"/>
                            </li>
                            <li class="pull-right">
                                <button id="filterSubmit" tabindex="3" class="btn" type="submit"><g:message code="button.filter.label"/></button>
                            </li>
                        </ul>
                    </div>
                </fieldset>
            </form>

            <div class="clear"></div>
            <div class="row">
                <div class="span12 stats">
                    <div class="kpi span2">
                        <label><g:message code="facilityStatistic.index.message4"/></label>
                        <h1 rel="tooltip" title="${message(code: 'adminStatistics.income.numberOfBookings')}">
                            ${result.total_occupancy}%
                        </h1>
                        <label rel="tooltip" title="${message(code: 'facilityStatistic.index.message15')}" class="footer">
                            ~${result.total_occupancy_na_not_included}%
                        </label>
                    </div>
                    <div class="kpi span2">
                        <label><g:message code="default.booking.plural"/></label>
                        <h1>${result.total_bookings}<g:message code="unit.st"/></h1>
                        <label class="footer">~${result.avg_bookings}<g:message code="facilityStatistic.index.message16"/></label>
                    </div>

                    <!--
                    <div class="kpi span2">
                        <label>Tider</label>
                        <h1>${result.total_slots}st</h1>
                        <label class="footer">23st/dag</label>
                    </div>
                    -->
                    <div class="kpi span2">
                        <label><g:message code="facilityStatistic.index.message6"/></label>
                        <h1>${result.total_standalone}<g:message code="unit.st"/></h1>
                        <label class="footer">~${result.avg_standalone}<g:message code="facilityStatistic.index.message16"/></label>
                    </div>

                    <div class="kpi span2">
                        <label><g:message code="facilityStatistic.index.message31"/></label>
                        <h1>${result.total_online}<g:message code="unit.st"/></h1>
                        <label class="footer">~${result.avg_online}<g:message code="facilityStatistic.index.message16"/></label>
                    </div>
                    <div class="kpi span2">
                        <label><g:message code="facilityStatistic.index.message8"/></label>
                        <div id="booking_distribution" style="padding-top:5px;margin:0;position:relative;top:0;height:55px;width:100%"></div>
                    </div>
                </div>
            </div>
            <div id="chart"></div>
            <div>
                <table id="datatable" class="table" width="100%">
                    <thead>
                    <tr>
                        <th><g:message code="default.date.label"/></th>
                        <th><g:message code="facilityStatistic.index.message10"/></th>
                        <th><g:message code="facilityStatistic.index.message11"/></th>
                        <th><g:message code="facilityStatistic.index.message31"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${result.rows}">
                        <tr>
                            <th><g:formatDate date="${it.date}" formatName="date.format.dateOnly"/></th>
                            <td>${it.num_available}</td>
                            <td>${it.num_bookings} <span>(${(int)it.num_occupancy}%)</span></td>
                            <td>${it.num_online}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </g:if>
        <g:else><g:message code="facility.onlyLocal"/></g:else>
    </div>
</div>

<r:script>
    $(document).ready(function() {

    $("[rel=tooltip]").tooltip();

    $("#courtIds").multiselect({
        create: function() {$(this).next().width(210);},
        classes: "multi",
        minWidth: 200,
        checkAllText: "${message(code: 'adminFacilityCourts.index.bookingAvailability.allUsers')}",
        uncheckAllText: "${message(code: 'facilityStatistic.index.message19')}",
        noneSelectedText: "${message(code: 'default.multiselect.noneSelectedText')}",
        selectedText: "${message(code: 'court.multiselect.selectedText')}"
    });

    $("#weekdays").multiselect({
        create: function() {$(this).next().width(210);},
        classes: "multi",
        minWidth: 200,
        checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
        uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
        noneSelectedText: "${message(code: 'facilitySubscription.index.weekdaySelect.noneSelectedText')}",
        selectedText: "${message(code: 'facilitySubscription.index.weekdaySelect.selectedText')}"
    });

    $(".filter-time").addTimePicker({
        hourText: '${message(code: 'default.timepicker.hour')}',
        minuteText: '${message(code: 'default.timepicker.minute')}'
    });

    $('#filterForm').on('submit', function(e) {
        $("#filterSubmit").attr("disabled", "true")
    });


    Highcharts.visualize = function(table, options) {
            // the categories
            options.xAxis.categories = [];
            $('tbody th', table).each( function(i) {

                var date = Date.parse(this.innerHTML);
                //console.log(this.innerHTML + " => " + Date.parse(this.innerHTML))
                options.xAxis.categories.push(date);
            });

            // the data series
            options.series = [];
            $('tr', table).each( function(i) {
                var tr = this;
                $('th, td', tr).each( function(j) {
                    if (j > 0) { // skip first column
                        if (i == 0) { // get the name and init the series
                            t = (j - 1 == 2 ? "spline":"column");
                            options.series = [
                                {
                                    type: "column",
                                    name: "${message(code: 'facilityStatistic.index.message10')}",
                                    color: '#D5D5D5',
                                    data: [],
                                    yAxis: 0

                                },
                                {
                                    type: "column",
                                    name: "${message(code: 'facilityStatistic.index.message11')}",
                                    color: '#1fb3de',
                                    data: [],
                                    yAxis: 0

                                },
                                {
                                    type: "spline",
                                    name: "${message(code: 'adminStatistics.income.online')}",
                                    color: '#39531E',
                                    data: [],
                                    yAxis: 1

                                }
                            ]

                        } else { // add values
                            options.series[j - 1].data.push(parseFloat(this.innerHTML));
                        }
                    }
                });
            });

            var chart = new Highcharts.Chart(options);
        };

        var table = document.getElementById('datatable'),
        options = {
            chart: {
                renderTo: 'chart',
                type: 'column'
            },
            title: {
            enable: false,

                text: ''
            },
            xAxis: {
                reversed: true,
                type: 'date',
                dateTimeLabelFormats: {
                    day: '%e'
                },
                labels: {
                    rotation: -45,
                    align: 'right',
                    step: ${g.forJavaScript(data: (int) (result.rows.size() / 15))},
                    formatter: function() {
                        return this.value.toString("dd/MM");
                    }
                }
            },
            yAxis: [
                {
                    title: {
                        text: "${message(code: 'default.quantity.label')}"
                    },
                    stackLabels: {
                        enabled: false
                    }
                },
                {
                    title: {
                        text: "${message(code: 'facilityStatistic.index.message23')}"
                    },
                    min: 0
                }
            ],

            tooltip: {
                formatter: function() {
                    return '<b>'+ this.series.name +'</b><br/>'+
                        this.y +'st '+ this.x.toString("dd/MM");
                }
            },
            plotOptions: {
                column: {
                    stacking: 'normal'
                },
                series: {
                    pointPadding: 0,
                    groupPadding: 0.05
                }
            },
            credits: { enabled: false }
        };

        Highcharts.visualize(table, options);


        var bookingDistributionChart = new Highcharts.Chart({
            colors: ['#1fb3de', '#993b3b', '#8a2b2b', '#82ab12', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4'],
            exporting: {
                enabled: false
            },
            credits: { enabled: false },
            chart: {
                margin: [0, 0, 0, 0],
                spacingTop: 0,
                spacingBottom: 0,
                spacingLeft: 0,
                spacingRight: 0,
                renderTo: 'booking_distribution',
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false
            },
            title: {
                text: ''
            },
            tooltip: {
        	    pointFormat: '{series.name}: <b>{point.percentage}%</b>',
            	percentageDecimals: 2
            },
            plotOptions: {
                pie: {


                    size:'100%',
                    allowPointSelect: false,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: false,
                        color: '#000000',
                        connectorColor: '#000000',
                        formatter: function() {
                            return '<b>'+ this.point.name +'</b>: '+ this.percentage +'adw %';
                        }
                    }
                }
            },
            series: [{
                pointPadding: 0,
                groupPadding: 0,
                type: 'pie',
                name: "${message(code: 'default.total.label')}",
                data: [
                    ['${message(code: 'subscription.label')}',  ${g.forJavaScript(data: result.percentage_subscription)}],
                    ['${message(code: 'bookingGroup.name.TRAINING')}',       ${g.forJavaScript(data: result.percentage_training)}],
                    ['${message(code: 'bookingGroup.name.COMPETITION')}',    ${g.forJavaScript(data: result.percentage_competition)}],
                    ['${message(code: 'facilityStatistic.index.message28')}',     ${g.forJavaScript(data: result.percentage_standalone)}],
                    ['${message(code: 'adminStatistics.activities.label')}',     ${g.forJavaScript(data: result.percentage_activity)}]
                ]
            }]
        });



    });

</r:script>
</body>
</html>
