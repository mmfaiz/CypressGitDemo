<%@ page import="com.matchi.Facility; com.matchi.FacilityContract" %>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title>MATCHi <g:message code="adminHome.index.title"/></title>
    <r:require modules="select2,highcharts,jquery-multiselect-widget,daterangepicker,matchi-selectpicker,matchi-selectall"/>
</head>
<body>

<div class="container content-container">
    <ol class="breadcrumb">
        <li><g:message code="adminStatistics.index.statistics"/></li>
    </ol>

    <div class="panel panel-admin">
        <div class="panel-heading no-padding">
            <div class="tabs tabs-style-underline">
                <nav>
                    <ul>
                        <li class="active tab-current">
                            <g:link action="index">
                                <i class="fas fa-list"></i>
                                <span><g:message code="adminStatistics.index.stats"/></span>
                            </g:link>
                        </li>
                        <li class="">
                            <g:link action="income">
                                <i class="fa fa-money"></i>
                                <span><g:message code="adminStatistics.index.income"/></span>
                            </g:link>
                        </li>
                    </ul>
                </nav>
            </div>
        </div>

        <g:form id="filterForm" class="form no-bottom-padding">
            <div class="well">
                <div class="row">
                    <div class="form-group col-sm-3 no-margin">
                        <div tabindex="2" onkeydown="test(this)" id="daterange" class="daterange form-control" style="background-color: #fff;margin: 0 10px 0 0;">
                            <i class="icon-calendar icon-large"></i>
                            <span ><g:formatDate format="MMMM d, yyyy"  date="${start.toDate()}" locale="sv"/> - <g:formatDate format="MMMM d, yyyy"  date="${end.toDate()}" locale="sv"/></span> <strong style="margin-top:6px" class="caret"></strong>
                        </div>

                        <input name="start" id="rangestart" class="span8" type="hidden" value="<g:formatDate format="yyyy-MM-dd"  date="${start.toDate()}" locale="sv"/>">
                        <input name="end" id="rangeend" class="span8" type="hidden" value="<g:formatDate format="yyyy-MM-dd"  date="${end.toDate()}" locale="sv"/>">
                    </div>
                    <div class="form-group col-sm-3 no-margin">
                        <g:select from="${facilities}" name="facilityIds" value="${selectedFacilities}"
                                  optionKey="id" optionValue="${{it.name +' ('+it.getFortnoxCustomerId()+')'}}" multiple="multiple"
                                  title="${message(code: 'adminStatistics.index.facilityIds.title')}"/>
                    </div>
                    <div class="form-group col-sm-2 no-margin">
                        <g:textField name="fortnoxId" class="form-control" placeholder="fortnoxId" value="${cmd.fortnoxId}"/>
                    </div>
                    <div class="form-group col-sm-2 no-margin">
                        <g:select from="${grails.util.Holders.config.matchi.settings.currency.keySet()}" name="currency" class="form-control"
                                  value="${cmd.currency}" title="${message(code: 'adminStatistics.index.facilityIds.title')}"/>
                    </div>
                    <div class="form-group col-sm-2 no-margin">
                        <button id="filterSubmit" tabindex="3" class="btn" style="margin-right: 20px" type="submit"><g:message code="button.update.label"/></button>
                    </div>
                </div>
            </div>
            <div class="well">
                <g:actionSubmit value="Get special numbers" action="specialValues" class="btn btn-success" />

                <g:link class="btn btn-info pull-right" action="invoice" params="[facilityIds: cmd.facilityIds, start: start, end: end, currency:cmd.currency]"
                        onclick="return confirm('${message(code: 'adminStatistics.index.invoice.confirm')}')">
                    ${cmd.facilityIds?.size() > 1 || !cmd.facilityIds ? message(code: 'adminStatistics.index.createInvoices') : message(code: 'adminStatistics.index.createInvoice')}
                </g:link>
            </div>
        </g:form>

        <div class="panel-body">
            <div class="kpi col-sm-4 text-center">
                <label><g:message code="adminStatistics.index.revenue"/></label>
                <h1 class="no-top-margin"><g:formatNumber format="##,###" number="${result.values().sum(0) { it.getTotalRevenue() }}"/></h1>
            </div>
            <div class="kpi col-sm-4 text-center">
                <label><g:message code="adminStatistics.index.charges"/></label>
                <h1 class="no-top-margin">
                    <g:formatNumber format="##,###" number="${result.values().sum(0) { it.getTotalFees() }}"/>
                </h1>
            </div>
            <div class="kpi col-sm-4 text-center">
                <label><g:message code="adminStatistics.index.toPay"/></label>
                <h1 class="no-top-margin"><g:formatNumber format="##,###" number="${result.values().sum(0) { it.getTotalMonthlyProfit() }}"/> </h1>
                <!-- <label class="footer">Krediterat <g:formatNumber number="${1}"/>kr</label> -->
            </div>

            <hr>

            <g:each in="${selectedFacilities}" var="facility">
                <g:set var="summary" value="${result[facility.id]}"/>
                <g:set var="contract" value="${facility.getActiveContract(start.toDate())}"/>
                <g:set var="totalMonthlyProfit" value="${summary.totalMonthlyProfit}"/>

                <div class="col-sm-12 vertical-margin20">
                    <h3>
                        ${facility.name}
                        <span class="pull-right">
                            <g:formatNumber format="##,###.##" minFractionDigits="0" maxFractionDigits="2" number="${summary.getTotalRevenue()}" />
                            /
                            <g:formatNumber format="##,###.##" minFractionDigits="0" maxFractionDigits="2" number="${summary.getTotalFees()}" />
                        </span>
                    </h3>

                    <table class="table" border="0">
                        <thead>
                        <tr>
                            <th width="150"><g:message code="adminStatistics.index.paymentMethod"/></th>
                            <th width="400"><g:message code="default.article.label"/></th>
                            <th><g:message code="default.price.label"/></th>
                            <th><g:message code="default.quantity.label"/></th>
                            <th width="200"><g:message code="adminStatistics.index.total"/> (<g:currentFacilityCurrency facility="${facility}"/>)</th>
                        </tr>
                        </thead>
                        <g:each in="${summary.entries}" var="row">
                            <tr>
                                <td>${row.type}</td>
                                <td><g:message code="articleType.${row.article}"/></td>
                                <td><g:formatNumber minFractionDigits="0" maxFractionDigits="2" format="##,###.##" number="${row.price}"/></td>
                                <td>${row.num}</td>
                                <td><g:formatNumber minFractionDigits="0" maxFractionDigits="2" format="##,###.##" number="${row.num * row.revenue}"/></td>
                            </tr>
                        </g:each>

                        <g:each in="${summary.promoCodeDiscounts}" var="promoCodeEntry">
                            <tr>
                                <td>promo code</td>
                                <td>${promoCodeEntry.type}</td>
                                <td>n/a</td>
                                <td>${promoCodeEntry.count}</td>
                                <td>
                                    - <g:formatNumber format="##,###.##" minFractionDigits="0"
                                                      maxFractionDigits="2" number="${promoCodeEntry.total}"/>
                                </td>
                            </tr>
                        </g:each>

                        <tr style="background-color: #f1f1f1">
                            <td colspan="3"><strong><g:message code="adminStatistics.index.income"/></strong></td>
                            <td>${summary.entries.sum(0) { it.num } + summary.promoCodeDiscounts.sum(0) { it.count }}</td>
                            <td>
                                <strong><g:formatNumber format="##,###.##" minFractionDigits="0" maxFractionDigits="2" number="${summary.getTotalRevenue()}" /></strong>
                            </td>
                        </tr>

                        <tr>
                            <td colspan="5">&nbsp;</td>
                        </tr>

                        <g:if test="${summary.hasContract()}">
                            <tr>
                                <td colspan="3"><g:message code="adminStatistics.index.fixedFees"/></td>
                                <td>&nbsp;</td>
                                <td> - <g:formatNumber format="##,###.##" minFractionDigits="0" maxFractionDigits="2" number="${summary.getFixedFee()}" /></td>
                            </tr>

                            <g:each in="${summary.contractItems}" var="contractItem">
                                <tr>
                                    <td colspan="3">${contractItem.description.encodeAsHTML()}</td>
                                    <td>&nbsp;</td>
                                    <td>
                                        - <g:formatNumber number="${contractItem.price}" format="##,###.##"
                                                          minFractionDigits="0" maxFractionDigits="2"/>
                                    </td>
                                </tr>
                            </g:each>

                            <g:if test="${summary.getMinimalFeeVariableFeesEntries().size() > 0}">
                                <tr>
                                    <td colspan="3"><g:message code="adminStatistics.index.variableMediationFee"/> (${summary.contract?.variableMediationFee})</td>
                                    <td>${summary.getMinimalFeeVariableFeesEntries().sum(0) { it.num }}</td>
                                    <td>
                                        - <g:formatNumber format="##,###.##" minFractionDigits="0"
                                                          maxFractionDigits="2" number="${summary.getMinimalFeeVariableFeesEntries().sum(0) { summary.getVariableFee(it) }}" />
                                    </td>
                                </tr>
                            </g:if>

                            <g:if test="${summary.getPercentageVariableFeesEntries().size() > 0 && summary.contract?.variableMediationFeePercentage}">
                                <tr>
                                    <td colspan="3">
                                        <g:message code="adminStatistics.index.variableMediationFee"/>
                                        (${summary.contract.variableMediationFeePercentage}%<g:if test="${summary.contract.mediationFeeMode == FacilityContract.MediationFeeMode.AND}"> + ${summary.contract.variableMediationFee}</g:if>)
                                    </td>
                                    <td>${summary.getPercentageVariableFeesEntries().sum(0) { it.num }}</td>
                                    <td>
                                        - <g:formatNumber format="##,###.##" minFractionDigits="0"
                                                          maxFractionDigits="2"
                                                          number="${summary.getPercentageVariableFeesEntries().sum(0) { summary.getVariableFee(it) }}" />

                                    </td>
                                </tr>
                            </g:if>

                            <g:each in="${summary.fees}" var="fee">
                                <tr>
                                    <td colspan="3">
                                        ${fee.type}
                                    </td>
                                    <td>${fee.count}</td>
                                    <td>
                                        - <g:formatNumber format="##,###.##" minFractionDigits="0"
                                                          maxFractionDigits="2" number="${fee.getTotal()}"/>
                                    </td>
                                </tr>
                            </g:each>

                            <g:each in="${summary.couponEntries}" var="couponEntry">
                                <tr>
                                    <td colspan="3">
                                        <g:message code="adminStatistics.index.${couponEntry.type}"
                                                   args='[couponEntry.fee, message(code: "adminStatistics.index.feeType.${couponEntry.feeType}")]'/>
                                    </td>
                                    <td>${couponEntry.count}</td>
                                    <td>
                                        - <g:formatNumber format="##,###.##" minFractionDigits="0"
                                                          maxFractionDigits="2" number="${couponEntry.totalFee}"/>
                                    </td>
                                </tr>
                            </g:each>

                            <g:each in="${summary.giftCardEntries}" var="giftCardEntry">
                                <tr>
                                    <td colspan="3">
                                        <g:message code="adminStatistics.index.${giftCardEntry.type}"
                                                   args='[giftCardEntry.fee, message(code: "adminStatistics.index.feeType.${giftCardEntry.feeType}")]'/>
                                    </td>
                                    <td>${giftCardEntry.count}</td>
                                    <td>
                                        - <g:formatNumber format="##,###.##" minFractionDigits="0"
                                                          maxFractionDigits="2" number="${giftCardEntry.totalFee}"/>
                                    </td>
                                </tr>
                            </g:each>

                            <tr style="background-color: #f1f1f1">
                                <th colspan="4"><strong><g:message code="adminStatistics.index.charges"/></strong></th>

                                <td>
                                    <strong> - <g:formatNumber format="##,###.##" minFractionDigits="0" maxFractionDigits="2"
                                                          number="${summary.getTotalFees()}" /></strong>
                                </td>
                            </tr>

                            <tr>
                                <td colspan="5">&nbsp;</td>
                            </tr>

                            <tr>
                                <td colspan="4"><g:message code="adminStatistics.index.income"/></td>
                                <td> + <g:formatNumber format="##,###.##" minFractionDigits="0" maxFractionDigits="2" number="${summary.getTotalRevenue()}" /></td>
                            </tr>

                            <tr>
                                <td colspan="4"><g:message code="adminStatistics.index.charges"/></td>
                                <td> - <g:formatNumber format="##,###.##" minFractionDigits="0" maxFractionDigits="2" number="${summary.getTotalFees()}" /></td>
                            </tr>

                            <tr style="background-color: #f1f1f1">
                                <th colspan="4"><strong><g:message code="adminStatistics.index.results"/></strong></th>
                                <td>
                                    <strong><g:formatNumber format="##,###.##" minFractionDigits="0" maxFractionDigits="2" number="${totalMonthlyProfit}" /></strong><br>
                                    <g:message code="adminStatistics.index.results.${((totalMonthlyProfit) > 0 ? 'toPay' : 'toBill')}"/>
                                </td>
                            </tr>

                        </g:if>
                        <g:else>
                            <tr>
                                <td colspan="5"><i><g:message code="adminStatistics.index.noContract"/></i></td>
                            </tr>
                        </g:else>
                    </table>

                    <g:if test="${facility.isMasterFacility()}">
                        <table style="width: 100%; margin-top: 40px;">
                            <thead>
                            <tr>
                                <th width="400"><g:message code="adminStatistics.couponTypePerFacility.label" args="${[facility.name]}"/></th>
                                <th width="250"><g:message code="adminStatistics.usedAtVenue.label"/></th>
                                <th><g:message code="default.quantity.label"/></th>
                                <th width="200"><g:message code="adminStatistics.index.total"/> (<g:currentFacilityCurrency facility="${facility}"/>)</th>
                            </tr>
                            </thead>
                            <g:each in="${summary.detailedOfferEntries}" var="couponEntry">
                                <tr>
                                    <td>
                                        <g:message code="adminStatistics.index.${couponEntry.type}"
                                                   args='[couponEntry.fee, message(code: "adminStatistics.index.feeType.${couponEntry.feeType}")]'/>
                                    </td>
                                    <td>${couponEntry.facility.name}</td>
                                    <td>${couponEntry.count}</td>
                                    <td>
                                        <g:formatNumber format="##,###.##" minFractionDigits="0"
                                                        maxFractionDigits="2" number="${couponEntry.totalFee}"/>
                                    </td>
                                </tr>
                            </g:each>
                        </table>
                    </g:if>

                    <g:if test="${facility.isMasterFacility()}">
                        <table style="width: 100%; margin-top: 40px;">
                            <thead>
                            <tr>
                                <th width=""><g:message code="adminStatistics.purchasedGlobalMembershipsBelongingToFacility.label" args="${[facility.name]}"/></th>
                                <th width="100"><g:message code="default.quantity.label"/></th>
                                <th width="200"><g:message code="adminStatistics.index.total"/> (<g:currentFacilityCurrency facility="${facility}"/>)</th>
                            </tr>
                            </thead>
                            <g:each in="${summary.membershipEntries}" var="membershipEntry">
                                <tr>
                                    <td>${membershipEntry.facility?.name ?: "Unknown"}</td>
                                    <td>${membershipEntry.count}</td>
                                    <td>
                                        <g:formatNumber format="##,###.##" minFractionDigits="0"
                                                        maxFractionDigits="2" number="${membershipEntry.totalFee}"/>
                                    </td>
                                </tr>
                            </g:each>
                        </table>
                    </g:if>
                </div>
            </g:each>
        </div>
    </div>
</div>

<r:script>
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
            $('#daterange').find('span').html(start.toString('MMMM d, yyyy') + ' - ' + end.toString('MMMM d, yyyy'));
            $('#filterForm').submit();
        }
    );

    $("#facilityIds").allselectpicker({
        selectAllText: "${message(code: 'default.multiselect.checkAllText')}",
        countSelectedText: "${message(code: 'facility.allselectpicker.countSelectedText')}",
        selectedTextFormat: 'count'
    });
</r:script>

</body>
</html>
