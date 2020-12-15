<%@ page import="org.joda.time.DateTime; grails.util.GrailsUtil; com.matchi.Facility; java.io.File" %>
<html>
<head>
    <meta name="layout" content="b3admin"/>
    <title>MATCHi <g:message code="adminHome.index.title"/></title>
    <r:require modules="select2,jquery-multiselect-widget,daterangepicker"/>
    <r:script>
        $(document).ready(function() {
            $("a[data-toggle=modal]").click(function() {

                target = $(this).attr('data-target');
                url = $(this).attr('href');
                $(target + " .file-contents").load(url);
            });
            //target = ($ @).attr('data-target')
            //url = ($ @).attr('href')
            //($ target).load(url)
            $('#daterange').daterangepicker({
                   ranges: {
                       '${message(code: 'default.dateRangePicker.currentWeek')}': [(Date.today().getDay()==1?Date.today():Date.today().moveToDayOfWeek(1, -1)), Date.today().moveToDayOfWeek(0)],
                       '${message(code: 'default.dateRangePicker.lastWeek')}': [(Date.today().getDay()==1?Date.today().add({ weeks: -1 }):Date.today().add({ weeks: -1 }).moveToDayOfWeek(1, -1)), Date.today().add({ weeks: -1 }).moveToDayOfWeek(0)],
                       '${message(code: 'default.dateRangePicker.currentMonth')}': [Date.today().moveToFirstDayOfMonth(), Date.today().moveToLastDayOfMonth()],
                       '${message(code: 'default.dateRangePicker.lastMonth')}': [Date.today().moveToFirstDayOfMonth().add({ months: -1 }), Date.today().moveToFirstDayOfMonth().add({ days: -1 })]
                   },

                   format: "yyyy-MM-dd",
                   startDate: "<g:formatDate format="yyyy-MM-dd"  date="${new DateTime().minusDays(1).toDate()}" locale="sv"/>",
                   endDate: "<g:formatDate format="yyyy-MM-dd"  date="${new DateTime().minusDays(1).toDate()}" locale="sv"/>",
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
               }
           );
        });
    </r:script>
</head>
<body>
<div class="container content-container">
    <ol class="breadcrumb">
        <li><g:message code="adminJob.index.heading"/></li>
    </ol>

    <div class="panel panel-default panel-admin">
        <div class="panel-body">
            <!-- JOBS -->
            <div class="well">
                <h3><g:message code="adminJob.index.jobs"/></h3>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th><g:message code="default.name.label"/></th>
                        <th><g:message code="adminJob.index.last"/></th>
                        <th><g:message code="adminJob.index.next"/></th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${jobs}" var="job">
                        <tr>
                            <td>${job.name}</td>
                            <td>${job.prev.toString("yyyy-MM-dd HH:mm:ss")}</td>
                            <td>${job.next.toString("yyyy-MM-dd HH:mm:ss")}</td>
                            <td class="text-center">
                                <g:if test="${job.executing}">
                                    <g:link class="btn btn-success btn-xs" action="${job.executing}"><g:message code="adminJob.index.run"/></g:link>
                                </g:if>
                            </td
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>

            <div class="well">
                <!-- BoxnetSync -->
                <h3><g:message code="adminJob.index.boxnetSync"/></h3>
                <p>
                    <g:message code="adminJob.index.boxnetSync.desc"/>
                </p>
                <table class="table table-striped table-bordered">
                    <tr>
                        <th><g:message code="facility.name.label"/></th>
                    </tr>

                    <g:each in="${boxnetFacilities}" var="facility">
                        <tr>
                            <td>${facility.name} (${facility.shortname})</td>
                        </tr>
                    </g:each>
                </table>
            </div>

            <div class="well">
                <!-- Get cash register events -->
                <h3><g:message code="adminJob.index.cashRegisterData"/></h3>
                <g:form action="runCashRegisterHistory" class="form-inline">
                    <div class="form-group">
                        <div tabindex="2" id="daterange" class="daterange form-control" style="background-color: #fff;margin: 0 10px 0 0px;">
                            <i class="icon-calendar icon-large"></i>
                            <span>${params.start ?: new DateTime().minusDays(1).toString("MMMM d, yyyy")} - ${params.end ?: new DateTime().minusDays(1).toString("MMMM d, yyyy")}</span> <b style="margin-top:6px" class="caret"></b>

                            <input name="start" id="rangestart" class="col-sm-3" type="hidden" value="${params.start ?: new DateTime().minusDays(1).toString("yyyy-MM-dd")}">
                            <input name="stop" id="rangeend" class="col-sm-3" type="hidden" value="${params.end ?: new DateTime().minusDays(1).toString("yyyy-MM-dd")}">
                        </div>
                        <g:submitButton class="btn btn-success btn-sm" value="${message(code: 'adminJob.index.run')}" name="submit"/>
                    </div>
                </g:form>
            </div>

            <!-- Redeem cancelled subscription bookings -->
            <!--<div class="well">
                <h3><g:message code="adminJob.index.redeemSubscriptionCancellations"/></h3>
                <g:link class="btn btn-success btn-sm" action="runRedeemSubscriptionCancellations"><g:message code="adminJob.index.run"/></g:link>
            </div>-->

            <!-- Update incorrect redeems -->
            <!--<div class="well">
                <h3><g:message code="adminJob.index.updateIncorrectRedeems"/></h3>
                <g:link class="btn btn-success btn-sm" action="updateIncorrectRedeems"><g:message code="adminJob.index.run"/></g:link>
            </div>-->

            <!-- Migrate payment and transactions to orders and order payments -->
            <div class="well">
                <h3>Migrate payment transactions</h3>
                <g:link class="btn btn-success btn-sm" action="migratePaymentTransactions">Kör</g:link>
            </div>

            <!-- Migrate TP submissions pointing at wrong course -->
            <div class="well">
                <h3>Migrate TP submissions pointing at wrong course</h3>
            <g:link class="btn btn-success btn-sm" action="migrateSubmissionsToRightCourse">Kör 1</g:link>
            </div>

            <div class="well">
                <!-- Sync Fortnox customers -->
                <h3><g:message code="adminJob.index.fortnoxCustomerSync"/></h3>
                <g:form action="forceFortnoxCustomerSync" class="form-inline">
                    <div class="form-group">
                        <g:select name="facilityId" from="${fortnoxFacilities}" optionKey="id" optionValue="name" class="form-control"/>
                    </div>
                    <g:submitButton class="btn btn-success btn-sm" value="${message(code: 'adminJob.index.run')}" name="submit"/>
                </g:form>
            </div>

            <div class="well">
                <!-- Resync fortnox invoices -->
                <h3><g:message code="adminJob.index.fortnoxInvoiceResync"/></h3>
                <p><g:message code="adminJob.index.fortnoxInvoiceResync.info"/></p>
                <g:form action="resyncFortnoxInvoices" class="form-inline">
                    <div class="form-group">
                        <g:select name="facilityId" from="${fortnoxFacilities}" optionKey="id" optionValue="name" class="form-control"/>
                    </div>
                    <g:submitButton class="btn btn-success btn-sm" value="${message(code: 'adminJob.index.run')}" name="submit"/>
                </g:form>
            </div>

            <div class="well">
                <!-- MembershipRenewJob -->
                <h3><g:message code="adminJob.index.renewMembership"/></h3>
                <g:form action="renewMemberships" class="form-inline">
                    <div class="form-group">
                        <g:textField name="endDate" class="form-control"
                                value="${formatDate(date: new Date(), formatName: 'date.format.dateOnly')}"/>
                    </div>
                    <g:submitButton class="btn btn-success btn-sm" value="${message(code: 'adminJob.index.run')}" name="submit"/>
                </g:form>
            </div>

            <div class="well">
                <!-- RecurringPaymentRetryJob -->
                <h3><g:message code="adminJob.index.retryRecurringPayments"/></h3>
                <g:link class="btn btn-success btn-sm" action="retryRecurringPayments">
                    <g:message code="adminJob.index.run"/>
                </g:link>
            </div>

            <div class="well">
                <h3><g:message code="adminJob.index.MarkTasksAsFinishedTitle"/></h3>
                <p><g:message code="adminJob.index.MarkTasksAsFinishedDescription"/></p>
                <p title="If season exists but can't be deleted because of bookings, this process will fail.">Fully supported tasks: create season. Will try to delete Season if exists. </p>
                <g:form action="MarkTaskAsFinished" class="form-inline">
                    <div class="form-group">
                        <g:select name="taskId" from="${unfinishedTasks}" optionKey="id" optionValue="name" class="form-control"/>
                    </div>
                    <g:submitButton class="btn btn-success btn-sm" value="${message(code: 'adminJob.index.run')}" name="submit"/>
                </g:form>
            </div>

        </div>
    </div>
</div>


<div class="modal hide" id="myModal">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal">×</button>
        <h3><g:message code="adminJob.index.fileContent"/></h3>
    </div>
    <div class="modal-body">
        <pre class="file-contents"><g:message code="adminJob.index.oneFineBody"/></pre>
    </div>
    <div class="modal-footer">
        <a href="#" class="btn btn-md" data-dismiss="modal"><g:message code="button.close.label"/></a>
    </div>
</div>

<r:script>
    $(function() {
        $("#endDate").datepicker({
            autoSize: true,
            dateFormat: "${message(code: 'date.format.dateOnly.small')}"
        });
    });
</r:script>
</body>
</html>
