<%@ page import="com.matchi.MatchiConfigKey; com.matchi.MatchiConfig; com.matchi.FacilityProperty; com.matchi.Court; com.matchi.facility.FacilitySubscriptionFilterCommand; com.matchi.invoice.Invoice; com.matchi.Subscription; org.joda.time.LocalTime; org.joda.time.DateTime; com.matchi.Season; com.matchi.BookingGroup; com.matchi.Facility" %>
<g:set var="returnUrl" value="${createLink(absolute: true, params: params.subMap(params.keySet() - 'error' - 'message'))}"/>
<g:set var="resetFilterParams" value="${cmd.properties + [reset: true]}"/>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="subscription.label"/></title>
    <r:require modules="matchi-selectall, jquery-multiselect-widget, jquery-timepicker"/>
</head>
<body>
<g:javascript>
    $(document).ready(function() {
        $('.dropdown-toggle').dropdown();
    });
</g:javascript>

<ul class="breadcrumb">
    <li><g:message code="facilitySubscription.index.message14" args="[count]"/></li>
</ul>

<g:if test="${!facility?.isMasterFacility()}">
    <g:render template="/templates/messages/webflowMessage"/>

    <ul class="nav nav-tabs">
        <li class="active"><g:link action="index"><g:message code="subscription.label"/></g:link></li>
        <li><g:link controller="facilitySlotRedeem" action="index"><g:message code="facilitySlotRedeem.index.title"/></g:link></li>
    </ul>

    <form method="GET" id="filterForm" class="form-search well" style="padding:12px 10px 4px 10px;">
        <g:hiddenField name="reset" value="true"/>
        <fieldset>
            <div class="control-group">
                <ul class="inline filter-list">
                    <li><g:textField id="subscription-search-input" placeholder="${message(code: 'facilitySubscription.index.message15')}" name="q" value="${cmd.q}" class="search span3" style="width: 202px;"/></li>
                    <li>
                        <select id="season" name="season" style="width: 216px">
                            <g:each in="${facility.seasons}" var="season">
                                <option value="${season.id}" ${ cmd.season == season.id ? "selected":""}>${season.name}</option>
                            </g:each>
                        </select>
                    </li>
                    <li>
                        <g:select name="weekday" from="${1..7}" value="${cmd.weekday}" multiple="multiple"
                                  valueMessagePrefix="time.weekDay"/>
                    </li>
                    <li>
                        <g:textField name="time" value="${cmd.time?.toString('HH:mm')}" class="span2" style="width: 202px"
                                     placeholder="${message(code: 'facilitySubscription.index.time.placeholder')}"/>
                    </li>
                    <li>
                        <select id="court" name="court" multiple="multiple">
                            <g:each in="${Court.findAllByFacilityAndArchived(facility, false, [sort: 'name'])}">
                                <option value="${it.id}" ${cmd.court?.contains(it.id) ? "selected" : ""}>
                                    ${it.name.encodeAsHTML()}
                                </option>
                            </g:each>
                        </select>
                    </li>
                    <li>
                        <g:select name="status" from="${Subscription.Status.values()}" value="${cmd.status}"
                                  multiple="multiple" valueMessagePrefix="subscription.status"/>
                    </li>
                    <g:if test="${facility.hasApplicationInvoice()}">
                        <li>
                            <select id="invoiceStatus" name="invoiceStatus" multiple="multiple">
                                <g:each in="${Invoice.InvoiceStatus.values()*.name()}">
                                    <option value="${it}" ${cmd.invoiceStatus?.contains(it) ? "selected" : ""}>
                                        <g:message code="invoice.status.${it}"/>
                                    </option>
                                </g:each>
                                <option value="${FacilitySubscriptionFilterCommand.NOT_INVOICED_STATUS}"
                                    ${cmd.invoiceStatus?.contains(FacilitySubscriptionFilterCommand.NOT_INVOICED_STATUS) ? "selected" : ""}>
                                    <g:message code="facilitySubscription.index.invoiceStatusSelect.notInvoiced"/>
                                </option>
                            </select>
                        </li>
                    </g:if>
                    <li class="pull-right">
                        <g:if test="${cmd.isActive()}">
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

    <div class="action-bar">

        <div class="btn-toolbar-left">
            <div class="btn-group">
                <button class="btn btn-inverse dropdown-toggle bulk-action" data-toggle="dropdown">
                    <g:message code="button.actions.label"/>
                    <span class="caret"></span>
                </button>
                <ul class="dropdown-menu">
                    <li class="dropdown-submenu">
                        <a tabindex="-1" href="#"><g:message code="subscription.status.label"/></a>
                        <ul class="dropdown-menu">
                            <li><a href="javascript:void(0)" onclick="submitFormTo('#subscriptions', '<g:createLink controller="facilitySubscription" action="changeStatus" params="[newStatus: Subscription.Status.ACTIVE.name()]" />');"><g:message code="subscription.status.ACTIVE"/></a></li>
                            <li><a href="javascript:void(0)" onclick="submitFormTo('#subscriptions', '<g:createLink controller="facilitySubscription" action="changeStatus" params="[newStatus: Subscription.Status.CANCELLED.name()]" />');"><g:message code="subscription.status.CANCELLED"/></a></li>
                        </ul>
                    </li>
                    <g:ifFacilityPropertyEnabled name="${FacilityProperty.FacilityPropertyKey.FEATURE_SUBSCRIPTION_REMINDER_ADJUST.name()}">
                        <li class="dropdown-submenu">
                            <a tabindex="-1" href="#"><g:message code="subscription.reminderEnabled.label"/></a>
                            <ul class="dropdown-menu">
                                <li><a href="javascript:void(0)" onclick="submitFormTo('#subscriptions', '<g:createLink controller="facilitySubscription" action="changeReminder" params="[enabled: true]" />');"><g:message code="subscription.reminderEnabled.true"/></a></li>
                                <li><a href="javascript:void(0)" onclick="submitFormTo('#subscriptions', '<g:createLink controller="facilitySubscription" action="changeReminder" params="[enabled: false]" />');"><g:message code="subscription.reminderEnabled.false"/></a></li>
                            </ul>
                        </li>
                    </g:ifFacilityPropertyEnabled>
                    <li><a href="javascript:void(0)" onclick="submitFormTo('#subscriptions', '<g:createLink controller="facilitySubscriptionCopy" action="copy" />');"><g:message code="facilitySubscription.index.message4"/></a></li>
                    <g:if test="${facility.hasApplicationInvoice()}">
                        <li><a href="javascript:void(0)" onclick="submitFormTo('#subscriptions', '<g:createLink controller="facilitySubscriptionInvoice" action="createSubscriptionInvoice"
                               params="['returnUrl': returnUrl]"/>');"><g:message code="facilityInvoiceRow.createInvoiceRow"/></a></li>
                    </g:if>
                    <sec:ifAnyGranted roles="ROLE_ADMIN">
                        <li class="divider"></li>
                        <li><a href="javascript:void(0)" onclick="submitFormTo('#subscriptions', '<g:createLink controller="facilitySubscription" action="updatePrices" />');"><g:message code="facility.subscription.bulkUpdate"/></a></li>
                        <li class="divider"></li>
                    </sec:ifAnyGranted>
                    <li><a href="javascript:void(0)" onclick="submitFormTo('#subscriptions', '<g:createLink controller="facilitySubscriptionDelete" action="delete"
                            params="['returnUrl': returnUrl]"/>');"><g:message code="facility.subscription.bulkDelete"/></a></li>
                    <li class="divider"></li>
                    <li><a href="javascript:void(0)" onclick="submitFormTo('#subscriptions', '<g:createLink action="customerAction"
                            params="['returnUrl': returnUrl, targetController: 'facilityCustomerMessage', targetAction: 'message']"/>');"><g:message code="facilityCustomer.index.message25"/></a></li>
                    <g:if test="${facility.hasSMS()}">
                        <li><a href="javascript:void(0)" onclick="submitFormTo('#subscriptions', '<g:createLink action="customerAction"
                                params="['returnUrl': returnUrl, targetController: 'facilityCustomerSMSMessage', targetAction: 'message']"/>');"><g:message code="facilityCustomer.index.message24"/></a></li>
                    </g:if>
                    <g:ifFacilityPropertyEnabled name="${FacilityProperty.FacilityPropertyKey.FEATURE_SUBSCRIPTION_CONTRACT.name()}">
                        <li><a href="javascript:void(0)" onclick="submitFormTo('#subscriptions', '<g:createLink controller="facilitySubscriptionMessage" action="message"
                                params="['returnUrl': returnUrl]"/>');"><g:message code="facilityCustomer.index.message23"/></a></li>
                    </g:ifFacilityPropertyEnabled>
                </ul>
            </div>

        </div>

        <div class="btn-toolbar-right">
            <g:set var="disableSubscriptionsConfig" value="${MatchiConfig.findByKey(MatchiConfigKey.DISABLE_SUBSCRIPTIONS)}" />
            <g:if test="${!disableSubscriptionsConfig.isBlocked()}">
                <sec:ifAnyGranted roles="ROLE_ADMIN">
                    <g:link class="btn btn-inverse" controller="facilitySubscriptionImport" action="import"><g:message code="facilitySubscriptionImport.import.title"/></g:link>
                </sec:ifAnyGranted>
                <g:link action="create" class="btn btn-inverse"><span><g:message code="button.add.label"/></span></g:link>
            </g:if>
            <g:else>
                <p><small>${disableSubscriptionsConfig.isBlockedMessage()}</small></p>
            </g:else>
            <g:if test="${facility.subscriptionRedeem}">
                <g:link class="btn btn-inverse" action="runFacilitySubscriptionRedeemJob"><g:message code="facilitySubscription.index.message7"/></g:link>
            </g:if>
        </div>

    </div>
    <g:form name="subscriptions" controller="facilitySubscriptionCopy" action="copy" class="no-margin">
        <g:hiddenField name="season" value="${cmd?.season}"/>
        <g:hiddenField name="q" value="${cmd?.q}"/>
        <g:each in="${cmd?.weekday}">
            <g:hiddenField name="weekday" value="${it}"/>
        </g:each>
        <g:hiddenField name="time" value="${cmd?.time?.toString('HH:mm')}"/>
        <g:each in="${cmd?.court}">
            <g:hiddenField name="court" value="${it}"/>
        </g:each>
        <g:each in="${cmd?.status}">
            <g:hiddenField name="status" value="${it.name()}"/>
        </g:each>
        <g:each in="${cmd?.invoiceStatus}">
            <g:hiddenField name="invoiceStatus" value="${it}"/>
        </g:each>

        <table id="subscriptions-table" class="table table-striped table-bordered table-hover">
            <thead>
            <tr height="34">
                <th class="center-text" width="20">
                    <g:checkBox class="checkbox-selector" name="" value="" checked="${params.allselected == 'true'}"/>
                </th>
                <g:sortableColumn titleKey="customer.label" property="lastname,firstname,companyname" params="${resetFilterParams}"/>
                <g:sortableColumn titleKey="facilitySubscription.index.message18" property="weekday,time" params="${resetFilterParams}"/>
                <g:sortableColumn titleKey="court.label" property="crt.id" params="${resetFilterParams}"/>
                <g:sortableColumn width="180" titleKey="default.date.label" property="startTime" params="${resetFilterParams}"/>
                <g:sortableColumn width="60" titleKey="subscription.status.label" property="status" params="${resetFilterParams}"/>
                <g:ifFacilityPropertyEnabled name="${FacilityProperty.FacilityPropertyKey.FEATURE_SUBSCRIPTION_REMINDER_ADJUST.name()}">
                    <g:sortableColumn width="60" titleKey="subscription.reminderEnabled.label" property="reminderEnabled" params="${resetFilterParams}"/>
                </g:ifFacilityPropertyEnabled>
                <g:sortableColumn width="60" titleKey="subscription.copied.label" property="copiedDate" params="${resetFilterParams}"/>
                <g:if test="${facility.hasApplicationInvoice()}">
                    <th width="90" class="center-text"><g:message code="facilitySubscription.index.message9"/></th>
                </g:if>
            </tr>
            </thead>
            <g:if test="${subscriptions.size() == 0}">
                <tr>
                    <td colspan="10"><i><g:message code="facilitySubscription.index.message10"/></i></td>
                </tr>
            </g:if>

            <tbody data-provides="rowlink">
            <g:set var="selectedIds" value="${params.list('subscriptionId').collect{Long.parseLong(it)}}"/>
            <g:each in="${subscriptions}" var="subscription">
                <tr class="rowlink">
                    <td class="center-text nolink">
                        <g:checkBox name="subscriptionId" value="${subscription.id}" checked="${selectedIds.contains(subscription.id)}" class="selector"/>
                    </td>
                    <td>${subscription.customerName ? subscription.customerName : subscription.companyName}</td>
                    <td><g:message code="time.weekDay.${subscription.weekday}" /> ${new DateTime(subscription.time).toString("HH:mm")}</td>
                    <td>${subscription.courtName}</td>
                    <td><g:formatDate date="${new DateTime(subscription.startTime).toDate()}" formatName="date.format.dateOnly"/> -
                    <g:formatDate date="${new DateTime(subscription.endTime).toDate()}" formatName="date.format.dateOnly"/> </td>
                    <td class="center-text">
                        <span class="badge label-${subscription.status == Subscription.Status.ACTIVE.name() ? 'success' : 'warning'}">
                            <g:message code="subscription.status.${subscription.status}"/>
                        </span>
                    </td>
                    <g:ifFacilityPropertyEnabled name="${FacilityProperty.FacilityPropertyKey.FEATURE_SUBSCRIPTION_REMINDER_ADJUST.name()}">
                        <td class="center-text">
                            <span class="badge label-${subscription.reminderEnabled ? 'success' : 'warning'}">
                                <g:message code="default.${subscription.reminderEnabled ? 'yes' : 'no'}.label"/>
                            </span>
                        </td>
                    </g:ifFacilityPropertyEnabled>
                    <td class="center-text">
                        <g:if test="${subscription.copiedDate}">
                            <span class="badge label-success"><g:message code="default.yes.label"/></span>
                        </g:if>
                        <g:else>
                            <span class="badge label-info"><g:message code="default.no.label"/></span>
                        </g:else>
                        <g:link action="edit" class="rowLink" params="[id: subscription.id]"></g:link>
                    </td>
                    <g:if test="${facility.hasApplicationInvoice()}">
                        <td class="center-text">
                            <g:if test="${subscription.invoiceRowId}">
                                <g:if test="${subscription.invoiceStatus}">
                                    <span class="badge ${Invoice.InvoiceStatus.valueOf(subscription.invoiceStatus).badgeClass}"
                                            title="<g:message code="invoice.status.${subscription.invoiceStatus}"/>">
                                        <g:message code="invoice.status.${subscription.invoiceStatus}"/>
                                    </span>

                                </g:if>
                                <g:else>
                                    <g:message code="facilitySubscription.index.message13"/>
                                </g:else>
                            </g:if>
                        </td>

                    </g:if>
                </tr>
            </g:each>
            </tbody>
        </table>

        <g:paginateTwitterBootstrap next="&raquo;" prev="&laquo;" class="pagination-centered"
                                    maxsteps="0" max="${cmd.max}" params="${resetFilterParams}"
                                    action="index" total="${count}" />

    </g:form>
    <r:script>
        $(document).ready(function() {
            $("#subscription-search-input").focus();

            $("#subscriptions-table").selectAll({ max: "${g.forJavaScript(data: cmd.max)}", count: "${g.forJavaScript(data: count)}", name: "abonnemang" });

            $("#weekday").multiselect({
                create: function() {$(this).next().width(210);},
                selectedText: "${message(code: 'default.multiselect.selectedText')}",
                classes: "multi",
                minWidth: 200,
                checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
                uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
                noneSelectedText: "${message(code: 'facilitySubscription.index.weekdaySelect.noneSelectedText')}",
                selectedText: "${message(code: 'facilitySubscription.index.weekdaySelect.selectedText')}"
            });

            $("#time").addTimePicker({
                hourText: '${message(code: 'default.timepicker.hour')}',
                minuteText: '${message(code: 'default.timepicker.minute')}'
            });

            $("#court").multiselect({
                create: function() {$(this).next().width(210);},
                selectedText: "${message(code: 'default.multiselect.selectedText')}",
                classes: "multi",
                minWidth: 200,
                checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
                uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
                noneSelectedText: "${message(code: 'facilitySubscription.index.courtSelect.noneSelectedText')}",
                selectedText: "${message(code: 'court.multiselect.selectedText')}"
            });

            $("#status").multiselect({
                create: function() {$(this).next().width(210);},
                selectedText: "${message(code: 'default.multiselect.selectedText')}",
                classes: "multi",
                minWidth: 200,
                checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
                uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
                noneSelectedText: "${message(code: 'facilitySubscription.index.statusSelect.noneSelectedText')}",
                selectedText: "${message(code: 'facilitySubscription.index.statusSelect.selectedText')}"
            });

            $("#invoiceStatus").multiselect({
                create: function() {$(this).next().width(210);},
                selectedText: "${message(code: 'default.multiselect.selectedText')}",
                classes: "multi",
                minWidth: 200,
                checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
                uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
                noneSelectedText: "${message(code: 'facilitySubscription.index.invoiceStatusSelect.noneSelectedText')}",
                selectedText: "${message(code: 'invoice.status.multiselect.selectedText')}"
            });
        });
    </r:script>
</g:if>
<g:else><g:message code="facility.onlyLocal"/></g:else>
</body>
</html>
