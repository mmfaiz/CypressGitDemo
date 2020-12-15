<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilitySlotRedeem.index.title"/></title>
    <r:require modules="daterangepicker"/>
    <r:script>
        $(function() {
            $("#redeem-search-input").focus();

            $('#daterange').daterangepicker(
                {
                    ranges: {
                        '${message(code: 'default.dateRangePicker.currentWeek')}': [(Date.today().getDay()==1?Date.today():Date.today().moveToDayOfWeek(1, -1)), Date.today().moveToDayOfWeek(0)],
                        '${message(code: 'default.dateRangePicker.lastWeek')}': [(Date.today().getDay()==1?Date.today():Date.today().add({ weeks: -1 }).moveToDayOfWeek(1, -1)), Date.today().add({ weeks: -1 }).moveToDayOfWeek(0)],
                        '${message(code: 'default.dateRangePicker.currentMonth')}': [Date.today().moveToFirstDayOfMonth(), Date.today().moveToLastDayOfMonth()],
                        '${message(code: 'default.dateRangePicker.lastMonth')}': [Date.today().moveToFirstDayOfMonth().add({ months: -1 }), Date.today().moveToFirstDayOfMonth().add({ days: -1 })]
                    },

                    format: "yyyy-MM-dd",
                    startDate: "${g.forJavaScript(data: filter.start.toString('yyyy-MM-dd'))}",
                    endDate: "${g.forJavaScript(data: filter.end.toString('yyyy-MM-dd'))}",
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
</head>

<body>
<ul class="breadcrumb">
    <li><g:message code="facilitySlotRedeem.index.total" args="[redeems.totalCount]"/></li>
</ul>

<g:if test="${!facility?.isMasterFacility()}">
    ${flash.error}

    <ul class="nav nav-tabs">
        <li><g:link controller="facilitySubscription" action="index"><g:message code="subscription.label"/></g:link></li>
        <li class="active"><g:link action="index"><g:message code="facilitySlotRedeem.index.title"/></g:link></li>
    </ul>

    <form method="GET" id="filterForm" class="form-search well" style="padding:12px 10px 4px 10px;">
        <fieldset>
            <div class="control-group">
                <ul class="inline filter-list">
                    <li>
                        <g:textField id="redeem-search-input" name="q" value="${filter.q}" style="width: 202px"
                                placeholder="${message(code: 'facilitySlotRedeem.index.q.placeholder')}"/>
                    </li>
                    <li>
                        <div id="daterange" class="daterange" style="background-color: #fff;margin: 0 10px 0 0px;">
                            <i class="icon-calendar icon-large"></i>
                            <span>
                                <g:formatDate formatName="date.format.daterangepicker" date="${filter.start.toDate()}"/>
                                -
                                <g:formatDate formatName="date.format.daterangepicker" date="${filter.end.toDate()}"/>
                            </span>
                            <b style="margin-top:6px" class="caret"></b>
                        </div>
                        <input name="start" id="rangestart" type="hidden"
                                value="${filter.start.toString('yyyy-MM-dd')}">
                        <input name="end" id="rangeend" type="hidden"
                                value="${filter.end.toString('yyyy-MM-dd')}">
                    </li>
                    <li class="pull-right">
                        <button id="filterSubmit" class="btn" type="submit">
                            <g:message code="button.filter.label"/>
                        </button>
                    </li>
                </ul>
            </div>
        </fieldset>
    </form>

    <g:form action="index" method="GET">
        <g:hiddenField name="q" value="${filter.q}"/>
        <g:hiddenField name="start" value="${filter.start.toString('yyyy-MM-dd')}"/>
        <g:hiddenField name="end" value="${filter.end.toString('yyyy-MM-dd')}"/>

        <table id="redeems-table" class="table table-striped table-bordered">
            <thead>
            <tr height="34">
                <g:sortableColumn width="40" titleKey="customer.number.label" property="c.number" params="${params}"/>
                <g:sortableColumn width="200" titleKey="customer.label" property="c.lastname,c.firstname,c.companyname" params="${params}"/>
                <th width="120"><g:message code="facilitySlotRedeem.index.type.label"/></th>
                <th><g:message code="facilitySlotRedeem.index.amountTicket.label"/></th>
                <g:sortableColumn width="150" titleKey="default.created.label" property="dateCreated" params="${params}"/>
            </tr>
            <thead>

            <tbody>
            <g:if test="${redeems}">
                <g:each in="${redeems}" var="redeem">
                    <tr>
                        <td>${redeem.slot.subscription?.customer?.number}</td>
                        <td>${redeem.slot.subscription?.customer?.fullName()}</td>
                        <g:if test="${redeem.coupon}">
                            <g:if test="${redeem.coupon.instanceOf(com.matchi.coupon.GiftCard)}">
                                <td><g:message code="offers.giftCard.label"/></td>
                                <td>${redeem.coupon.name.encodeAsHTML()}</td>
                            </g:if>
                            <g:else>
                                <td><g:message code="offers.coupon.label"/></td>
                                <td><g:message code="facilitySlotRedeem.index.type.coupon.amount"
                                        args="[redeem.coupon.name]" encodeAs="HTML"/></td>
                            </g:else>
                        </g:if>
                        <g:else>
                            <td><g:message code="facilitySlotRedeem.index.type.invoice"/></td>
                            <td><g:formatMoney value="${redeem.invoiceRow?.getTotalIncludingVAT()}"/></td>
                        </g:else>
                        <td><g:humanDateFormat  date="${redeem.dateCreated}"/>
                            <sec:ifAnyGranted roles="ROLE_ADMIN">
                                <g:if test="${!redeem.invoiceRow && !redeem.coupon}">
                                    <g:link class="btn btn-inverse pull-right" action="remove" params="[slotRedeemId: redeem.id]">X</g:link>
                                </g:if>
                            </sec:ifAnyGranted>
                        </td>
                    </tr>
                </g:each>
            </g:if>
            <g:else>
                <tr>
                    <td colspan="5"><i><g:message code="facilitySlotRedeem.index.results.empty"/></i></td>
                </tr>
            </g:else>
            </tbody>
        </table>

        <g:if test="${redeems.totalCount > filter.max}">
            <div class="text-center">
            <g:paginateTwitterBootstrap next="&raquo;" prev="&laquo;" class="pagination-centered"
                    maxsteps="0" max="${filter.max}" params="${params}"
                    action="index" total="${redeems.totalCount}"/>
            </div>
        </g:if>
    </g:form>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
</body>
</html>
