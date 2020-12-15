<%@ page import="com.matchi.FacilityProperty; com.matchi.facility.Organization; com.matchi.invoice.Invoice; com.matchi.Facility"%>
<g:set var="returnUrl" value="${createLink(absolute: true, controller: 'facilityInvoice', action: 'index', params: params.subMap(params.keySet() - 'error' - 'message'))}"/>
<g:set var="resetFilterParams" value="${filter.properties + [reset: true]}"/>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="invoice.label.plural"/></title>
    <r:require modules="select2,highcharts,jquery-multiselect-widget,daterangepicker,matchi-selectall"/>
</head>
<body>

<ul class="breadcrumb">
    <li><g:message code="facilityInvoice.index.message19" args="[invoices.getTotalCount()]"/></li>
</ul>

<g:if test="${!facility?.isMasterFacility()}">
    <ul id="invoice-tab" class="nav nav-tabs">
        <li class="active"><g:link contronller="facilityInvoice" action="index"><g:message code="invoice.label.plural"/></g:link></li>
        <li><g:link controller="facilityInvoiceRow" action="index"><g:message code="facilityInvoice.index.message3"/></g:link></li>

        <g:if test="${facility.hasFortnox()}">
            <li class="dropdown pull-right">
                <a class="dropdown-toggle" id="drop5" role="button" data-toggle="dropdown" href="#"><r:img width="90" dir="/images/fortnox.png" style="margin-top: 0"/> <b class="caret"></b></a>
                <ul id="menu3" class="dropdown-menu" role="menu" aria-labelledby="drop5">
                    <li role="presentation"><a href="http://www.fortnox.se" target="_new">www.fortnox.se</a></li>
                    <li role="presentation" class="divider"></li>
                    <li role="presentation"><g:link action="updateFortnoxArticles"><g:message code="facilityInvoice.index.message5"/></g:link> </li>
                </ul>
            </li>
        </g:if>
    </ul>

    <g:render template="/templates/messages/webflowMessage"/>

    <form method="GET" id="filterForm" class="form-table-filter">
        <g:hiddenField name="reset" value="true"/>
        <fieldset>
            <div class="control-group">
                <ul class="inline filter-list">
                    <li>
                        <g:textField placeholder="${message(code: 'facilityInvoice.index.message27')}" name="q" value="${filter.q}" style="width: 202px"/>
                    </li>
                    <li>
                        <div tabindex="2" id="daterange" class="daterange" style="background-color: #fff;margin: 0 10px 0 0px;">
                            <i class="icon-calendar icon-large"></i>
                            <span><g:formatDate formatName="date.format.daterangepicker.short"  date="${filter.start.toDate()}"/> - <g:formatDate formatName="date.format.daterangepicker.short"  date="${filter.end.toDate()}"/></span> <b style="margin-top:6px" class="caret"></b>
                        </div>
                        <input name="start" id="rangestart" class="span8" type="hidden" value="<g:formatDate format="yyyy-MM-dd"  date="${filter.start?.toDate()}"/>">
                        <input name="end" id="rangeend" class="span8" type="hidden" value="<g:formatDate format="yyyy-MM-dd"  date="${filter?.end?.toDate()}"/>">
                    </li>
                    <li>
                        <select id="status" name="status" multiple="true">
                            <g:each in="${com.matchi.invoice.Invoice.InvoiceStatus.values()}">
                                <option value="${it}" ${( filter.statuses()?.contains(it) ? "selected":"")}><g:message code="invoice.status.${it}"/></option>
                            </g:each>
                        </select>
                    </li>
                    <g:if test="${FacilityProperty.findByFacilityAndKey(facility,
                            FacilityProperty.FacilityPropertyKey.FEATURE_ORGANIZATIONS.name())?.value}">
                        <li>
                            <select id="organizations" name="organizations" multiple="true">
                                <g:each in="${com.matchi.facility.Organization.findAllByFacility(facility)}">
                                    <option value="${it.id}" ${( filter.organizations?.contains(it.id) ? "selected":"")}><g:fieldValue field="name" bean="${it}"/></option>
                                </g:each>
                                <option value="-1" ${( filter.organizations?.contains(-1l) ? "selected":"")}>${facility?.name}</option>
                            </select>
                        </li>
                    </g:if>
                    <li class="pull-right">
                        <g:if test="${filter.isActive()}">
                            <g:link action="index" params="[reset: true]" class="btn btn-danger">
                                <g:message code="button.filter.remove.label"/>
                            </g:link>
                        </g:if>
                        <g:else>
                            <a href="javascript: void(0)" class="btn btn-default disabled">
                                <g:message code="button.filter.remove.label"/>
                            </a>
                        </g:else>
                        <button id="filterSubmit" tabindex="3" class="btn" type="submit"><g:message code="button.filter.label"/></button>
                    </li>
                </ul>
            </div>
        </fieldset>
    </form>

    <g:form name="invoiceForm">
        <g:hiddenField name="q" value="${filter.q}"/>
        <g:hiddenField name="start" value="${filter?.start?.toString("yyyy-MM-dd")}"/>
        <g:hiddenField name="end" value="${filter?.end?.toString("yyyy-MM-dd")}"/>
        <g:if test="${filter?.statuses()}">
            <g:each in="${filter?.statuses()}">
                <g:hiddenField name="status" value="${it.toString()}"/>
            </g:each>
        </g:if>

        <div class="action-bar">

            <div class="btn-toolbar-left">
                <div class="btn-group">
                    <button class="btn btn-inverse dropdown-toggle bulk-action" data-toggle="dropdown">
                        <g:message code="button.actions.label"/>
                        <span class="caret"></span>
                    </button>
                    <ul class="dropdown-menu">
                        <li><a href="javascript:void(0)" onclick="submitFormTo('#invoiceForm', '<g:createLink controller="facilityInvoice" action="remove" />');"><g:message code="facilityInvoice.index.remove"/></a></li>
                        <g:if test="${!facility.hasFortnox()}">
                            <li><a href="javascript:void(0)" onclick="submitFormTo('#invoiceForm', '<g:createLink controller="facilityInvoice" action="print" />');"><g:message code="button.print.label"/></a></li>
                            <li><a href="javascript:void(0)" onclick="submitFormTo('#invoiceForm', '<g:createLink controller="facilityInvoiceFlow" action="send" params="[returnUrl: returnUrl]"/>');"><g:message code="facilityInvoice.index.sendByEmail"/></a></li>
                            <li class="dropdown-submenu">
                                <a tabindex="-1" href="#"><g:message code="facilityInvoice.index.message9"/></a>
                                <ul class="dropdown-menu">
                                    <li><a href="javascript:void(0)" onclick="submitFormTo('#invoiceForm', '<g:createLink controller="facilityInvoice" action="status" params="[newStatus: Invoice.InvoiceStatus.PAID.toString()]" />');"><g:message code="payment.paid.label"/></a></li>
                                    <li><a href="javascript:void(0)" onclick="submitFormTo('#invoiceForm', '<g:createLink controller="facilityInvoice" action="status" params="[newStatus: Invoice.InvoiceStatus.POSTED.toString()]" />');"><g:message code="facilityInvoice.index.message11"/></a></li>
                                </ul>
                            </li>
                            <li class="divider"></li>
                            <li class="dropdown-submenu">
                                <a tabindex="-1" href="#"><g:message code="button.export.label"/></a>
                                <ul class="dropdown-menu">
                                    <li><a href="javascript:void(0)" onclick="submitFormTo('#invoiceForm', '<g:createLink controller="facilityInvoice" action="paymentExport" />');"><g:message code="facilityInvoice.index.export.payments"/></a></li>
                                    <li><a href="javascript:void(0)" onclick="submitFormTo('#invoiceForm', '<g:createLink controller="facilityInvoice" action="invoiceJournalExport" />');"><g:message code="facilityInvoice.index.export.journal"/></a></li>
                                    <li><a href="javascript:void(0)" onclick="submitFormTo('#invoiceForm', '<g:createLink controller="facilityInvoice" action="invoiceJournalSummaryExport" />');"><g:message code="facilityInvoice.index.export.summary"/></a></li>
                                    <li><a href="javascript:void(0)" onclick="submitFormTo('#invoiceForm', '<g:createLink controller="facilityInvoice" action="listExport" />');"><g:message code="facilityInvoice.index.export.list"/></a></li>
                                    <g:if test="${facility.country.equals('SE')}">
                                        <li><a href="javascript:void(0)" onclick="submitFormTo('#invoiceForm', '<g:createLink controller="facilityInvoice" action="sieType4Export" />');"><g:message code="facilityInvoice.index.export.sie"/></a></li>
                                    </g:if>
                                    <g:if test="${facility.shortname.equals('salk')}">
                                        <li><a href="javascript:void(0)" onclick="submitFormTo('#invoiceForm', '<g:createLink controller="facilityInvoice" action="export" />');"><g:message code="facilityInvoice.index.export.reskontra"/></a></li>
                                    </g:if>
                                </ul>
                            </li>
                        </g:if>
                    </ul>
                </div>
            </div>

            <div class="btn-toolbar-right">
                <div class="btn-group">
                    <g:link class="btn btn-inverse" controller="facilityInvoicePayment"><g:message code="facilityInvoice.index.message13"/></g:link>
                </div>
            </div>
        </div>


        <table id="invoice-table" class="table table-striped table-bordered table-hover">
            <thead>
            <tr height="34">
                <th width="15" class="center-text">
                    <g:checkBox class="checkbox-selector" name="selectAll" value="0" checked="false"/>
                </th>
                <g:if test="${facility.hasApplicationInvoice()}">
                    <g:sortableColumn width="40" titleKey="facilityInvoice.index.message21" property="id" params="${resetFilterParams}"/>
                </g:if>
                <g:sortableColumn width="120" titleKey="customer.label" property="c.lastname,c.firstname,c.companyname" params="${resetFilterParams}"/>
                <g:sortableColumn width="70"  titleKey="default.status.label" property="status" params="${resetFilterParams}" class="center-text"/>
                <g:sortableColumn width="100" titleKey="invoice.invoiceDate.label" property="invoiceDate" params="${resetFilterParams}"/>
                <g:sortableColumn width="100" titleKey="invoice.expirationDate.label" property="expirationDate" params="${resetFilterParams}"/>
                <g:sortableColumn width="100" titleKey="facilityInvoice.index.paidDate" property="paidDate" params="${resetFilterParams}"/>
                <th width="20" class="center-text"><g:message code="facilityInvoice.index.sentBy"/></th>
                <g:sortableColumn width="70" titleKey="facilityInvoice.index.lastSent" property="lastSent" params="${resetFilterParams}"/>
                <th width="20"><g:message code="default.amount.label"/></th>
            </tr>
            </thead>

            <tbody data-provides="rowlink">
            <g:if test="${invoices.isEmpty()}">
                <tr>
                    <td colspan="10"><i><g:message code="facilityInvoice.index.message17"/></i></td>
                </tr>
            </g:if>
            <g:each in="${invoices}" var="invoice">
                <tr>
                    <td class="select-row nolink center-text">
                        <g:checkBox name="invoiceIds" value="${invoice.id}" checked="false" class="selector"/>
                    </td>

                    <g:if test="${facility.hasApplicationInvoice()}">
                        <td>${invoice.number}</td>
                    </g:if>

                    <td>${invoice.customer.fullName()}
                        <g:if test="${params.ids?.contains(invoice.id.toString())}">
                            <span class="badge badge-info" title="${message(code: 'facilityInvoice.index.message18')}">
                                <g:message code="facilityInvoice.index.message18"/>
                            </span>
                        </g:if>
                    </td>
                    <td class="center-text">
                        <span class="badge ${invoice.status.badgeClass}" title="<g:message code="invoice.status.${invoice.status}"/>">
                            <g:message code="invoice.status.${invoice.status}"/>
                        </span>
                    </td>

                    <td><g:formatDate date="${invoice.invoiceDate?.toDate()}" formatName="date.format.dateOnly"/></td>
                    <td><g:formatDate date="${invoice.expirationDate?.toDate()}" formatName="date.format.dateOnly"/></td>
                    <td><g:formatDate date="${invoice.paidDate?.toDate()}" formatName="date.format.dateOnly"/></td>

                    <td class="center-text">
                        <g:if test="${com.matchi.invoice.Invoice.InvoiceSentStatus.EMAIL == invoice.sent}">
                            <i title="${message(code: 'facilityInvoice.index.message26')}" class="icon-envelope"></i>
                        </g:if>
                        <g:elseif test="${com.matchi.invoice.Invoice.InvoiceSentStatus.PRINT == invoice.sent}">
                            <i title="${message(code: 'facilityInvoice.index.message28')}" class="icon-print">&nbsp;</i>
                        </g:elseif>
                        <g:elseif test="${com.matchi.invoice.Invoice.InvoiceSentStatus.NOT_SENT == invoice.sent}">

                        </g:elseif>
                        <g:link action="edit" class="rowLink" params="[id: invoice.id]"></g:link>
                    </td>

                    <td><g:formatDate date="${invoice.lastSent}" formatName="date.format.timeShort"/></td>

                    <td><g:formatMoneyShort value="${invoice.getTotalIncludingVATRounded()}"/></td>
                </tr>
            </g:each>
            </tbody>


        </table>

        <g:paginateTwitterBootstrap next="&raquo;" prev="&laquo;" class="pagination-centered"
                                    maxsteps="0" max="50" params="${resetFilterParams}"
                                    action="index" total="${invoices.getTotalCount()}" />

    </g:form>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
<r:script>
    $(function () {
        $("#status").multiselect({
            create: function() {$(this).next().width(210);},
            classes: "multi",
            minWidth: 200,
            checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'default.multiselect.noneSelectedText')}",
            selectedText: "${message(code: 'facilityInvoice.index.statusSelect.selectedText')}"
        });

        $("#organizations").multiselect({
            create: function() {$(this).next().width(210);},
            classes: "multi",
            minWidth: 200,
            checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'facilityInvoice.index.organizationsSelect.noneSelectedText')}",
            selectedText: "${message(code: 'facilityInvoice.index.organizationsSelect.selectedText')}"
        });

        $('#daterange').daterangepicker(
            {
                ranges: {
                    '${message(code: 'default.dateRangePicker.currentWeek')}': [(Date.today().getDay()==1?Date.today():Date.today().moveToDayOfWeek(1, -1)), Date.today().moveToDayOfWeek(0)],
                    '${message(code: 'default.dateRangePicker.lastWeek')}': [(Date.today().getDay()==1?Date.today():Date.today().add({ weeks: -1 }).moveToDayOfWeek(1, -1)), Date.today().add({ weeks: -1 }).moveToDayOfWeek(0)],
                    '${message(code: 'default.dateRangePicker.currentMonth')}': [Date.today().moveToFirstDayOfMonth(), Date.today().moveToLastDayOfMonth()],
                    '${message(code: 'default.dateRangePicker.lastMonth')}': [Date.today().moveToFirstDayOfMonth().add({ months: -1 }), Date.today().moveToFirstDayOfMonth().add({ days: -1 })]
                },

                format: "${message(code: 'date.format.dateOnly')}",
                startDate: "<g:formatDate format="yyyy-MM-dd"  date="${filter?.start.toDate()}"/>",
                endDate: "<g:formatDate format="yyyy-MM-dd"  date="${filter?.end.toDate()}"/>",
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
                $('#daterange span').html(moment(start).format('MMM D, YYYY') + ' - ' + moment(end).format('MMM D, YYYY'));
                $('#filterForm').submit();
            }
    );

        $("#invoice-table").selectAll({ max: "${g.forJavaScript(data: filter.max)}", count: "${g.forJavaScript(data: invoices.getTotalCount())}", name: "fakturor" });

        $("#q").focus();
    });

    $(document).ready(function () {
        $("#filterForm").preventDoubleSubmission({});
    });
</r:script>

</body>
</html>
