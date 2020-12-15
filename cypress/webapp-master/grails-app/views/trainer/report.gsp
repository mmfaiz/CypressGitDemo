<!doctype html>
<html>
<head>
    <meta name="layout" content="b3facility">
    <g:set var="entityName" value="${message(code: 'trainer.label', default: 'Trainer')}"/>
    <title><g:message code="default.lessonsreport.label"/></title>
    <r:require modules="bootstrap3-wysiwyg, select2, matchi-customerselect,daterangepicker, bootstrap-switch, matchi-selectall"/>
    <r:script>
        $("#booking-table").selectAll({  });
    </r:script>
</head>

<body>
    <div class="container vertical-padding20">

        <g:if test="${!facility?.isMasterFacility()}">
            <form method="GET" class="form well no-bottom-padding">
                <div class="row">
                    <div class="form-group col-sm-3 no-margin">
                        <select id="trainersSelect" name="trainerId" data-style="form-control">
                            <option value=""><g:message code="facilityPrivateLessons.trainers.all"/></option>
                            <g:each in="${trainers}">
                                <option value="${it.id}" ${trainerInstance?.id == it.id ? "selected" : ""}> ${it.fullName()} </option>
                            </g:each>
                        </select>
                    </div>
                    <div class="form-group col-sm-4 no-margin">
                        <div tabindex="2" onkeydown="test(this)" id="daterange" class="daterange form-control" style="background-color: #fff;margin: 0 10px 0 0;">
                            <i class="icon-calendar icon-large"></i>
                            <span ><g:formatDate format="MMMM d, yyyy"  date="${start.toDate()}" locale="sv"/> - <g:formatDate format="MMMM d, yyyy"  date="${end.toDate()}" locale="sv"/></span> <strong style="margin-top:6px" class="caret"></strong>
                        </div>

                        <input name="start" id="rangestart" class="span8" type="hidden" value="<g:formatDate format="yyyy-MM-dd"  date="${start.toDate()}" locale="sv"/>">
                        <input name="end" id="rangeend" class="span8" type="hidden" value="<g:formatDate format="yyyy-MM-dd"  date="${end.toDate()}" locale="sv"/>">
                    </div>
                    <div class="col-sm-2">
                        <select id="paidUnpaid" name="paid" data-style="form-control">
                            <option value=""><g:message code="facilityPrivateLessons.lessons.all"/></option>
                            <g:each in="[true, false]">
                                <option value="${it}"  ${cmd.paid == it ? "selected" : ""}>
                                    <g:message code="facilityPrivateLessons.lessons.${it ? 'paid' : 'unpaid'}"/>
                                </option>
                            </g:each>
                        </select>
                    </div>
                    <div class="form-group col-sm-2 no-top-margin pull-right">
                        <button type="submit" class="btn btn-block btn-info"><g:message code="button.filter.label"/></button>
                    </div>
                </div>
            </form>

            <div class="panel panel-default panel-admin">
                <g:if test="${!bookings}">
                    <tfoot>
                    <tr>
                        <td colspan="6" class="vertical-padding20">
                            <span class="text-muted text-md"><i class="ti-info-alt"></i><g:message code="default.noElements"/></span>
                        </td>
                    </tr>
                    </tfoot>
                </g:if>
                <g:else>
                    <div class="panel-header">
                        <div class="panel-heading table-header">
                            <div class="row">
                                <div class="col-sm-6">
                                    <div class="dropdown">
                                        <button class="btn btn-xs btn-white dropdown-toggle" type="button" id="action-btn" data-toggle="dropdown" aria-expanded="true">
                                            <g:message code="button.actions.label"/>
                                            <span class="caret"></span>
                                        </button>
                                        <ul class="dropdown-menu" role="menu" aria-labelledby="action-btn">
                                            <li role="presentation">
                                                <a role="menuitem" tabindex="-1" href="javascript:void(0)" onclick="submitFormTo('#bookings',
                                                    '${createLink(action: 'export')}')">
                                                    <g:message code="trainer.lessons.export.label"/>
                                                </a>
                                            </li>
                                            <li role="presentation">
                                                <a role="menuitem" tabindex="0" href="javascript:void(0)" onclick="submitFormTo('#bookings',
                                                    '${createLink(action: 'invoice')}')">
                                                    <g:message code="trainer.lessons.invoice.label"/>
                                                </a>
                                            </li>
                                        </ul>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <g:form name="bookings" class="no-margin">
                        <table id="booking-table" class="table table-striped table-hover table-bordered">
                            <thead>
                                <tr>
                                    <th width="5%" class="text-center">
                                        <g:checkBox class="checkbox-selector" name="selectAll" value="0" checked="false"/>
                                    </th>
                                    <th class="vertical-padding10"><g:message code="trainer.name.label"/></th>
                                    <th class="vertical-padding10"><g:message code="trainer.customer.lessonTime"/></th>
                                    <th class="vertical-padding10"><g:message code="trainer.customer.label"/></th>
                                    <th class="vertical-padding10"><g:message code="trainer.customer.membership"/></th>
                                    <th class="vertical-padding10"><g:message code="trainer.price.label"/></th>
                                    <th class="vertical-padding10 text-center"><g:message code="trainer.paymentStatus.label"/></th>
                                </tr>
                            </thead>
                            <tbody>
                                <g:each in="${bookings}" var="booking">
                                    <tr data-id="${booking.id}">
                                        <td class="text-center rowlink-skip">
                                            <g:checkBox name="bookingIds" value="${booking.id}" checked="false" class="selector" />
                                        </td>
                                        <td>
                                            ${booking.trainers?.first()?.fullName()}
                                        </td>
                                        <td>
                                            <g:formatDate date="${booking.slot.startTime}" format="yyyy-MM-dd HH:mm"/>
                                        </td>
                                        <td>
                                            <g:link controller="facilityCustomer" action="show" id="${booking.customer.id}" class="rowlink">
                                                ${booking.customer.fullName()}&nbsp;
                                            </g:link>
                                        </td>
                                        <td>
                                            ${booking.customer.getActiveMembership()?.type?.name}
                                        </td>
                                        <td>
                                            <g:formatMoney value="${booking.order?.total()}" facility="${facility}" />
                                        </td>
                                        <td class="text-center">
                                            <g:if test="${booking.isFinalPaid()}">
                                                <span class="label label-success">
                                                    <g:message code="membership.status.PAID"/>
                                                </span>
                                            </g:if>
                                            <g:else>
                                                <span class="label label-important label-danger">
                                                    <g:message code="membership.status.UNPAID"/>
                                                </span>
                                            </g:else>
                                        </td>
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>
                    </g:form>
                </g:else>
            </div>
        </g:if>
        <g:else><g:message code="facility.onlyLocal"/></g:else>
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
    $('#trainersSelect').selectpicker({
        title: '<g:message code="facilityCourseParticipant.index.trainers.noneSelectedText"/>'
    });
    $('#paidUnpaid').selectpicker({
        title: '<g:message code="facilityPrivateLessons.lessons.all"/>'
    });
</r:script>

</body>
</html>
