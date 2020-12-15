<%@ page import="com.matchi.invoice.Invoice; com.matchi.excel.ExcelExportManager; com.matchi.facility.FilterCustomerCommand; com.matchi.Customer; com.matchi.membership.Membership; com.matchi.Sport; org.joda.time.DateTime; com.matchi.Facility; com.matchi.FacilityProperty" %>
<g:set var="returnUrlParams" value="${params.subMap(params.keySet() - 'error' - 'message')}"/>
<g:set var="resetFilterParams" value="${filter.properties + [reset: true]}"/>
<html>
<head>
    <meta name="layout" content="facilityLayout"/>
    <title><g:message code="customer.label.plural"/></title>
    <r:require modules="jquery-multiselect-widget,matchi-selectall,daterangepicker"/>
</head>
<body>

<ul class="breadcrumb">
    <li><g:link action="index"><g:message code="customer.label.plural"/></g:link> <span class="divider">/</span></li>
    <li><g:message code="facilityCustomerMembers.index.message20" args="[members.getTotalCount()]"/></li>
</ul>

<g:render template="/templates/messages/webflowMessage"/>

<ul class="nav nav-tabs">
    <li><g:link controller="facilityCustomer"><g:message code="customer.select.all"/></g:link></li>
    <li class="active"><g:link action="index"><g:message code="default.member.label.plural"/></g:link></li>
    <li><g:link controller="facilityCustomerArchive" action="index"><g:message code="default.archive.label"/></g:link></li>

    <g:ifFacilityPropertyEnabled name="${FacilityProperty.FacilityPropertyKey.FEATURE_IDROTT_ONLINE.name()}">
        <g:if test="${facility.hasIdrottOnlineActivitySync()}">
            <li class="pull-right"><g:link action="iOSyncStatusActivities" controller="facilityCustomerMembers"><g:message code="facilityCustomer.iosync.activities.label"/></g:link></li>
        </g:if>
        <li class="pull-right"><g:link action="iOSyncStatusMembers" controller="facilityCustomerMembers"><g:message code="facilityCustomer.iosync.label"/></g:link></li>
    </g:ifFacilityPropertyEnabled>
</ul>

<form method="GET" id="filterForm" class="form-search well" style="padding:7px 10px 4px 10px;">
    <g:hiddenField name="reset" value="true"/>
    <fieldset>
        <div class="control-group">
            <ul class="inline filter-list">
                <li><g:textField id="members-search-input" class="search span3" name="q" value="${filter?.q}" placeholder="${message(code: 'facilityCustomerMembers.index.message21')}" style="width: 202px;"/></li>
                <li>
                    <div id="daterange" class="daterange" style="background-color: #fff; width: 199px">
                        <i class="icon-calendar icon-large"></i>
                        <span>
                            <g:if test="${filter.membershipStartDate && filter.membershipEndDate}">
                                <g:formatDate date="${filter.membershipStartDate.toDate()}" formatName="date.format.daterangepicker.short"/>
                                -
                                <g:formatDate date="${filter.membershipEndDate.toDate()}" formatName="date.format.daterangepicker.short"/>
                            </g:if>
                            <g:else>
                                <g:formatDate date="${new Date()}" formatName="date.format.daterangepicker.short"/>
                            </g:else>
                        </span>
                        <b style="margin-top:6px" class="caret"></b>
                    </div>
                    <input name="membershipStartDate" id="rangestart" type="hidden"
                            value="${filter.membershipStartDate.toString()}">
                    <input name="membershipEndDate" id="rangeend" type="hidden"
                            value="${filter.membershipEndDate.toString()}">
                </li>
                <li>
                    <select id="gender" name="gender" multiple="true">
                        <option value="NULL" ${ filter.gender.contains(Customer.CustomerType.NULL) ? "selected" : ""}>
                            <g:message code="facilityCustomer.index.noGender"/>
                        </option>
                        <g:each in="${Customer.CustomerType.list()}">
                            <option value="${it}" ${ filter.gender.contains(it) ? "selected":""}><g:message code="customer.type.${it}" /></option>
                        </g:each>
                    </select>
                </li>
                <g:if test="${facilityGroups.size() > 0}">
                    <li>
                        <select id="group" name="group" multiple="true">
                            <option value="0" ${ filter.group.contains(0L) ? "selected" : ""}>
                                <g:message code="facilityCustomer.index.noGroup"/>
                            </option>
                            <g:each in="${facilityGroups}">
                                <option value="${it.id}" ${ filter.group.contains(it.id) ? "selected":""}>${it.name}</option>
                            </g:each>
                        </select>
                    </li>
                </g:if>
                <li>
                    <select id="members" name="members" multiple="true">
                        <g:each in="${FilterCustomerCommand.ShowMembers.familyList()}">
                            <option value="${it}" ${ filter.members.contains(it) ? "selected":""}>
                                <g:message code="filterCustomerCommand.members.showmembers.choise.${it}"/>
                            </option>
                        </g:each>
                    </select>
                </li>
                <g:if test="${types.size() > 0}">
                    <li>
                        <select id="type" name="type" multiple="true">
                            <option value="0" ${ filter.type.contains(0L) ? "selected":""}>
                                <g:message code="facilityCustomer.index.noMembershipType"/>
                            </option>
                            <g:each in="${types}">
                                <option value="${it.id}" ${ filter.type.contains(it.id) ? "selected":""}>${it.name}</option>
                            </g:each>
                        </select>
                    </li>
                </g:if>
                <li>
                    <select id="status" name="status" multiple="true">
                        <g:each in="${FilterCustomerCommand.MemberStatus.list(facility)}">
                            <option value="${it}" ${ filter.status.contains(it) ? "selected":""}>
                                <g:message code="facilityCustomerMembers.index.status.${it}"/>
                            </option>
                        </g:each>
                    </select>
                </li>
                <li>
                    <select id="birthyear" name="birthyear" multiple="true">
                        <option value="0" ${filter.birthyear.contains(0) ? "selected" : ""}>
                            <g:message code="facilityCustomer.index.birthyearSelect.noBirthyear"/>
                        </option>
                        <g:each in="${birthyears}">
                            <option value="${it}" ${filter.birthyear.contains(it) ? "selected" : ""}>${it}</option>
                        </g:each>
                    </select>
                </li>
                <li>
                    <select id="seasons" name="seasons" multiple="true">
                        <option ${filter.seasons.contains(0l) ? "selected" : ""}>
                            <g:message code="facilityCustomer.index.seasons.noSeason"/>
                        </option>
                        <g:each in="${seasons}">
                            <option value="${it?.id}" ${filter.seasons.contains(it?.id) ? "selected" : ""}>${it?.name}</option>
                        </g:each>
                    </select>
                </li>
                <g:if test="${facility.hasApplicationInvoice()}">
                    <li>
                        <select id="invoiceStatus" name="invoiceStatus" multiple="true">
                            <g:each in="${Invoice.InvoiceStatus.values()}">
                                <option value="${it}" ${(filter.invoiceStatus?.contains(it) ? "selected" : "")}><g:message code="invoice.status.${it}"/></option>
                            </g:each>
                        </select>
                    </li>
                </g:if>
                <g:ifFacilityPropertyEnabled name="${FacilityProperty.FacilityPropertyKey.FEATURE_TRAINING_PLANNER.name()}">
                    <li>
                        <select id="courses" name="courses" multiple="true">
                            <option ${filter.courses.contains(0l) ? "selected" : ""}>
                                <g:message code="facilityCustomer.index.courses.noCourse"/>
                            </option>
                            <g:each in="${courses}">
                                <option value="${it?.id}" ${filter.courses.contains(it?.id) ? "selected" : ""}>${ it.isArchived() ? "${message(code: 'facilityActivity.tabs.archived.label.singular')} - ${it.name}" : "${it.name}" }</option>
                            </g:each>
                        </select>
                    </li>
                </g:ifFacilityPropertyEnabled>
                <g:ifFacilityPropertyEnabled name="${FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name()}">
                    <li>
                        <select id="clubs" name="clubs" multiple="true">
                            <option ${filter.clubs.contains(message(code: "facilityCustomer.index.clubs.noClub")) ? "selected" : ""}>
                                <g:message code="facilityCustomer.index.clubs.noClub"/>
                            </option>
                            <g:each in="${clubs}">
                                <option value="${it}" ${filter.clubs.contains(it) ? "selected" : ""}>${it}</option>
                            </g:each>
                        </select>
                    </li>
                </g:ifFacilityPropertyEnabled>
                <li>
                    <select id="lastActivity" name="lastActivity" multiple="false">
                        <option ${filter.lastActivity == 0 ? "selected" : ""}>
                            <g:message code="facilityCustomer.index.lastActivity.noLastActivity"/>
                        </option>
                        <g:each var="i" in="${ (1..<8) }">
                            <option value="${i}" ${filter.lastActivity == i ? "selected" : ""}> <g:message code="facilityCustomer.index.lastActivity.selectedLastActivity" args="[i]"/></option>
                        </g:each>
                    </select>
                </li>
                <g:if test="${facility.isMasterFacility()}">
                    <li>
                        <select id="localFacilities" name="localFacilities" multiple="true">
                            <g:each in="${localFacilities}">
                                <option value="${it.id}" ${ filter.localFacilities.contains(it.id) ? "selected":""}>${it.name}</option>
                            </g:each>
                        </select>
                    </li>
                </g:if>
                <g:if test="${facility.isMemberFacility()}">
                    <li>
                        <select id="dontIncludeMemberFacilitysCustomer" name="dontIncludeMemberFacilitysCustomer" multiple="false">
                            <option ${filter.dontIncludeMemberFacilitysCustomer == 0 ? "selected" : ""} value="0">
                                <g:message code="facilityCustomer.index.includeGlobalFacilitysCustomer.doInclude"/>
                            </option>
                            <option value="1" ${filter.dontIncludeMemberFacilitysCustomer == true ? "selected" : ""}> <g:message code="facilityCustomer.index.includeGlobalFacilitysCustomer.dontInclude"/>
                        </select>
                    </li>
                </g:if>
                <li class="pull-right">
                    <g:if test="${filter.isActive(true)}">
                        <g:link params="[reset: true]" class="btn btn-danger">
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
            <button class="btn btn-inverse dropdown-toggle" data-toggle="dropdown">
                <g:message code="button.actions.label"/>
                <span class="caret"></span>
            </button>
            <ul class="dropdown-menu">
                <li class="dropdown-submenu">
                    <a tabindex="-1" href="#"><g:message code="membership.label"/></a>
                    <ul class="dropdown-menu">
                        <g:if test="${facility.membershipRequiresApproval}">
                            <li>
                                <a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerMembersFlow" action="activate" />');">
                                    <g:message code="facilityCustomerMembers.membershipForm.activate.label"/>
                                </a>
                            </li>
                            <li>
                                <a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerMembersFlow" action="deactivate" />');">
                                    <g:message code="facilityCustomerMembers.membershipForm.deactivate.label"/>
                                </a>
                            </li>
                        </g:if>
                        <li>
                            <a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerUpdateRequest" action="sendRequest"
                                    params="['returnUrl': g.createLink(absolute: true, params: returnUrlParams)]"/>');">
                                <g:message code="facilityCustomerMembers.index.message9"/>
                            </a>
                        </li>
                        <g:if test="${facility.hasApplicationInvoice()}">
                            <li>
                                <a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerMembersInvoice" action="createMembershipInvoice"
                                        params="['returnUrl': g.createLink(absolute: true, params: returnUrlParams)]"/>');">
                                    <g:message code="facilityCustomerMembers.index.message11"/>
                                </a>
                            </li>
                        </g:if>
                        <li>
                            <a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerMembersFlow" action="setPaid" />');">
                                <g:message code="facilityCustomerMembers.setPaid.title"/>
                            </a>
                        </li>
                        <li>
                            <a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerMembersFlow" action="setUnpaid" />');">
                                <g:message code="facilityCustomerMembers.setUnpaid.title"/>
                            </a>
                        </li>
                        <li>
                            <a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerMembersFlow" action="cancel" />');">
                                <g:message code="membershipCommand.cancel.label.short"/>
                            </a>
                        </li>
                        <li>
                            <a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerMembersFlow" action="editMemberships" />');">
                                <g:message code="facilityCustomerMembers.editMemberships.title"/>
                            </a>
                        </li>
                        <li>
                            <a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerMembersFlow" action="remove" />');">
                                <g:message code="button.remove.label"/>
                            </a>
                        </li>
                    </ul>
                </li>
                <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerInvite" action="invite"
                        params="['returnUrl': g.createLink(absolute: true, params: returnUrlParams)]"/>');"><g:message code="button.invite.label"/></a></li>
                <li class="dropdown-submenu">
                    <a tabindex="-1" href="#"><g:message code="facility.customer.nav.groupMgmt"/></a>
                    <ul class="dropdown-menu">
                        <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerGroup" action="add"
                                params="['returnUrl': g.createLink(absolute: true, params: returnUrlParams)]"/>');"><g:message code="facility.customer.nav.groupMgmt.add"/></a></li>
                        <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerGroup" action="remove"
                                params="['returnUrl': g.createLink(absolute: true, params: returnUrlParams)]"/>');"><g:message code="facility.customer.nav.groupMgmt.remove"/></a></li>
                    </ul>
                </li>
                <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityOfferFlow" action="bulkAdd"
                                        params="['returnUrl': g.createLink(absolute: true, params: returnUrlParams)]"/>');"><g:message code="facilityOffer.bulkAdd.title"/></a></li>
                <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerMessage" action="message"
                        params="['returnUrl': g.createLink(absolute: true, params: returnUrlParams)]"/>');"><g:message code="facilityCustomer.index.message25"/></a></li>
                <g:if test="${facility.hasSMS()}">
                    <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomerSMSMessage" action="message"
                                        params="['returnUrl': createLink(absolute: true, params: returnUrlParams)]"/>');"><g:message code="facilityCustomer.index.message24"/></a></li>
                </g:if>
                <li class="divider"></li>
                <li class="dropdown-submenu">
                    <a tabindex="-1" href="#"><g:message code="button.export.label"/></a>
                    <ul class="dropdown-menu">
                        <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomer" action="export" params="[exportType: ExcelExportManager.ExportType.COMPLETE]" />');">
                            <g:message code="facilityCustomer.index.export.complete"/></a></li>
                        <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomer" action="export" params="[exportType: ExcelExportManager.ExportType.IDROTT_ONLINE]" />');">
                            <g:message code="facilityCustomer.index.export.idrottOnline"/></a></li>
                        <g:if test="${facility.isNorwegian()}">
                            <li><a href="javascript:void(0)" onclick="submitFormTo('#customers', '<g:createLink controller="facilityCustomer" action="export" params="[exportType: ExcelExportManager.ExportType.NIF]" />');">
                                <g:message code="facilityCustomer.index.export.nif"/></a></li>
                        </g:if>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
</div>
<g:form name="customers" class="no-margin">
    <g:hiddenField name="q" value="${filter?.q}"/>
    <g:hiddenField name="membershipStartDate" value="${filter?.membershipStartDate}"/>
    <g:hiddenField name="membershipEndDate" value="${filter?.membershipEndDate}"/>
    <g:each in="${filter?.members}">
        <g:hiddenField name="members" value="${it}"/>
    </g:each>
    <g:each in="${filter?.group}">
        <g:hiddenField name="group" value="${it}"/>
    </g:each>
    <g:each in="${filter?.type}">
        <g:hiddenField name="type" value="${it}"/>
    </g:each>
    <g:each in="${filter?.status}">
        <g:hiddenField name="status" value="${it}"/>
    </g:each>
    <g:each in="${filter?.gender}">
        <g:hiddenField name="gender" value="${it}"/>
    </g:each>
    <g:each in="${filter?.birthyear}">
        <g:hiddenField name="birthyear" value="${it}"/>
    </g:each>
    <g:each in="${filter?.seasons}">
        <g:hiddenField name="seasons" value="${it}"/>
    </g:each>
    <g:each in="${filter?.invoiceStatus}">
        <g:hiddenField name="invoiceStatus" value="${it}"/>
    </g:each>
    <g:each in="${filter?.courses}">
        <g:hiddenField name="courses" value="${it}"/>
    </g:each>

    <table id="customer-table" class="table table-striped table-bordered table-hover">
        <thead>
        <tr>
            <th class="center-text nolink" width="20">
                <g:checkBox class="checkbox-selector" name="selectAll" value="" checked="false"/>
            </th>
            <g:sortableColumn property="number" titleKey="customer.number.label" params="${resetFilterParams}" width="40"/>
            <g:sortableColumn property="lastname,firstname,companyname" titleKey="default.name.label" params="${resetFilterParams}"/>
            <g:sortableColumn property="birthyear" titleKey="customer.birthyear.label" params="${resetFilterParams}"/>
            <g:if test="${facility.isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name())}">
                <g:sortableColumn property="club" titleKey="default.club.label" params="${resetFilterParams}" class="center-text"/>
            </g:if>
            <th><g:message code="membership.type.label"/></th>
            <th class="center-text"><g:message code="membership.startDate.label"/></th>
            <th class="center-text"><g:message code="membership.endDate.label"/></th>
            <th class="center-text"><g:message code="default.status.label"/></th>
            <g:if test="${filter.lastActivity}">
                <th><g:message code="last.activity.label"/></th>
            </g:if>
        </tr>
        </thead>

        <tbody data-provides="rowlink">
        <g:if test="${!members}">
            <tr>
                <td colspan="8"><i><g:message code="facilityCustomerMembers.index.message17"/></i></td>
            </tr>
        </g:if>

        <g:each in="${members}" var="customer">
            <g:set var="membership" value="${customer.getMembershipByFilter(filter)}"/>
            <tr>
                <td class="center-text nolink">
                    <g:checkBox name="customerId" value="${customer.id}" checked="false" class="selector"/>
                </td>
                <td>
                    <g:if test="${customer.facility?.isMasterFacility() && !facility?.isMasterFacility()}">
                        <g:link controller="facilityCustomer" action="show" id="${customer.id}" class="rowlink">
                            <i rel="tooltip" title="${message(code: 'facilityCustomer.edit.message2')}" class="icon-globe"></i></g:link>
                    </g:if>
                    <g:elseif test="${customer.getCountOfMasterFacilityCustomers() >= 1}">
                        <g:link controller="facilityCustomer" action="show" id="${customer.id}"
                                class="rowlink">${customer.number}&nbsp;<i
                            class="icon-globe"></i></g:link>
                    </g:elseif>
                    <g:elseif test="${customer.getCountOfMemberFacilityCustomers() >= 1}">
                        <g:link controller="facilityCustomer" action="show" id="${customer.id}"
                                class="rowlink">${customer.number}&nbsp;<i
                            class="fas fa-share-alt"></i></g:link>
                    </g:elseif>
                    <g:else>
                        <g:link controller="facilityCustomer" action="show" id="${customer.id}"
                                class="rowlink">${customer.number}</g:link>
                    </g:else>
                </td>
                <td>
                    <span class="align-middle">${customer.fullName()}</span>
                    <g:if test="${customer.user}">
                        <img src="${resource(dir: "images", file: "favicon.png")}" width="13" height="13"
                                rel="tooltip" title="${message(code: 'customer.user.connected.label')}" alt=""/>
                    </g:if>
                </td>
                <td>
                    ${customer.shortBirthYear()}
                </td>
                <g:if test="${facility.isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name())}">
                    <td>${customer.club}</td>
                </g:if>
                <td>
                    ${membership?.type ?: "-"}
                    <g:if test="${membership?.type?.facility?.isMasterFacility() && !facility?.isMasterFacility()}">
                        <i rel="tooltip"
                           title="${message(code: 'facilityCustomer.index.message27', args: [customer.facility.name])}"
                           class="icon-globe"></i>
                    </g:if>
                    <g:if test="${customer.getCountOfMasterFacilityMemberships() >= 1}">
                        &nbsp;<i rel="tooltip"
                                 title="${message(code: 'facilityCustomer.index.message26')}"
                                 class="icon-globe"></i>
                    </g:if>
                    <g:if test="${customer.getCountOfMemberFacilityMemberships() >= 1}">
                        &nbsp;<i rel="tooltip"
                                 title="${message(code: 'facilityCustomer.index.message26')}"
                                 class="icon-globe"></i>
                    </g:if>
                </td>
                <td class="center-text">
                    <g:formatDate date="${membership?.startDate?.toDate()}" formatName="date.format.dateOnly"/>
                </td>
                <td class="center-text">
                    <g:membershipEndDate membership="${membership}"/>
                </td>
                <td class="center-text">
                    <g:membershipStatus membership="${membership}"/>
                </td>
                <g:if test="${filter.lastActivity}">
                    <td>
                        <g:formatDate date="${customer?.lastActivity}" formatName="date.format.dateOnly"/>
                    </td>
                </g:if>
            </tr>

        </g:each>
        </tbody>
    </table>
    <div class="row">
        <div class="span12">
            <g:paginateTwitterBootstrap prev="&laquo;" next="&raquo;" class="pagination-centered" params="${resetFilterParams}"
                                        maxsteps="10" max="100" action="index" total="${members.getTotalCount()}" />
        </div>
    </div>
</g:form>
<r:script>
    $("[rel='tooltip']").tooltip();

    $("#gender").multiselect({
        create: function() {$(this).next().width(210);},
        classes: "multi",
        minWidth: 200,
        checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
        uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
        noneSelectedText: "${message(code: 'customer.type.multiselect.noneSelectedText')}",
        selectedText: "${message(code: 'customer.type.multiselect.selectedText')}"
    });
    $("#group").multiselect({
        create: function() {$(this).next().width(210);},
        classes: "multi",
        minWidth: 200,
        checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
        uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
        noneSelectedText: "${message(code: 'group.multiselect.noneSelectedText')}",
        selectedText: "${message(code: 'group.multiselect.selectedText')}"
    });
    $("#type").multiselect({
        create: function() {$(this).next().width(210);},
        classes: "multi",
        minWidth: 200,
        checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
        uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
        noneSelectedText: "${message(code: 'membershipType.multiselect.noneSelectedText')}",
        selectedText: "${message(code: 'membershipType.multiselect.selectedText')}"
    });
    $("#status").multiselect({
        create: function() {$(this).next().width(210);},
        classes: "multi",
        minWidth: 200,
        checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
        uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
        noneSelectedText: "${message(code: 'membership.status.multiselect.noneSelectedText')}",
        selectedText: "${message(code: 'membership.status.multiselect.selectedText')}"
    });
    $("#birthyear").multiselect({
        create: function() {$(this).next().width(210);},
        classes: "multi",
        minWidth: 200,
        checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
        uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
        noneSelectedText: "${message(code: 'facilityCustomer.index.birthyearSelect.noneSelectedText')}",
        selectedText: "${message(code: 'facilityCustomer.index.birthyearSelect.selectedText')}"
    });
    $("#members").multiselect({
        create: function() {$(this).next().width(210);},
        classes: "multi",
        minWidth: 280,
        checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
        uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
        noneSelectedText: "${message(code: 'facilityCustomerMembers.index.members.noneSelectedText')}",
        selectedText: "${message(code: 'filterCustomerCommand.members.multiselect.selectedText')}"
    });
    $("#seasons").multiselect({
        create: function() {$(this).next().width(210);},
        classes: "multi",
        minWidth: 200,
        checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
        uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
        noneSelectedText: "${message(code: 'facilityCustomer.index.seasons.noneSelectedText')}",
        selectedText: "${message(code: 'facilityCustomer.index.seasons.selectedText')}"
    });
    $("#invoiceStatus").multiselect({
        create: function() {$(this).next().width(210);},
        classes: "multi",
        minWidth: 200,
        checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
        uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
        noneSelectedText: "${message(code: 'facilityCustomer.index.invoiceStatus.noneSelectedText')}",
        selectedText: "${message(code: 'invoice.status.multiselect.selectedText')}"
    });
    $("#courses").multiselect({
        create: function() {$(this).next().width(210);},
        classes: "multi",
        minWidth: 200,
        checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
        uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
        noneSelectedText: "${message(code: 'facilityCustomer.index.courses.noneSelectedText')}",
        selectedText: "${message(code: 'facilityCustomer.index.courses.selectedText')}"
    });
    $("#lastActivity").multiselect({
        create: function() {$(this).next().width(210);},
        minWidth: 200,
        classes: "multi",
        multiple: false,
        selectedList: 1,
        showCheckAll: true,
        showUncheckAll: true,
        uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
        noneSelectedText: "${message(code: 'facilityCustomer.index.lastActivity.noLastActivity')}"
    });
    <g:ifFacilityPropertyEnabled name="${FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name()}">
    $("#clubs").multiselect({
        create: function() {$(this).next().width(210);},
        classes: "multi",
        minWidth: 200,
        checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
        uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
        noneSelectedText: "${message(code: 'facilityCustomer.index.clubs.noneSelectedText')}",
        selectedText: "${message(code: 'facilityCustomer.index.clubs.selectedText')}"
    });
    </g:ifFacilityPropertyEnabled>

    <g:if test="${facility.isMasterFacility()}">
        $("#localFacilities").multiselect({
            create: function() {$(this).next().width(210);},
            classes: "multi",
            minWidth: 200,
            checkAllText: "${message(code: 'default.multiselect.checkAllText')}",
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'facilityLocal.multiselect.noneSelectedText')}",
            selectedText: "${message(code: 'facility.multiselect.selectedText')}"
        });
    </g:if>
    <g:if test="${facility.isMemberFacility()}">
        $("#dontIncludeMemberFacilitysCustomer").multiselect({
            create: function() {$(this).next().width(210);},
            minWidth: 200,
            classes: "multi",
            multiple: false,
            selectedList: 1,
            showCheckAll: false,
            showUncheckAll: false,
            uncheckAllText: "${message(code: 'default.multiselect.uncheckAllText')}",
            noneSelectedText: "${message(code: 'facilityCustomer.index.includeGlobalFacilitysCustomer.doInclude')}"
        });
    </g:if>

    $("#customer-table").selectAll({ max: "${g.forJavaScript(data: filter.max)}", count: "${g.forJavaScript(data: members.getTotalCount())}", name: "${message(code: 'js.selectall.members')}" });

    $(function() {
        $('#daterange').daterangepicker(
            {
                ranges: {
                    '${message(code: 'default.dateRangePicker.currentYear')}': [Date.january(), Date.december().moveToLastDayOfMonth()],
                    '${message(code: 'default.dateRangePicker.lastYear')}': [Date.january().addYears(-1), Date.december().addYears(-1).moveToLastDayOfMonth()],
                    '${message(code: 'default.dateRangePicker.nextYear')}': [Date.january().addYears(1), Date.december().addYears(1).moveToLastDayOfMonth()],
                    '${message(code: 'default.dateRangePicker.lastMonth')}': [Date.today().moveToFirstDayOfMonth().add({ months: -1 }), Date.today().moveToFirstDayOfMonth().add({ days: -1 })]
                },

                format: "${message(code: 'date.format.dateOnly')}",
                <g:if test="${filter.membershipStartDate && filter.membershipEndDate}">
                    startDate: "${filter.membershipStartDate.toString()}",
                    endDate: "${filter.membershipEndDate.toString()}",
                </g:if>
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
    });
</r:script>
</body>
</html>
