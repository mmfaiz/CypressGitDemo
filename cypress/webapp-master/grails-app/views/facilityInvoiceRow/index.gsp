<%@ page import="com.matchi.invoice.InvoiceRow; com.matchi.FacilityProperty; com.matchi.Facility"%>
<html>
<head>
    <meta name="layout" content="facilityLayout" />
    <title><g:message code="facilityInvoiceRow.index.message1"/></title>
    <r:require modules="select2,jquery-multiselect-widget,daterangepicker,matchi-selectall"/>
</head>
<body>

<ul class="breadcrumb">
    <li><g:link controller="facilityInvoice" action="index"><g:message code="invoice.label.plural"/></g:link> <span class="divider">/</span></li>
    <li><g:message code="facilityInvoiceRow.index.message11" args="[rows.getTotalCount()]"/></li>
</ul>

<ul class="nav nav-tabs">
    <li><g:link controller="facilityInvoice" action="index"><g:message code="invoice.label.plural"/></g:link></li>
    <li class="active"><g:link controller="facilityInvoiceRow" action="index"><g:message code="facilityInvoiceRow.index.message1"/></g:link></li>
</ul>

<form method="GET" id="filterForm" class="form-table-filter">
    <fieldset>
        <div class="control-group">
            <ul class="inline filter-list">
                <li>
                    <g:textField id="invoicerow-search-input" placeholder="${message(code: 'facilityInvoiceRow.index.message12')}" name="q" value="${filter.q}" style="width: 202px"/>
                </li>
                <li>
                    <div tabindex="2" id="daterange" class="daterange" style="background-color: #fff;margin: 0 10px 0 0px;">
                        <i class="icon-calendar icon-large"></i>

                        <span ><g:formatDate formatName="date.format.daterangepicker.short" date="${filter.start.toDate()}" /> - <g:formatDate formatName="date.format.daterangepicker.short"  date="${filter.end.toDate()}" /></span> <b style="margin-top:6px" class="caret"></b>
                    </div>

                    <input name="start" id="rangestart" class="span8" placeholder="Sök bokningar..." type="hidden" value="<g:formatDate format="yyyy-MM-dd"  date="${filter.start?.toDate()}"/>">
                    <input name="end" id="rangeend" class="span8" placeholder="Sök bokningar..." type="hidden" value="<g:formatDate format="yyyy-MM-dd"  date="${filter?.end?.toDate()}"/>">
                </li>
                <g:if test="${FacilityProperty.findByFacilityAndKey(facility,
                        FacilityProperty.FacilityPropertyKey.FEATURE_ORGANIZATIONS.name())?.value}">
                    <li>
                        <select id="organizations-list" name="organizations" multiple="true">
                            <g:each in="${com.matchi.facility.Organization.findAllByFacility(facility)}">
                                <option value="${it.id}" ${( filter.organizations?.contains(it.id) ? "selected":"")}><g:fieldValue field="name" bean="${it}"/></option>
                            </g:each>
                            <option value="-1" ${( filter.organizations?.contains(-1l) ? "selected":"")}>${facility?.name}</option>
                        </select>
                    </li>
                </g:if>
                <li class="pull-right">
                    <button id="filterSubmit" tabindex="3" class="btn" type="submit"><g:message code="button.filter.label"/></button>
                </li>
            </ul>
        </div>
    </fieldset>
</form>

<g:form action="index" method="GET" name="invoiceRowForm">
    <g:hiddenField name="q" value="${filter.q}"/>
    <g:hiddenField name="start" value="${filter?.start?.toString("yyyy-MM-dd")}"/>
    <g:hiddenField name="end" value="${filter?.end?.toString("yyyy-MM-dd")}"/>
    <g:each in="${filter?.organizations}">
        <g:hiddenField name="organizations" value="${it}"/>
    </g:each>
    <g:hiddenField name="returnUrl" value="${createLink(controller: 'facilityInvoice', action: 'index', absolute: true)}"/>

    <div class="action-bar">
        <div class="btn-toolbar-left">
            <g:actionSubmit value="${message(code: 'button.delete.label')}" onclick="return confirm('${message(code: 'facilityInvoiceRow.index.message13')}')" action="remove" class="btn btn-inverse right right-margin5 bulk-action"/>
            <a href="javascript:void(0)" onclick="submitFormTo('#invoiceRowForm', '${createLink(controller: 'facilityInvoiceRowFlow', action: 'createInvoice')}');" class="btn btn-inverse right right-margin5 bulk-action"><g:message code="facilityInvoiceRow.index.message14"/></a>
        </div>
        <div class="btn-toolbar-right">
            <g:link controller="facilityInvoiceRowFlow" action="createInvoiceRow" class="btn btn-inverse right"
                    params="['returnUrl': g.createLink(absolute: true, params: params)]">
                <span><g:message code="facilityInvoiceRow.index.message6"/></span>
            </g:link>
        </div>
    </div>

    <table id="invoice-row-table" class="table table-striped table-bordered">
        <thead>
        <tr height="34">
            <th width="15" class="center-text"><g:checkBox class="checkall-rowids" name="selectAll" value="0" checked="false"/></th>
            <g:sortableColumn width="40"  titleKey="customer.number.label" property="c.number" params="${params}"/>
            <g:sortableColumn width="150"  titleKey="customer.label" property="c.lastname,c.firstname,c.companyname" params="${params}"/>
            <g:if test="${facility.hasFortnox() || facility.hasExternalArticles()}">
                <g:sortableColumn width="50"  titleKey="default.article.label" property="description" params="${params}"/>
            </g:if>
            <g:sortableColumn width="200"  titleKey="facilityInvoiceRow.index.message17" property="description" params="${params}"/>
            <g:sortableColumn titleKey="default.created.label" property="dateCreated" params="${params}"/>
            <g:sortableColumn width="120" titleKey="default.price.label" property="price" params="${params}"/>
            <th><g:message code="default.vat.label"/></th>
            <g:sortableColumn titleKey="default.amount.label" property="price" params="${params}"/>
        </tr>
        </thead>

        <g:if test="${rows.isEmpty()}">
            <tr>
                <td colspan="9"><i><g:message code="facilityInvoiceRow.index.message8"/></i></td>
            </tr>
        </g:if>

        <g:else>
            <g:each in="${rows}" var="row">
                <tr>
                    <td class="select-row center-text">
                        <g:checkBox class="selector" name="rowIds" value="${row.id}" checked="false"/>
                    </td>
                    <td>${row.customer.number}</td>
                    <td nowrap>${row.customer.fullName()}
                        <g:if test="${row.isNew()}">
                            <span class="badge badge-info" title="${message(code: 'facilityInvoiceRow.index.message21')}">
                                <g:message code="facilityInvoiceRow.index.message9"/>
                            </span>
                        </g:if>
                    </td>
                    <g:if test="${facility.hasFortnox() || facility.hasExternalArticles()}">
                        <td>${row.externalArticleId}</td>
                    </g:if>
                    <td>${row.description}</td>
                    <td><g:humanDateFormat  date="${row.dateCreated}"/></td>
                    <td>
                        ${row.amount}<g:message code="unit.st"/> <g:message code="facilityInvoiceRow.index.message22"/> <g:formatMoney value="${row.price}"/>
                        <g:if test="${row.discount}">
                            (-<g:formatDiscount invoiceRow="${row}"/>)
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${row.getTotalVAT() != 0}">
                            <g:formatMoney value="${row.getTotalVAT()}"/> (<g:formatNumber number="${row.vat}"/>%)
                        </g:if>
                        <g:else>
                            <g:formatMoney value="${0}"/>
                        </g:else>
                    </td>
                    <td><g:formatMoney value="${row.getTotalIncludingVAT()}"/></td>
                </tr>
            </g:each>
        </g:else>

    </table>

    <g:paginateTwitterBootstrap next="&raquo;" prev="&laquo;" class="pagination-centered"
                                maxsteps="0" max="${filter.max}" params="${params}"
                                action="index" total="${rows.getTotalCount()}" />


</g:form>

<r:script>
    $(function () {
        $("#invoicerow-search-input").focus();
        $("#organizations-list").multiselect({
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

        $("#invoice-row-table").selectAll({ max: "${g.forJavaScript(data: filter.max)}", count: "${g.forJavaScript(data: rows.getTotalCount())}", name: "underlag" });
    });
</r:script>
</body>
</html>
