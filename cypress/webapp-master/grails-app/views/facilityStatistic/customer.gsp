<%@ page import="com.matchi.excel.ExcelExportManager; com.matchi.orders.Order; org.joda.time.LocalDate; com.matchi.payment.PaymentMethod; org.joda.time.DateTime; com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title>${ facility } - <g:message code="facilityStatistic.index.message13"/></title>
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
});

    </r:script>
</head>
<body>

<div id="container">
    <div class="content">
        <g:if test="${!facility?.isMasterFacility()}">
            <ul id="statistics-tab" class="nav nav-tabs">
                <li><g:link action="index" params="[start: start.toString('yyyy-MM-dd'), end: end.toString('yyyy-MM-dd')]"><g:message code="default.booking.plural"/></g:link></li>
                <li><g:link action="payment" params="[start: start.toString('yyyy-MM-dd'), end: end.toString('yyyy-MM-dd')]"><g:message code="adminStatistics.index.income"/></g:link></li>
                <li class="active"><g:link action="customer" params="[start: start.toString('yyyy-MM-dd'), end: end.toString('yyyy-MM-dd')]"><g:message code="adminStatistics.index.customer"/></g:link></li>
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
            <div id="chart"></div>
            <div>
                <table id="datatable" class="table" width="100%">
                    <thead>
                    <tr>
                        <th><g:message code="customer.number.label"/></th>
                        <th><g:message code="default.name.label"/></th>
                        <g:sortableColumn width="60" titleKey="adminStatistics.bookings.label" property="bookings" class="center-text" params="${params}"/>
                        <g:sortableColumn width="60" titleKey="adminStatistics.activities.label" property="activities" class="center-text" params="${params}"/>
                        <g:sortableColumn width="60" titleKey="adminStatistics.coupons.label" property="coupons" class="center-text" params="${params}"/>
                        <g:sortableColumn width="60" titleKey="adminStatistics.others.label" property="others" class="center-text" params="${params}"/>
                        <g:sortableColumn width="60" titleKey="adminStatistics.index.total" property="total" class="center-text" params="${params}"/>
                        <g:sortableColumn width="40" titleKey="adminStatistics.amount.label" property="amount" class="right-text" params="${params}"/>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${customers}">
                        <tr>
                            <td>${it.key.id}</td>
                            <td>${it.key}</td>
                            <td class="center-text">${it.value?.bookings ?: 0}</td>
                            <td class="center-text">${it.value?.activities ?: 0}</td>
                            <td class="center-text">${it.value?.coupons ?: 0}</td>
                            <td class="center-text">${it.value?.others ?: 0}</td>
                            <td class="center-text">${it.value?.total}</td>
                            <td class="right-text">${it.value?.amount ?: 0}</td>
                            <td>${facility.currency}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
                (total: ${customers.size()})
            </div>
        </g:if>
        <g:else><g:message code="facility.onlyLocal"/></g:else>
    </div>
</div>
</body>
</html>
