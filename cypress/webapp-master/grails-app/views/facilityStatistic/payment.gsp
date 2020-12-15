<%@ page import="com.matchi.excel.ExcelExportManager; com.matchi.orders.Order; org.joda.time.LocalDate; com.matchi.payment.PaymentMethod; org.joda.time.DateTime; com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="facilityStatistic.payment.message18"/></title>
    <r:require modules="select2,highcharts,jquery-multiselect-widget,daterangepicker,jquery-timepicker"/>
    <r:script>
    $(document).ready(function() {
        $("#methods").multiselect({
            create: function() {$(this).next().width(210);},
            classes: "multi",
            minWidth: 200,
            checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'default.multiselect.noneSelectedText')}",
            selectedText: "${message(code: 'paymentMethod.multiselect.selectedText')}"
        });

        $("#courtIds").multiselect({
            create: function() {$(this).next().width(210);},
            classes: "multi",
            minWidth: 200,
            checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'court.multiselect.noneSelectedText')}",
            selectedText: "${message(code: 'court.multiselect.selectedText')}"
        });

        $("#incomeTypes").multiselect({
            create: function() {$(this).next().width(210);},
            classes: "multi",
            minWidth: 200,
            checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'incomeType.multiselect.noneSelectedText')}",
            selectedText: "${message(code: 'incomeType.multiselect.selectedText')}"
        });

        $(".filter-time").addTimePicker({
            hourText: '${message(code: 'default.timepicker.hour')}',
            minuteText: '${message(code: 'default.timepicker.minute')}'
        });

        $('#filterForm').on('submit', function(e) {
            $("#filterSubmit").attr("disabled", "true")

        });

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
                reversed: false,
                categories: [
        <g:each in="${result.dates.collect() { it.toString('yyyy-MM-dd') }}">
            Date.parse('${g.forJavaScript(data: it)}'),
        </g:each>
        ],
        type: 'date',
        dateTimeLabelFormats: {
            day: '%e'
        },
        labels: {
            rotation: -45,
            align: 'right',
            step: ${g.forJavaScript(data: (int) (result.dates.size() / 15))},
                    formatter: function() {
                        return this.value.toString("dd/MM");
                    }
                }
            },
            yAxis: [
                {
                    title: {
                        text: "${message(code: 'adminStatistics.index.income')}"
                    },
                    stackLabels: {
                        enabled: false
                    },
                    min: 0

                },
                {
                    title: {
                        text: "${message(code: 'payment.method.CREDIT_CARD')}"
                    },
                    stackLabels: {
                        enabled: false
                    },
                    min: 0

                },
                {
                    title: {
                        text: "${message(code: 'payment.method.COUPON')}"
                    },
                    min: 0,
                    stackLabels: {
                        enabled: false
                    }


                }
            ],

            tooltip: {
                formatter: function() {
                    return '<b>'+ this.series.name +'</b><br/>'+
                        this.y + ' '+ this.x.toString("dd/MM");
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
            series: [{
                yAxis: 0,
                color: '#1fb3de',
                name: "${message(code: 'adminStatistics.index.income')}",

                data: [
        <g:each in="${result.dates}" var="date">
            <g:set var="row" value="${result.cache.get(date.toString("yyyy-MM-dd"))}"/>
            <g:if test="${row}">
                ${g.forJavaScript(data: row.amount_online/100)},
            </g:if>
            <g:else>
                0,
            </g:else>
        </g:each>
        ]
    },
    {
        yAxis: 1,
        color: '#82ab12',
        name: "${message(code: 'payment.method.CREDIT_CARD')}",
        type: 'spline',
        visible: false,
        data: [
        <g:each in="${result.dates}" var="date">
            <g:set var="row" value="${result.cache.get(date.toString("yyyy-MM-dd"))}"/>
            <g:if test="${row}">${g.forJavaScript(data: row.num_creditcard)},</g:if>
            <g:else>0,</g:else>
        </g:each>
        ]
    },
    {
        yAxis: 2,
        color: '#39531E',
        name: "${message(code: 'payment.method.COUPON')}",
        type: 'spline',

        visible: false,
        data: [
        <g:each in="${result.dates}" var="date">
            <g:set var="row" value="${result.cache.get(date.toString("yyyy-MM-dd"))}"/>
            <g:if test="${row}">${g.forJavaScript(data: row.num_coupon)},</g:if>
            <g:else>0,</g:else>
        </g:each>
        ]
    }
    ],
    credits: { enabled: false }
};

var chart = new Highcharts.Chart(options);

});

    </r:script>
</head>
<body>

<div id="container">
    <div class="content">
        <g:if test="${!facility?.isMasterFacility()}">
            <ul id="statistics-tab" class="nav nav-tabs">
                <li><g:link action="index" params="[start: start.toString('yyyy-MM-dd'), end: end.toString('yyyy-MM-dd')]"><g:message code="default.booking.plural"/></g:link></li>
                <li class="active"><g:link action="payment" params="[start: start.toString('yyyy-MM-dd'), end: end.toString('yyyy-MM-dd')]"><g:message code="adminStatistics.index.income"/></g:link></li>
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
                                <select id="methods" name="methods" multiple="true">
                                    <g:each in="[
                                            (PaymentMethod.COUPON.toString()) : message(code: 'payment.method.COUPON'),
                                            (PaymentMethod.GIFT_CARD.toString()) : message(code: 'payment.method.GIFT_CARD'),
                                            (PaymentMethod.CREDIT_CARD.toString()) : message(code: 'payment.method.CREDIT_CARD')
                                    ]">
                                        <option value="${it.key}" ${selectedMethods.contains(it.key)?"selected":""}>${it.value}</option>
                                    </g:each>
                                </select>
                            </li>
                            <li>
                                <select id="courtIds" name="courtIds" multiple="true">
                                    <g:each in="${courts}">
                                        <option value="${it.id}" ${selectedCourtIds.contains(it.id)?"selected":""}>${it.name}</option>
                                    </g:each>
                                </select>
                            </li>
                            <li>
                                <g:set var="incomeTypesList"
                                       value="[(Order.Article.ACTIVITY.toString()): g.message(code: 'statistic.income.ACTIVITY'),
                                               (Order.Article.BOOKING.toString()) : g.message(code: 'statistic.income.BOOKING'),
                                               (Order.Article.COUPON.toString())  : g.message(code: 'statistic.income.COUPON')]"/>
                                <g:ifFacilityPropertyEnabled
                                        name="${com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_MEMBERSHIP_REQUEST_PAYMENT.name()}">
                                    <g:set var="incomeTypesList"
                                           value="${incomeTypesList + [(Order.Article.MEMBERSHIP.toString()): g.message(code: 'statistic.income.MEMBERSHIP')]}"/>
                                </g:ifFacilityPropertyEnabled>
                                <g:ifFacilityPropertyEnabled
                                        name="${com.matchi.FacilityProperty.FacilityPropertyKey.FEATURE_FACILITY_DYNAMIC_FORMS.name()}">
                                    <g:set var="incomeTypesList"
                                           value="${incomeTypesList + [(Order.Article.FORM_SUBMISSION.toString()): g.message(code: 'statistic.income.FORM_SUBMISSION')]}"/>
                                </g:ifFacilityPropertyEnabled>
                                <select id="incomeTypes" name="incomeTypes" multiple="true">
                                    <g:each in="${incomeTypesList}">
                                        <option value="${it.key}" ${selectedIncomeTypes.contains(it.key) ? "selected" : ""}>${it.value}</option>
                                    </g:each>
                                </select>
                            </li>
                            <li>
                                <g:textField class="span1 filter-time" name="startTime" value="${params.startTime}" placeholder="${message(code: 'facilityStatistic.payment.message27')}"/>
                            </li>
                            <li>
                                <g:textField class="span1 filter-time" name="endTime" value="${params.endTime}" placeholder="${message(code: 'facilityStatistic.payment.message28')}" />
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
                <div class="span12">
                    <div class="kpi span3">
                        <label><g:message code="adminStatistics.index.income"/></label>
                        <h1><g:formatMoney value="${result.total_amount}" forceZero="true"/></h1>
                        <label class="footer">
                            ~${result.avg_amount}
                            <g:message code="facilityStatistic.payment.curPerDay" args="[currentFacilityCurrency()]"/>
                        </label>
                    </div>
                    <div class="kpi span2">
                        <label><g:message code="payment.method.CREDIT_CARD"/></label>
                        <h1>${result.total_num_creditcard}<g:message code="unit.st"/></h1>
                        <label class="footer">~${result.avg_num_creditcard}<g:message code="facilityStatistic.payment.pcsPerDay"/></label>
                    </div>
                    <div class="kpi span2">
                        <label><g:message code="facilityStatistic.payment.coupons"/></label>
                        <h1>${result.total_num_coupons}<g:message code="unit.st"/></h1>
                        <label class="footer">~${result.avg_num_coupon}<g:message code="facilityStatistic.payment.pcsPerDay"/></label>
                    </div>
                    <div class="kpi span2">
                        <label><g:message code="facilityStatistic.payment.message11"/></label>
                        <h1>${result.total_cancelled}<g:message code="unit.st"/></h1>
                        <label class="footer"></label>
                    </div>
                    <div class="kpi span2">
                        <label><g:message code="default.total.label"/></label>
                        <h1>${result.total_num_coupons+result.total_num_creditcard}<g:message code="unit.st"/></h1>
                        <label class="footer"></label>
                    </div>

                </div>
            </div>
            <div id="chart"></div>

            <div class="action-bar">
                <div class="btn-toolbar-left">
                    <div class="btn-group">
                        <button class="btn btn-inverse dropdown-toggle" data-toggle="dropdown">
                            <g:message code="button.actions.label"/>
                            <span class="caret"></span>
                        </button>
                        <ul class="dropdown-menu">
                            <li><a href="javascript:void(0)" onclick="submitFormTo('#filterForm', '<g:createLink action="export" />');">
                                <g:message code="button.export.label"/></a></li>
                        </ul>
                    </div>
                </div>
            </div>
            <div>
                <table id="datatable" class="table" width="100%">
                    <thead>
                    <tr>
                        <th><g:message code="facilityStatistic.payment.message13"/></th>
                        <th></th>
                        <th><g:message code="facilityStatistic.payment.message14"/></th>
                        <th><g:message code="user.label.plural"/></th>
                        <th><g:message code="facilityStatistic.payment.message16"/></th>
                        <th><g:message code="facilityStatistic.payment.message17"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:set var="currentDate" value="${null}"/>
                    <g:each in="${transactions}">
                        <tr>

                            <th>
                                <g:if test="${!new LocalDate(it.date).equals(currentDate)}">
                                    <g:formatDate date="${it.date}" formatName="date.format.dateOnly"/>
                                </g:if>
                            </th>

                            <th><g:formatDate date="${it.date}" format="HH:mm"/></th>
                            <td><g:message code="articleType.${it.article}"/> (${it.method})</td>
                            <td>${it.customer?.fullName()}</td>
                            <td>${it.info?.encodeAsHTML()}</td>
                            <td>
                                ${it.amount}
                                <g:if test="${it.order_status == Order.Status.ANNULLED.name()}">
                                    <i class="icon-exclamation-sign" title="${message(code: 'facilityStatistic.payment.lateCancellation')}"></i>
                                </g:if>
                            </td>
                        </tr>

                        <g:set var="currentDate" value="${new LocalDate(it.date)}"/>
                    </g:each>

                    </tbody>
                </table>
                (totalt: ${transactions.size()})

            </div>
        </g:if>
        <g:else><g:message code="facility.onlyLocal"/></g:else>
    </div>
</div>
</body>
</html>
